package ru.flashsafe.client.api;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.FileSystems;
import java.util.List;
import java.util.function.Consumer;
import net.samuelcampos.usbdrivedectector.USBDeviceDetectorManager;
import net.samuelcampos.usbdrivedectector.USBStorageDevice;
import org.pkcs11.jacknji11.CE;
import org.pkcs11.jacknji11.CKA;
import org.pkcs11.jacknji11.CKO;
import org.pkcs11.jacknji11.CK_TOKEN_INFO;

import ru.flashsafe.client.SplashUI;
import ru.flashsafe.client.util.OSUtil;
import ru.flashsafe.client.util.TokenUtil;
import ru.flashsafe.client.util.os.OSType;

/**
 * Created by igorstemper on 26.08.16.
 */
public class Settings {
    public static String getDSN() {
        if(!TokenUtil.isInitialized()) TokenUtil.init();
        CK_TOKEN_INFO tokenInfo = TokenUtil.getTokenInfo(TokenUtil.slot);
        return new String(tokenInfo.serialNumber);
    }

    public static String getToken() {
        if(!TokenUtil.isInitialized()) TokenUtil.init();
        TokenUtil.findObjectsInit(TokenUtil.session, new CKA(CKA.CLASS, CKO.PUBLIC_KEY));
        long[] objects = TokenUtil.findObjects(TokenUtil.session, 1);
        TokenUtil.findObjectsFinal(TokenUtil.session);
        CKA publicValue = TokenUtil.getAttributeValue(TokenUtil.session, objects[0], CKA.MODULUS);
        return MD5(publicValue.getValue());
    }

    /*private static String read(String uri) {
        String s = "";
        try {
            FileReader freader = new FileReader(uri);
            int c;
            while((c = freader.read()) != -1) {
                s += (char) c;
            }
            freader.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
        return s;
    }*/
    
    public static int needEncrypt() {
        //File e = new File("./.e");
        //return e.exists() ? 1 : 0;
        if(!TokenUtil.isInitialized()) TokenUtil.init();
        long objCount = TokenUtil.findObjects(new CKA(CKA.LABEL, "Encrypt"));
        return objCount > 0 ? 1 : 0;
    }
    
    private static String MD5(byte[] md5) {
        try {
             java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
             byte[] array = md.digest(md5);
             StringBuffer sb = new StringBuffer();
             for (int i = 0; i < array.length; ++i) {
               sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1,3));
            }
             return sb.toString();
         } catch (java.security.NoSuchAlgorithmException e) {
             System.err.println("Error on generate MD5" + e);
         }
        return null;
    }
    
    /*private static String detectFlashPath() {
        if(OSUtil.getOSType() == OSType.MACOS) {
            String[] uri = new String[1];
            FileSystems.getDefault().getFileStores().forEach(new Consumer<FileStore>() {
                @Override
                public void accept(FileStore t) {
                    String uuri = t.toString().substring(0, t.toString().indexOf(" ("));
                    if(uuri.contains("Flashsafe")) {
                        uri[0] = uuri;
                    }
                }
            });
            return uri[0];
        } else {
            List<USBStorageDevice> removableDevices = new USBDeviceDetectorManager().getRemovableDevices();
            for(USBStorageDevice device : removableDevices) {
                String name = device.getSystemDisplayName();
                if(name.contains("Flashsafe")) {
                    return /*device.getRootDirectory().getAbsolutePath()*//*new File(".").getAbsolutePath();
                }
            }
        }
        return null;
    }*/
}
