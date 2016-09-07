package ru.flashsafe.client.api;

/**
 * Created by igorstemper on 26.08.16.
 */
public interface UploadProgressListener {
    void transferred(long num);
}
