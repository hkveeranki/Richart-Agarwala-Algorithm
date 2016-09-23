/**
 * Created by harry7 on 14/9/16.
 */

import java.io.*;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.LinkedHashSet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import assignment.MessageProto;
import assignment.MessageProto.Message;
import assignment.MessageProto.generalisedClock;
import com.google.protobuf.InvalidProtocolBufferException;

import static java.lang.Thread.sleep;

public class Node implements Nodedef {
    private static LinkedHashSet<Integer> queue, need_ack;
    private static boolean is_in_critical, needed_lock;
    private static Lock Request_Lock, mylock;
    private static VectorClock my_clock;
    private static VectorClock lock_clock;
    private static int wait_count, my_id, N;


    public Node() {
        super();
    }

    public void LockRequest(byte[] info) throws RemoteException, MalformedURLException {
    /* Other Objects send lock  request to this node using this Remote Method */
        VectorClock sender_clock = new VectorClock(N);
        int sender_id = 0;
        try {
            Message message = Message.parseFrom(info);
            sender_id = message.getId();
            MessageProto.generalisedClock clock = message.getClock();
            for (MessageProto.clock cl : clock.getClocksList()) {
                int id = cl.getId();
                int counter = cl.getClock();
                sender_clock.put_clock(id, counter);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }

        System.out.println("Recieved Lockrequest at " + my_id + " with clock " + my_clock.toString() + " for " + sender_id + " with clock " + sender_clock.toString());
        Request_Lock.lock();
        my_clock.update_clock(sender_clock);
        boolean lock_acquired = is_in_critical || (needed_lock && (wait_count <= 0));
        boolean to_wait = lock_acquired || needed_lock && lock_clock.lessthan(sender_clock, my_id, sender_id);
        Request_Lock.unlock();
        if (to_wait) {
            Request_Lock.lock();
            queue.add(sender_id);
            System.out.println("Making " + sender_id + " wait at " + my_id);
            Request_Lock.unlock();
        } else {
            /* Send Acknowledgement */
            try {
                System.out.println("Sending ack to " + sender_id + " from " + my_id);
                Nodedef remote_object = (Nodedef) Naming.lookup("rmi://localhost:1099/node-" + sender_id);
                remote_object.SendAckRequest(get_protobuf_data(my_id));
            } catch (NotBoundException e) {
                System.out.println("No Such Node found for " + sender_id);
                System.exit(-1);
            } catch (InvalidProtocolBufferException e) {
                e.printStackTrace();
                System.exit(-1);
            }
        }
    }

    public void SendAckRequest(byte[] info) throws RemoteException {
        int sender_id = 0;
        boolean is_unlock = false;
        try {
            Message message = Message.parseFrom(info);
            sender_id = message.getId();
            is_unlock = message.hasClock();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
        if (is_unlock) {
            System.out.println("Recieved unlock at " + my_id + " from " + sender_id + " Wait count is " + wait_count);
        }
        Request_Lock.lock();
        if (needed_lock && need_ack.contains(sender_id)) {
            wait_count--;
            System.out.println("Recieved ack at " + my_id + " from " + sender_id + " Wait count is " + wait_count);
            need_ack.remove(sender_id);
        }
        Request_Lock.unlock();
    }

    private static byte[] get_protobuf_data(int sender_id, VectorClock clock) throws InvalidProtocolBufferException {
        Message.Builder message = Message.newBuilder();
        message.setId(sender_id);
        generalisedClock.Builder vector_clock = generalisedClock.newBuilder();
        for (int i = 0; i < N; i++) {
            MessageProto.clock.Builder tmp_clock = MessageProto.clock.newBuilder();
            tmp_clock.setId(i);
            tmp_clock.setClock(clock.get_clock(i));
            vector_clock.addClocks(tmp_clock.build());
            //  System.err.println("Set Clock for " + i + " as " + clock.get_clock(i));
        }
        message.setClock(vector_clock.build());
        return message.build().toByteArray();

    }

    private static byte[] get_protobuf_data(int sender_id) throws InvalidProtocolBufferException {
        Message.Builder message = Message.newBuilder();
        message.setId(sender_id);
        byte[] data;
        return message.build().toByteArray();
    }

    public static void main(String[] args) throws InterruptedException, IOException {
        /* Initialisations and stuff */
        if (args.length != 6) {
            System.err.println("Wrong Input Arguments given");
            System.err.println("Arguments Needed: -i <MyId> -n <Total_Number_Of_Nodes> -o <Output_File>");
        }
        String output_file = args[5];
        is_in_critical = false;
        needed_lock = false;
        N = Integer.valueOf(args[3]);
        my_id = Integer.valueOf(args[1]);
        lock_clock = new VectorClock(N);
        my_clock = new VectorClock(N);
        Request_Lock = new ReentrantLock();
        mylock = new ReentrantLock();
        queue = new LinkedHashSet<>();
        need_ack = new LinkedHashSet<>();
        Node node = new Node();
        /* port Mapping the given data */
        try {
            Nodedef stub = (Nodedef) UnicastRemoteObject.exportObject(node, 0);
            Naming.rebind("rmi://localhost:1099/node-" + my_id, stub);
            sleep(5000);
            System.out.println("Initialised " + my_id);
        } catch (MalformedURLException e) {
            System.out.println("Cannot Bind the Stub");
            System.exit(-1);
        }
        int i = 1;
        /* Running the given loop */
        while(i<=100) {
            lock();
            int counter = 1;
            System.out.println("I am " + my_id + " Acquired " + i + " th Lock with lock  " + lock_clock.toString());
            try {
                BufferedReader br = new BufferedReader(new FileReader(output_file));
                String last = "", current = "";
                while ((current = br.readLine()) != null) {
                    last = current;
                }
                String data;
                if (!last.equals(""))
                    counter = (Integer.valueOf(last.split(":")[0]) + 1);
                /* if (counter == 101){
                    unlock();
                    break;
                } */
                data = counter + ":" + my_id + ":" + lock_clock.toString();
                PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(output_file, true)));
                out.println(data);
                out.close();

            } catch (FileNotFoundException e) {
                System.err.println("File Not Found " + output_file);
                System.exit(-1);
            } catch (ArrayIndexOutOfBoundsException e) {
                System.err.println("Wrong Format File");
                System.exit(-1);
            } finally {
                unlock();
                System.out.println("This is unlock for " + i + " th request to write "+counter+" at " + my_id + " with lock " + lock_clock.toString());
            }
            i++;
            sleep(1000);

        }
        System.out.println("I " + my_id + " am done with my "+my_clock.toString());
    }

