package com.cannizarro.jukebox.config.exception;

public class JukeboxException extends RuntimeException{

    public JukeboxException(String message, Throwable cause) {
        super(message, cause);
    }

    public JukeboxException(String message) {
        super(message);
    }
}
