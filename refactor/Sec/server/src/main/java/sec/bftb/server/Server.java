package sec.bftb.server;

import sec.bftb.crypto.CryptographicFunctions;
import sec.bftb.crypto.BFTBKeyStore;

import sec.bftb.server.Logger;
import sec.bftb.server.exceptions.ErrorMessage;
import sec.bftb.server.exceptions.ServerException;

import sec.bftb.grpc.Contract.*;
import sec.bftb.grpc.BFTBankingGrpc;

import java.io.ByteArrayOutputStream;
import com.google.common.primitives.Bytes;
import com.google.protobuf.ByteString;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;



import java.io.IOException;
import java.security.*;

public class Server {

    public final float INITIAL_BALANCE = 50;
	public final ServerRepo serverRepo;
    private final Logger logger;
    private final int serverPort;
    private Map<String, List<Integer>> nonces = new TreeMap<>();
    //protected static final String KEY_PASSWORD = BFTBKeyStore.generatePassword();

	public Server(int server_port)
            throws IOException {

        //BFTBKeyStore.storeKeyPair(BFTBKeyStore.KEYSTORE_PASSWORD, String.valueOf(serverPort), BFTBKeyStore.generateKeyPair(), KEY_PASSWORD);
        serverRepo = new ServerRepo(server_port);
        logger = new Logger("Server", "App");
        serverPort = server_port;
    }

	public synchronized String ping() {
		return "I'm alive!";
	}



    public openAccountResponse open_account(ByteString clientPublicKey, int sequenceNumber, ByteString hashMessage) throws Exception{
        
        List <Integer> values = nonces.get(new String(clientPublicKey.toByteArray()));
        if(values != null && values.contains(sequenceNumber))
            throw new ServerException(ErrorMessage.SEQUENCE_NUMBER);

        
        try{
            ByteArrayOutputStream messageBytes = new ByteArrayOutputStream();
            messageBytes.write(clientPublicKey.toByteArray());
            messageBytes.write(":".getBytes());
            messageBytes.write(String.valueOf(sequenceNumber).getBytes());
            
            String hashMessageString = CryptographicFunctions.decrypt(clientPublicKey.toByteArray(), hashMessage.toByteArray());

            if(!CryptographicFunctions.verifyMessageHash(messageBytes.toByteArray(), hashMessageString))
                throw new ServerException(ErrorMessage.MESSAGE_INTEGRITY);
        
        
        //getBalance (if!=-1) -> já existe -> devolve erros
        //see if key already exists in db (if it does throw user already existent exception (must also sign exceptions)) - Larissa
        //save in database - Larissa

            ByteArrayOutputStream replyBytes = new ByteArrayOutputStream();
            replyBytes.write(String.valueOf(INITIAL_BALANCE).getBytes());
            replyBytes.write(":".getBytes());
            replyBytes.write(String.valueOf(sequenceNumber + 1).getBytes());
            
            String hashReply = CryptographicFunctions.hashString(new String(replyBytes.toByteArray()));
            System.out.println("here");
            ByteString encryptedHashReply = ByteString.copyFrom(CryptographicFunctions
            .encrypt(CryptographicFunctions.getServerPrivateKey("../crypto/"), hashReply.getBytes()));
            System.out.println("here2");
        
        
            List<Integer> nonce = new ArrayList<>(sequenceNumber);
            nonces.put(new String(clientPublicKey.toByteArray()), nonce);

            openAccountResponse response = openAccountResponse.newBuilder()
                        .setBalance(INITIAL_BALANCE).setSequenceNumber(sequenceNumber + 1)
                        .setHashMessage(encryptedHashReply).build();
            return response;
        }  
        catch(GeneralSecurityException e){
            logger.log("Exception with message: " + e.getMessage() + " and cause:" + e.getCause());
            throw new GeneralSecurityException(e); 
        }
    }
	
}