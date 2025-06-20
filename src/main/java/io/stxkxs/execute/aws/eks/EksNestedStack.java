package io.stxkxs.execute.aws.eks;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import io.fabric8.kubernetes.client.utils.Serialization;
import io.stxkxs.execute.aws.sqs.SqsConstruct;
import io.stxkxs.execute.serialization.Mapper;
import io.stxkxs.execute.serialization.Template;
import io.stxkxs.model._main.Common;
import io.stxkxs.model.aws.eks.KubernetesConf;
import io.stxkxs.model.aws.eks.NodeGroup;
import io.stxkxs.model.aws.eks.RbacConf;
import io.stxkxs.model.aws.eks.TenancyConf;
import io.stxkxs.model.aws.eks.Tenant;
import io.stxkxs.model.aws.sqs.Sqs;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awscdk.NestedStack;
import software.amazon.awscdk.NestedStackProps;
import software.amazon.awscdk.cdk.lambdalayer.kubectl.v33.KubectlV33Layer;
import software.amazon.awscdk.services.ec2.SubnetSelection;
import software.amazon.awscdk.services.ec2.SubnetType;
import software.amazon.awscdk.services.ec2.Vpc;
import software.amazon.awscdk.services.eks.AwsAuthMapping;
import software.amazon.awscdk.services.eks.Cluster;
import software.amazon.awscdk.services.eks.ClusterLoggingTypes;
import software.amazon.awscdk.services.eks.EndpointAccess;
import software.amazon.awscdk.services.eks.KubernetesManifest;
import software.amazon.awscdk.services.eks.KubernetesVersion;
import software.amazon.awscdk.services.iam.Role;
import software.amazon.awscdk.services.sqs.IQueue;
import software.constructs.Construct;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static io.stxkxs.execute.serialization.Format.id;

@Slf4j
@Getter
public class EksNestedStack extends NestedStack {
  private final Cluster cluster;
  private final IQueue interruptQueue;
  private final ManagedAddonsConstruct managedAddonsConstruct;
  private final NodeGroupsConstruct nodeGroupsConstruct;
  private final AddonsConstruct addonsConstruct;
  private final ObservabilityConstruct observabilityConstruct;

  @SneakyThrows
  public EksNestedStack(Construct scope, Common common, KubernetesConf conf, Vpc vpc, NestedStackProps props) {
    super(scope, "eks", props);

    this.cluster = cluster(common, conf, vpc);

    var sqs = Mapper.get().readValue(Template.parse(this, conf.sqs()), Sqs.class);
    this.interruptQueue = new SqsConstruct(this, common, sqs).sqs().getQueue();

    this.managedAddonsConstruct = new ManagedAddonsConstruct(this, common, conf, this.cluster());

    var configuration = Mapper.get().readValue(Template.parse(this, conf.nodeGroups()), new TypeReference<List<NodeGroup>>() {});
    this.nodeGroupsConstruct = new NodeGroupsConstruct(this, conf.name(), common, configuration, this.cluster());
    this.nodeGroupsConstruct().getNode().addDependency(this.interruptQueue());

    this.addonsConstruct = new AddonsConstruct(this, common, conf, this.cluster());
    this.addonsConstruct().getNode().addDependency(this.managedAddonsConstruct(), this.nodeGroupsConstruct());

    this.observabilityConstruct = new ObservabilityConstruct(this, common, conf.observability());
    this.observabilityConstruct().getNode().addDependency(this.managedAddonsConstruct(), this.nodeGroupsConstruct(), this.addonsConstruct());
  }

  private static EndpointAccess type(String endpointAccess) {
    enum types {PUBLIC_AND_PRIVATE, PRIVATE, PUBLIC}
    if (endpointAccess.equalsIgnoreCase(types.PUBLIC_AND_PRIVATE.name())) {
      return EndpointAccess.PUBLIC_AND_PRIVATE;
    } else if (endpointAccess.equalsIgnoreCase(types.PRIVATE.name())) {
      return EndpointAccess.PUBLIC_AND_PRIVATE;
    } else if (endpointAccess.equalsIgnoreCase(types.PUBLIC.name())) {
      return EndpointAccess.PUBLIC;
    }

    throw new InvalidParameterException("error deciding endpoint access type for cluster");
  }

