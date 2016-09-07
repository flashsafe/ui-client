package ru.flashsafe.client.api;

import ru.flashsafe.client.util.TokenUtil;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * Created by igorstemper on 29.08.16.
 */
public class FSCrypto {
    private static void encrypt(File inputFile, File outputFile) throws Exception {
        doCrypto(Cipher.ENCRYPT_MODE, inputFile, outputFile);
    }

    private static void decrypt(File inputFile, File outputFile) throws Exception {
        doCrypto(Cipher.DECRYPT_MODE, inputFile, outputFile);
    }

    private static void doCrypto(int cipherMode, File inputFile, File outputFile) throws Exception {
        /*Key secretKey = new SecretKeySpec(getKey().getBytes(), "AES");
        System.err.print("CRYPTO KEY LENGTH: "+secretKey.getEncoded().toString().length()+"\n");
        if (secretKey == null) throw new Exception("Error generate secret key");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        byte[] iv = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
        IvParameterSpec ivspec = new IvParameterSpec(iv);
        cipher.init(cipherMode, secretKey, ivspec);*/

        FileInputStream inputStream = new FileInputStream(inputFile);
        FileOutputStream outputStream = new FileOutputStream(outputFile);
        //byte[] inputBytes = new byte[(int) inputFile.length()];
        byte[] buffer = new byte[4096];
        int s;
        if(!TokenUtil.isInitialized()) TokenUtil.init();
        while((s = inputStream.read(buffer)) != 0) {
            byte[] part = buffer.length == 4096 ? buffer : Arrays.copyOf(buffer, s);
            if(cipherMode == Cipher.ENCRYPT_MODE) {
                outputStream.write(TokenUtil.encrypt(part));
            } else if(cipherMode == Cipher.DECRYPT_MODE) {
                outputStream.write(TokenUtil.decrypt(part));
            }
        }
        //inputStream.read(inputBytes);

        //byte[] outputBytes = cipher.doFinal(inputBytes);

        //FileOutputStream outputStream = new FileOutputStream(outputFile);
        //outputStream.write(outputBytes);

        inputStream.close();
        outputStream.close();
    }

    private static String md5(final String s) {
        final String MD5 = "MD5";
        try {
            MessageDigest digest = MessageDigest.getInstance(MD5);
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                String h = Integer.toHexString(0xFF & aMessageDigest);
                while (h.length() < 2) h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    private static String getKey() {
        String res = Settings.getToken()+Settings.getDSN();
        for (int i = 0; i < 10; i++) {
            res = md5(res);
        }
        return res.substring(0, 16);
    }

    public static void encryptFile(final File inFile, final FSCallback<File> callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    File temp = File.createTempFile(md5(inFile.getAbsolutePath()), "");
                    FSCrypto.encrypt(inFile, temp);
                    if (callback != null) callback.onResult(temp);
                } catch (Exception e) {
                    if (callback != null) callback.onResult(null);
                }
            }
        }).start();
    }

    public static void decryptFile(final File inFile, final String destinationPath, final FSCallback<File> callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    File temp = new File(destinationPath);
                    FSCrypto.decrypt(inFile, temp);
                    if (callback != null) callback.onResult(temp);
                } catch (Exception e) {
                    System.err.print("decrypt error "+e.getMessage());
                    if (callback != null) callback.onResult(null);
                }
            }
        }).start();
    }

}

