package io.stxkxs.execute.aws.eks;

import static io.stxkxs.execute.serialization.Format.id;

import com.fasterxml.jackson.core.type.TypeReference;
import io.stxkxs.execute.aws.iam.RoleConstruct;
import io.stxkxs.execute.aws.kms.KmsConstruct;
import io.stxkxs.execute.serialization.Mapper;
import io.stxkxs.execute.serialization.Template;
import io.stxkxs.model._main.Common;
import io.stxkxs.model._main.Common.Maps;
import io.stxkxs.model.aws.eks.KubernetesConf;
import io.stxkxs.model.aws.eks.addon.AddonsConf;
import io.stxkxs.model.aws.eks.addon.managed.AwsEbsCsiAddon;
import io.stxkxs.model.aws.eks.addon.managed.ManagedAddon;
import io.stxkxs.model.aws.iam.Principal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awscdk.CfnTag;
import software.amazon.awscdk.services.eks.CfnAddon;
import software.amazon.awscdk.services.eks.Cluster;
import software.amazon.awscdk.services.eks.KubernetesManifest;
import software.amazon.awscdk.services.kms.Key;
import software.constructs.Construct;

/**
 * AWS EKS Managed Add-ons orchestration construct that provisions and configures essential Kubernetes cluster components with AWS-managed
 * lifecycle and updates.
 *
 * <p>
 * This construct manages the complete suite of AWS-provided EKS add-ons, featuring nested inner classes for specialized add-on handling and
 * complex OIDC integration:
 *
 * <p>
 * <b>Managed Add-on Components:</b>
 * <ul>
 * <li><b>VPC CNI</b> - AWS VPC Container Network Interface for pod networking</li>
 * <li><b>kube-proxy</b> - Kubernetes network proxy for service load balancing</li>
 * <li><b>CoreDNS</b> - Cluster DNS server for service discovery</li>
 * <li><b>Pod Identity Agent</b> - AWS IAM integration for pod-level permissions</li>
 * <li><b>EBS CSI Driver</b> - Elastic Block Store Container Storage Interface</li>
 * <li><b>Container Insights</b> - CloudWatch monitoring and logging for containers</li>
 * </ul>
 *
 * <p>
 * <b>Advanced Architecture:</b>
 * <ul>
 * <li><b>Nested Classes</b> - {@code ManagedAddonConstruct} and {@code AwsEbsCsiConstruct}</li>
 * <li><b>OIDC Integration</b> - OpenID Connect provider for service account role assumption</li>
 * <li><b>IAM Role Management</b> - Automatic role creation with service account binding</li>
 * <li><b>KMS Integration</b> - Encryption keys for EBS volumes and storage classes</li>
 * </ul>
 *
 * <p>
 * <b>EBS CSI Specialization:</b> The {@code AwsEbsCsiConstruct} inner class provides enhanced functionality:
 * <ul>
 * <li>Custom KMS encryption key provisioning</li>
 * <li>Default StorageClass manifest deployment</li>
 * <li>Template-based storage class configuration</li>
 * <li>Integration with cluster RBAC policies</li>
 * </ul>
 *
 * <p>
 * <b>Service Account Role Binding:</b> Each add-on that requires AWS permissions automatically receives:
 * <ul>
 * <li>IAM role with appropriate AWS managed policies</li>
 * <li>OIDC trust relationship with the EKS cluster</li>
 * <li>Kubernetes service account annotation</li>
 * <li>Automatic role ARN injection into add-on configuration</li>
 * </ul>
 *
 * <p>
 * <b>Configuration Management:</b>
 * <ul>
 * <li>Version management with AWS-recommended defaults</li>
 * <li>Conflict resolution strategies (OVERWRITE/NONE/PRESERVE)</li>
 * <li>Custom configuration values via JSON</li>
 * <li>Preservation policies for add-on deletion</li>
 * </ul>
 *
 * <p>
 * <b>Usage Example:</b>
 *
 * <pre>{@code
 * ManagedAddonsConstruct addons = new ManagedAddonsConstruct(this, common, kubernetesConfig, cluster);
 *
 * // Automatically provisions:
 * // - All 6 managed add-ons with IAM roles
 * // - EBS CSI with encryption and storage class
 * // - Service account role bindings
 * // - OIDC trust relationships
 * }</pre>
 *
 * @author CDK Common Framework
 * @see CfnAddon for AWS managed add-on resources
 * @see RoleConstruct for IAM role provisioning
 * @see KmsConstruct for encryption key management
 * @see KubernetesManifest for storage class deployment
 * @since 1.0.0
 */
