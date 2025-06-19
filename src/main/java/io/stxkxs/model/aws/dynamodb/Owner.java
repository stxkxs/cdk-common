package io.stxkxs.model.aws.dynamodb;

public enum Owner {
  AWS, DYNAMODB, SELF;

  public static Owner of(Object o) {
    return Owner.valueOf(o.toString().toUpperCase());
  }

  @Override
  public String toString() {
    return name().toLowerCase();
  }
}
