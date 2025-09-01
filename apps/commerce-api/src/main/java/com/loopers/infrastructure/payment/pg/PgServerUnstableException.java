package com.loopers.infrastructure.payment.pg;

public class PgServerUnstableException extends RuntimeException{
    public PgServerUnstableException(String message) {
        super(message);
    }
}
