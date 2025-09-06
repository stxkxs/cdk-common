package io.stxkxs.model.aws.cognito.client;

public record ClientAttributesConf(boolean address, boolean birthdate, boolean email, boolean email_verified, boolean family_name,
  boolean name, boolean gender, boolean given_name, boolean updated_at, boolean locale, boolean middle_name, boolean nickname,
  boolean phone_number, boolean phone_number_verified, boolean preferred_username, boolean profile_page, boolean profile_picture,
  boolean timezone, boolean website) {}
