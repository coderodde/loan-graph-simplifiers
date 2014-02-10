package net.coderodde.cskit.loan;

import java.util.ArrayList;
import java.util.List;

/**
 * This class implements an index set which can produce all possible index
 * combinations.
 *
 * @author coderodde
 * @version 1.6
 */
public class IndexSet {
    private int n;
    private int k;
    private int[] indices;
    private int[][] results;

    public IndexSet(final int size) {
        if (size < 2) {
            throw new IllegalArgumentException("Bad size.");
        }

        this.n = size;
        this.k = 1;
        this.indices = new int[size];
        this.indices[0] = -1;
        this.results = new int[size + 1][];

        for (int i = 0; i <= size; ++i) {
            this.results[i] = new int[i];
        }
    }

    public boolean inc() {
        if (n == 0) {
            return false;
        }

        if (indices[k - 1] == n - 1) {
            for (int i = k - 2; i >= 0; --i) {
                if (indices[i] + 1 < indices[i + 1]) {
                    ++indices[i++];

                    while (i < k) {
                        indices[i] = indices[i - 1] + 1;
                        ++i;
                    }

                    return true;
                }
            }

            ++k;

            for (int i = 0; i < k; ++i) {
                indices[i] = i;
            }

            return true;
        }

        ++indices[k - 1];
        return true;
    }

    /**
     * Returns <code>true</code> if this index set has no gaps between any two
     * adjacent indices.
     *
     * @return <code>true</code> if this set has no gaps; <code>false</code>
     * otherwise.
     */
    public boolean hasNoGaps() {
        for (int i = 0; i < k - 1; ++i) {
            if (indices[i] + 1 != indices[i + 1]) {
                return false;
            }
        }

        return true;
    }

    /**
     * Returns an array of length <code>k</code>. Runs in <tt>O(k)</tt> - time.
     *
     * @return the current index combination.
     */
    public int[] getIndices() {
        int[] ret = results[k];

        for (int i = 0; i < k; ++i) {
            ret[i] = indices[i];
        }

        return ret;
    }

    public void remove() {
        n -= k;

        if (k > n) {
            k = n;
        }

        if (k > 0) {
            for (int i = 0; i < k; ++i) {
                indices[i] = i;
            }

            --indices[k - 1];
        }
    }

    public void reset() {
        k = 1;
        indices[0] = 0;
    }
}
