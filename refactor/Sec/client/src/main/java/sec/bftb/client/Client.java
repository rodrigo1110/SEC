package sec.bftb.client;

import java.io.ByteArrayOutputStream;
import java.security.Key;
import java.security.KeyPair;
import java.util.concurrent.TimeUnit;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.ArrayList;
import java.util.List;

import com.google.protobuf.ByteString;

import sec.bftb.crypto.*;
import sec.bftb.client.Logger;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import sec.bftb.grpc.BFTBankingGrpc;
import sec.bftb.grpc.Contract.*;

public class Client {
    private BFTBankingGrpc.BFTBankingBlockingStub stub;
    private final ManagedChannel channel;
    private Key privateKey, serverPublicKey;
    private final Logger logger;
    private Map<Integer, List<Integer>> nonces = new TreeMap<>();

   
    public Client(String target){
        channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
        stub = BFTBankingGrpc.newBlockingStub(channel);
        logger = new Logger("Client", "App");
    }

    public ManagedChannel getChannel(){
        return channel;
    }

    /*public PingResponse ping(PingRequest request) {
        return stub.ping(request);
    }*/


    public int generateNonce(int userID){
        int sequenceNumber;
        do{
            sequenceNumber = new Random().nextInt(10000);
        }while(nonces.get(userID) != null && nonces.get(userID).contains(sequenceNumber));
        return sequenceNumber;
    }




    //-----------------------------------Open account----------------------------

    public void open() throws Exception{
        
        ByteArrayOutputStream messageBytes;
        String hashMessage;
        ByteString encryptedHashMessage;
        byte[] publicKeyBytes;
        KeyPair pair;

        int sequenceNumber = new Random().nextInt(10000);
        
        try{
            pair = CryptographicFunctions.createKeyPair();
            publicKeyBytes = pair.getPublic().getEncoded();
            privateKey = pair.getPrivate();

            messageBytes = new ByteArrayOutputStream();
            messageBytes.write(publicKeyBytes);
            messageBytes.write(":".getBytes());
            messageBytes.write(String.valueOf(sequenceNumber).getBytes());
            
            hashMessage = CryptographicFunctions.hashString(new String(messageBytes.toByteArray()));
            encryptedHashMessage = ByteString.copyFrom(CryptographicFunctions
            .encrypt(privateKey, hashMessage.getBytes()));
        }
        catch (Exception e){
            logger.log("Exception with message: " + e.getMessage() + " and cause:" + e.getCause());
            return;
        }

		openAccountRequest request = openAccountRequest.newBuilder()
        .setPublicKeyClient(ByteString.copyFrom(publicKeyBytes))
        .setSequenceNumber(sequenceNumber).setHashMessage(encryptedHashMessage).build();     

		openAccountResponse response = stub.withDeadlineAfter(7000, TimeUnit.MILLISECONDS).openAccount(request);
        if(response.getSequenceNumber() != sequenceNumber + 1){
            logger.log("Invalid sequence number. Possible replay attack detected.");
            return;
        }
        
        try{
            messageBytes = new ByteArrayOutputStream();
            messageBytes.write(String.valueOf(response.getBalance()).getBytes());
            messageBytes.write(":".getBytes());
            messageBytes.write(String.valueOf(response.getSequenceNumber()).getBytes());
            
            serverPublicKey = CryptographicFunctions.getServerPublicKey("../crypto/");
            String hashMessageString = CryptographicFunctions.decrypt(serverPublicKey.getEncoded(), response.getHashMessage().toByteArray()); 
            if(!CryptographicFunctions.verifyMessageHash(messageBytes.toByteArray(), hashMessageString)){
                logger.log("Message reply integrity compromissed.");
                return;
            }
        
            int localUserID = CryptographicFunctions.saveKeyPair(pair); 
            
            List<Integer> nonce = new ArrayList<>(sequenceNumber);
            nonces.put(localUserID, nonce);
            System.out.println("Your local user id: " + localUserID);
        }
        catch(Exception e){
            logger.log("Exception with message: " + e.getMessage() + " and cause:" + e.getCause());
        }
		System.out.println(response);
    }


    //--------------------------------------Send amount--------------------------------------



