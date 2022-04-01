package sec.bftb.client;

import java.util.Scanner;

import javax.lang.model.util.ElementScanner6;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import sec.bftb.grpc.Contract.*;

import sec.bftb.client.Logger;

public class ClientMain {
	public static void main(String[] args) throws Exception {
		System.out.println(ClientMain.class.getSimpleName());
		Scanner myObj = new Scanner(System.in);

		Logger logger = new Logger("Client", "Main");

		// receive and print arguments
		System.out.printf("Received %d arguments%n", args.length);
		for (int i = 0; i < args.length; i++) {
			System.out.printf("arg[%d] = %s%n", i, args[i]);
		}

		if (args.length != 2) {
			logger.log("Invalid Number of Arguments. Must be two: host port");
			myObj.close();
			return;
		} 

		final String host = args[0];
		final int port = Integer.parseInt(args[1]);
		final String target = host + ":" + port;

		String[] command;
		String str;

		try{
			Client user = new Client(target);

			System.out.println("Type 'help' to see avaliable operations.");

			while(myObj.hasNext()){
				System.out.print("> ");
				str = myObj.nextLine();
				command = str.split("\\s+");

				try{
					switch (command[0]) {
						case "open":
							user.open();
							break;
						case "send":
							if(command.length == 4)
								user.send(Integer.parseInt(command[1]), Integer.parseInt(command[2]),Float.parseFloat(command[3]));
							else
								System.out.printf("Send command must have exaclty 3 arguments: senderUserID receiverUserID AmoutOfTransfer.%n");
							break;
						case "check":
							if(command.length == 2)
								user.check(Integer.parseInt(command[1]));
							else 
								System.out.printf("Check command must have exactly 1 argument: UserID.%n");
							break;
						case "receive":
							if(command.length == 3)
								user.receive(Integer.parseInt(command[1]), Integer.parseInt(command[2]));
							else
								System.out.printf("Receive command must have exactly 2 arguments: UserID TransferId.%n");
							break;
						case "audit":
							if(command.length == 2)
								user.audit(Integer.parseInt(command[1]));
							else
								System.out.printf("Audit command must have exactly 1 argument: UserID.%n");
							break;
						/*case "mov": //for debug
							user.checkMovement(Integer.parseInt(command[1]));
							break;*/
						case "help":
							System.out.printf("Avaliable operations:\n");
							//System.out.printf(" - signup -> create credentials (necessary only once) \n");
							System.out.printf(" - open -> open account \n");
							System.out.printf(" - send (1) (2) (3) -> send amount (3) from (1) to (2) \n");
							System.out.printf(" - check (1)-> check balance and pending movements of account (1) \n");
							System.out.printf(" - receive (1) (2) -> approve movement (2) with account(1)  \n");
							System.out.printf(" - audit (1) -> check balance and all movements of account (1) \n");
							System.out.printf(" - exit\n");
							break;
						case "exit":
							System.exit(0);
						default: 
							System.out.printf("That operation is unavailable.%n");
							break;
					}
				}catch(StatusRuntimeException e){
					if((e.getStatus().getCode().equals(Status.UNAVAILABLE.getCode()))){
						logger.log("Server unavailable at the moment.");
					}
					else if((e.getStatus().getCode().equals(Status.DEADLINE_EXCEEDED.getCode()))){
						logger.log("Time deadline for request excedeed. Request or Response may have been intentionally dropped.");
					}
					else
						logger.log(e.getStatus().getDescription());
				}catch(Exception ex){
					logger.log("Exceeeption with message: " + ex.getMessage() + " and cause:" + ex.getCause());
				} 	
			}
			myObj.close();
			//user.getChannel().shutdownNow();

		}catch(Exception ex){
			logger.log("Exceeption with message: " + ex.getMessage() + " and cause:" + ex.getCause());
		}
	}
}