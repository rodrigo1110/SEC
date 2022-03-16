package pt.tecnico.grpc.user;


import pt.tecnico.grpc.UserServer;
import pt.tecnico.grpc.UserServerServiceGrpc;

import java.util.Scanner;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;


public class User {

	public static void main(String[] args) throws Exception {
		System.out.println(User.class.getSimpleName());
		Scanner myObj = new Scanner(System.in);

		// receive and print arguments
		System.out.printf("Received %d arguments%n", args.length);
		for (int i = 0; i < args.length; i++) {
			System.out.printf("arg[%d] = %s%n", i, args[i]);
		}

		if (args.length != 1) {
			System.err.println("Invalid Number of Arguments");
			myObj.close();
			return;
		} 

		final int port = Integer.parseInt(args[0]);
		final String target = "localhost:" + port;

		String[] command;
		String str;

		try{
			UserImpl user = new UserImpl(target);
		
			System.out.println("==========================");
			System.out.print("= BFT Banking =\n");
			System.out.println("==========================");

			System.out.println("Type 'help' to see avaliable operations.");

			while(myObj.hasNext()){
				System.out.print("> ");
				str = myObj.nextLine();
				command = str.split("\\s+");

				try{
					switch (command[0]) {
						case "hello":
							user.hello();
							break;
						case "signup":
							user.signup();
							break;
						case "help":
							System.out.printf("Avaliable operations:\n");
							System.out.printf(" - signup\n");
							System.out.printf(" - hello\n");
							System.out.printf(" - exit\n");
							break;
						case "exit":
							System.exit(0);
						default: 
							System.out.printf("That operation is unavailable.%n");
							break;
					}
				}catch(StatusRuntimeException e){
					if((e.getStatus().getCode().equals(Status.UNAVAILABLE.getCode()))){//server down
						System.out.println("Server unavailable.");
						System.exit(0);
					}
				}	
			}
			myObj.close();
			user.getChannel().shutdownNow();

		}catch(Exception e){
			System.out.println("Server's public key not found");
			System.exit(0);
		}
	}
}
