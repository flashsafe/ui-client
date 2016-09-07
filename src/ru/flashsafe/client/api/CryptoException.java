package ru.flashsafe.client.api;

/**
 * Created by igorstemper on 29.08.16.
 */
public class CryptoException extends Exception {
    public CryptoException() {
    }

    public CryptoException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
