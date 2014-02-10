package net.coderodde.cskit.loan;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class implements the loan simplification algorithms.
 *
 * @author coderodde
 * @version 1.6
 */
public class Algorithms {

    public static Algorithm linearSimplify() {
        return new Algorithm("linearSimplify") {
            @Override
            public List<Node> exec(List<Node> nodeList) {
                return linearSimplify(nodeList);
            }
        };
    }

    public static Algorithm greedyCombinatorialSimplify() {
        return new Algorithm("greedyCombinatorialSimplify") {
            @Override
            public List<Node> exec(List<Node> nodeList) {
                return greedyCombinatorialSimplify(nodeList);
            }
        };
    }

    public static Algorithm permutationalSimplify() {
        return new Algorithm("permutationalSimplify") {
            @Override
            public List<Node> exec(List<Node> nodeList) {
                return permutationalSimplify(nodeList);
            }
        };
    }

    private static final List<Node> linearSimplify(List<Node> nodeList) {
        final int N = nodeList.size();
        List<Node> positiveNodes = new ArrayList<Node>(N);
        List<Node> negativeNodes = new ArrayList<Node>(N);
        List<Node> resultNodeList = new ArrayList<Node>(N);

        for (Node node : nodeList) {
            if (node.getEquity() > 0L) {
                positiveNodes.add(node);
            } else if (node.getEquity() < 0L) {
                negativeNodes.add(node);
            } else {
                resultNodeList.add(new Node(node));
            }
        }

        final int POS_LIMIT = positiveNodes.size();
        final int NEG_LIMIT = negativeNodes.size();

        long[] positiveNodesEquities = new long[POS_LIMIT];
        long[] negativeNodesEquities = new long[NEG_LIMIT];

        List<Node> newPositiveNodes = new ArrayList<Node>(POS_LIMIT);
        List<Node> newNegativeNodes = new ArrayList<Node>(NEG_LIMIT);

        int pi = 0;
        int ni = 0;

        for (Node node : positiveNodes) {
            newPositiveNodes.add(new Node(positiveNodes.get(pi)));
            positiveNodesEquities[pi++] = node.getEquity();
        }

        for (Node node : negativeNodes) {
            newNegativeNodes.add(new Node(negativeNodes.get(ni)));
            negativeNodesEquities[ni++] = -node.getEquity();
        }

        pi = 0;
        ni = 0;

        while (pi < POS_LIMIT) {
            if (positiveNodesEquities[pi] > negativeNodesEquities[ni]) {
                newPositiveNodes.get(pi).connectTo(newNegativeNodes.get(ni),
                                                   negativeNodesEquities[ni]);
                positiveNodesEquities[pi] -= negativeNodesEquities[ni++];
            } else if (positiveNodesEquities[pi] < negativeNodesEquities[ni]) {
                newPositiveNodes.get(pi).connectTo(newNegativeNodes.get(ni),
                                                   positiveNodesEquities[pi]);
                negativeNodesEquities[ni] -= positiveNodesEquities[pi++];
            } else {
                newPositiveNodes.get(pi).connectTo(newNegativeNodes.get(ni),
                                                   positiveNodesEquities[pi]);
                ++pi;
                ++ni;
            }
        }

        resultNodeList.addAll(newPositiveNodes);
        resultNodeList.addAll(newNegativeNodes);
        return resultNodeList;
    }

    private static final List<Node>
            greedyCombinatorialSimplify(List<Node> nodeList) {
        final int N = nodeList.size();
        List<Node> positiveNodes = new ArrayList<Node>(N);
        List<Node> negativeNodes = new ArrayList<Node>(N);
        List<Node> resultNodeList = new ArrayList<Node>(N);
        Map<Node, Node> map = new HashMap<Node, Node>(N);

        for (Node node : nodeList) {
            if (node.getEquity() > 0L) {
                positiveNodes.add(node);
                map.put(node, new Node(node));
            } else if (node.getEquity() < 0L) {
                negativeNodes.add(node);
                map.put(node, new Node(node));
            } else {
                resultNodeList.add(new Node(node));
            }
        }

        IndexSet positiveIndexSet = new IndexSet(positiveNodes.size());
        IndexSet negativeIndexSet = new IndexSet(negativeNodes.size());

        outer:
        while (positiveIndexSet.inc()) {
            long currentPositive = evaluatePositive(positiveNodes,
                                                    positiveIndexSet);

            while (negativeIndexSet.inc()) {
                long currentNegative = evaluateNegative(negativeNodes,
                                                        negativeIndexSet);

                if (currentNegative > currentPositive) {
                    if (negativeIndexSet.hasNoGaps()) {
                        // Successive negative groups will be no less than the
                        // current.
                        negativeIndexSet.reset();
                        continue outer;
                    }
                } else if (currentNegative == currentPositive) {
                    int[] positiveIndices = positiveIndexSet.getIndices();
                    int[] negativeIndices = negativeIndexSet.getIndices();

                    link(positiveIndices,
                         negativeIndices,
                         positiveNodes,
                         negativeNodes,
                         map);

                    positiveIndexSet.remove();
                    negativeIndexSet.remove();

                    for (int i = positiveIndices.length - 1; i >= 0; --i) {
                        positiveNodes.remove(positiveIndices[i]);
                    }

                    for (int i = negativeIndices.length - 1; i >= 0; --i) {
                        negativeNodes.remove(negativeIndices[i]);
                    }

                    continue outer;
                }
            }
        }

        for (Node node : map.values()) {
            resultNodeList.add(node);
        }

        return resultNodeList;
    }

