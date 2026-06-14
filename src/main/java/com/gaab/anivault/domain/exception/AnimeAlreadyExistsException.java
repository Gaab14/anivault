package com.gaab.anivault.domain.exception;

public class AnimeAlreadyExistsException extends RuntimeException {
    public AnimeAlreadyExistsException(String title) {
        super("El anime " + title + " ya existe.");
    }
}
