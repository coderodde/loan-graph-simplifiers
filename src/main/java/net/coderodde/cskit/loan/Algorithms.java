package net.coderodde.cskit.loan;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    public static Algorithm partitionalSimplify() {
        return new Algorithm("partitionalSimplify") {
            @Override
            public List<Node> exec(List<Node> nodeList) {
                return partitionalSimplify(nodeList);
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

    private static final List<Node> partitionalSimplify(List<Node> nodeList) {
        List<Node> positiveNodes = new ArrayList<Node>(nodeList.size());
        List<Node> negativeNodes = new ArrayList<Node>(nodeList.size());
        List<Node> resultNodeList = new ArrayList<Node>(nodeList.size());

        for (Node node : nodeList) {
            if (node.getEquity() > 0L) {
                positiveNodes.add(node);
            } else if (node.getEquity() < 0L) {
                negativeNodes.add(node);
            } else {
                resultNodeList.add(new Node(node));
            }
        }

        int bestEdgeAmount = Integer.MAX_VALUE;
        List<Integer>[] bestPositivePartition = null;
        List<Integer>[] bestNegativePartition = null;

        PartitionGenerator pospg = new PartitionGenerator(positiveNodes.size());

        do {
            PartitionGenerator negpg =
                    new PartitionGenerator(negativeNodes.size());

            int[] positiveIndices = pospg.getIndices();
            List<Integer>[] positivePartition = loadPartition(positiveIndices);

            do {
                int[] negativeIndices = negpg.getIndices();
                List<Integer>[] negativePartition =
                        loadPartition(negativeIndices);

                int edges = countEdgesFromPartitions(positiveNodes,
                                                     negativeNodes,
                                                     positivePartition,
                                                     negativePartition);

                if (bestEdgeAmount > edges) {
                    bestEdgeAmount = edges;
                    bestPositivePartition = copyPartitions(positivePartition);
                    bestNegativePartition = copyPartitions(negativePartition);
                }

            } while (negpg.inc());
        } while (pospg.inc());

        List<List<Node>> positivePartitions =
                loadNodePartition(positiveNodes, bestPositivePartition);

        List<List<Node>> negativePartitions =
                loadNodePartition(negativeNodes, bestNegativePartition);

        for (int i = 0; i < positivePartitions.size(); ++i) {
            resultNodeList.addAll(link(positivePartitions.get(i),
                                       negativePartitions.get(i)));
        }

        return resultNodeList;
    }

    private static final List<List<Node>>
            loadNodePartition(List<Node> nodeList, List<Integer>[] indexList) {
        List<List<Node>> list = new ArrayList<List<Node>>(indexList.length);

        for (List<Integer> indices : indexList) {
            List<Node> tmp = new ArrayList<Node>(indices.size());

            for (Integer i : indices) {
                tmp.add(nodeList.get(i));
            }

            list.add(tmp);
        }

        return list;
    }

    private static final List<Integer>[]
            copyPartitions(List<Integer>[] partition) {
        List<Integer>[] ret = new List[partition.length];
        int i = 0;

        for (List<Integer> list : partition) {
            ret[i++] = new ArrayList<Integer>(list);
        }

        return ret;
    }

    private static final int countEdgesFromPartitions(
            List<Node> positiveNodes,
            List<Node> negativeNodes,
            List<Integer>[] positivePartition,
            List<Integer>[] negativePartition) {
        if (positivePartition.length != negativePartition.length) {
            return Integer.MAX_VALUE;
        }

        Arrays.sort(positivePartition, new BlockComparator(positiveNodes));
        Arrays.sort(negativePartition, new BlockComparator(negativeNodes));

        for (int i = 0; i < positivePartition.length; ++i) {
            if (sumEquities(positiveNodes, positivePartition[i])
                    != -sumEquities(negativeNodes, negativePartition[i])) {
                return Integer.MAX_VALUE;
            }
        }

        return positiveNodes.size() +
               negativeNodes.size() -
               positivePartition.length;
    }

    private static final long sumEquities(List<Node> nodeList,
                                          List<Integer> indices) {
        long sum = 0L;

        for (Integer i : indices) {
            sum += nodeList.get(i).getEquity();
        }

        return sum;
    }

    private static final class BlockComparator
    implements Comparator<List<Integer>> {
        private Node[] nodes;

        public BlockComparator(List<Node> nodes) {
            this.nodes = new Node[nodes.size()];

            int index = 0;

            for (Node node : nodes) {
                this.nodes[index++] = node;
            }
        }

        public int compare(List<Integer> o1, List<Integer> o2) {
            long e1 = 0L;
            long e2 = 0L;

            for (Integer i : o1) {
                e1 += Math.abs(nodes[i].getEquity());
            }

            for (Integer i : o2) {
                e2 += Math.abs(nodes[i].getEquity());
            }

            return e1 < e2 ? -1 : (e1 > e2 ? 1 : 0);
        }
    }

    private static final List<Integer>[] loadPartition(int[] indices) {
        Set<Integer> set = new HashSet<Integer>(indices.length);

        for (int index : indices) {
            set.add(index);
        }

        List<Integer>[] ret = new List[set.size()];

        for (int i = 0; i < set.size(); ++i) {
            ret[i] = new ArrayList<Integer>();
        }

        int i = 0;

        for (int index : indices) {
            ret[index].add(i++);
        }

        return ret;
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

    public static final void testS() {
        long sum = 0L;

        for (long l = 1; l <= 6; ++l) {
            sum += S(10, l);
        }

        System.out.println(sum);

        sum = 0L;

        for (long l = 1; l <= 6; ++l) {
            sum += S(6, l);
        }

        System.out.println(sum);
    }

    private static final long S(long n, long k) {
        if (k == 1L) {
            return 1L;
        }

        if (k == n) {
            return 1L;
        }

        return S(n - 1, k - 1) + k * S(n - 1, k);
    }
}