    public void send(int sourceID, int destID, float amount) throws Exception{
        
        ByteArrayOutputStream messageBytes;
        String hashMessage;
        int sequenceNumber;
        ByteString encryptedHashMessage;
        byte[] sourcePublicKeyBytes, destPublicKeyBytes;
        Key privateKey;


        sequenceNumber = generateNonce(sourceID);
        try{
            privateKey = CryptographicFunctions.getClientPrivateKey(sourceID);
            sourcePublicKeyBytes = CryptographicFunctions.getClientPublicKey(sourceID).getEncoded();
            destPublicKeyBytes = CryptographicFunctions.getClientPublicKey(destID).getEncoded();

            messageBytes = new ByteArrayOutputStream();
            messageBytes.write(sourcePublicKeyBytes);
            messageBytes.write(":".getBytes());
            messageBytes.write(destPublicKeyBytes);
            messageBytes.write(":".getBytes());
            messageBytes.write(String.valueOf(amount).getBytes());
            messageBytes.write(":".getBytes());
            messageBytes.write(String.valueOf(sequenceNumber).getBytes());
            
            hashMessage = CryptographicFunctions.hashString(new String(messageBytes.toByteArray()));
            encryptedHashMessage = ByteString.copyFrom(CryptographicFunctions
            .encrypt(privateKey, hashMessage.getBytes()));
        }
        catch (Exception e){
            logger.log("Exception with message: " + e.getMessage() + " and cause:" + e.getCause());
            return;
        }

		
        sendAmountRequest request = sendAmountRequest.newBuilder().setPublicKeySender(ByteString.copyFrom(sourcePublicKeyBytes))
        .setPublicKeyReceiver(ByteString.copyFrom(destPublicKeyBytes)).setAmount(amount)
        .setSequenceNumber(sequenceNumber).setHashMessage(encryptedHashMessage).build();   

		sendAmountResponse response = stub.withDeadlineAfter(7000, TimeUnit.MILLISECONDS).sendAmount(request);
        if(response.getSequenceNumber() != sequenceNumber + 1){
            logger.log("Invalid sequence number. Possible replay attack detected.");
            return;
        }

        
        try{
            messageBytes = new ByteArrayOutputStream();
            messageBytes.write(String.valueOf(response.getTransferId()).getBytes());
            messageBytes.write(":".getBytes());
            messageBytes.write(String.valueOf(response.getSequenceNumber()).getBytes());
            
            serverPublicKey = CryptographicFunctions.getServerPublicKey("../crypto/");
            String hashMessageString = CryptographicFunctions.decrypt(serverPublicKey.getEncoded(), response.getHashMessage().toByteArray()); 
            if(!CryptographicFunctions.verifyMessageHash(messageBytes.toByteArray(), hashMessageString)){
                logger.log("Message reply integrity compromissed.");
                return;
            }
        
            List<Integer> nonce = new ArrayList<>(sequenceNumber);
            nonces.put(sourceID, nonce);
        }
        catch(Exception e){
            logger.log("Exception with message: " + e.getMessage() + " and cause:" + e.getCause());
        }
		System.out.println(response);
    }


    //---------------------------------Check account--------------------------------