@Slf4j
@Getter
public class ManagedAddonsConstruct extends Construct {
  private final ManagedAddonConstruct vpcCniConstruct;
  private final ManagedAddonConstruct kubeProxyConstruct;
  private final ManagedAddonConstruct coreDnsConstruct;
  private final ManagedAddonConstruct podIdentityAgentConstruct;
  private final AwsEbsCsiConstruct awsEbsCsiConstruct;
  private final ManagedAddonConstruct containerInsightsConstruct;

  @SneakyThrows
  public ManagedAddonsConstruct(Construct scope, Common common, KubernetesConf conf, Cluster cluster) {
    super(scope, id("managed-addons", conf.name()));

    log.debug("{} [common: {} conf: {}]", "ManagedAddonsConstruct", common, conf);

    var addons = Mapper.get().readValue(Template.parse(scope, conf.addons()), AddonsConf.class);
    this.vpcCniConstruct = new ManagedAddonConstruct(this, common, addons.managed().awsVpcCni(), cluster);
    this.kubeProxyConstruct = new ManagedAddonConstruct(this, common, addons.managed().kubeProxy(), cluster);
    this.coreDnsConstruct = new ManagedAddonConstruct(this, common, addons.managed().coreDns(), cluster);
    this.podIdentityAgentConstruct = new ManagedAddonConstruct(this, common, addons.managed().podIdentityAgent(), cluster);
    this.awsEbsCsiConstruct = new AwsEbsCsiConstruct(this, common, addons.managed().awsEbsCsi(), cluster);
    this.containerInsightsConstruct = new ManagedAddonConstruct(this, common, addons.managed().containerInsights(), cluster);
  }

  @Getter
  static class AwsEbsCsiConstruct extends Construct {
    private final ManagedAddonConstruct addonConstruct;
    private final KubernetesManifest storageClass;
    private final Key encryptionKey;

    @SneakyThrows
    public AwsEbsCsiConstruct(Construct scope, Common common, AwsEbsCsiAddon conf, Cluster cluster) {
      super(scope, "aws-ebs-csi");

      log.debug("{} [common: {} conf: {}]", "AwsEbsCsiConstruct", common, conf);

      this.addonConstruct = new ManagedAddonConstruct(this, common, conf, cluster);
      this.encryptionKey = new KmsConstruct(this, common, conf.kms()).key();

      var parsed = Template.parse(scope, conf.defaultStorageClass());
      var manifest = Mapper.get().readValue(parsed, new TypeReference<Map<String, Object>>() {});
      this.storageClass = KubernetesManifest.Builder.create(this, "storageclass").cluster(cluster).overwrite(true).prune(true)
        .skipValidation(true).manifest(List.of(manifest)).build();
    }
  }

  @Getter
  static class ManagedAddonConstruct extends Construct {
    private final CfnAddon addon;
    private final String roleArn;

    public ManagedAddonConstruct(Construct scope, Common common, ManagedAddon conf, Cluster cluster) {
      super(scope, id("managed-addon", conf.name()));

      log.debug("{} [common: {} conf: {}]", "ManagedAddonConstruct", common, conf);

      this.roleArn = Optional.ofNullable(conf.serviceAccount()).map(serviceAccount -> {
        var oidc = cluster.getOpenIdConnectProvider();
        var principal = Principal.builder().build().oidcPrincipal(this, oidc, serviceAccount);
        return new RoleConstruct(this, common, principal, serviceAccount.role()).role().getRoleArn();
      }).orElse(null);

      this.addon = CfnAddon.Builder.create(this, conf.name()).clusterName(cluster.getClusterName()).addonName(conf.name())
        .addonVersion(conf.version()).configurationValues(conf.configurationValues()).preserveOnDelete(conf.preserveOnDelete())
        .resolveConflicts(conf.resolveConflicts().toUpperCase()).serviceAccountRoleArn(this.roleArn())
        .tags(Maps.from(common.tags(), conf.tags()).entrySet().stream()
          .map(entry -> CfnTag.builder().key(entry.getKey()).value(entry.getValue()).build()).toList())
        .build();
    }
  }
}
