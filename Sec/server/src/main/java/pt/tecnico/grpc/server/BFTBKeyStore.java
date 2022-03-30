package pt.tecnico.grpc.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.NoSuchElementException;
import java.util.Random;

public class BFTBKeyStore {
    public static String filename = "keystore.jks";
    //private static final String directory = "../../../../";
    private static final String directory = "../";
    public static final String KEYSTORE_PASSWORD = "el*AvJ#U";

    public static KeyStore createOrLoadKeyStore(String password) throws
            CertificateException, IOException, NoSuchAlgorithmException, KeyStoreException {

        File file = new File(directory + filename);
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());

    if (file.exists()) {
            // if exists, load
            keyStore.load(new FileInputStream(file), password.toCharArray());
        } else {
            // if not exists, create
            keyStore.load(null, null);
            keyStore.store(new FileOutputStream(file), password.toCharArray());
        }
        return keyStore;
    }
}
