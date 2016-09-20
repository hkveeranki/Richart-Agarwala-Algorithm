/**
 * Created by harry7 on 14/9/16.
 */

import java.rmi.*;

public interface Nodedef extends Remote {
    void LockRequest(int sender_id, VectorClock sender_clock);


    void SendAckRequest(int sender_id);
}