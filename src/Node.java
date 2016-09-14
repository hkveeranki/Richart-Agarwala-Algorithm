/**
 * Created by harry7 on 14/9/16.
 */

import java.util.PriorityQueue;

public class Node implements Nodedef {
    PriorityQueue<Message> queue;
    boolean is_in_critical;

    public Node() {
        super();
        this.queue = new PriorityQueue<>();
        this.is_in_critical = false;
    }

    public void LockRequest(byte[] info) {
    /* Other Objects send lock  request to this node using this Remote Method */
        Message lock_request = new Message(info);
        if (is_in_critical) {
            queue.add(lock_request);
        } else {
            send_ack();
        }
    }


    public void UnlockRequest(byte[] info) {
        /* Other Objects send lock  request to this node using this Remote Method */

    }


    public void SendAckRequest(byte[] info) {
    }


    public static void main(String[] args) {

    }

    public static void send_ack() {
        /* Send Ack to particular Node */
    }

    public static void lock() {
        /* Perform Mechanism for locking */
    }

    public static void unlock() {
        /* Perform Mechanism for unlocking */
    }
}
