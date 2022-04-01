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
        .setSequenceNumber(sequenceNumber).setHashMessage(encryptedHashMessage).build();     // TO DO fix sequence number

		openAccountResponse response = stub.withDeadlineAfter(7000, TimeUnit.MILLISECONDS).openAccount(request);

        if(response.getSequenceNumber() != sequenceNumber + 1){
            logger.log("Trudy detected. eliminate");
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
                logger.log("fake server detected. eliminate");
                return;
            }
        
        }catch(Exception e){
            logger.log("Exception with message: " + e.getMessage() + " and cause:" + e.getCause());
            return;
        }

        try{
            int localUserID = CryptographicFunctions.saveKeyPair(pair); 
            
            List<Integer> nonce = new ArrayList<>(sequenceNumber);
            //if(!nonces.get(localUserID).contains(nonce))
            nonces.put(localUserID, nonce);
            System.out.println("Your local user id: " + localUserID);
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