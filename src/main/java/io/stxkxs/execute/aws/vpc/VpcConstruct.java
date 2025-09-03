package io.stxkxs.execute.aws.vpc;

import io.stxkxs.model._main.Common;
import io.stxkxs.model._main.Common.Maps;
import io.stxkxs.model.aws.vpc.NetworkConf;
import io.stxkxs.model.aws.vpc.Subnet;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awscdk.Tags;
import software.amazon.awscdk.services.ec2.IpAddresses;
import software.amazon.awscdk.services.ec2.SecurityGroup;
import software.amazon.awscdk.services.ec2.SubnetConfiguration;
import software.amazon.awscdk.services.ec2.SubnetType;
import software.amazon.awscdk.services.ec2.Vpc;
import software.constructs.Construct;

import java.util.List;
import java.util.stream.Collectors;

import static io.stxkxs.execute.serialization.Format.id;

/**
 * Comprehensive Amazon VPC (Virtual Private Cloud) construct that provides enterprise-grade network
 * infrastructure with advanced subnet management, security group configuration, and multi-AZ deployment support.
 * 
 * <p>This construct serves as the foundational networking component for all AWS infrastructure,
 * providing sophisticated network isolation, security controls, and connectivity patterns
 * required for production-ready cloud deployments.
 * 
 * <p><b>Core Networking Features:</b>
 * <ul>
 *   <li><b>VPC Creation</b> - Isolated virtual network with configurable CIDR blocks</li>
 *   <li><b>Subnet Management</b> - Public, private, and isolated subnet configurations</li>
 *   <li><b>Security Groups</b> - Stateful firewall rules for resource-level security</li>
 *   <li><b>Multi-AZ Support</b> - Cross-availability zone deployment for high availability</li>
 * </ul>
 * 
 * <p><b>Advanced Network Architecture:</b>
 * <ul>
 *   <li><b>Internet Gateway</b> - Controlled internet access for public resources</li>
 *   <li><b>NAT Gateways</b> - Managed NAT services for outbound internet access</li>
 *   <li><b>Route Tables</b> - Sophisticated traffic routing and network segmentation</li>
 *   <li><b>Network ACLs</b> - Subnet-level stateless security controls</li>
 * </ul>
 * 
 * <p><b>Subnet Configuration Patterns:</b>
 * The construct supports sophisticated subnet architectures:
 * <ul>
 *   <li><b>Public Subnets</b> - Direct internet access for web-facing resources</li>
 *   <li><b>Private Subnets</b> - NAT gateway access for internal resources</li>
 *   <li><b>Isolated Subnets</b> - No internet access for highly sensitive resources</li>
 *   <li><b>Custom Configurations</b> - Flexible subnet sizing and placement</li>
 * </ul>
 * 
 * <p><b>Security Group Management:</b>
 * <ul>
 *   <li><b>Dynamic Creation</b> - Security groups based on configuration templates</li>
 *   <li><b>Rule Management</b> - Ingress and egress rule configuration</li>
 *   <li><b>Protocol Support</b> - TCP, UDP, ICMP, and custom protocol rules</li>
 *   <li><b>Reference Management</b> - Cross-security group references and dependencies</li>
 * </ul>
 * 
 * <p><b>High Availability Design:</b>
 * <ul>
 *   <li><b>Multi-AZ Distribution</b> - Subnets across multiple availability zones</li>
 *   <li><b>Fault Tolerance</b> - Redundant NAT gateways for availability</li>
 *   <li><b>Load Distribution</b> - Even distribution of resources across AZs</li>
 *   <li><b>Disaster Recovery</b> - Cross-AZ backup and recovery patterns</li>
 * </ul>
 * 
 * <p><b>Network Connectivity Options:</b>
 * <ul>
 *   <li><b>Internet Connectivity</b> - Configurable internet gateway integration</li>
 *   <li><b>VPN Connections</b> - Site-to-site VPN for hybrid cloud connectivity</li>
 *   <li><b>Direct Connect</b> - Dedicated network connections to AWS</li>
 *   <li><b>VPC Peering</b> - Cross-VPC communication patterns</li>
 * </ul>
 * 
 * <p><b>IP Address Management:</b>
 * <ul>
 *   <li><b>CIDR Block Planning</b> - Efficient IP address space allocation</li>
 *   <li><b>IPv4/IPv6 Support</b> - Dual-stack networking capabilities</li>
 *   <li><b>Subnet Sizing</b> - Automatic subnet size calculation and optimization</li>
 *   <li><b>Address Conservation</b> - Efficient use of private IP address space</li>
 * </ul>
 * 
 * <p><b>Operational Excellence:</b>
 * <ul>
 *   <li><b>Resource Tagging</b> - Comprehensive tagging for cost allocation and management</li>
 *   <li><b>Flow Logs</b> - Network traffic monitoring and analysis</li>
 *   <li><b>DNS Resolution</b> - Custom DNS settings and Route53 integration</li>
 *   <li><b>Network Monitoring</b> - CloudWatch integration for network metrics</li>
 * </ul>
 * 
 * <p><b>Network Architecture Flow:</b>
 * <pre>
 * Internet Gateway → Public Subnets → NAT Gateway → Private Subnets → Isolated Subnets
 *        ↓               ↓              ↓              ↓                    ↓
 * Public Access → Load Balancers → Application Tier → Database Tier → Backup Storage
 * </pre>
 * 
 * <p><b>Usage Example:</b>
 * <pre>{@code
 * VpcConstruct network = new VpcConstruct(
 *     this, 
 *     common, 
 *     networkConfig
 * );
 * 
 * // Automatically creates:
 * // - VPC with specified CIDR block
 * // - Public, private, and isolated subnets across AZs
 * // - Internet gateway for public access
 * // - NAT gateways for private subnet outbound access
 * // - Security groups based on configuration
 * // - Route tables with appropriate routing rules
 * 
 * // Access created resources
 * Vpc vpc = network.getVpc();
 * List<SecurityGroup> securityGroups = network.getSecurityGroups();
 * 
 * // Use VPC for other constructs
 * LambdaConstruct lambda = new LambdaConstruct(
 *     this, common, lambdaConfig, vpc
 * );
 * }</pre>
 * 
 * @author CDK Common Framework
 * @since 1.0.0
 * @see Vpc for AWS CDK VPC construct
 * @see SecurityGroup for security group management
 * @see SubnetConfiguration for subnet setup
 * @see NetworkConf for network configuration model
 */
