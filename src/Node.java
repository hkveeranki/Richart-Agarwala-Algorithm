/**
 * Created by harry7 on 14/9/16.
 */

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


import static java.lang.Thread.sleep;

public class Node implements Nodedef {
    private static LinkedHashSet<Integer> queue;
    private static boolean is_in_critical, needed_lock;
    private static Nodedef[] remote_objects;
    private static Lock Request_Lock, mylock;
    private static VectorClock my_clock;
    private static int wait_count, my_id, N;

    public Node() {
        super();
    }

    public void LockRequest(int sender_id, VectorClock sender_clock) {
    /* Other Objects send lock  request to this node using this Remote Method */
        Request_Lock.lock();
        my_clock.update_clock(sender_clock);
        if (is_in_critical || needed_lock && sender_clock.clock[sender_id] > my_clock.clock[my_id]) {
            synchronized (queue) {
                queue.add(sender_id);
            }
        } else {
            /* Send Acknowledgement */
            remote_objects[sender_id].SendAckRequest(my_id);
        }
        Request_Lock.unlock();
    }

    public void SendAckRequest(int sender_id) {
        synchronized (queue) {
            if (queue.contains(sender_id)) {
                queue.remove(sender_id);
            }
        }
        mylock.lock();
        if (needed_lock) {
            wait_count--;
        }
        mylock.unlock();
    }


    public static void main(String[] args) {
        /* Initialisations and stuff */

        N = Integer.valueOf(args[1]);
        my_id = Integer.valueOf(args[0]);
        remote_objects = new Nodedef[N];
        my_clock = new VectorClock(N);
        Request_Lock = new ReentrantLock();
        mylock = new ReentrantLock();
        queue = new LinkedHashSet<>();
        Node node = new Node();
        /* Getting all those Mappings */

        /* Running the given loop */
        for (int i = 0; i < 100; i++) {
            lock();
            unlock();
        }
    }

    private static void lock() {
        /* Perform Mechanism for locking */
        wait_count = N - 1;
        /* Send Lock Requests */
        for (int i = 0; i < N; i++) {
            if (i != my_id) remote_objects[i].LockRequest(my_id, my_clock);
        }
        /* Now Wait */
        try {
            while (wait_count != 0) {
                sleep(1);
            }
            mylock.lock();
            needed_lock = false;
            is_in_critical = true;
            mylock.unlock();
            System.out.println("I am " + my_id + "Acquired Lock");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void unlock() {
        /* Perform Mechanism for unlocking */
        mylock.lock();
        is_in_critical = false;
        mylock.unlock();
        synchronized (queue) {
            if (queue.contains(my_id)) queue.remove(my_id);
            for (int id : queue) {
                remote_objects[id].SendAckRequest(my_id);
            }
        }
        System.out.println("I am " + my_id + "Released Lock");
    }
}