package pt.tecnico.grpc.server;


public enum TransactionState{
    PENDING, APPROVED, DENIED;
}

public class Movement{
    private int id;
    private Key originAcc;
    private Key destAcc;
    private float amount;
    private TransactionState state;

    public Movement(int _id, Key _originAcc, Key _destAcc, float _amount){
        id=_id;
        originAcc=_originAcc;
        destAcc=_destAcc;
        amount=_amount;
        state=PENDING;
    }

    public Movement(int _id, Key _originAcc, Key _destAcc, float _amount, TransactionState _state){
        id=_id;
        originAcc=_originAcc;
        destAcc=_destAcc;
        amount=_amount;
        state=_state;
    }

    public void approve(){
        state=APPROVED;
    }

    public void deny(){
        state=DENIED;
    }
}