package pt.tecnico.grpc.user;

import pt.tecnico.grpc.UserServer;
import pt.tecnico.grpc.UserServerServiceGrpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;

import java.math.BigInteger;
import com.google.protobuf.ByteString;
import com.google.protobuf.ByteString.*;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import java.security.*;
import java.security.spec.*;
import java.io.*;
import java.nio.file.*;

import java.util.List;
import java.util.ArrayList;

public class UserImpl {
    
    private final ManagedChannel channel;
    private UserServerServiceGrpc.UserServerServiceBlockingStub stub;
    private static Key privateKey;
    private static Key publicKey;
    private static Key serverPublicKey;
    private static String username;
    private int sequenceNumber;
    private byte[] clientPublicKeyBytes;
    

    public UserImpl(String target) throws Exception{
        channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
        stub = UserServerServiceGrpc.newBlockingStub(channel);
        new File("publicKey").mkdirs();
        new File("privateKey").mkdirs();
        serverPublicKey = getPublicKey("../server/rsaPublicKey");
        sequenceNumber=0;
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

    //-----------------------------Sign Functions------------------------------------

    public static byte[] sign(byte[] content, PrivateKey privateKey) throws
            NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(privateKey);
        signature.update(content);
        return signature.sign();
    }

    public static boolean verifySignature(byte[] content, PublicKey publicKey, byte[] signature)
            throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Signature signAlgorithm = Signature.getInstance("SHA256withRSA");
        signAlgorithm.initVerify(publicKey);
        signAlgorithm.update(content);
        return signAlgorithm.verify(signature);
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
        clientPublicKeyBytes = Files.readAllBytes(Paths.get(path));
        
        sequenceNumber = 1;
        
        ByteArrayOutputStream messageBytes = new ByteArrayOutputStream();
        messageBytes.write(clientPublicKeyBytes);
        messageBytes.write(":".getBytes());
        messageBytes.write(String.valueOf(sequenceNumber).getBytes());
        String hashMessage = hashMessage(new String(messageBytes.toByteArray()));
        ByteString encryptedHashMessage = ByteString.copyFrom(encrypt(privateKey, hashMessage.getBytes()));
    }   

    public void open() throws Exception{
        
        ByteString encryptedHashMessage = createHashMessage();

		UserServer.openAccountRequest request = UserServer.openAccountRequest.newBuilder()
        .setPublicKeyClient(ByteString.copyFrom(publicKey.getEncoded()))
        .setSequenceNumber(sequenceNumber).setHashMessage(encryptedHashMessage)
        .build(); //TODO setters

		UserServer.openAccountResponse response = stub.openAccount(request);
		System.out.println(response);
        
    }

    public void send(ByteString DestAcc, float amount) throws Exception{
    
        ByteString encryptedHashMessage = createHashMessage();

		UserServer.sendAmountRequest request = UserServer.sendAmountRequest.newBuilder()
        .setPublicKeySender(ByteString.copyFrom(publicKey.getEncoded())).setPublicKeySender(DestAcc).setAmount(amount)
        .setSequenceNumber(sequenceNumber).setHashMessage(encryptedHashMessage)
        .build();

		UserServer.sendAmountResponse response = stub.sendAmount(request);
		System.out.println(response);
    }

    public void receive(int movid) throws Exception{
    
        ByteString encryptedHashMessage = createHashMessage();

		UserServer.receiveAmountRequest request = UserServer.receiveAmountRequest.newBuilder()
        .setMovementId(movid).setPublicKeyClient(ByteString.copyFrom(publicKey.getEncoded()))
        .setSequenceNumber(sequenceNumber).setHashMessage(encryptedHashMessage)
        .build();

		UserServer.receiveAmountResponse response = stub.receiveAmount(request);
		System.out.println(response);
    } 

    public void checkMovement(int id){ //not called by user

        ByteString encryptedHashMessage = createHashMessage();

        UserServer.checkMovementRequest request = UserServer.checkMovementRequest.newBuilder()
        .setPublicKeyClient(ByteString.copyFrom(publicKey.getEncoded())).setNumberMovement(id)
        .setSequenceNumber(sequenceNumber).setHashMessage(encryptedHashMessage)
        .build();

        UserServer.checkMovementResponse response = stub.checkMovement(request);
        System.out.println(response);
    }

    public void check() throws Exception{
    
        ByteString encryptedHashMessage = createHashMessage();

		UserServer.checkAccountRequest request = UserServer.checkAccountRequest.newBuilder()
        .setPublicKeyClient(ByteString.copyFrom(publicKey.getEncoded()))
        .setSequenceNumber(sequenceNumber).setHashMessage(encryptedHashMessage)
        .build();

		UserServer.checkAccountResponse response = stub.checkAccount(request);
		System.out.println(response);
        List<Integer> movements = response.getNumberMovementsList();
        
        for (int i = 0; i < movements.size(); i++) {
            checkMovement(movements.get(i));
        }

        //proceeds to ask for the movements
    }

    public void audit() throws Exception{

        ByteString encryptedHashMessage = createHashMessage();

		UserServer.auditRequest request = UserServer.auditRequest.newBuilder()
        .setPublicKeyClient(ByteString.copyFrom(publicKey.getEncoded()))
        .setSequenceNumber(sequenceNumber).setHashMessage(encryptedHashMessage)
        .build();

		UserServer.auditResponse response = stub.audit(request);
		System.out.println(response);

        List<Integer> movements = response.getNumberMovementsList();
        
        for (int i = 0; i < movements.size(); i++) {
            checkMovement(movements.get(i));
        }
    }

    public ByteString createHashMessage(){
        try{
            sequenceNumber++;
            ByteArrayOutputStream messageBytes = new ByteArrayOutputStream();
            messageBytes.write(clientPublicKeyBytes);
            messageBytes.write(":".getBytes());
            messageBytes.write(String.valueOf(sequenceNumber).getBytes());
            String hashMessage = hashMessage(new String(messageBytes.toByteArray()));
            ByteString encryptedHashMessage = ByteString.copyFrom(encrypt(privateKey, hashMessage.getBytes()));
            return encryptedHashMessage;
        }
        catch (Exception e){
            System.out.println(e);
            return null;
        }
    }

}