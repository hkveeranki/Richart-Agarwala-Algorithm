/**
 * Created by harry7 on 14/9/16.
 */

import java.rmi.*;

public interface Nodedef extends Remote {
    void LockRequest(byte[] info);

    void UnlockRequest(byte[] info);

    void SendAckRequest(byte[] info);
}