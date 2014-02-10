package net.coderodde.cskit.loan;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * This iterator class generates all the possible lexicographic permutations of
 * a list.
 *
 * @author coderodde
 * @version 1.6
 */
public class PermutationIterator<T> implements Iterable<T[]>, Iterator<T[]> {

    private T[] array;
    private int[] keys;
    private boolean hasMore;
    private boolean initial = true;

    public PermutationIterator(T[] array) {
        this.array = array;
        this.keys = new int[array.length];

        if (array.length > 0) {
            for (int i = 0; i < array.length; ++i) {
                this.keys[i] = i;
            }

            hasMore = true;
        }
    }

    @Override
    public Iterator<T[]> iterator() {
        return this;
    }

    @Override
    public boolean hasNext() {
        return hasMore;
    }

    @Override
    public T[] next() {
        if (hasMore == false) {
            throw new NoSuchElementException("No permutations available.");
        }

        if (initial) {
            initial = false;
            return array;
        }

        int i = keys.length - 2;

        while (i >= 0 && keys[i] > keys[i + 1]) {
            --i;
        }

        if (i == -1) {
            hasMore = false;
            return null;
        }

        int j = i + 1;
        int min = keys[j];
        int minIndex = j;

        while (j < keys.length) {
            if (keys[i] < keys[j] && keys[j] < min) {
                min = keys[j];
                minIndex = j;
            }

            ++j;
        }

        int tmp = keys[i];
        keys[i] = keys[minIndex];
        keys[minIndex] = tmp;

        T ttmp = array[i];
        array[i] = array[minIndex];
        array[minIndex] = ttmp;

        ++i;
        j = array.length - 1;

        while (i < j) {
            tmp = keys[i];
            keys[i] = keys[j];
            keys[j] = tmp;

            ttmp = array[i];
            array[i] = array[j];
            array[j] = ttmp;

            ++i;
            --j;
        }

        return array;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException(
                "Removing a permutation is not a meaningful operation.");
    }
}
