package net.coderodde.cskit.loan;

import java.util.List;
import java.util.Random;

/**
 * This class demonstrates the performance of the loan graph simplification
 * algorithms.
 *
 * @author coderodde
 * @version 1.6
 */
public class Demo {

    public static void main(String... args) {
        Algorithms.testS();
        final long SEED = System.currentTimeMillis(); //1392045592989L; //System.currentTimeMillis();
        final int N = 14;

        System.out.println("Seed: " + SEED);
        System.out.println("Amount of nodes in the input graphs: " + N);

        Random r = new Random(SEED);
        List<Node> nodeList = Utilities.getRandomGraph(N, 0.4f, 30L, r);

        profile(Algorithms.linearSimplify(), nodeList);
        profile(Algorithms.greedyCombinatorialSimplify(), nodeList);
        profile(Algorithms.permutationalSimplify(), nodeList);
        profile(Algorithms.partitionalSimplify(), nodeList);
    }

    private static final void profile(final Algorithm algorithm,
                                      final List<Node> nodeList) {
        title("Profiling " + algorithm);

        long ta = System.currentTimeMillis();

        List<Node> result = algorithm.exec(nodeList);

        long tb = System.currentTimeMillis();

        System.out.println(
                "Total flow in input:  " +
                Utilities.sumAllLoans(nodeList));

        System.out.println(
                "Total flow in output: " +
                Utilities.sumAllLoans(result));

        System.out.println(
                "Edges in the input:  " + Utilities.getEdgeAmount(nodeList));

        System.out.println(
                "Edges in the output: " + Utilities.getEdgeAmount(result));

        System.out.println("Time for " + algorithm + " " + (tb - ta) + " ms.");
        System.out.println(
                "Loan graphs equivalent: " +
                Utilities.loanGraphsAreEquivalent(nodeList, result));

        System.out.println();
    }

    private static final void title(String name) {
        final int NAME_LENGTH = name.length();
        final StringBuilder sb = new StringBuilder(80);

        for (int i = 0; i < (80 - NAME_LENGTH - 2) >>> 1; ++i) {
            sb.append('-');
        }

        sb.append(' ')
          .append(name)
          .append(' ');

        while (sb.length() < 80) {
            sb.append('-');
        }

        System.out.println(sb.toString());
    }
}
