package io.stxkxs.model.aws.ses;

public record Sender(String sesRegion, String sesVerifiedDomain, String fromName, String fromEmail, String replyTo,
  String configurationSetName) {}
