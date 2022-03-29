package pt.tecnico.grpc.server.exceptions;

public class UnknownUserException extends Exception{

    public UnknownUserException(){
        super("User unknown.");
    }
}