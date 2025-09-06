package io.stxkxs.model.aws.ses;

public record IdentityConf(String hostedZone, String email, String domain, String mxFailure, String mailFromDomain,
  boolean feedbackForwarding, ConfigurationSetConf configurationSet) {}
