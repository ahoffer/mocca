package weka.subspaceClusterer;

import java.util.ArrayList;
import java.util.List;

public class MoccaExperimentGenerator {

    public static void main(String[] args) {

    }

    /*
     * TODO: Turn all the instance variables into a hashmap. Then create a superclass that iterates over the table to
     * build the parameter string.
     */

    static double[] gammas = { 0.0, 0.1, 0.3, 0.5 };
    static double[] alphas = { 0.1 };
    static double[] widths = { .001, .01, .1, };
    static double[] betas = { 0.25, 0.45 };
    static double[] epsilons = { 0.01 };
    static double[] subspaceSimilarityThresholds = { -1 };
    static double[] clusterSimilarityThresholds = { 0, 0.1, 0.5 };
    static int maxiter = 1000000;
    static String subspaceClutererName = "Mocca";

    public static List<List<String>> getArgLines() {

        ArrayList<List<String>> argLines = new ArrayList<List<String>>();
        ArrayList<String> args;

        for (double e : epsilons) {
            for (double a : alphas) {
                for (double b : betas) {
                    for (double s : subspaceSimilarityThresholds) {
                        for (double i : clusterSimilarityThresholds) {
                            for (double g : gammas) {
                                for (double w : widths) {

                                    args = new ArrayList<String>();
                                    args.add("-sc");
                                    args.add(subspaceClutererName);
                                    args.add("-a");
                                    args.add("" + a);
                                    args.add("-b");
                                    args.add("" + b);
                                    args.add("-e");
                                    args.add("" + e);
                                    args.add("-g");
                                    args.add("" + g);
                                    args.add("-i");
                                    args.add("" + i);
                                    args.add("-s");
                                    args.add("" + s);
                                    args.add("-w");
                                    args.add("" + w);
                                    args.add("-maxiter");
                                    args.add("" + maxiter);
                                    argLines.add(args);
                                }
                            }
                        }
                    }
                }
            }
        }

        return argLines;
    }
}// class
