package ru.flashsafe.client.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;

import org.json.JSONObject;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.converter.ConversionException;
import retrofit.converter.Converter;
import retrofit.http.*;
import retrofit.mime.TypedInput;
import retrofit.mime.TypedOutput;

public class Api {
	private static IApi apiInterfaceService;

	public static IApi execute() {
		if (apiInterfaceService == null) {
			RestAdapter restAdapter = new RestAdapter.Builder()
					.setEndpoint("http://api.flash.so")
                    .setConverter(new JsonConverter())
                    .build();
            apiInterfaceService = restAdapter.create(IApi.class);
		}
		return apiInterfaceService;
	}

    static class JsonConverter implements Converter {

        @Override
        public Object fromBody(TypedInput typedInput, Type type) throws ConversionException {
            JSONObject json = null;
            try {
                json = fromStream(typedInput.in());
            } catch (IOException ignored) {/*NOP*/ }

            return json;
        }

        @Override
        public TypedOutput toBody(Object o) {
            return null;
        }

        public static JSONObject fromStream(InputStream in) throws IOException {
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            StringBuilder out = new StringBuilder();
            String newLine = System.getProperty("line.separator");
            String line;
            while ((line = reader.readLine()) != null) {
                out.append(line);
                out.append(newLine);
            }
            return new JSONObject(out.toString());
        }
    }

	public interface IApi {
        @FormUrlEncoded
        @POST("/api.php")
        void getTree (
                @Field("dsn") String dsn,
                @Field("token") String token,
                @Field("method") String method,
                Callback<JSONObject> callback
        );
        @FormUrlEncoded
        @POST("/api.php")
        void listObjects (
                @Field("dsn") String dsn,
                @Field("token") String token,
                @Field("method") String method,
                @Field("parent") String parent,
                Callback<JSONObject> callback
        );
        @FormUrlEncoded
        @POST("/api.php")
        void makeFolder (
                @Field("dsn") String dsn,
                @Field("token") String token,
                @Field("method") String method,
                @Field("parent") String parent,
                @Field("folderName") String folderName,
                Callback<JSONObject> callback
        );
        @FormUrlEncoded
        @POST("/api.php")
        void delete (
                @Field("dsn") String dsn,
                @Field("token") String token,
                @Field("method") String method,
                @Field("hash") String parent,
                Callback<JSONObject> callback
        );
		@Multipart
		@POST("/api.php")
		void upload(
                @Part("dsn") String dsn,
                @Part("token") String token,
                @Part("method") String method,
                @Part("fileSize") String fileSize,
                @Part("parent") String parent,
                @Part("file") FSTypedFile file,
                @Part("isEncrypted") String isEncrypted,
                @Part("fileName") String fileName,
                Callback<JSONObject> callback
        );
	}
}