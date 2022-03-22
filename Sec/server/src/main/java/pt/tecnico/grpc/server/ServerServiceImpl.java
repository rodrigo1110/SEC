package pt.tecnico.grpc.server;

import pt.tecnico.grpc.UserServer;
import pt.tecnico.grpc.UserServerServiceGrpc;


import pt.tecnico.grpc.server.ServerImpl;
import pt.tecnico.grpc.server.ServerMain;
import pt.tecnico.grpc.server.exceptions.*;
import io.grpc.stub.StreamObserver;
import static io.grpc.Status.*;


public class ServerServiceImpl extends UserServerServiceGrpc.UserServerServiceImplBase {

	//--------------------------user-Server communication implementation--------------------------
	
	private ServerMain listeningServer = new ServerMain();
	private ServerImpl server = new ServerImpl();
	
	
	@Override
	public void greeting(UserServer.HelloRequest request, StreamObserver<UserServer.HelloResponse> responseObserver) {
		System.out.println(request);

		try{
			UserServer.HelloResponse response = UserServer.HelloResponse.newBuilder()
					.setGreeting(server.greet(request.getName())).build();
			responseObserver.onNext(response);
			responseObserver.onCompleted();
		}
		catch (Exception e){
			responseObserver.onError(INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
		}
	}


	@Override
	public void openAccount(UserServer.openAccountRequest request, StreamObserver<UserServer.openAccountResponse> responseObserver) {
		System.out.println(request);

		try{
			UserServer.openAccountResponse response = UserServer.openAccountResponse.newBuilder()
					.setBalance(server.openAccount(request.getPublicKeyClient(), 
					request.getSequenceNumber(), request.getHashMessage())).build();
			responseObserver.onNext(response);
			responseObserver.onCompleted();
		}
		catch (Exception e){
			responseObserver.onError(INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
		}
	}		



}
