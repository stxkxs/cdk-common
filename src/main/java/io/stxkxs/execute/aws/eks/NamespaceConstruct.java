package io.stxkxs.execute.aws.eks;

import com.fasterxml.jackson.core.type.TypeReference;
import io.fabric8.kubernetes.api.model.NamespaceBuilder;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.client.utils.Serialization;
import io.stxkxs.execute.serialization.Mapper;
import io.stxkxs.model._main.Common;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awscdk.services.eks.ICluster;
import software.amazon.awscdk.services.eks.KubernetesManifest;
import software.constructs.Construct;

import java.util.List;
import java.util.Map;

import static io.stxkxs.execute.serialization.Format.id;

@Slf4j
@Getter
public class NamespaceConstruct extends Construct {
  private final KubernetesManifest manifest;

  @SneakyThrows
  public NamespaceConstruct(Construct scope, Common common, ObjectMeta metadata, ICluster cluster) {
    super(scope, id("namespace", metadata.getName()));

    log.debug("{} [common: {}]", "NamespaceConstruct", common);

    var namespace = new NamespaceBuilder()
      .withNewMetadata()
      .withName(metadata.getNamespace())
      .withLabels(metadata.getLabels())
      .withAnnotations(metadata.getAnnotations())
      .endMetadata()
      .build();

    var manifest = Mapper.get()
      .readValue(Serialization.asYaml(namespace), new TypeReference<Map<String, Object>>() {});

    this.manifest = KubernetesManifest.Builder
      .create(this, metadata.getName())
      .cluster(cluster)
      .prune(true)
      .overwrite(true)
      .skipValidation(true)
      .manifest(List.of(manifest))
      .build();
  }
}
