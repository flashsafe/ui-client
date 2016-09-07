package ru.flashsafe.client.util;

import com.trolltech.qt.osinfo.OSInfo;
import org.pkcs11.jacknji11.*;
import ru.flashsafe.client.NativeUtils;
import ru.flashsafe.client.util.os.OSArch;
import ru.flashsafe.client.util.os.OSType;

import java.io.IOException;

import static org.pkcs11.jacknji11.CK_SESSION_INFO.CKF_RW_SESSION;
import static org.pkcs11.jacknji11.CK_SESSION_INFO.CKF_SERIAL_SESSION;

/**
 * Util for work with PKI-token
 * @author Alexander Krysin
 */
public class TokenUtil {
    public static long session = 0;
    public static long slot;
    private static final String PIN = "12345678";
    private static boolean initialized = false;

    static {
        String os = OSUtil.getOSType() == OSType.LINUX ? "linux"
                : OSUtil.getOSType() == OSType.MACOS ? "macos" : "windows";
        String arch = OSUtil.getOSArch() == OSArch.X64 ? "x64" : "x86";
        String libname = os.equals("linux") ? "libcastle.so.1.0.0"
                : os.equals("macos") ? "libcastle.1.0.0.dylib" : "eps2003csp11.dll";
    	try {
    	    NativeUtils.loadLibraryFromJar("/native/" + os + "/" + arch + "/" + libname);
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean isInitialized() {
        return initialized;
    }

	public static void init() {
		PKCS11Initialize();
		long[] slots = getSlotList(); 
		if(slots.length > 0) {
			slot = slots[0];
            closeAllSessions(slot);
			session = openSession(slot);
			login(session, PIN);
            initialized = true;
		}
	}
	
	public static void destruct() {
		//logout(session);
		//closeSession(slot);
		PKCS11Finalize();
        initialized = false;
	}
    
    public static byte[] encrypt(byte[] part) {
    	findObjectsInit(session, new CKA[] {new CKA(CKA.LABEL, "publicKey")});
    	long[] keys = findObjects(session, 1);
    	findObjectsFinal(session);
        encryptInit(session, new CKM(CKM.RSA_PKCS), keys[0]);
    	byte[] enc = encrypt(session, new CKM(CKM.RSA_PKCS), keys[0], part);
        return enc;
    }
    
    public static byte[] decrypt(byte[] part) {
    	findObjectsInit(session, new CKA[] {new CKA(CKA.LABEL, "privateKey")});
    	long[] keys = findObjects(session, 1);
    	findObjectsFinal(session);
        decryptInit(session, new CKM(CKM.RSA_PKCS), keys[0]);
    	byte[] dec = decrypt(session, new CKM(CKM.RSA_PKCS), keys[0], part);
        return dec;
    }

    public static long findObjects(CKA templ) {
        CE.FindObjectsInit(session, templ);
        long[] objects = CE.FindObjects(session, templ);
        CE.FindObjectsFinal(session);
        return objects.length;
    }

    public static long findObject(CKA templ) {
        CE.FindObjectsInit(session, templ);
        long[] objects = CE.FindObjects(session, templ);
        CE.FindObjectsFinal(session);
        return objects[0];
    }

    public static void createEncryptFlag() {
        CE.CreateObject(session, new CKA(CKA.CLASS, CKO.DATA),
                new CKA(CKA.LABEL, "Encrypt"),
                new CKA(CKA.TOKEN, true),
                new CKA(CKA.VALUE, 1));
    }

    public static void destroyEncryptFlag(long object) {
        CE.DestroyObject(session, object);
    }

    public static void PKCS11Initialize() {
        CE.Initialize();
    }
    
    public static long[] getSlotList() {
        return CE.GetSlotList(true);
    }
    
    public CK_INFO getInfo() {
    	return CE.GetInfo();
    }
    
    public static CK_SLOT_INFO getSlotInfo(long slotId) {
    	return CE.GetSlotInfo(slotId);
    }
    
    public static CK_TOKEN_INFO getTokenInfo(long slotId) {
    	return CE.GetTokenInfo(slotId);
    }
    
    public static long openSession(long slotId) {
        return CE.OpenSession(slotId, CKF_SERIAL_SESSION | CKF_RW_SESSION, null, null);
    }
    
    public static void login(long sessionId, String pin) {
        CE.LoginUser(sessionId, pin);
    }
    
    public static void logout(long sessionId) {
        CE.Logout(sessionId);
    }
    
    public static CK_SESSION_INFO getSessionInfo(long sessionId) {
    	return CE.GetSessionInfo(session);
    }
    
    public static void closeSession(long slotId) {
        CE.CloseSession(slotId);
    }
    
    public static long createObject(long sessionId, CKA template) {
    	return CE.CreateObject(sessionId, template);
    }
    
    public static void destroyObject(long sessionId, long objectId) {
    	CE.DestroyObject(sessionId, objectId);
    }
    
    public static void decryptInit(long sessionId, CKM mechanism, long key) {
    	CE.DecryptInit(sessionId, mechanism, key);
    }
    
    public static byte[] decrypt(long sessionId, byte[] encryptedData) {
    	return CE.DecryptPad(sessionId, encryptedData);
    }
    
    public static byte[] decrypt(long sessionId, CKM mechanism, long key, byte[] encryptedData) {
    	return CE.Decrypt(sessionId, mechanism, key, encryptedData);
    }
    
    public static byte[] decryptUpdate(long sessionId, byte[] encryptedPart) {
    	return CE.DecryptUpdate(sessionId, encryptedPart);
    }
    
    public static byte[] decryptFinal(long sessionId) {
    	return CE.DecryptFinal(sessionId);
    }
    
    public static void digestInit(long sessionId, CKM mechanism) {
    	CE.DigestInit(sessionId, mechanism);
    }
    
    public static byte[] digest(long sessionId, byte[] data) {
    	return CE.Digest(sessionId, data);
    }
    
    public static byte[] digest(long sessionId, CKM mechanism, byte[] data) {
    	return CE.Digest(sessionId, mechanism, data);
    }
    
    public static void digestUpdate(long sessionId, byte[] part) {
    	CE.DigestUpdate(sessionId, part);
    }
    
    public static byte[] digestFinal(long sessionId) {
    	return CE.DigestFinal(sessionId);
    }
    
    public static void encryptInit(long sessionId, CKM mechanism, long key) {
    	CE.EncryptInit(sessionId, mechanism, key);
    }
    
    public static byte[] encrypt(long sessionId, byte[] data) {
    	return CE.EncryptPad(sessionId, data);
    }
    
    public static byte[] encrypt(long sessionId, CKM mechanism, long key, byte[] data) {
    	return CE.Encrypt(sessionId, mechanism, key, data);
    }
    
    public static byte[] encryptUpdate(long sessionId, byte[] part) {
    	return CE.EncryptUpdate(sessionId, part);
    }
    
    public static byte[] encryptFinal(long sessionId) {
    	return CE.EncryptFinal(sessionId);
    }
    
    public static void findObjectsInit(long sessionId, CKA... templ) {
        CE.FindObjectsInit(session, templ);
    }
    
    public static long[] findObjects(long sessionId, int maxCount) {
        return CE.FindObjects(session, maxCount);
    }
    
    public static void findObjectsFinal(long sessionId) {
        CE.FindObjectsFinal(session);
    }
    
    public static CKA getAttributeValue(long sessionId, long objectId, long cka) {
        return CE.GetAttributeValue(sessionId, objectId, cka);
    }
    
    public static void setAttributeValue(long sessionId, long objectId, CKA... templ) {
        CE.SetAttributeValue(sessionId, objectId, templ);
    }
    
    public static long generateKey(long sessionId, CKM mechanism, CKA... templ) {
    	return CE.GenerateKey(sessionId, mechanism, templ);
    }
    
    public static void generateKeyPair(long sessionId, CKM mechanism, CKA[] publicKeyTemplate, CKA[] privateKeyTemplate, LongRef publicKey, LongRef privateKey) {
    	CE.GenerateKeyPair(sessionId, mechanism, publicKeyTemplate, privateKeyTemplate, publicKey, privateKey);
    }
    
    public static byte[] wrapKey(long sessionId, CKM mechanism, long wrappingKey, long key) {
    	return CE.WrapKey(sessionId, mechanism, wrappingKey, key);
    }
    
    public static long unwrapKey(long sessionId, CKM mechanism, long unwrappingKey, byte[] wrappedKey, CKA... templ) {
    	return CE.UnwrapKey(sessionId, mechanism, unwrappingKey, wrappedKey, templ);
    }
    
    public static long[] getMechanismList(long slotId) {
    	return CE.GetMechanismList(slotId);
    }
    
    public static CK_MECHANISM_INFO getMechanismInfo(long slotId, long type) {
    	return CE.GetMechanismInfo(slotId, type);
    }
    
    public static byte[] generateRandom(long sessionId, int randomLen) {
    	return CE.GenerateRandom(sessionId, randomLen);
    }
    
    public static void signInit(long sessionId, CKM mechanism, long key) {
    	CE.SignInit(sessionId, mechanism, key);
    }
    
    public static byte[] sign(long sessionId, byte[] data) {
    	return CE.Sign(sessionId, data);
    }
    
    public static void signUpdate(long sessionId, byte[] part) {
    	CE.SignUpdate(sessionId, part);
    }
    
    public static byte[] signFinal(long sessionId) {
    	return CE.SignFinal(sessionId);
    }
    
    public static void signRecoverInit(long sessionId, CKM mechanism, long key) {
    	CE.SignRecoverInit(sessionId,  mechanism, key);
    }
    
    public static byte[] signRecover(long sessionId, byte[] data) {
    	return CE.SignRecover(sessionId, data);
    }
    
    public static void verifyInit(long sessionId, CKM mechanism, long key) {
    	CE.VerifyInit(sessionId, mechanism, key);
    }
    
    public static void verify(long sessionId, byte[] data, byte[] signature) {
    	CE.Verify(sessionId, data, signature);
    }
    
    public static void verifyUpdate(long sessionId, byte[] part) {
    	CE.VerifyUpdate(sessionId, part);
    }
    
    public static void verifyFinal(long sessionId, byte[] signature) {
    	CE.VerifyFinal(sessionId, signature);
    }
    
    public static void verifyRecoverInit(long sessionId, CKM mechanism, long key) {
    	CE.VerifyRecoverInit(sessionId, mechanism, key);
    }
    
    public static byte[] verifyRecover(long sessionId, byte[] signature) {
    	return CE.VerifyRecover(sessionId, signature);
    }
    
    public static byte[] verifyRecover(long sessionId, CKM mechanism, long key, byte[] signature) {
    	return CE.VerifyRecover(sessionId, mechanism, key, signature);
    }
    
    public static void waitForSlotEvent(long flags, LongRef slot) {
    	CE.WaitForSlotEvent(flags, slot, null);
    }
    
    public static void closeAllSessions(long slotId) {
        CE.CloseAllSessions(slotId);
    }
    
    public static void PKCS11Finalize() {
        CE.Finalize();
    }
}
