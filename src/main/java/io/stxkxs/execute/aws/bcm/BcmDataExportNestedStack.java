package io.stxkxs.execute.aws.bcm;

import io.stxkxs.execute.aws.s3.BucketConstruct;
import io.stxkxs.model._main.Common;
import io.stxkxs.model.aws.bcm.DataExportConf;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awscdk.NestedStack;
import software.amazon.awscdk.NestedStackProps;
import software.amazon.awscdk.services.bcmdataexports.CfnExport;
import software.amazon.awscdk.services.bcmdataexports.CfnExport.DataQueryProperty;
import software.amazon.awscdk.services.bcmdataexports.CfnExport.DestinationConfigurationsProperty;
import software.amazon.awscdk.services.bcmdataexports.CfnExport.ExportProperty;
import software.amazon.awscdk.services.bcmdataexports.CfnExport.RefreshCadenceProperty;
import software.amazon.awscdk.services.bcmdataexports.CfnExport.ResourceTagProperty;
import software.amazon.awscdk.services.bcmdataexports.CfnExport.S3DestinationProperty;
import software.amazon.awscdk.services.bcmdataexports.CfnExport.S3OutputConfigurationsProperty;
import software.constructs.Construct;

@Slf4j
@Getter
public class BcmDataExportNestedStack extends NestedStack {
  private final BucketConstruct storage;
  private final CfnExport export;

  public BcmDataExportNestedStack(Construct scope, Common common, DataExportConf conf, NestedStackProps props) {
    super(scope, "bcmdataexport", props);

    log.debug("billing and cost management configuration [common: {} data-export: {}]", common, conf);

    this.storage = new BucketConstruct(this, common, conf.bucket());

    this.export = CfnExport.Builder
      .create(this, conf.name())
      .export(ExportProperty.builder()
        .name(conf.name())
        .description(conf.description())
        .dataQuery(
          DataQueryProperty.builder()
            .queryStatement(conf.dataQuery().queryStatement())
            .tableConfigurations(conf.dataQuery().tableConfigurations())
            .build())
        .destinationConfigurations(
          DestinationConfigurationsProperty.builder()
            .s3Destination(
              S3DestinationProperty.builder()
                .s3Region(conf.destinationConfigurations().region())
                .s3Bucket(conf.destinationConfigurations().bucket())
                .s3Prefix(conf.destinationConfigurations().prefix())
                .s3OutputConfigurations(
                  S3OutputConfigurationsProperty.builder()
                    .compression(conf.destinationConfigurations().outputConfigurations().compression().toUpperCase())
                    .format(conf.destinationConfigurations().outputConfigurations().format().toUpperCase())
                    .outputType(conf.destinationConfigurations().outputConfigurations().outputType().toUpperCase())
                    .overwrite(conf.destinationConfigurations().outputConfigurations().overwrite().toUpperCase())
                    .build())
                .build())
            .build())
        .refreshCadence(
          RefreshCadenceProperty.builder()
            .frequency(conf.refreshCadence().toUpperCase())
            .build())
        .build())
      .tags(conf.tags().entrySet().stream()
        .map(e -> ResourceTagProperty.builder()
          .key(e.getKey())
          .value(e.getValue())
          .build()).toList())
      .build();

    this.export().getNode().addDependency(this.storage());
  }
}
