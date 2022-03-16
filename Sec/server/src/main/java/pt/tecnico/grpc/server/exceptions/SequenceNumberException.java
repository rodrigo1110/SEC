package pt.tecnico.grpc.server.exceptions;

public class SequenceNumberException extends Exception{
    
    public SequenceNumberException(){
        super("Operation time expired.");
    }
}
