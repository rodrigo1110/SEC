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
	
	
	/*@Override
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
	}*/


	@Override
	public void openAccount(UserServer.openAccountRequest request, StreamObserver<UserServer.openAccountResponse> responseObserver) {
		System.out.println(request);

		try{
			UserServer.openAccountResponse response = server.open_account(request.getPublicKeyClient(), 
					request.getSequenceNumber(), request.getHashMessage());
					
			responseObserver.onNext(response);
			responseObserver.onCompleted();
		}
		catch (Exception e){
			responseObserver.onError(INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
		}
	}	

	@Override
	public void sendAmount(UserServer.sendAmountRequest request, StreamObserver<UserServer.sendAmountResponse> responseObserver) {
		System.out.println(request);

		try{
			UserServer.sendAmountResponse response = UserServer.sendAmountResponse.newBuilder()
				.setTransferId(server.send_amount(request.getPublicKeySender(), request.getPublicKeySender(), request.getAmount(),
				request.getSequenceNumber(), request.getHashMessage())).build();
			responseObserver.onNext(response);
			responseObserver.onCompleted();
		}
		catch (Exception e){
			responseObserver.onError(INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
		}
	}	

	@Override
	public void checkAccount(UserServer.checkAccountRequest request, StreamObserver<UserServer.checkAccountResponse> responseObserver) {
		System.out.println(request);

		try{
			UserServer.checkAccountResponse response = UserServer.checkAccountResponse.newBuilder()
				//.addAllNumberMovements(server.check_account(request.getPublicKeyClient())).setBalance(server.check_account_balance(request.getPublicKeyClient()))
				.build();
			responseObserver.onNext(response);
			responseObserver.onCompleted();
		}
		catch (Exception e){
			responseObserver.onError(INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
		}
	}

	@Override
	public void checkMovement(UserServer.checkMovementRequest request, StreamObserver<UserServer.checkMovementResponse> responseObserver) {
		System.out.println(request);

		try{
			Movement mov = server.getMovement(request.getNumberMovement());
			UserServer.checkMovementResponse response = UserServer.checkMovementResponse.newBuilder()
				.setId(mov.getId()).setOriginAcc(mov.getOriginAcc()).setDestAcc(mov.getDestAcc()).setAmount(mov.getAmount()).setState(mov.getStateString())
				.build();
			responseObserver.onNext(response);
			responseObserver.onCompleted();
		}
		catch (Exception e){
			responseObserver.onError(INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
		}
	}

	@Override
	public void receiveAmount(UserServer.receiveAmountRequest request, StreamObserver<UserServer.receiveAmountResponse> responseObserver) {
		System.out.println(request);

		try{
			UserServer.receiveAmountResponse response = UserServer.receiveAmountResponse.newBuilder()
				.setDone(server.receive_amount(request.getPublicKeyClient(), request.getMovementId())).build();
			responseObserver.onNext(response);
			responseObserver.onCompleted();	
		}
		catch (Exception e){
			responseObserver.onError(INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
		}
	}

	@Override
	public void audit(UserServer.auditRequest request, StreamObserver<UserServer.auditResponse> responseObserver) {
		System.out.println(request);

		try{
			UserServer.auditResponse response = UserServer.auditResponse.newBuilder()
				.addAllNumberMovements(server.audit(request.getPublicKeyClient())).setBalance(server.check_account_balance(request.getPublicKeyClient()))
				.build();
			responseObserver.onNext(response);
			responseObserver.onCompleted();
		}
		catch (Exception e){
			responseObserver.onError(INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
		}
	}

}
