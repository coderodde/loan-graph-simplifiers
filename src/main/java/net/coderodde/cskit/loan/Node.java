package net.coderodde.cskit.loan;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * This class implements a loan graph node.
 *
 * @author coderodde
 * @version 1.6
 */
public class Node implements Iterable<Node> {
    private final String name;
    private final Map<Node, Long> in;
    private final Map<Node, Long> out;

    /**
     * If equity is below 0, this node owes, and, vice versa, if positive,
     * is eligible to receive cash.
     */
    private long equity;

    public Node(final String name) {
        this.name = name;
        this.in = new HashMap<Node, Long>();
        this.out = new HashMap<Node, Long>();
    }

    public Node(final Node copy) {
        this(copy.name);
    }

    public String getName() {
        return name;
    }

    public void connectTo(final Node borrower, final long amount) {
        checkAmount(amount);
        checkBorrower(borrower);

        if (out.containsKey(borrower)) {
            out.put(borrower, out.get(borrower) + amount);
            borrower.in.put(this, borrower.in.get(this) + amount);
        } else {
            out.put(borrower, amount);
            borrower.in.put(this, amount);
        }

        equity += amount;
        borrower.equity -= amount;
    }

    public int getBorrowerAmount() {
        return this.out.size();
    }

    public long getLoanTo(final Node borrower) {
        if (out.containsKey(borrower) == false) {
            throw new IllegalStateException("Querying non-existing loan.");
        }

        return out.get(borrower);
    }

    public String toString() {
        return "[Node " + name + "; equity: " + getEquity() + " ]";
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return ((Node) o).name.equals(this.name);
    }

    @Override
    public Iterator<Node> iterator() {
        return new ChildIterator();
    }

    public long getEquity() {
        return this.equity;
    }

    private void checkAmount(final long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException(
                    "Illegal amount given: " + amount);
        }
    }

    private void checkBorrower(final Node borrower) {
        if (borrower == null) {
            throw new NullPointerException("Borrower is null.");
        }

        if (borrower == this) {
            throw new IllegalArgumentException("Requesting a self-loop.");
        }
    }

    private class ChildIterator implements Iterator<Node> {

        private Iterator<Node> iterator = Node.this.out.keySet().iterator();

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public Node next() {
            return iterator.next();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException(
                    "Removal not supported.");
        }
    }
}
