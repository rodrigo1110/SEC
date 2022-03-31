package pt.tecnico.grpc.server.exceptions;

public class ExistentUserException extends Exception{

    public ExistentUserException(){
        super("User already exists.");
    }
}