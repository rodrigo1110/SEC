package pt.tecnico.grpc.server;

public class Account{
    private Integer id;
    private Key publicKey;
    private float balance;

    public Account(Key _publicKey, float _balance){
        publicKey = _publicKey;
        balance = _balance;
    }
    
    public float getBalance(){
        return balance;
    }

    public bool has_balance(int amount){
        return balance>=amount;
    }

    public void transfer (int amount){ //loose money
        balance-=amount;
    }
}






