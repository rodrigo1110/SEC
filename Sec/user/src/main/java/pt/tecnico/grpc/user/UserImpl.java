package pt.tecnico.grpc.user;

import pt.tecnico.grpc.UserServer;
import pt.tecnico.grpc.UserServerServiceGrpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;

import java.math.BigInteger;
import com.google.protobuf.ByteString;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import java.security.*;
import java.security.spec.*;
import java.io.*;
import java.nio.file.*;



public class UserImpl {
    
    private final ManagedChannel channel;
    private UserServerServiceGrpc.UserServerServiceBlockingStub stub;
    private static Key privateKey;
    private static Key publicKey;
    private static Key serverPublicKey;
    private static String username;
    

    public UserImpl(String target) throws Exception{
        channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
        stub = UserServerServiceGrpc.newBlockingStub(channel);
        new File("publicKey").mkdirs();
        new File("privateKey").mkdirs();
        serverPublicKey = getPublicKey("../server/rsaPublicKey");
	}

    public ManagedChannel getChannel(){
        return channel;
    }


    //----------------------------Encryption/Decryption--------------------------


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


    public void createKeys(String username) throws NoSuchAlgorithmException, Exception{
        //Generate key pair
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(1024);
        KeyPair pair = keyGen.generateKeyPair();
        Key privateKey1 = pair.getPrivate();
        Key publicKey1 = pair.getPublic();

        File file = null;
        file = new File("publicKey/" + username + "-PublicKey");
        if (file.createNewFile()) {
            System.out.println("New file created: " + file.getName());
            OutputStream os = new FileOutputStream(file);
            os.write(publicKey1.getEncoded());
            os.close();
        } 
        else{
            System.out.println("User already exists.");
            return;
        }

        file = new File("privateKey/" + username + "-PrivateKey");
        if (file.createNewFile()) {
            System.out.println("New file created: " + file.getName());
            OutputStream os = new FileOutputStream(file);
            os.write(privateKey1.getEncoded());
            os.close();
        } 
        else{
            System.out.println("User already exists.");
            return;
        }
    }


    public byte[] encrypt(Key key, byte[] text) {
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


    public String decrypt(Key key, byte[] buffer) {
        try {
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


    //------------------------------Hash Functions-----------------------------------


    public String hashMessage(String secretString) throws NoSuchAlgorithmException, NoSuchProviderException{
        String hashtext = null;
        MessageDigest md = MessageDigest.getInstance("SHA-256");

        byte[] messageDigest = md.digest(secretString.getBytes());
        hashtext = convertToHex(messageDigest);
        System.out.println("hash text:" + hashtext);
        return hashtext;
    }


    private boolean verifyMessageHash(byte[] Message,String hashMessage) throws Exception{
        String message = new String(Message);
        if((hashMessage(message).compareTo(hashMessage)) == 0)
            return true;
        return false;   
    }


    private String convertToHex(byte[] messageDigest) {
        BigInteger value = new BigInteger(1, messageDigest);
        String hexText = value.toString(16);

        while (hexText.length() < 32) 
            hexText = "0".concat(hexText);
        return hexText;
    }


    //-----------------------------Implementation----------------------------------


    public void hello() throws Exception{
    
		UserServer.HelloRequest request = UserServer.HelloRequest.newBuilder().setName("friend").build();

		UserServer.HelloResponse response = stub.greeting(request);
		System.out.println(response);
    }


    public void signup() throws Exception{
        System.out.println("Enter a username.");
        String userName = System.console().readLine();
        try{
            createKeys(userName); 
        }catch(NoSuchAlgorithmException e) {
            System.out.println("No algorithm");
        }catch(Exception e){
            throw new RuntimeException(e);
        }
        try{
            String targetPublic = "publicKey/" + userName + "-PublicKey";
            String targetPrivate = "privateKey/" + userName + "-PrivateKey";
            publicKey = getPublicKey(targetPublic);
            privateKey = getPrivateKey(targetPrivate);
        }catch(NoSuchFileException e){
            System.out.println("User not existent locally. Must sign up locally first.");
            return;
        }

        String path = "publicKey/" + userName + "-PublicKey";
        byte[] clientPublicKeyBytes = Files.readAllBytes(Paths.get(path));
        
        int seqNumber = 1;
        
        ByteArrayOutputStream messageBytes = new ByteArrayOutputStream();
        messageBytes.write(clientPublicKeyBytes);
        messageBytes.write(":".getBytes());
        messageBytes.write(String.valueOf(seqNumber).getBytes());
        //messageBytes.write(encryptedTimeStamp.toByteArray());
        String hashMessage = hashMessage(new String(messageBytes.toByteArray()));
        ByteString encryptedHashMessage = ByteString.copyFrom(encrypt(privateKey, hashMessage.getBytes()));
    }    
}