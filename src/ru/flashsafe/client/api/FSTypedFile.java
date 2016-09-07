package ru.flashsafe.client.api;

import retrofit.mime.TypedFile;
import ru.flashsafe.client.util.TokenUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import org.pkcs11.jacknji11.CKA;
import org.pkcs11.jacknji11.CKO;
import org.pkcs11.jacknji11.CK_TOKEN_INFO;

/**
 * Created by igorstemper on 26.08.16.
 */
public class FSTypedFile extends TypedFile {
    private static final int BUFFER_SIZE = 4096;
    private long len;
    private final UploadProgressListener listener;

    public FSTypedFile(String mimeType, File file, UploadProgressListener listener) {
        super(mimeType, file);
        if (file != null) len = file.length();
        this.listener = listener;
    }

    @Override
    public void writeTo(OutputStream out) throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE];
        FileInputStream in = new FileInputStream(super.file());
        long total = 0;
        try {
            int read;
            while ((read = in.read(buffer)) != -1) {
                total += read;
                if (this.listener != null) {
                    this.listener.transferred(total * 100 / len);
                }
                out.write(/*isEncryptEnabled() ? TokenUtil.encrypt(buffer) : */buffer, 0, read);
            }
        } finally {
            in.close();
        }
    }
    
    private boolean isEncryptEnabled() {
        /*TokenUtil.findObjectsInit(TokenUtil.session, new CKA(CKA.LABEL, "Encrypt"));
        long[] encrypt = TokenUtil.findObjects(TokenUtil.session, 1);
        TokenUtil.findObjectsFinal(TokenUtil.session);*/
        return /*encrypt.length > 0*/Settings.needEncrypt() == 1;
    }
    
}
