package sec.bftb.server;

import io.grpc.stub.StreamObserver;

import java.io.IOException;
import sec.bftb.grpc.Contract.*;
import sec.bftb.grpc.BFTBankingGrpc;

import static io.grpc.Status.INVALID_ARGUMENT;


public class ServerServiceImpl extends BFTBankingGrpc.BFTBankingImplBase {
    
	private final Server server;
    private final int serverPort;
    private final Logger logger;

	public ServerServiceImpl(int serverPort) throws IOException {
        this.server = new Server(serverPort);
        this.serverPort = serverPort;
        this.logger = new Logger("Server", "Service");
    }


	@Override
	public void ping(PingRequest request, StreamObserver<PingResponse> responseObserver) {
			PingResponse response = PingResponse.newBuilder().
					setOutputText(server.ping()).build();
			responseObserver.onNext(response);
			responseObserver.onCompleted();
	}



	@Override
	public void openAccount(openAccountRequest request, StreamObserver<openAccountResponse> responseObserver) {
		try{
		openAccountResponse response = server.open_account(request.getPublicKeyClient(),
		request.getSequenceNumber(), request.getHashMessage());
		responseObserver.onNext(response);
		responseObserver.onCompleted();
		}
		catch (Exception e){
			responseObserver.onError(INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
		}
	}

}