package ru.flashsafe.client.api;

import org.json.JSONObject;

/**
 * Created by igorstemper on 26.08.16.
 */

/*

objectHash – хэш-код объекта
parentHash – хэш-код родителя (если «» то верхний уровень)
objectType – тип объекта (FOLDER, FILE)
objectName – название объекта
extension – расширение объекта (для типа FOLDER – пустое)
size – размер файла в байтах
mimeType – mime-тип объекта

 */
public class FlashObject {
    public String objectHash;
    public String parentHash;
    public String objectType;
    public String objectName;
    public String extension;
    public long size;
    public String mimeType;
    public int isEncrypted;

    public static FlashObject parseFromJson(JSONObject object) {
        FlashObject fo =  new FlashObject();
        fo.objectHash = (object.isNull("objectHash")) ? null : object.getString("objectHash");
        fo.parentHash = (object.isNull("parentHash")) ? null : object.getString("parentHash");
        fo.objectType = (object.isNull("objectType")) ? null : object.getString("objectType");
        fo.objectName = (object.isNull("objectName")) ? null : object.getString("objectName");
        fo.extension = (object.isNull("extension")) ? null : object.getString("extension");
        fo.size = (object.isNull("size")) ? 0 : object.getLong("size");
        fo.mimeType = (object.isNull("mimeType")) ? null : object.getString("mimeType");
        fo.isEncrypted = (object.isNull("isEncrypted")) ? 0 : object.getInt("isEncrypted");
        return fo;
    }
}
