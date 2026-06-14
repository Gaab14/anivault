package com.gaab.anivault.web.exception;

import com.gaab.anivault.domain.exception.AnimeAlreadyExistsException;
import com.gaab.anivault.domain.exception.AnimeDoesNotExistException;
import com.gaab.anivault.domain.exception.EpisodeDoesNotExistException;
import com.gaab.anivault.domain.exception.UserAlreadyExistsException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
public class RestExceptionHandler {
    @ExceptionHandler(AnimeAlreadyExistsException.class)
    public ResponseEntity<Error> handleAnimeAlreadyExistsException(AnimeAlreadyExistsException e) {
        Error error = new Error("anime-already-exists", e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(AnimeDoesNotExistException.class)
    public ResponseEntity<Error> handleAnimeDoesNotExistException(AnimeDoesNotExistException e) {
        Error error = new Error("anime-does-not-exist", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<List<Error>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        List<Error> errors = new ArrayList<>();
        e.getBindingResult().getFieldErrors().forEach(fieldError -> {
            errors.add(new Error(fieldError.getField(), fieldError.getDefaultMessage()));
        });
        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(EpisodeDoesNotExistException.class)
    public ResponseEntity<Error> handleEpisodeDoesNotExistException(EpisodeDoesNotExistException e) {
        Error error = new Error("episode-does-not-exist", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<Error> handleUserAlreadyExistsException(UserAlreadyExistsException e) {
        Error error = new Error("user-already-exists", e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Error> handleBadCredentialsException(BadCredentialsException e) {
        Error error = new Error("bad-credentials", "Invalid username or password.");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }
}
