package weka.subspaceClusterer;

import java.util.ArrayList;
import java.util.List;

public class MoccaExperimentBuilder {

    public static void main(String[] args) {

    }

    /*
     * TODO: Turn all the instance variables into a hashmap. Then create a superclass that iterates over the table to
     * build the parameter string.
     */

    static double[] gammas = { 0.0, 0.1, 0.5, 0.75, 1.0 };
    static double[] alphas = { 0.1 };
    static double[] widths = { 0.01, 0.1, 0.25, 0.5, 0.75, 1.0 };
    static double[] betas = { 0.1, 0.35, 0.5 };
    static double[] epsilons = { 0.05 };
    static double[] subspaceOverlapThresholds = { 0.95, 0.99 };
    static double[] objectOverlapThresholds = { 0.3, 0.9, 0.95, 0.99 };
    static int maxiter = 10000;
    static String subspaceClutererName = "Mocca";

    public static List<List<String>> getArgLines() {

        ArrayList<List<String>> argLines = new ArrayList<List<String>>();
        ArrayList<String> args;

        for (double e : epsilons) {
            for (double a : alphas) {
                for (double b : betas) {
                    for (double s : subspaceOverlapThresholds) {
                        for (double i : objectOverlapThresholds) {
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
