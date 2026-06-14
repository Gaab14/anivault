package com.gaab.anivault.web.exception;

public record Error(
        String type,
        String message
) {
}
