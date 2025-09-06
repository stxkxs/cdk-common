package io.stxkxs.model.aws.bcm;

import io.stxkxs.model.aws.s3.S3Bucket;
import java.util.Map;

public record DataExportConf(String name, String description, DataQuery dataQuery, DestinationConfigurations destinationConfigurations,
  String refreshCadence, S3Bucket bucket, Map<String, String> tags) {}
