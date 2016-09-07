package ru.flashsafe.client;

import com.trolltech.qt.core.QFile;
import java.io.File;

public interface FileController {
    
    void upload(QFile fileObject, String toPath);
    
    
    void download(boolean encrypted, String fromPath, QFile toFile);
    
    void loadContent(String path);
    
    void move(String fromPath, String toPath);
    
    void copy(String fromPath, String toPath);
    
    void rename(String fileObjectHash, String name);
    
    void delete(String path);
    
    void createDirectory(String name);
    
    String createDirectory(String parentHash, String name);
    
    String getCurrentLocation();
    
}
