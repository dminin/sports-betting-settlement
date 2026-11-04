package com.sporty.settlement.domain;

final class Preconditions {

    private Preconditions() {
    }

    static String requireNonBlank(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }
}
