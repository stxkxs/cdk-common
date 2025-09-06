package io.stxkxs.model.aws.vpc;

import java.util.Map;
import software.amazon.awscdk.services.ec2.SubnetType;

public record Subnet(String name, SubnetType subnetType, int cidrMask, boolean reserved, boolean mapPublicIpOnLaunch,
  Map<String, String> tags) {}
