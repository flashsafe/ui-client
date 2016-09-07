package ru.flashsafe.client.api;

/**
 * Created by igorstemper on 26.08.16.
 */
public interface FSCallback<T> {
    void onResult(T t);
}