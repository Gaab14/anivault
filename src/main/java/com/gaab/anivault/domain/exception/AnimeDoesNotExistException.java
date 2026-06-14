package com.gaab.anivault.domain.exception;

public class AnimeDoesNotExistException extends RuntimeException {
    public AnimeDoesNotExistException(Long id) {
        super("El anime con ID: " + id + " no existe.");
    }
}