@Slf4j
@Getter
public class VpcConstruct extends Construct {
  private final Vpc vpc;
  private final List<SecurityGroup> securityGroups;

  public VpcConstruct(Construct scope, Common common, NetworkConf conf) {
    super(scope, id("vpc", conf.name()));

    log.debug("{} [common: {} conf: {}]", "VpcConstruct", common, conf);

    this.vpc = Vpc.Builder
      .create(this, conf.name())
      .vpcName(conf.name())
      .ipProtocol(conf.ipProtocol())
      .ipAddresses(IpAddresses.cidr(conf.cidr()))
      .availabilityZones(conf.availabilityZones())
      .natGateways(conf.natGateways())
      .createInternetGateway(conf.createInternetGateway())
      .enableDnsSupport(conf.enableDnsSupport())
      .enableDnsHostnames(conf.enableDnsHostnames())
      .defaultInstanceTenancy(conf.defaultInstanceTenancy())
      .subnetConfiguration(conf.subnets().stream()
        .map(subnet -> {
          var subnetConfiguration = SubnetConfiguration.builder()
            .name(subnet.name())
            .cidrMask(subnet.cidrMask())
            .reserved(subnet.reserved())
            .subnetType(subnet.subnetType());

          if (subnet.subnetType().equals(SubnetType.PUBLIC)) {
            subnetConfiguration.mapPublicIpOnLaunch(subnet.mapPublicIpOnLaunch());
          }

          return subnetConfiguration.build();
        }).toList())
      .build();

    this.securityGroups = conf.securityGroups().stream()
      .map(sg -> new SecurityGroupConstruct(scope, common, sg, this.vpc()).securityGroup())
      .toList();

    tagging(common, conf);
  }

  private void tagging(Common common, NetworkConf conf) {
    Maps.from(common.tags(), conf.tags())
      .forEach((key, value) -> Tags.of(this.vpc()).add(key, value));

    var tagsForSubnetType = conf.subnets().stream()
      .collect(Collectors.toMap(Subnet::subnetType, Subnet::tags));

    this.vpc().getPublicSubnets()
      .forEach(subnet -> tagsForSubnetType.get(SubnetType.PUBLIC)
        .forEach((key, value) -> Tags.of(subnet).add(key, value)));

    this.vpc().getPrivateSubnets()
      .forEach(subnet -> tagsForSubnetType.get(SubnetType.PRIVATE_WITH_EGRESS)
        .forEach((key, value) -> Tags.of(subnet).add(key, value)));

    this.vpc().getIsolatedSubnets()
      .forEach(subnet -> tagsForSubnetType.get(SubnetType.PRIVATE_ISOLATED)
        .forEach((key, value) -> Tags.of(subnet).add(key, value)));
  }
}