    public void check(int userID){
        
        ByteArrayOutputStream messageBytes;
        String hashMessage;
        int sequenceNumber;
        ByteString encryptedHashMessage;
        byte[] publicKeyBytes;
        Key privateKey;


        sequenceNumber = generateNonce(userID);
        try{
            privateKey = CryptographicFunctions.getClientPrivateKey(userID);
            publicKeyBytes = CryptographicFunctions.getClientPublicKey(userID).getEncoded();
        
            messageBytes = new ByteArrayOutputStream();
            messageBytes.write(publicKeyBytes);
            messageBytes.write(":".getBytes());
            messageBytes.write(String.valueOf(sequenceNumber).getBytes());
            
            hashMessage = CryptographicFunctions.hashString(new String(messageBytes.toByteArray()));
            encryptedHashMessage = ByteString.copyFrom(CryptographicFunctions
            .encrypt(privateKey, hashMessage.getBytes()));
        }
        catch (Exception e){
            logger.log("Exception with message: " + e.getMessage() + " and cause:" + e.getCause());
            return;
        }

		
        checkAccountRequest request = checkAccountRequest.newBuilder().setPublicKeyClient(ByteString.copyFrom(publicKeyBytes))
        .setSequenceNumber(sequenceNumber).setHashMessage(encryptedHashMessage).build();   

		checkAccountResponse response = stub.withDeadlineAfter(7000, TimeUnit.MILLISECONDS).checkAccount(request);
        if(response.getSequenceNumber() != sequenceNumber + 1){
            logger.log("Invalid sequence number. Possible replay attack detected.");
            return;
        }

        
        try{
            messageBytes = new ByteArrayOutputStream();
            messageBytes.write(response.getPendingMovementsList().toString().getBytes());
            messageBytes.write(":".getBytes());
            messageBytes.write(String.valueOf(response.getBalance()).getBytes());
            messageBytes.write(":".getBytes());
            messageBytes.write(String.valueOf(response.getSequenceNumber()).getBytes());
            
            serverPublicKey = CryptographicFunctions.getServerPublicKey("../crypto/");
            String hashMessageString = CryptographicFunctions.decrypt(serverPublicKey.getEncoded(), response.getHashMessage().toByteArray()); 
            if(!CryptographicFunctions.verifyMessageHash(messageBytes.toByteArray(), hashMessageString)){
                logger.log("Message reply integrity compromissed.");
                return;
            }
        
            List<Integer> nonce = new ArrayList<>(sequenceNumber);
            nonces.put(userID, nonce);

            System.out.println("Pending movements: ");
            for(int transferID : response.getPendingMovementsList()){
                System.out.println(transferID + ", ");
            }
            System.out.println("\nCurrent balance: " + response.getBalance());
        }
        catch(Exception e){
            logger.log("Exception with message: " + e.getMessage() + " and cause:" + e.getCause());
        }
		System.out.println(response);
    }



    public void receive(int userID, int transferID){
        ByteArrayOutputStream messageBytes;
        String hashMessage;
        int sequenceNumber;
        ByteString encryptedHashMessage;
        byte[] publicKeyBytes;
        Key privateKey;


        sequenceNumber = generateNonce(userID);
        try{
            privateKey = CryptographicFunctions.getClientPrivateKey(userID);
            publicKeyBytes = CryptographicFunctions.getClientPublicKey(userID).getEncoded();
        
            messageBytes = new ByteArrayOutputStream();
            messageBytes.write(publicKeyBytes);
            messageBytes.write(":".getBytes());
            messageBytes.write(String.valueOf(transferID).getBytes());
            messageBytes.write(":".getBytes());
            messageBytes.write(String.valueOf(sequenceNumber).getBytes());
            
            hashMessage = CryptographicFunctions.hashString(new String(messageBytes.toByteArray()));
            encryptedHashMessage = ByteString.copyFrom(CryptographicFunctions
            .encrypt(privateKey, hashMessage.getBytes()));
        }
        catch (Exception e){
            logger.log("Exception with message: " + e.getMessage() + " and cause:" + e.getCause());
            return;
        }

		
        receiveAmountRequest request = receiveAmountRequest.newBuilder().setPublicKeyClient(ByteString.copyFrom(publicKeyBytes))
        .setMovementId(transferID).setSequenceNumber(sequenceNumber).setHashMessage(encryptedHashMessage).build();   

		receiveAmountResponse response = stub.withDeadlineAfter(7000, TimeUnit.MILLISECONDS).receiveAmount(request);
        if(response.getSequenceNumber() != sequenceNumber + 1){
            logger.log("Invalid sequence number. Possible replay attack detected.");
            return;
        }

        
        try{
            messageBytes = new ByteArrayOutputStream();
            messageBytes.write(String.valueOf(response.getSequenceNumber()).getBytes());
            
            serverPublicKey = CryptographicFunctions.getServerPublicKey("../crypto/");
            String hashMessageString = CryptographicFunctions.decrypt(serverPublicKey.getEncoded(), response.getHashMessage().toByteArray()); 
            if(!CryptographicFunctions.verifyMessageHash(messageBytes.toByteArray(), hashMessageString)){
                logger.log("Message reply integrity compromissed.");
                return;
            }
        
            List<Integer> nonce = new ArrayList<>(sequenceNumber);
            nonces.put(userID, nonce);

            System.out.println("Transfer accepted, amount received.");
        }
        catch(Exception e){
            logger.log("Exception with message: " + e.getMessage() + " and cause:" + e.getCause());
        }
		System.out.println(response);
    }






    public void channelEnd() {
        channel.shutdownNow();
    }
}
