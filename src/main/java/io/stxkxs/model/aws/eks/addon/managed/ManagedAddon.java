package io.stxkxs.model.aws.eks.addon.managed;

import io.stxkxs.model.aws.eks.ServiceAccountConf;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ManagedAddon {
  private String configurationValues;
  private String name;
  private boolean preserveOnDelete;
  private String resolveConflicts;
  private ServiceAccountConf serviceAccount;
  private Map<String, String> tags;
  private String version;
}
