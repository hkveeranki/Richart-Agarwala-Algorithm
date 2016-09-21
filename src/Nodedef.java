/**
 * Created by harry7 on 14/9/16.
 */

import com.google.protobuf.InvalidProtocolBufferException;

import java.net.MalformedURLException;
import java.rmi.*;

public interface Nodedef extends Remote {
    void LockRequest(byte[] info) throws RemoteException, MalformedURLException;

    void SendAckRequest(byte[] info) throws RemoteException;
}