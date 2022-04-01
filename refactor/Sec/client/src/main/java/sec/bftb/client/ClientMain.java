package sec.bftb.client;

import sec.bftb.grpc.Contract.*;

public class ClientMain {
	public static void main(String[] args) throws Exception {
		System.out.println(ClientMain.class.getSimpleName());
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
			Client user = new Client(target);

			System.out.println("Type 'help' to see avaliable operations.");

			while(myObj.hasNext()){
				System.out.print("> ");
				str = myObj.nextLine();
				command = str.split("\\s+");

				try{
					switch (command[0]) {
						case "signup":
							user.signup();
							break;
						case "open":
							user.open();
							break;
						case "send":
							user.send(ByteString.copyFromUtf8(command[1]), Float.parseFloat(command[2]));
							break;
						case "check":
							user.check();
							break;
						case "receive":
							user.receive(Integer.parseInt(command[1]));
							break;
						case "audit":
							user.audit();
							break;
						case "mov": //for debug
							user.checkMovement(Integer.parseInt(command[1]));
							break;
						case "help":
							System.out.printf("Avaliable operations:\n");
							//System.out.printf(" - signup -> create credentials (necessary only once) \n");
							System.out.printf(" - open -> open account \n");
							System.out.printf(" - send (1) (2) -> send amount (2) to who (1) \n");
							System.out.printf(" - check -> check balance and pending movements of account \n");
							System.out.printf(" - receive (1) -> approve movement (1) \n");
							System.out.printf(" - audit -> check balance and all movements of account\n");
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