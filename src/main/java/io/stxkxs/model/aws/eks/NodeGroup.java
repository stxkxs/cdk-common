package io.stxkxs.model.aws.eks;

import io.stxkxs.model.aws.iam.IamRole;
import software.amazon.awscdk.services.ec2.InstanceClass;
import software.amazon.awscdk.services.ec2.InstanceSize;
import software.amazon.awscdk.services.eks.NodegroupAmiType;

import java.util.Map;

public record NodeGroup(
  NodegroupAmiType amiType,
  boolean forceUpdate,
  String capacityType,
  int desiredSize,
  InstanceClass instanceClass,
  InstanceSize instanceSize,
  int maxSize,
  int minSize,
  String name,
  IamRole role,
  Map<String, String> labels,
  Map<String, String> tags
) {}
