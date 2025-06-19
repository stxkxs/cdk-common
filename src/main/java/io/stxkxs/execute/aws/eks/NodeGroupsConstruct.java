package io.stxkxs.execute.aws.eks;

import io.stxkxs.execute.aws.iam.RoleConstruct;
import io.stxkxs.model._main.Common;
import io.stxkxs.model._main.Common.Maps;
import io.stxkxs.model.aws.eks.NodeGroup;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awscdk.services.ec2.InstanceType;
import software.amazon.awscdk.services.eks.CapacityType;
import software.amazon.awscdk.services.eks.ICluster;
import software.amazon.awscdk.services.eks.Nodegroup;
import software.constructs.Construct;

import java.util.List;

import static io.stxkxs.execute.serialization.Format.id;

@Slf4j
@Getter
public class NodeGroupsConstruct extends Construct {
  private final List<Nodegroup> nodeGroups;

  public NodeGroupsConstruct(Construct scope, String id, Common common, List<NodeGroup> conf, ICluster cluster) {
    super(scope, id("nodegroups", id));

    log.debug("node groups configuration [common: {} node-groups: {}]", common, conf);

    this.nodeGroups = conf.stream()
      .map(nodeGroup -> {
        var principal = nodeGroup.role().principal().iamPrincipal();
        var role = new RoleConstruct(this, common, principal, nodeGroup.role()).role();

        return Nodegroup.Builder
          .create(this, nodeGroup.name())
          .cluster(cluster)
          .nodegroupName(nodeGroup.name())
          .amiType(nodeGroup.amiType())
          .instanceTypes(List.of(InstanceType.of(nodeGroup.instanceClass(), nodeGroup.instanceSize())))
          .minSize(nodeGroup.minSize())
          .maxSize(nodeGroup.maxSize())
          .desiredSize(nodeGroup.desiredSize())
          .capacityType(CapacityType.valueOf(nodeGroup.capacityType().toUpperCase()))
          .nodeRole(role)
          .forceUpdate(nodeGroup.forceUpdate())
          .labels(nodeGroup.labels())
          .tags(Maps.from(common.tags(), nodeGroup.tags()))
          .build();
      }).toList();
  }
}
