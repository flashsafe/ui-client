package ru.flashsafe.client.api;

import org.json.JSONArray;
import org.json.JSONObject;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import ru.flashsafe.client.util.TokenUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

import org.pkcs11.jacknji11.CKA;

/**
 * Created by igorstemper on 26.08.16.
 */
public class FlashSafe {

    public static void main(String[] args) {
        FlashSafe flashSafe = new FlashSafe();
    }

    public FlashSafe() {}

    // возвращает список объектов JSONObject элементов дерева директорий
    public static void getTree(final FSCallback<ArrayList<FlashObject>> callback) {
        Api.execute().getTree(
                Settings.getDSN(),
                Settings.getToken(),
                "getTree",
                new Callback<JSONObject>() {
                    @Override
                    public void success(JSONObject j, Response response) {
                        if (j != null) {
                            if ("success".equals(j.getString("status"))) {
                                JSONArray array = j.getJSONArray("response");
                                ArrayList<FlashObject> list = new ArrayList<FlashObject>();
                                for (int i = 0; i < array.length(); i++) {
                                    FlashObject flashObject = FlashObject.parseFromJson(array.getJSONObject(i));
                                    list.add(flashObject);
                                }
                                if (callback != null) callback.onResult(list);
                            }
                            if ("error".equals(j.getString("status"))) {
                                if (callback != null) callback.onResult(null);
                            }
                        } else {
                            if (callback != null) callback.onResult(null);
                        }
                    }

                    @Override
                    public void failure(RetrofitError retrofitError) {
                        System.err.print(retrofitError.toString());
                        if (callback != null) callback.onResult(null);
                    }
                }
        );
    }

    // возвращает список объектов родителя. если родитель = "", то возвращает список объектов корня
    public static void listObjects(String parent, final FSCallback<ArrayList<FlashObject>> callback) {
        Api.execute().listObjects(
                Settings.getDSN(),
                Settings.getToken(),
                "listObjects",
                parent,
                new Callback<JSONObject>() {
                    @Override
                    public void success(JSONObject j, Response response) {
                        if (j != null) {
                            if ("success".equals(j.getString("status"))) {
                                JSONArray array = j.getJSONArray("response");
                                ArrayList<FlashObject> list = new ArrayList<FlashObject>();
                                for (int i = 0; i < array.length(); i++) {
                                    FlashObject flashObject = FlashObject.parseFromJson(array.getJSONObject(i));
                                    list.add(flashObject);
                                }
                                if (callback != null) callback.onResult(list);
                            }
                            if ("error".equals(j.getString("status"))) {
                                if (callback != null) callback.onResult(null);
                            }
                        } else {
                            if (callback != null) callback.onResult(null);
                        }
                    }

                    @Override
                    public void failure(RetrofitError retrofitError) {
                        System.err.print(retrofitError.toString());
                        if (callback != null) callback.onResult(null);
                    }
                }
        );
    }

    // создает директорию. возвращает HASH
    public static void makeFolder(String parent, String folderName, final FSCallback<String> callback) {
        Api.execute().makeFolder(
                Settings.getDSN(),
                Settings.getToken(),
                "MakeFolder",
                parent,
                folderName,
                new Callback<JSONObject>() {
                    @Override
                    public void success(JSONObject j, Response response) {
                        if (j != null) {
                            if ("success".equals(j.getString("status"))) {
                                if (callback != null) callback.onResult(j.getString("response"));
                            }
                            if ("error".equals(j.getString("status"))) {
                                System.err.println(j.getString("result"));
                                if (callback != null) callback.onResult(null);
                            }
                        } else {
                            if (callback != null) callback.onResult(null);
                        }
                    }

                    @Override
                    public void failure(RetrofitError retrofitError) {
                        if (callback != null) callback.onResult(null);
                    }
                }
        );
    }

    // удаляет объект. возвращает список объектов родителя
    public static void delete(String hash, final FSCallback<ArrayList<FlashObject>> callback) {
        Api.execute().delete(
                Settings.getDSN(),
                Settings.getToken(),
                "delete",
                hash,
                new Callback<JSONObject>() {
                    @Override
                    public void success(JSONObject j, Response response) {
                        if (j != null) {
                            if ("success".equals(j.getString("status"))) {
                                JSONArray array = j.getJSONArray("response");
                                ArrayList<FlashObject> list = new ArrayList<FlashObject>();
                                for (int i = 0; i < array.length(); i++) {
                                    FlashObject flashObject = FlashObject.parseFromJson(array.getJSONObject(i));
                                    list.add(flashObject);
                                }
                                if (callback != null) callback.onResult(list);
                            }
                            if ("error".equals(j.getString("status"))) {
                                if (callback != null) callback.onResult(null);
                            }
                        } else {
                            if (callback != null) callback.onResult(null);
                        }
                    }

                    @Override
                    public void failure(RetrofitError retrofitError) {
                        System.err.print(retrofitError.toString());
                        if (callback != null) callback.onResult(null);
                    }
                }
        );
    }

