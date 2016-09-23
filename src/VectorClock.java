/**
 * Created by harry7 on 14/9/16.
 */

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;

import static java.lang.Integer.max;

public class VectorClock implements Serializable {
    private HashMap<Integer, Integer> clock;
    private int n;

    public VectorClock(int n) {
        /* n = Number of Nodes */
        this.n = n;
        this.clock = new HashMap<Integer, Integer>();
        for (int i = 0; i < n; i++) this.clock.put(i, 0);
    }

    public void update_clock(VectorClock new_clock) {
        for (int i = 0; i < this.n; i++) {
            this.put_clock(i, max(this.get_clock(i), new_clock.get_clock(i)));
        }
    }

    public int get_clock(int id) {
        return this.clock.get(id);
    }

    public void put_clock(int id, int clock) {
        this.clock.put(id, clock);
    }

    public boolean lessthan(VectorClock new_clock, int my_id, int sender_id) {
        int cnt = 0;
        for (int i = 0; i < this.n; i++) {
            if (new_clock.get_clock(i) < this.get_clock(i))
                return true;
            else if (new_clock.get_clock(i) > this.get_clock(i)) {
                return false;
            } else {
                cnt++;
            }
        }
        return my_id < sender_id;
    }

    public String toString() {
        return "Clock: " + this.clock.toString();
    }
}
