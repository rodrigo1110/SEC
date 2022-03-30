package pt.tecnico.grpc.server;

import pt.tecnico.grpc.UserServer;
import pt.tecnico.grpc.server.exceptions.*;
import io.grpc.StatusRuntimeException;

import java.math.BigInteger;
import com.google.common.primitives.Bytes;
import com.google.protobuf.ByteString;
import java.util.List;
import java.util.ArrayList;

import javax.crypto.Cipher;
import java.security.*;
import java.security.spec.*;
import java.io.*;
import java.nio.file.*;


public class ServerImpl {
    
    private final float INITIAL_BALANCE = 50;
    private ByteString privateKey;
    private ByteString publicKey;
    private boolean hasKeys = false;
    private int seqNumber = 0;
    //public final ServerRepo serverRepo;

    public ServerImpl(){
        //this.serverRepo = new ServerRepo();
    }

    //private Account[] accounts; //dictionary or find function
    //private Movement[] movements; //dictionary or find function


    public UserServer.openAccountResponse open_account(ByteString clientPublicKey, int sequenceNumber, ByteString hashMessage) throws Exception{
        
        //if(sequenceNumber != seqNumber + 1)
        //    throw new SequenceNumberException();
        //seqNumber++;
        try{
            ByteArrayOutputStream messageBytes = new ByteArrayOutputStream();
            messageBytes.write(clientPublicKey.toByteArray());
            messageBytes.write(":".getBytes());
            messageBytes.write(String.valueOf(sequenceNumber).getBytes());
            
            String hashMessageString = decrypt(clientPublicKey.toByteArray(), hashMessage.toByteArray());
            if(!verifyMessageHash(messageBytes.toByteArray(), hashMessageString))
                throw new MessageIntegrityException();

        }catch(Exception ex){
            System.err.println("Exception with message: " + ex.getMessage() + " and cause:" + ex.getCause());
        }

        
        //getBalance (if!=-1) -> já existe -> devolve erros
        //see if key already exists in db (if it does throw user already existent exception (must also sign exceptions)) - Larissa
        Account acc = new Account(clientPublicKey, INITIAL_BALANCE);
        //save in database - Larissa

        
        ByteArrayOutputStream replyBytes = new ByteArrayOutputStream();
        replyBytes.write(String.valueOf(INITIAL_BALANCE).getBytes());
        replyBytes.write(":".getBytes());
        replyBytes.write(String.valueOf(sequenceNumber).getBytes());
        
        String hashReply = hashString(new String(replyBytes.toByteArray()));
        ByteString encryptedHashReply = ByteString.copyFrom(encrypt(getPrivateKey(), hashReply.getBytes()));
        
        UserServer.openAccountResponse response = UserServer.openAccountResponse.newBuilder()
					.setBalance(acc.getBalance()).setSequenceNumber(sequenceNumber + 1)
                    .setHashMessage(encryptedHashReply).build();
        return response;
    }


    public int send_amount(ByteString sourcePublicKey, ByteString destinationPublicKey, float amount, int sequenceNumber, ByteString hashMessage) throws Exception{

        //Seq number verification --> sequence number exception
        try{
            ByteArrayOutputStream messageBytes = new ByteArrayOutputStream();
            messageBytes.write(sourcePublicKey.toByteArray());
            messageBytes.write(":".getBytes());
            messageBytes.write(destinationPublicKey.toByteArray());
            messageBytes.write(":".getBytes());
            messageBytes.write(String.valueOf(amount).getBytes());
            messageBytes.write(":".getBytes());
            messageBytes.write(String.valueOf(sequenceNumber).getBytes());
            
            String hashMessageString = decrypt(sourcePublicKey.toByteArray(), hashMessage.toByteArray());
            if(!verifyMessageHash(messageBytes.toByteArray(), hashMessageString))
                throw new MessageIntegrityException();
        
        }catch(Exception ex){
            System.err.println("Exception with message: " + ex.getMessage() + " and cause:" + ex.getCause());
        }
        //see if source and destination exist in DB --> Unknown user exception
        //see if has balance source.has_balance(amount) --> Not enough balance exception
        
        //create movement(source, destination, amount) then add it to db
        
        int movId=0; //testing purpose
        return movId;
    }

    public List<Integer> check_account(ByteString clientPublicKey, int sequenceNumber, ByteString hashMessage) throws Exception{
        
        //Seq number verification --> sequence number exception
        try{
            ByteArrayOutputStream messageBytes = new ByteArrayOutputStream();
            messageBytes.write(clientPublicKey.toByteArray());
            messageBytes.write(String.valueOf(seqNumber).getBytes());
            
            String hashMessageString = decrypt(clientPublicKey.toByteArray(), hashMessage.toByteArray());
            if(!verifyMessageHash(messageBytes.toByteArray(), hashMessageString))
                throw new MessageIntegrityException();

        }catch(Exception ex){
            System.err.println("Exception with message: " + ex.getMessage() + " and cause:" + ex.getCause());
        }
        //get ids of movements related to client to send
        List<Integer> i = new ArrayList<Integer>();    //Just for test
        i.add(20);
        return i;
    }

    public float check_account_balance(ByteString clientPublicKey){
        //get balance of account
        float i = 0; //testing purpose
        return i;
    }

    public Movement getMovement(int movementId){
        //get movement
        Movement mov = new Movement(); //testing purpose
        return mov;
    }

    public boolean receive_amount(ByteString key, int id){
        //see if key exists
        //see if movement id exists
        //change state of movement
        //update balance of key
        return true; //testing purpose
    }
    public List<Integer> audit(ByteString key){
        //get list of movement ids with key on origin or destination account and state = wtv
        List<Integer> i = new ArrayList<Integer>();    //declaring array
        i.add(20);
        return i;
    }


//------------------------------Obtain Keys-------------------------


    public static Key getPublicKey(String filename) throws Exception {
    
        byte[] keyBytes = Files.readAllBytes(Paths.get(filename));
    
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePublic(spec);
    }
    
    public static Key getPrivateKey() throws Exception {
    
        byte[] keyBytes = Files.readAllBytes(Paths.get("src/main/java/pt/tecnico/grpc/server/rsaPrivateKey"));
    
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




    



    