  @SneakyThrows
  private Cluster cluster(Common common, KubernetesConf conf, Vpc vpc) {
    var eks = Cluster.Builder
      .create(this, conf.name())
      .clusterName(conf.name())
      .version(KubernetesVersion.of(conf.version()))
      .endpointAccess(type(conf.endpointAccess()))
      .vpc(vpc)
      .vpcSubnets(conf.vpcSubnetTypes().stream()
        .map(type -> SubnetSelection.builder()
          .subnetType(SubnetType.valueOf(type.toUpperCase()))
          .build())
        .toList())
      .placeClusterHandlerInVpc(true)
      .kubectlLayer(new KubectlV33Layer(this, id("kubectl", conf.name())))
      .defaultCapacity(0)
      .clusterLogging(
        conf.loggingTypes().stream()
          .map(String::toUpperCase)
          .map(ClusterLoggingTypes::valueOf).toList())
      .prune(conf.prune())
      .tags(Common.Maps.from(common.tags(), conf.tags()))
      .build();

    rbac(conf, eks);
    awsAuthConfigMap(conf, eks);

    return eks;
  }

  @SneakyThrows
  private void awsAuthConfigMap(KubernetesConf conf, Cluster eks) {
    var mapper = Mapper.get();
    var parsed = Template.parse(this, conf.tenancy(), Map.ofEntries(
      Map.entry("hosted:eks:administrators", tenant("hosted:eks:administrators")),
      Map.entry("hosted:eks:users", tenant("hosted:eks:users"))));
    var tenancy = mapper.readValue(parsed, TenancyConf.class);

    tenancy.administrators()
      .forEach(administrator -> eks.getAwsAuth()
        .addMastersRole(Role.fromRoleArn(this, String.format("%s-admin-lookup", administrator.role()), administrator.role())));

    Optional.ofNullable(tenancy.users())
      .filter(users -> !users.isEmpty())
      .ifPresent(users -> users.forEach(user ->
        eks.getAwsAuth()
          .addRoleMapping(
            Role.fromRoleArn(this, String.format("%s-user-lookup", user.role()), user.role()),
            AwsAuthMapping.builder()
              .username(user.username())
              .groups(List.of("eks:read-only"))
              .build()
          )
      ));
  }

  private void rbac(KubernetesConf conf, Cluster eks) throws JsonProcessingException {
    var mapper = Mapper.get();
    var parsed = Template.parse(this, conf.rbac());
    var rbac = Serialization.unmarshal(parsed, RbacConf.class);

    var userClusterRoleBindingManifest = mapper.readValue(Serialization.asYaml(rbac.userClusterRoleBinding()), new TypeReference<Map<String, Object>>() {});
    KubernetesManifest.Builder
      .create(this, "user-cluster-role-binding")
      .cluster(eks)
      .overwrite(true)
      .prune(true)
      .skipValidation(true)
      .manifest(List.of(userClusterRoleBindingManifest))
      .build();

    var userClusterRoleManifest = mapper.readValue(Serialization.asYaml(rbac.userClusterRole()), new TypeReference<Map<String, Object>>() {});
    KubernetesManifest.Builder
      .create(this, "user-cluster-role")
      .cluster(eks)
      .overwrite(true)
      .prune(true)
      .skipValidation(true)
      .manifest(List.of(userClusterRoleManifest))
      .build();
  }

  private List<Tenant> tenant(String type) {
    var tenants = this.getNode().tryGetContext(type);
    var results = new ArrayList<Tenant>();
    if (tenants instanceof List<?> t) {
      for (var admin : t) {
        if (admin instanceof Map<?, ?> o) {
          var email = (String) o.get("email");
          var role = (String) o.get("role");
          var username = (String) o.get("username");

          results.add(new Tenant(email, role, username));
        }
      }
    }

    return results;
  }
}
