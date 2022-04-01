package sec.bftb.crypto;

import javax.crypto.Cipher;
import java.security.*;
import java.security.spec.*;
import java.io.*;
import java.math.BigInteger;
import java.nio.file.*;


public class CryptographicFunctions{

    //------------------------------Create/Obtain Keys-------------------------


    public void createKeyPair() throws NoSuchAlgorithmException, Exception{
        
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(1024);
        KeyPair pair = keyGen.generateKeyPair();
        Key privateKey = pair.getPrivate();
        Key publicKey = pair.getPublic();
    }
      

    public void saveKeyPair (KeyPair keypair, int userID){
      File file = null;
        file = new File("keys/publicKeys/" + userID + "-PublicKey");
        if (file.createNewFile()) {
            System.out.println("New file created: " + file.getName());
            OutputStream os = new FileOutputStream(file);
            os.write(publicKey.getEncoded());
            os.close();
        } 
        else{
            System.out.println("User already exists.");
            return;
        }

        file = new File("keys/privateKeys/" + userID + "-PrivateKey");
        if (file.createNewFile()) {
            System.out.println("New file created: " + file.getName());
            OutputStream os = new FileOutputStream(file);
            os.write(privateKey.getEncoded());
            os.close();
        } 
        else{
            System.out.println("User already exists.");
            return;
        }
    }

    
    
    
    public static Key getPublicKey(String filename) throws Exception {
    
        byte[] keyBytes = Files.readAllBytes(Paths.get(filename));
    
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePublic(spec);
    }
    
    public static Key getClientPrivateKey(int userID) throws Exception {
    
        byte[] keyBytes = Files.readAllBytes(Paths.get("keys/privateKeys/" + userID + "-PrivateKey"));
    
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(spec);
    }

    public static Key getServerPrivateKey() throws Exception {
    
        byte[] keyBytes = Files.readAllBytes(Paths.get("keys/serverPrivateKey"));
    
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(spec);
    }


    //---------------------------Hash Functions----------------------------------

    
    public static String hashString(String secretString) throws NoSuchAlgorithmException, NoSuchProviderException{

        String hashtext = null;
        MessageDigest md = MessageDigest.getInstance("SHA-256");

        byte[] messageDigest = md.digest(secretString.getBytes());
        hashtext = convertToHex(messageDigest);
        System.out.println("hash text:" + hashtext);
        return hashtext;
    }


    public static String convertToHex(byte[] messageDigest) {
        BigInteger value = new BigInteger(1, messageDigest);
        String hexText = value.toString(16);

        while (hexText.length() < 32) 
            hexText = "0".concat(hexText);
        return hexText;
    }
    
    public static boolean verifyMessageHash(byte[] Message,String hashMessage) throws Exception{
        String message = new String(Message);
        if((hashString(message).compareTo(hashMessage)) == 0)
            return true;
        return false;   
    }


    //---------------------------Encryption/Decryption Functions----------------------------------


    public static byte[] encrypt(Key key, byte[] text) {
        try {
            Cipher rsa;
            rsa = Cipher.getInstance("RSA");
            rsa.init(Cipher.ENCRYPT_MODE, key);
            return rsa.doFinal(text); 

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

   
    public static String decrypt(byte[] keyBytes, byte[] buffer) {
        try {
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA"); 
            Key key = keyFactory.generatePublic(keySpec);

            Cipher rsa;
            rsa = Cipher.getInstance("RSA");
            rsa.init(Cipher.DECRYPT_MODE, key);
            byte[] value = rsa.doFinal(buffer);
            return new String(value);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}