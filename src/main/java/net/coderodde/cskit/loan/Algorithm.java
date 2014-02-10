package net.coderodde.cskit.loan;

import java.util.List;

/**
 * This interface defines a loan simplification algorithm.
 *
 * @author coderodde
 * @version 1.6
 */
public abstract class Algorithm {

    private String name;

    public Algorithm(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    public abstract List<Node> exec(List<Node> nodeList);
}
