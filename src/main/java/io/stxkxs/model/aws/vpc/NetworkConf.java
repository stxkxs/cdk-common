package io.stxkxs.model.aws.vpc;

import io.stxkxs.model.aws.vpc.securitygroup.SecurityGroup;
import software.amazon.awscdk.services.ec2.DefaultInstanceTenancy;
import software.amazon.awscdk.services.ec2.IpProtocol;

import java.util.List;
import java.util.Map;

public record NetworkConf(
  String name,
  String cidr,
  IpProtocol ipProtocol,
  int natGateways,
  List<SecurityGroup> securityGroups,
  List<Subnet> subnets,
  List<String> availabilityZones,
  DefaultInstanceTenancy defaultInstanceTenancy,
  boolean createInternetGateway,
  boolean enableDnsHostnames,
  boolean enableDnsSupport,
  Map<String, String> tags
) {}