    // загружает файл на сервер. fileUrl - путь к локальному файлу
    public static void upload(final String parent, final String fileUrl, final FSCallback<ArrayList<FlashObject>> callback, final UploadProgressListener progressListener) throws IOException {
        final File f = new File(fileUrl);
        if (!f.exists()) throw new IOException("File not found");
        final long size = f.length();
        if (Settings.needEncrypt() == 1) {
            FSCrypto.encryptFile(f, new FSCallback<File>() {
                @Override
                public void onResult(File _f) {
                    FSTypedFile file = new FSTypedFile("multipart/mixed", _f, progressListener);
                    //System.err.print("SIZE: " + parent);
                    Api.execute().upload(
                            Settings.getDSN(),
                            Settings.getToken(),
                            "Upload",
                            String.valueOf(size),
                            parent,
                            file,
                            String.valueOf(Settings.needEncrypt()),
                            f.getName(),
                            new Callback<JSONObject>() {
                                @Override
                                public void success(JSONObject j, Response response) {
                                    if (j != null) {
                                        if ("success".equals(j.getString("status"))) {
                                            JSONArray array = j.getJSONArray("response");
                                            ArrayList<FlashObject> list = new ArrayList<FlashObject>();
                                            for (int i = 0; i < array.length(); i++) {
                                                FlashObject flashObject = FlashObject.parseFromJson(array.getJSONObject(i));
                                                list.add(flashObject);
                                            }
                                            if (callback != null) callback.onResult(list);
                                        }
                                        if ("error".equals(j.getString("status"))) {
                                            if (callback != null) callback.onResult(null);
                                        }
                                    } else {
                                        if (callback != null) callback.onResult(null);
                                    }
                                }

                                @Override
                                public void failure(RetrofitError retrofitError) {
                                    System.err.print(retrofitError.toString());
                                    if (callback != null) callback.onResult(null);
                                }
                            }
                    );
                }
            });
        } else {
            FSTypedFile file = new FSTypedFile("multipart/mixed", f, progressListener);
            //System.err.print("SIZE: " + parent);
            Api.execute().upload(
                    Settings.getDSN(),
                    Settings.getToken(),
                    "Upload",
                    String.valueOf(size),
                    parent,
                    file,
                    String.valueOf(Settings.needEncrypt()),
                    f.getName(),
                    new Callback<JSONObject>() {
                        @Override
                        public void success(JSONObject j, Response response) {
                            if (j != null) {
                                if ("success".equals(j.getString("status"))) {
                                    JSONArray array = j.getJSONArray("response");
                                    ArrayList<FlashObject> list = new ArrayList<FlashObject>();
                                    for (int i = 0; i < array.length(); i++) {
                                        FlashObject flashObject = FlashObject.parseFromJson(array.getJSONObject(i));
                                        list.add(flashObject);
                                    }
                                    if (callback != null) callback.onResult(list);
                                }
                                if ("error".equals(j.getString("status"))) {
                                    System.out.println("Error: " + j.getString("result"));
                                    if (callback != null) callback.onResult(null);
                                }
                            } else {
                                if (callback != null) callback.onResult(null);
                            }
                        }

                        @Override
                        public void failure(RetrofitError retrofitError) {
                            System.err.print(retrofitError.toString());
                            if (callback != null) callback.onResult(null);
                        }
                    }
            );
        }
    }
    
    public static void download(boolean encrypted, String hash, String localUrl, DownloadListener listener, DownloadProgressListener progressListener) throws IOException {
    	new Thread(() -> {
    	    try {
    	        System.out.println("Downloading file " + hash + ", dsn " + Settings.getDSN() + ", token " + Settings.getToken());
                HttpURLConnection conn = (HttpURLConnection) new URL("http://api.flash.so/getFile.php?dsn=" + Settings.getDSN() + "&token=" + Settings.getToken() + "&hash=" + hash).openConnection();
                conn.setRequestMethod("GET");
                conn.connect();
                InputStream in = conn.getInputStream();
                OutputStream out = new FileOutputStream(localUrl);
                long totalBytes = conn.getContentLength();
                long processedBytes = 0;
                byte[] buffer = new byte[4096];
                int b;
                while ((b = in.read(buffer)) != -1) {
                    byte[] part = b == 4096 ? buffer : Arrays.copyOf(buffer, b);
                    out.write(encrypted ? TokenUtil.decrypt(part) : part);
                    processedBytes += b;
                    progressListener.transferred(processedBytes * 100 / totalBytes);
                }
                in.close();
                conn.disconnect();
                out.close();
                listener.onSuccess();
            } catch(IOException e) {
                System.err.println("Error on download file");
            }
        }).start();
    }
    
    public interface DownloadListener {
    	void onSuccess();
    }
    
    /*private static boolean isEncryptEnabled() {
        File e = new File("./.e");
        return e.exists();
    }*/

}
