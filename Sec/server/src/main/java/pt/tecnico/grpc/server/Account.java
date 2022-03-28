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

    public boolean has_balance(int amount){
        return balance>=amount;
    }

    public void transfer (int amount){ //loose money
        balance-=amount;
    }
}






