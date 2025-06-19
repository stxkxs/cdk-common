package io.stxkxs.model.aws.msk;

import java.util.List;
import java.util.Map;

public record Msk(
  String name,
  List<Client> clients,
  Map<String, String> tags
) {}
