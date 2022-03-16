package pt.tecnico.grpc.server;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.StatusRuntimeException;

import java.io.IOException;


public class ServerMain {

	//-------------- Main function only------------------

	private static int port;
	private static Server server;
	private static BindableService impl;
	

	public static void main(String[] args) throws StatusRuntimeException, Exception {
		
		System.out.println(ServerMain.class.getSimpleName());

		System.out.printf("Received %d arguments%n", args.length);
		for (int i = 0; i < args.length; i++) {
			System.out.printf("arg[%d] = %s%n", i, args[i]);
		}

		if (args.length != 1){
			System.err.println("Invalid Number of Arguments");
			System.err.printf("Usage: java %s port %n", ServerMain.class.getName());
			return;
		} 

		port = Integer.valueOf(args[0]);
		
		try{
			createServer();
		} catch (IOException ex){
			System.err.println("IOException with message: " + ex.getMessage() + " and cause:" + ex.getCause());
			System.exit(-1);
		}

		// Server threads are running in the background.
		System.out.println("Server started");
		// Do not exit the main thread. Wait until server is terminated.
		server.awaitTermination();
	}


	public static void createServer() throws IOException{
		impl = new ServerServiceImpl();
		server = ServerBuilder.forPort(port).addService(impl).build();
		server.start();
	}
	

	public Server getServer(){
		return server;
	}
}
