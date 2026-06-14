package com.gaab.anivault.domain.exception;

public class EpisodeDoesNotExistException extends RuntimeException {
    public EpisodeDoesNotExistException(Integer episodeNumber) {
        super("El episodio " + episodeNumber + " no existe.");
    }
}