    private static final List<Node> permutationalSimplify(List<Node> nodeList) {
        final int N = nodeList.size();
        List<Node> positiveNodes = new ArrayList<Node>(N);
        List<Node> negativeNodes = new ArrayList<Node>(N);
        List<Node> resultNodeList = new ArrayList<Node>(N);

        for (Node node : nodeList) {
            if (node.getEquity() > 0L) {
                positiveNodes.add(node);
            } else if (node.getEquity() < 0L) {
                negativeNodes.add(node);
            } else {
                resultNodeList.add(new Node(node));
            }
        }

        Node[] positiveNodeArray = new Node[positiveNodes.size()];
        Node[] negativeNodeArray = new Node[negativeNodes.size()];

        Node[] bestPositiveNodeArray = null;
        Node[] bestNegativeNodeArray = null;

        int i = 0;

        for (Node node : positiveNodes) {
            positiveNodeArray[i++] = node;
        }

        i = 0;

        for (Node node : negativeNodes) {
            negativeNodeArray[i++] = node;
        }

        int bestEdgeAmount = Integer.MAX_VALUE;

        PermutationIterator<Node> positiveNodesPermuter =
                new PermutationIterator<Node>(positiveNodeArray);

        // Permuters return 'null' as to indicate the end of iteration;
        // this is done for performance's sake.
        for (Node[] positiveNodePermutation : positiveNodesPermuter) {
            if (positiveNodePermutation == null) {
                break;
            }

            PermutationIterator<Node> negativeNodesPermuter =
                    new PermutationIterator<Node>(negativeNodeArray);

            for (Node[] negativeNodePermutation : negativeNodesPermuter) {
                if (negativeNodePermutation == null) {
                    break;
                }

                int linkageEdges = countLinkageEdges(positiveNodePermutation,
                                                     negativeNodePermutation);

                if (bestEdgeAmount > linkageEdges) {
                    bestEdgeAmount = linkageEdges;
                    bestPositiveNodeArray =
                            Arrays.copyOf(positiveNodeArray,
                                          positiveNodeArray.length);
                    bestNegativeNodeArray =
                            Arrays.copyOf(negativeNodeArray,
                                          negativeNodeArray.length);
                }
            }
        }

        resultNodeList.addAll(link(Arrays.asList(bestPositiveNodeArray),
                                   Arrays.asList(bestNegativeNodeArray)));
        return resultNodeList;
    }

    private static final int countLinkageEdges(Node[] positiveNodes,
                                               Node[] negativeNodes) {
        int pi = 0;
        int ni = 0;
        int edgeAmount = 0;

        final int POS_LIMIT = positiveNodes.length;
        final int NEG_LIMIT = negativeNodes.length;

        long[] positiveEquities = new long[POS_LIMIT];
        long[] negativeEquities = new long[NEG_LIMIT];

        for (Node node : positiveNodes) {
            positiveEquities[pi++] = node.getEquity();
        }

        for (Node node : negativeNodes) {
            negativeEquities[ni++] = -node.getEquity();
        }

        pi = 0;
        ni = 0;

        while (pi < POS_LIMIT) {
            if (positiveEquities[pi] > negativeEquities[ni]) {
                positiveEquities[pi] -= negativeEquities[ni++];
            } else if (positiveEquities[pi] < negativeEquities[ni]) {
                negativeEquities[ni] -= positiveEquities[pi++];
            } else {
                ++pi;
                ++ni;
            }

            ++edgeAmount;
        }

        return edgeAmount;
    }

