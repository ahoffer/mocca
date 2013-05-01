package weka.subspaceClusterer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class TestRunner {

    static List<String> dataSets = new ArrayList<String>();
    static boolean dryrun = false;
    static String metrics, outputPath, command, classPath, javaExecutable, dataPath;
    static int numProcessors;
    static ProcessBuilder procBuilder;
    static ArrayList<Process> runningProcs = new ArrayList<Process>();
    static String space = " ";

    static void dispatch(List<String> commands) throws IOException, InterruptedException {

        // Keep the system busy, but don't overwhelm it.
        if (runningProcs.size() < numProcessors) {
            runningProcs.add(forkProcess(commands));
        }

        else {

            /*
             * TODO: Use a thread pool to manage each forked process, so that as soon as a process complted, the thread
             * is returned to the pool ready to be used. In the meanwhile, assume that the oldest process will be the
             * first to complete.
             */
            Process oldest = runningProcs.get(0);
            oldest.waitFor();
            runningProcs.remove(oldest);
            runningProcs.add(forkProcess(commands));

        }// else
    }// method

    static Process forkProcess(List<String> commands) throws IOException {
        if (procBuilder == null) {
            procBuilder = new ProcessBuilder();
            procBuilder.inheritIO();
        }

        procBuilder.command(commands);
        Process proc = procBuilder.start();
        return proc;

    }// method

    public static void main(String[] args) throws IOException, InterruptedException {

        parseArgs(args);

        // Set platform independent state
        numProcessors = Runtime.getRuntime().availableProcessors();
        setMetrics();

        // WINDOWS - Set platform dependent state
        setForWindows();

        // LINUX LAB - Set platform dependent state
        // setForLinuxLab();

        // Select the data sets to use.
        // TODO: Maybe I should just use every .arff in the data directory?. Yes. Do that.
        setDataSets();

        // Run tests
        run();

        // Pull all the results into one file
        if (!dryrun) {
            Consolidator.consolidate(outputPath, ResultsWriter.separatedPath(outputPath) + "results.csv");
        }

        // Avoid the error
        // JDWP exit error AGENT_ERROR_NO_JNI_ENV
        System.exit(0);
    }

    public static void parseArgs(String[] args) {
        dryrun = args.length > 0 && args[0].equals("-dryrun");
    }

    static void printCommandLine(List<String> line) {
        // Dump a list of string to the transcipt.
        // Separate each string element by one space.
        for (String element : line) {
            System.out.printf("%s ", element);
        }

        // Move to the next line.
        System.out.printf("\n");

    }

    static String quote(String string) {
        // Enclose a string in double quotes
        return "\"" + string + "\"";
    }

    static void run() throws IOException, InterruptedException {
        int experimentLabel;
        List<List<String>> argLines;

        // Do not modify the arg list because it is used every time through the data set filename loop
        List<String> args;

        argLines = MoccaExperimentGenerator.getArgLines();
        System.out.printf("Number of experiments to run=%,d\n", argLines.size() * dataSets.size());
        experimentLabel = 1;
        for (String dataFname : dataSets) {
            Path datafile = Paths.get(dataPath, dataFname);
            if (Files.isReadable(datafile)) {

                String datafileName = datafile.toString();

                for (List<String> orginalArgLine : argLines) {

                    args = new ArrayList<String>(orginalArgLine);

                    // Build final command line by prepending/appending as necessary.
                    // PREPEND
                    args.add(0, "weka.subspaceClusterer.MySubspaceClusterEvaluation");
                    args.add(0, quote(classPath));
                    args.add(0, "-cp");
                    args.add(0, javaExecutable);
                    // APPEND
                    args.add("-label");
                    args.add("" + experimentLabel);
                    args.add("-M");
                    args.add(metrics);
                    args.add("-path");
                    args.add(outputPath);
                    args.add("-c");
                    args.add("last");
                    args.add("-t");
                    args.add(datafileName);

                    if (dryrun) {
                        /***** DEBUG *****/
                        printCommandLine(args);
                    } else {
                        // Schedule the experiment
                        dispatch(args);
                    }
                    // Set the ID for the next experiment to run
                    experimentLabel++;
                }// for

            }// if

            else {
                System.err.printf("File %s is not readable\n", datafile);
            }// else

        }// for

        // For for all experiments to finish before exiting method.
        waitForAll();

    }// method

    public static void setDataSets() {

        // dataSets.add("v.arff");

        // Datasets to cluster
        dataSets.add("breast.arff");
        dataSets.add("glass.arff");
        // dataSets.add("liver.arff");
        // dataSets.add("N30.arff");
        // dataSets.add("S1500.arff");
        dataSets.add("sonar.arff");

        // Pendigits takes a really long time. Don't use it in normal runs.
        // dataSets.add("pendigits.arff");
    }

    public static void setForLinuxLab() {
        outputPath = ResultsWriter.separatedPath("/net/metis/home2/ahoffer/results");
        dataPath = ResultsWriter.separatedPath("/net/metis/home2/ahoffer/git/sepc/data");
        classPath = ".:/net/metis/home2/ahoffer/git/sepc/workspace/OpenSubspace/lib/*";
        javaExecutable = "java";
    }

    public static void setForWindows() {
        outputPath = ResultsWriter.separatedPath("C:\\results");
        dataPath = ResultsWriter.separatedPath("C:\\Users\\ahoffer\\Documents\\GitHub\\sepc\\data");
        classPath = ".;\\Users\\ahoffer\\Documents\\GitHub\\sepc\\workspace\\OpenSubspace\\lib\\*";
        javaExecutable = "javaw.exe";
    }

    public static void setMetrics() {
        // metrics = "F1Measure:Accuracy:Entropy:CE:RNIA:Coverage:ClusterDistribution";
        metrics = "F1Measure:Accuracy:Entropy:Coverage";
    }

    public static void waitForAll() throws InterruptedException {
        for (Process proc : runningProcs) {
            proc.waitFor();
        }
    }

}// class