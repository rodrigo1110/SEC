package sec.bftb.server;


import sec.bftb.crypto.BFTBKeyStore;
import java.io.IOException;
import java.security.*;

public class Server {

	public final ServerRepo serverRepo;
    private final Logger logger;
    private final int serverPort;
    //protected static final String KEY_PASSWORD = BFTBKeyStore.generatePassword();

	public Server(int serverPort)
            throws IOException {

        //BFTBKeyStore.storeKeyPair(BFTBKeyStore.KEYSTORE_PASSWORD, String.valueOf(serverPort), BFTBKeyStore.generateKeyPair(), KEY_PASSWORD);
        this.serverRepo = new ServerRepo(serverPort);
        this.logger = new Logger("Server", "App");
        this.serverPort = serverPort;
    }

	public synchronized String ping() {
		return "I'm alive!";
	}
	
}