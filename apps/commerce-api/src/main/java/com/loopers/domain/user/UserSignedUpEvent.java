package com.loopers.domain.user;

import java.io.Serializable;
public record UserSignedUpEvent(
        UserId userId
) implements Serializable {
}
