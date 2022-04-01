package sec.bftb.client;

import java.io.ByteArrayOutputStream;

import com.google.protobuf.ByteString;

import sec.bftb.crypto.*;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import sec.bftb.grpc.BFTBankingGrpc;
import sec.bftb.grpc.Contract.*;

public class Client {
    private BFTBankingGrpc.BFTBankingBlockingStub stub;
    private final ManagedChannel channel;
    private int sequenceNumber;

   
    public Client(String target){
        channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
        stub = BFTBankingGrpc.newBlockingStub(channel);
        sequenceNumber = 0;
    }

    /*public PingResponse ping(PingRequest request) {
        return stub.ping(request);
    }*/

    public void open() throws Exception{
        
        sequenceNumber++;
        ByteArrayOutputStream messageBytes;
        ByteString encryptedHashMessage;
        try{
            messageBytes = new ByteArrayOutputStream();
            messageBytes.write(clientPublicKeyBytes);
            messageBytes.write(":".getBytes());
            messageBytes.write(String.valueOf(sequenceNumber).getBytes());
            encryptedHashMessage = createHashMessage(messageBytes);
        }
        catch (Exception e){
            System.out.println(e);
            return;
        }

		openAccountRequest request = openAccountRequest.newBuilder()
        .setPublicKeyClient(ByteString.copyFrom(publicKey.getEncoded()))
        .setSequenceNumber(sequenceNumber).setHashMessage(encryptedHashMessage)
        .build();

		openAccountResponse response = stub.openAccount(request);
        
        try{
            messageBytes = new ByteArrayOutputStream();
            messageBytes.write(clientPublicKeyBytes);
            messageBytes.write(":".getBytes());
            messageBytes.write(String.valueOf(sequenceNumber+1).getBytes());
            
            String hashMessageString = decrypt(privateKey, response.getHashMessage().toByteArray()); //incompatible types: byte[] cannot be converted to java.security.Key
            if(!verifyMessageHash(messageBytes.toByteArray(), hashMessageString))
                System.out.println("message integrity exception should be thrown because of your dumbself");//throw new MessageIntegrityException();
        
        }catch(Exception ex){
            System.err.println("Exception with message: " + ex.getMessage() + " and cause:" + ex.getCause());
        }
        
        //TODO validate response
		System.out.println(response);
        
    }

    public void channelEnd() {
        channel.shutdownNow();
    }
}
