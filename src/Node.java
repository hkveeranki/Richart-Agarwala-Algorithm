/**
 * Created by harry7 on 14/9/16.
 */

import java.util.PriorityQueue;

public class Node implements Nodedef {
    private PriorityQueue<Request> queue;
    private boolean is_in_critical;
    static Nodedef[] remote_objects;

    public Node() {
        super();
        this.queue = new PriorityQueue<>(new RequestComparator());
        this.is_in_critical = false;
    }

    public void LockRequest(byte[] info) {
    /* Other Objects send lock  request to this node using this Remote Method */
        Request lock_request = new Request(info);
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
        /* Initialisations and stuff */

        int N = Integer.valueOf(args[1]);
        int my_id = Integer.valueOf(args[0]);
        remote_objects = new Nodedef[N];
        /* Getting all those Mappings */

        /* Running the given loop */
        for (int i = 0; i < 100; i++) {
            lock();
            /* Critical Section */
            unlock();
        }
    }

    private static void send_ack() {
        /* Send Ack to particular Node */
    }

    private static void lock() {
        /* Perform Mechanism for locking */
    }

    private static void unlock() {
        /* Perform Mechanism for unlocking */
    }
}
