package pt.tecnico.grpc.server.exceptions;

public class NotEnoughBalanceException extends Exception{

    public NotEnoughBalanceException(){
        super("Balance isn't enough for the requested operation.");
    }
}