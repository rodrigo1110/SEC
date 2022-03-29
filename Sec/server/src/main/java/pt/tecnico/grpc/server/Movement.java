package pt.tecnico.grpc.server;

import pt.tecnico.grpc.UserServer;
import pt.tecnico.grpc.server.exceptions.*;
import io.grpc.StatusRuntimeException;

import java.math.BigInteger;
import com.google.common.primitives.Bytes;
import com.google.protobuf.ByteString;

import javax.crypto.Cipher;
import java.security.*;
import java.security.spec.*;
import java.io.*;
import java.nio.file.*;

public class Movement{
    private int id;
    private ByteString originAcc;
    private ByteString destAcc;
    private float amount;
    private TransactionState state;

    public Movement(){
    }

    public Movement(ByteString _originAcc, ByteString _destAcc, float _amount){
        originAcc=_originAcc;
        destAcc=_destAcc;
        amount=_amount;
        state=TransactionState.PENDING;
    }

    public Movement(int _id, ByteString _originAcc, ByteString _destAcc, float _amount, TransactionState _state){
        id=_id;
        originAcc=_originAcc;
        destAcc=_destAcc;
        amount=_amount;
        state=_state;
    }

    public int getId(){
        return id;
    }
    public ByteString getOriginAcc(){
        return originAcc;
    }
    public ByteString getDestAcc(){
        return destAcc;
    }
    public float getAmount(){
        return amount;
    }
    public TransactionState getState(){
        return state;
    }
    public String getStateString(){
        if(state==TransactionState.APPROVED){
            return "Approved";
        }
        else if(state==TransactionState.DENIED){
            return "Denied";
        }
        else{ //(state==TransactionState.APPROVED){
            return "Pending";
        }
    }

    public void approve(){
        state=TransactionState.APPROVED;
    }

    public void deny(){
        state=TransactionState.DENIED;
    }
}