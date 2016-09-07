package ru.flashsafe.client;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import ru.flashsafe.client.api.FlashObject;
import javafx.scene.image.Image;

/**
 * 
 * 
 * @author Andrew
 *
 */
public class IconUtil {
    
    private static final Image EMPTY_FOLDER_ICON = new Image(IconUtil.class.getResourceAsStream("/img/fs/folder_empty.png"));
    
    private static final String EMPTY_FOLDER_ICON_URI = "classpath:img/fs/folder_empty.png";
    
    @SuppressWarnings("unused")
    private static final Image LOCKED_FOLDER_ICON = new Image(IconUtil.class.getResourceAsStream("/img/fs/folder_lock.png"));
    
    private static final Image FOLDER_ICON = new Image(IconUtil.class.getResourceAsStream("/img/fs/folder.png"));
    
    private static final String FOLDER_ICON_URI = "classpath:img/fs/folder.png";

    private static final Map<String, Image> extensionToIconMap = new HashMap<>();
    
    private static final Map<String, String> extensionToIconUriMap = new HashMap<>();

    private static final String UNKNOWN_EXTENSION = "UNKNOWN_EXTENSION";
    
    public enum IconType {

        ARCHIVE,

        DOCUMENT,

        PICTURE,

        MUSIC,

        VIDEO,

        BINARY

    }

    static {
        init();
    }

    private IconUtil() {
    }

    public static Image getFileIcon(String filename) {
        String extension = filename.substring(filename.lastIndexOf(".") + 1);
        Image fileIcon = extensionToIconMap.get(extension);
        return fileIcon != null ? fileIcon : extensionToIconMap.get(UNKNOWN_EXTENSION); 
    }
    
    public static Image getFolderIcon(FlashObject folder) {
        if (folder.size > 0 ) {
            return FOLDER_ICON;
        }
        return EMPTY_FOLDER_ICON;
    }
    
    public static String getFileIconUri(String filename) {
        String extension = filename.substring(filename.lastIndexOf(".") + 1);
        String fileIconUri = extensionToIconUriMap.get(extension);
        return fileIconUri != null ? fileIconUri : extensionToIconUriMap.get(UNKNOWN_EXTENSION); 
    }
    
    public static String getFolderIconUri(FlashObject folder) {
        if (folder.size > 0 ) {
            return FOLDER_ICON_URI;
        }
        return EMPTY_FOLDER_ICON_URI;
    }

    private static void init() {
        Map<IconType, Image> typeToImageMap = loadIcons();
        Map<IconType, String> typeToImageUriMap = loadUris();
        Map<IconType, List<String>> typeToExtentionMap = getExtensions();
        mapExtensionsToIcons(typeToExtentionMap, typeToImageMap);
        mapExtensionsToIconUris(typeToExtentionMap, typeToImageUriMap);
    }

    private static void mapExtensionsToIcons(Map<IconType, List<String>> typeToExtentionMap, Map<IconType, Image> typeToImageMap) {
        for (Entry<IconType, List<String>> typeToExtention : typeToExtentionMap.entrySet()) {
            for (String extension : typeToExtention.getValue()) {
                extensionToIconMap.put(extension, typeToImageMap.get(typeToExtention.getKey()));
            }
        }
        extensionToIconMap.put(UNKNOWN_EXTENSION, typeToImageMap.get(IconType.BINARY));
    }
    
    private static void mapExtensionsToIconUris(Map<IconType, List<String>> typeToExtentionMap, Map<IconType, String> typeToImageUriMap) {
        for (Entry<IconType, List<String>> typeToExtention : typeToExtentionMap.entrySet()) {
            for (String extension : typeToExtention.getValue()) {
                extensionToIconUriMap.put(extension, typeToImageUriMap.get(typeToExtention.getKey()));
            }
        }
        extensionToIconUriMap.put(UNKNOWN_EXTENSION, typeToImageUriMap.get(IconType.BINARY));
    }

    private static Map<IconType, List<String>> getExtensions() {
        Map<IconType, List<String>> typeToImageMap = new HashMap<>();
        typeToImageMap.put(IconType.ARCHIVE, Arrays.asList("rar", "zip", "gz", "bz", "7z", "bz2", "tar", "deb", "rpm"));
        typeToImageMap.put(IconType.DOCUMENT,
                Arrays.asList("txt", "rtf", "doc", "xls", "ppt", "docx", "xlsx", "pptx", "odt", "odp", "ods", "odg"));
        typeToImageMap.put(IconType.PICTURE,
                Arrays.asList("jpeg", "jpe", "jpg", "png", "gif", "tiff", "tif", "bmp", "wlmp", "svg", "eps", "ico", "icns"));
        typeToImageMap.put(IconType.MUSIC, Arrays.asList("mp1", "mp2", "mp3", "wma", "wav", "amr", "aac", "midi", "ogg"));
        typeToImageMap.put(IconType.VIDEO, Arrays.asList("3gp", "3gpp", "avi", "flv", "mkv", "mov", "qt", "vob", "wmv"));
        return typeToImageMap;
    }

    private static Map<IconType, Image> loadIcons() {
        Map<IconType, Image> typeToImage = new HashMap<>();
        typeToImage.put(IconType.ARCHIVE, new Image(IconUtil.class.getResourceAsStream("/img/fs/archive.png")));
        typeToImage.put(IconType.DOCUMENT, new Image(IconUtil.class.getResourceAsStream("/img/fs/document.png")));
        typeToImage.put(IconType.PICTURE, new Image(IconUtil.class.getResourceAsStream("/img/fs/picture.png")));
        typeToImage.put(IconType.MUSIC, new Image(IconUtil.class.getResourceAsStream("/img/fs/music.png")));
        typeToImage.put(IconType.VIDEO, new Image(IconUtil.class.getResourceAsStream("/img/fs/video.png")));
        typeToImage.put(IconType.BINARY, new Image(IconUtil.class.getResourceAsStream("/img/fs/binary.png")));
        return typeToImage;
    }
    
    private static Map<IconType, String> loadUris() {
        Map<IconType, String> typeToUri = new HashMap<>();
        typeToUri.put(IconType.ARCHIVE, "classpath:img/fs/archive.png");
        typeToUri.put(IconType.DOCUMENT, "classpath:img/fs/document.png");
        typeToUri.put(IconType.PICTURE, "classpath:img/fs/picture.png");
        typeToUri.put(IconType.MUSIC, "classpath:img/fs/music.png");
        typeToUri.put(IconType.VIDEO, "classpath:img/fs/video.png");
        typeToUri.put(IconType.BINARY, "classpath:img/fs/binary.png");
        return typeToUri;
    }

}
