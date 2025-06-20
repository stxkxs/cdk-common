package io.stxkxs.model.aws.cognito.userpool;

public record UserVerification(
  String emailBody,
  String emailStyle,
  String emailSubject,
  String smsMessage
) {}
