/**
 * Created by harry7 on 14/9/16.
 */

import java.util.Comparator;

import static java.lang.Long.max;

public class VectorClock {
    long[] clock;
    int n;

    public VectorClock(int n) {
        /* n = Number of Nodes */
        this.n = n;
        this.clock = new long[n];
        for (int i = 0; i < n; i++) this.clock[i] = 0;
    }

    public void update_clock(VectorClock new_clock) {
        for (int i = 0; i < this.n; i++) {
            this.clock[i] = max(this.clock[i], new_clock.clock[i]) + 1;
        }
    }
}