    private static final List<Node> link(List<Node> positiveNodes,
                                         List<Node> negativeNodes) {
        final int POS_LIMIT = positiveNodes.size();
        final int NEG_LIMIT = negativeNodes.size();

        long[] positiveEquities = new long[POS_LIMIT];
        long[] negativeEquities = new long[NEG_LIMIT];

        int pi = 0;
        int ni = 0;

        List<Node> resultPositiveNodes = new ArrayList<Node>(POS_LIMIT);
        List<Node> resultNegativeNodes = new ArrayList<Node>(NEG_LIMIT);
        List<Node> resultNodeList = new ArrayList<Node>(POS_LIMIT + NEG_LIMIT);

        for (Node node : positiveNodes) {
            resultPositiveNodes.add(new Node(node));
            positiveEquities[pi++] = node.getEquity();
        }

        for (Node node : negativeNodes) {
            resultNegativeNodes.add(new Node(node));
            negativeEquities[ni++] = -node.getEquity();
        }

        pi = 0;
        ni = 0;

        while (pi < POS_LIMIT) {
            if (positiveEquities[pi] > negativeEquities[ni]) {
                resultPositiveNodes
                        .get(pi)
                        .connectTo(resultNegativeNodes.get(ni),
                            negativeEquities[ni]);
                positiveEquities[pi] -= negativeEquities[ni++];
            } else if (positiveEquities[pi] < negativeEquities[ni]) {
                resultPositiveNodes
                        .get(pi)
                        .connectTo(resultNegativeNodes.get(ni),
                            positiveEquities[pi]);
                negativeEquities[ni] -= positiveEquities[pi++];
            } else {
                resultPositiveNodes
                        .get(pi)
                        .connectTo(resultNegativeNodes.get(ni),
                            positiveEquities[pi]);
                ++pi;
                ++ni;
            }
        }

        resultNodeList.addAll(resultPositiveNodes);
        resultNodeList.addAll(resultNegativeNodes);
        return resultNodeList;
    }

    /**
     * Links a group. Only indexed nodes get linked.
     *
     * @param positiveIndices the indices into positive-equity nodes.
     * @param negativeIndices the indices into negative-equity nodes.
     * @param positiveNodes the list of positive nodes.
     * @param negativeNodes the list of negative nodes.
     * @param map the map from input nodes to output nodes.
     */
    private static final void link(int[] positiveIndices,
                                   int[] negativeIndices,
                                   List<Node> positiveNodes,
                                   List<Node> negativeNodes,
                                   Map<Node, Node> map) {
        final int POS_LIMIT = positiveIndices.length;
        final int NEG_LIMIT = negativeIndices.length;

        long[] positiveEquities = new long[POS_LIMIT];
        long[] negativeEquities = new long[NEG_LIMIT];

        int pi = 0;
        int ni = 0;

        for (int i : positiveIndices) {
            positiveEquities[pi++] = positiveNodes.get(i).getEquity();
        }

        for (int i : negativeIndices) {
            negativeEquities[ni++] = -negativeNodes.get(i).getEquity();
        }

        pi = 0;
        ni = 0;

        Node p = map.get(positiveNodes.get(positiveIndices[0]));
        Node n = map.get(negativeNodes.get(negativeIndices[0]));

        while (pi < POS_LIMIT) {
            if (positiveEquities[pi] > negativeEquities[ni]) {
                p.connectTo(n, negativeEquities[ni]);
                positiveEquities[pi] -= negativeEquities[ni];
                n = map.get(negativeNodes.get(negativeIndices[++ni]));
            } else if (positiveEquities[pi] < negativeEquities[ni]) {
                p.connectTo(n, positiveEquities[pi]);
                negativeEquities[ni] -= positiveEquities[pi];
                p = map.get(positiveNodes.get(positiveIndices[++pi]));
            } else {
                p.connectTo(n, positiveEquities[pi]);
                ++pi;
                ++ni;

                if (pi < POS_LIMIT) {
                    p = map.get(positiveNodes.get(positiveIndices[pi]));
                    n = map.get(negativeNodes.get(negativeIndices[ni]));
                }
            }
        }
    }

    /**
     * Assumes that all of the nodes in the input list have positive equities.
     *
     * @param nodeList the list to get the nodes from.
     * @param indexSet the index set.
     *
     * @return the sum of equities from list indexed by <code>indexSet</code>.
     */
    private static final long evaluatePositive(
            List<Node> nodeList,
            IndexSet indexSet) {
        long sum = 0L;

        for (int index : indexSet.getIndices()) {
            sum += nodeList.get(index).getEquity();
        }

        return sum;
    }

    /**
     * Assumes that all of the nodes in the input list have negative equities.
     *
     * @param nodeList the list to get the nodes from.
     * @param indexSet the index set.
     *
     * @return the sum of equities from list indexed by <code>indexSet</code>.
     */
    private static final long evaluateNegative(
            List<Node> nodeList,
            IndexSet indexSet) {
        long sum = 0L;

        for (int index : indexSet.getIndices()) {
            sum += nodeList.get(index).getEquity();
        }

        return -sum;
    }
}
