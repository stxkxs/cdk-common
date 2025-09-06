package io.stxkxs.model.aws.cognito.userpool;

import io.stxkxs.model.aws.fn.Lambda;
import io.stxkxs.model.aws.fn.LambdaLayer;

public record Triggers(LambdaLayer base, Lambda preSignUp, Lambda customMessage, Lambda postConfirmation, Lambda postAuthentication) {}