    private static void lock() throws RemoteException, MalformedURLException, InvalidProtocolBufferException {
        /* Perform Mechanism for locking */
        Request_Lock.lock();
        needed_lock = true;
        wait_count = N - 1;
        my_clock.put_clock(my_id, my_clock.get_clock(my_id) + 1);
        lock_clock.update_clock(my_clock);
        //System.err.println("Updated clock at " + my_id + " is " + my_clock.toString());
        byte[] info = get_protobuf_data(my_id, my_clock);
        for (int i = 0; i < N; i++) {
            if (i != my_id)
                need_ack.add(i);
        }
        Request_Lock.unlock();
        /* Send Lock Requests */
        for (int i = 0; i < N; i++) {
            if (i != my_id) {
                System.out.println("Sent Lockrequest from" + my_id + " to " + i);
                try {
                    Nodedef remote_object = (Nodedef) Naming.lookup("rmi://localhost:1099/node-" + i);
                    remote_object.LockRequest(info);
                } catch (NotBoundException e) {
                    System.err.println("No Such Node found for " + i);
                    System.exit(-1);
                }
            }
        }
        /* Now Wait */
        try {
            while (true) {
                if (wait_count <= 0)
                    break;
                sleep(1);
            }
            Request_Lock.lock();
            needed_lock = false;
            is_in_critical = true;
            Request_Lock.unlock();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void unlock() throws RemoteException, MalformedURLException, InvalidProtocolBufferException {
        /* Perform Mechanism for unlocking */
        Request_Lock.lock();
        is_in_critical = false;
        Request_Lock.unlock();
        byte[] info = get_protobuf_data(my_id, my_clock);
        System.out.println("Queue Length: " + queue.size());

        for (int id : queue) {
            try {
                Nodedef remote_object = (Nodedef) Naming.lookup("rmi://localhost:1099/node-" + id);
                remote_object.SendAckRequest(info);
                System.out.println("Sending ack from " + my_id + " to " + id);
                System.out.println("Sending Unlock from " + my_id + " to " + id);
            } catch (NotBoundException e) {
                System.err.println("No Such Node found for " + id);
                System.exit(-1);
            }
        }
        queue.clear();
    }
}