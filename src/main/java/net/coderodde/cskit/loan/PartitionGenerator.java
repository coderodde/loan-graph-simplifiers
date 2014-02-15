package net.coderodde.cskit.loan;

/**
 * This class generate all partitions of a set 0, 1, ..., N - 1.
 *
 * @author coderodde
 * @version
 */
public class PartitionGenerator {

    private int n;
    private int k;
    private int[] s;
    private int[] m;

    public PartitionGenerator(final int n) {
        check(n);

        this.n = n;
        this.s = new int[n];
        this.m = new int[n];
    }

    public boolean inc() {
        int i = 0;
        ++s[i];

        while (i < n - 1 && s[i] > m[i] + 1) {
            s[i++] = 0;
            ++s[i];
        }

        if (i == n - 1) {
            return false;
        }

        int max = s[i];

        for (--i; i >= 0; --i) {
            m[i] = max;
        }

        return true;
    }

    public int[] getIndices() {
        return s;
    }

    private void check(final int n) {
        if (n < 1) {
            throw new IllegalArgumentException("'n' < 1.");
        }
    }

    public static final void print(int[] arr) {
        for (int i : arr) {
            System.out.print(i + " ");
        }

        System.out.println();
    }

    public static void main(String... args) {
        PartitionGenerator pg = new PartitionGenerator(3);
        int[] indices = pg.getIndices();

        do {
            print(indices);
        } while (pg.inc());
    }
}
