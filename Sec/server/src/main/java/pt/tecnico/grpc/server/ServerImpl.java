package pt.tecnico.grpc.server;

import pt.tecnico.grpc.UserServer;
import pt.tecnico.grpc.server.exceptions.*;
import io.grpc.StatusRuntimeException;

import java.math.BigInteger;
import com.google.common.primitives.Bytes;
import com.google.protobuf.ByteString;

import javax.crypto.Cipher;
import java.security.*;
import java.security.spec.*;
import java.io.*;
import java.nio.file.*;


public class ServerImpl {
    
    private final float INITIAL_BALANCE = 50;
    private Key privateKey;
    private Key publicKey;
    private boolean hasKeys = false;
    private int seqNumber = 0;


    //private Account[] accounts; //dictionary or find function
    //private Movement[] movements; //dictionary or find function


    public float open_account(ByteString clientPublicKey, int sequenceNumber, ByteString hashMessage){
        
        //if(sequenceNumber != seqNumber + 1)
        //    throw new SequenceNumberException();
        //seqNumber++;
        
        ByteArrayOutputStream messageBytes = new ByteArrayOutputStream();
        messageBytes.write(publicKey.toByteArray());
        messageBytes.write(":".getBytes());
        messageBytes.write(String.valueOf(seqNumber).getBytes());
        
        String hashMessageString = decrypt(clientPublicKey.toByteArray(), hashMessage.toByteArray());
        if(!verifyMessageHash(messageBytes.toByteArray(), hashMessageString))
            throw new MessageIntegrityException();
        
        
        //see if key already exists in db - Larissa

        Account acc = new Account(clientPublicKey, INITIAL_BALANCE);

        //save in database - Larissa
        return acc.getBalance();
    }


    public void send_amount(Key source, Key destination, int amount, int sequenceNumber, ByteString hashMessage){
        //see if source exists
        //see if has balance source.has_balance(amount)
        //see if destination exists
        //source.transfer(amount)
        //create movement(source, destinaiton, amount)
    }

    public void check_account(Key key){
        //see if key exists
        //get balance of account
        //get list of movement with key on destination account and state = PENDING 
    }

    public void receive_amount(Key key, int id){
        //see if key exists
        //see if movement id exists
        //change state of movement
        //update balance of key
    }
    public void audit(Key key){
        
        //get list of movement with key on origin or destination account and state = wtv
    }

    public static Key getPublicKey(String filename) throws Exception {
    
        byte[] keyBytes = Files.readAllBytes(Paths.get(filename));
    
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePublic(spec);
    }
    
    public static Key getPrivateKey(String filename) throws Exception {
    
        byte[] keyBytes = Files.readAllBytes(Paths.get(filename));
    
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(spec);
    }


    //---------------------------Hash Functions----------------------------------

    
    public String hashString(String secretString) throws NoSuchAlgorithmException, NoSuchProviderException{

        String hashtext = null;
        MessageDigest md = MessageDigest.getInstance("SHA-256");

        byte[] messageDigest = md.digest(secretString.getBytes());
        hashtext = convertToHex(messageDigest);
        System.out.println("hash text:" + hashtext);
        return hashtext;
    }


    public String convertToHex(byte[] messageDigest) {
        BigInteger value = new BigInteger(1, messageDigest);
        String hexText = value.toString(16);

        while (hexText.length() < 32) 
            hexText = "0".concat(hexText);
        return hexText;
    }
    
    public boolean verifyMessageHash(byte[] Message,String hashMessage) throws Exception{
        String message = new String(Message);
        if((hashString(message).compareTo(hashMessage)) == 0)
            return true;
        return false;   
    }


    //---------------------------Encryption/Decryption Functions----------------------------------


    private byte[] encrypt(Key key, byte[] text) {
        try {
            Cipher rsa;
            rsa = Cipher.getInstance("RSA");
            rsa.init(Cipher.ENCRYPT_MODE, key);
            return rsa.doFinal(text); //text.getBytes()

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

   
    private String decrypt(byte[] keyBytes, byte[] buffer) {
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




    



    
