/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.coderodde.cskit.loan;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * This class implements common utilities.
 *
 * @author coderodde
 * @version 1.6
 */
public class Utilities {

    public static final List<Node> getRandomGraph(final int size,
                                                  final float loadFactor,
                                                  final long maximumLoan,
                                                  final Random r) {
        List<Node> nodeList = new ArrayList<Node>(size);

        for (int i = 0; i < size; ++i) {
            nodeList.add(new Node("" + i));
        }

        for (int i = 0; i < size; ++i) {
            for (int j = 0; j < size; ++j) {
                if (i != j && r.nextFloat() < loadFactor) {
                    final long loan = Math.abs(r.nextLong()) % maximumLoan + 1L;
                    nodeList.get(i).connectTo(nodeList.get(j), loan);
                }
            }
        }

        return nodeList;
    }

    public static final boolean loanGraphsAreEquivalent(List<Node> nodeList1,
                                                        List<Node> nodeList2) {
        Set<Node> set1 = new HashSet<Node>(nodeList1.size());
        Set<Node> set2 = new HashSet<Node>(nodeList2.size());

        set1.addAll(nodeList1);
        set2.addAll(nodeList2);

        if (set1.size() != set2.size()) {
            return false;
        }

        Map<String, Node> map1 = new HashMap<String, Node>(set1.size());
        Map<String, Node> map2 = new HashMap<String, Node>(set1.size());

        for (Node node : set1) {
            map1.put(node.getName(), node);
        }

        for (Node node : set2) {
            map2.put(node.getName(), node);
        }

        for (String name : map1.keySet()) {
            if (map2.containsKey(name) == false) {
                return false;
            }

            if (map1.get(name).getEquity() != map2.get(name).getEquity()) {
                return false;
            }
        }

        return true;
    }

    public static final long sumAllLoans(List<Node> nodeList) {
        long sum = 0L;

        for (Node node : nodeList) {
            for (Node node2 : node) {
                sum += node.getLoanTo(node2);
            }
        }

        return sum;
    }

    public static final int getEdgeAmount(List<Node> nodeList) {
        int edges = 0;

        for (Node node : nodeList) {
            edges += node.getBorrowerAmount();
        }

        return edges;
    }


}
