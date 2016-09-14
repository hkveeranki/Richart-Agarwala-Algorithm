/**
 * Created by harry7 on 14/9/16.
 */

import java.util.Comparator;

public class Request {
    int id;
    int clock;

    public Request(byte[] info) {
        this.clock = 0;
        this.id = 0;
    }

}

class RequestComparator implements Comparator<Request> {
    @Override
    public int compare(Request o1, Request o2) {
        if (o1.clock < o2.clock) {
            return -1;
        } else if (o1.clock == o2.clock && o1.id < o2.id) {
            return 1;
        }
        return 0;
    }

}