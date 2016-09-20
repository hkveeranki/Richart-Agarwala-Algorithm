/**
 * Created by harry7 on 14/9/16.
 */

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


import static java.lang.Thread.sleep;

public class Node implements Nodedef {
    private static LinkedHashSet<Integer> queue;
    private static boolean is_in_critical, needed_lock;
    private static Lock Request_Lock, mylock;
    private static VectorClock my_clock;
    private static int wait_count, my_id, N;

    public Node() {
        super();
    }

    public void LockRequest(int sender_id, VectorClock sender_clock) throws RemoteException, MalformedURLException {
    /* Other Objects send lock  request to this node using this Remote Method */
        System.out.println("Recieved Lockrequest at" + my_id + " for " + sender_id);
        Request_Lock.lock();
        my_clock.update_clock(sender_clock);
        if (is_in_critical || needed_lock && my_clock.lessthan(sender_clock)) {
            synchronized (queue) {
                System.out.println("Making "+sender_id+" wait at" + my_id);
                queue.add(sender_id);
            }
        } else {
            /* Send Acknowledgement */
            try {
                System.out.println("Sending ack to "+sender_id+" from " + my_id);
                Nodedef remote_object = (Nodedef) Naming.lookup("rmi://localhost:1099/node-" + sender_id);
                remote_object.SendAckRequest(my_id);
            } catch (NotBoundException e) {
                System.out.println("No Such Node found for " + sender_id);
                System.exit(-1);
            }
        }
        Request_Lock.unlock();
    }

    public void SendAckRequest(int sender_id) throws RemoteException {
        synchronized (queue) {
            if (queue.contains(sender_id)) {
                System.out.println("Recieved unlock at " + my_id + " from " + sender_id + " Wait count is " + wait_count);
                queue.remove(sender_id);
            }
        }
        mylock.lock();
        if (needed_lock) {
            wait_count--;
            System.out.println("Recieved ack at " + my_id + " from " + sender_id + " Wait count is " + wait_count);
        }
        mylock.unlock();
        
    }


    public static void main(String[] args) throws InterruptedException, RemoteException, MalformedURLException {
        /* Initialisations and stuff */

        System.out.println(Arrays.toString(args));
        N = Integer.valueOf(args[3]);
        my_id = Integer.valueOf(args[1]);
        my_clock = new VectorClock(N);
        Request_Lock = new ReentrantLock();
        mylock = new ReentrantLock();
        queue = new LinkedHashSet<>();
        Node node = new Node();
        try {
            Nodedef stub = (Nodedef) UnicastRemoteObject.exportObject(node, 0);
            Naming.rebind("rmi://localhost:1099/node-" + my_id, stub);
            sleep(100 * N);
            System.out.println("Initialised " + my_id);
        } catch (MalformedURLException e) {
            System.out.println("Cannot Bind the Stub");
            System.exit(-1);
        }
        /* Getting all those Mappings */

        /* Running the given loop */
        Lock tmp_lock = new ReentrantLock();
        for (int i = 0; i < 100; i++) {
            System.out.println("This is lock for "+i+"th request at "+my_id);
            lock();
            System.out.println("I am " + my_id + " Acquired "+i+" thLock");
            unlock();
            System.out.println("This is unlock for "+i+"th request at "+my_id);
        }
        System.out.println("I "+my_id+" am done");
    }

    private static void lock() throws RemoteException, MalformedURLException {
        /* Perform Mechanism for locking */
        mylock.lock();
        wait_count = N - 1;
        needed_lock = true;
        my_clock.clock[my_id]++;
        mylock.unlock();
        /* Send Lock Requests */
        for (int i = 0; i < N; i++) {
            if (i != my_id) {
                System.out.println("Sent Lockrequest from" + my_id + " to " + i);
                try {
                    Nodedef remote_object = (Nodedef) Naming.lookup("rmi://localhost:1099/node-" + i);
                    remote_object.LockRequest(my_id, my_clock);
                } catch (NotBoundException e) {
                    System.err.println("No Such Node found for " + i);
                    System.exit(-1);
                }
            }
        }
        /* Now Wait */
        try {
            while (true) {
                if (wait_count == 0)
                    break;
                sleep(1);
            }
            mylock.lock();
            needed_lock = false;
            is_in_critical = true;
            mylock.unlock();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void unlock() throws RemoteException, MalformedURLException {
        /* Perform Mechanism for unlocking */
        mylock.lock();
        is_in_critical = false;
        mylock.unlock();
        synchronized (queue) {
            if (queue.contains(my_id)) queue.remove(my_id);
            for (int id : queue) {
                System.out.println("Sending ack from " + my_id + " to " + id);
                System.out.println("Sending Unlock from " + my_id + " to " + id);
                try {
                    Nodedef remote_object = (Nodedef) Naming.lookup("rmi://localhost:1099/node-" + id);
                    remote_object.SendAckRequest(my_id);
                } catch (NotBoundException e) {
                    System.err.println("No Such Node found for " + id);
                    System.exit(-1);
                }

            }
        }
    }
}