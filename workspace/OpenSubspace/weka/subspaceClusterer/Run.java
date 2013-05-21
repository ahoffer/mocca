package weka.subspaceClusterer;

import i9.subspace.base.Cluster;

import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import weka.clusterquality.ClusterQualityMeasure;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.Utils;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Normalize;

/*
 * OPTIONS
 * 
 * Specify the data set file.
 * -t < name of the data set file > 
 *
 * -T < true clusters file > 
 *  This is needed for CE and RNIA metrics.

 * -c <class index> 
 * Set the index of the data set's attribute that indicates and instances class. Class must be a nominal attribute. By convention, this is the last attribute in the data set. Use "-c last".
 * 
 * -M <cluster quality measures> 
 * Specify subspace cluster quality measures in package weka.clusterquality to apply to clustering results. Sseparate measures with ':' e.g. -M F1Measure:Entropy:CE
 * 
 * -sc <subspace clusterer> 
 * Subspace clustering algorithms in package weka.subspaceClusterer.
 * 
 * -timelimit <time limit in minutes> 
 * Specify the time limit on clustering in minutes (whole numbers only). Applies
 * only to clustering, not the time to evaluate the results.
 * 
 * -exp <experiement ID> 
 * Used to uniquely identify a test run. It used in the filenames of the output files and is included  in the file contentsas to be used a foregin key .

 */
public class Run {
    private class Task implements Callable<Void> {
        // State vars
        SubspaceClusterer clusterer;
        Instances dataSet;

        // Constructor
        Task(SubspaceClusterer clusterer, Instances dataSet) {
            this.clusterer = clusterer;
            this.dataSet = dataSet;
        }// constructor

        @Override
        public Void call() throws Exception {
            clusterer.buildSubspaceClusterer(dataSet);
            return null;
        }// method
    }// Task class

    /*-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----*/
    public static void main(String[] args) {
        Run eval = new Run(args);
        try {
            eval.run();
        } catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }// main

    /*-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----*/
    SubspaceClusterer m_clusterer;
    private ArrayList<Cluster> m_clusters = null;
    ResultsWriter m_writer = new ResultsWriter();
    /*
     * the command line args
     */
    String[] m_options;

    public Run(String[] options) {
        this.m_options = options;
    }

    /** The raw instances loaded from disk. */
    Instances m_dataSet;
    /** The data that is to clusters. The class attribute is removed and data may be scaled or normalized. **/
    Instances m_preprocessedDataSet;
    /* The metrics to perform on the clustering result. */
    ArrayList<ClusterQualityMeasure> m_metricsObjects = new ArrayList<ClusterQualityMeasure>();
    /* Used to measure the run time. */
    long m_startTime;
    long m_stoptTime;
    /**
     * A time limit in minutes. If the clustering process is interrupted if it does not complete before the time limit.
     */
    long m_timeLimit = 30;
    /**
     * The true clusters hidden in the data set. This is required for some metrics (RNIA, and CE).
     */
    ArrayList<Cluster> m_trueClusters;

    /*-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----*/
    /**
     * Evaluates a clusterer with the options given.
     * 
     * @param options
     *            An array of strings containing options for clustering.
     * @throws Exception
     */
    public void run() throws Exception {
        setOptions();
        Instances data = MoccaUtils.removeClassAttribute(m_dataSet);
        m_preprocessedDataSet = runDataPreprocessor(data);
        startTimer();
        // TODO:Change this around that there is no so that if the timelimit is not specified, the thread is never
        // killed. That way, I don't need two methods to do the job of one.
        // runClustererWithTimeLimit();
        runClusterer();
        stopTimer();
        m_writer.put("runtime_clustering_sec", getElapsedTime());
        startTimer();
        runMetrics();
        stopTimer();
        m_writer.put("runtime_metrics_sec", getElapsedTime());
        m_writer.setClusterer(m_clusterer);
        m_writer.recordDescriptiveStats(getClusters());
        m_writer.writeResults();
        m_writer.writeClusters(getClusters());
    }

    /*-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----*/
    public double minQuality(double[] quality) {
        return StdStats.min(quality);
    }

    /**
     * Calculates all quality metrics specified in m_metrics on the clustering result. Returns the results as a
     * StringBuffer.
     * 
     * @return The results of applying quality metrics to the clustering result.
     */
    void runMetrics() {
        // PCA-assisted MOCCA loses reference to the original dimension, but dimensionality is used in certain metrics.
        // The answer is to set the subspace of the clusters to be the full space.
        ArrayList<Cluster> clusters = MoccaUtils.setToFullsapce(getClusters());
        // calculate each quality metric
        for (ClusterQualityMeasure metricObj : m_metricsObjects) {
            metricObj.calculateQuality(clusters, m_dataSet, m_trueClusters);
            m_writer.put(metricObj.getName(), metricObj.getOverallValue());
        }
    }// method

    /*-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----*/
    ArrayList<Cluster> getClusters() {
        // Lazy init the cluster list field
        if (m_clusters == null) {
            m_clusters = new ArrayList<Cluster>();
            m_clusters = (ArrayList<Cluster>) m_clusterer.getSubspaceClustering();
        }
        return m_clusters;
    }

    /*-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----*/
    /**
     * PRECONDITION: The field m_clusterer must be set before this method is called.
     * 
     * @param clusterer
     *            the clusterer to include options for
     * @return a string detailing the valid command line options
     */
    String getOptionString() {
        StringBuffer optionsText = new StringBuffer("");
        // General options
        optionsText.append("\n\nGeneral options:\n\n");
        optionsText.append("-sc <subspace clusterer>\n");
        optionsText.append("\tSpecifies the subspace clustering algorithm to\n");
        optionsText.append("\tevaluate. It must be one of the algorithms in \n");
        optionsText.append("\tin the package weka.subspaceClusterer.\n");
        optionsText.append("-t <name of input file>\n");
        optionsText.append("\tSpecifies the input arff file containing the\n");
        optionsText.append("\tdata set to cluster.\n");
        optionsText.append("-T <name of true cluster file>\n");
        optionsText.append("\tSpecifies the .true file containing the\n");
        optionsText.append("\ttrue clustering.\n");
        optionsText.append("-M <cluster quality measures to evaluate>\n");
        optionsText.append("\tSpecifies the subspace cluster quality metrics\n");
        optionsText.append("\tin the weka.clusterquality package to apply.\n");
        optionsText.append("\tSeparate metrics with a colon (':').\n");
        optionsText.append("\t\te.g. -M F1Measure:Entropy:CE\n");
        optionsText.append("-c <class index>\n");
        optionsText.append("\tSpecifies the index of the class attribute,\n");
        optionsText.append("\tstarting with 1. If supplied, the class  is\n");
        optionsText.append("\tignored during clustering but is used in a\n");
        optionsText.append("\tclasses to clusters evaluation.\n");
        optionsText.append("-timelimit <time limit for clustering>\n");
        optionsText.append("\tSpecifies a time limit in minutes for\n");
        optionsText.append("\tclustering. The value should be a whole number\n");
        optionsText.append("\tgreater than zero.\n");
        optionsText.append("\t-path. The directory where the output files are written\n");
        optionsText.append("-label <experiment label>\n");
        optionsText.append("\tUnique ID of this experimental run\n");
        // Get scheme-specific options
        optionsText.append("\nOptions specific to " + m_clusterer.getClass().getName() + ":\n\n");
        @SuppressWarnings("unchecked")
        Enumeration<Option> enu = ((OptionHandler) m_clusterer).listOptions();
        while (enu.hasMoreElements()) {
            Option option = (Option) enu.nextElement();
            optionsText.append(option.synopsis() + '\n');
            optionsText.append(option.description() + "\n");
        }// while
        return optionsText.toString();
    }

    /*-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----*/
    Double getElapsedTime() {
        // Return number of SECONDS.
        // PRECONDITIONS. The methods statrt() and start must have been called at least once.
        return Double.valueOf(Math.round((m_stoptTime - m_startTime) / 1e9));
    }

    /*-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----*/
    boolean runClustererWithTimeLimit() {
        // Assume the clusterer compeletes without interruption.
        boolean timeout = false;
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<Void> future = executor.submit(new Task(m_clusterer, m_preprocessedDataSet));
        try {
            future.get(m_timeLimit, TimeUnit.MINUTES);
        } catch (TimeoutException e) {
            // This is not an error. This is our timeout.
            timeout = true;
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        executor.shutdownNow();
        // Assume that no timeout means the clusterer ran successfully
        return !timeout;
    }

    Instances runDataPreprocessor(Instances data) {
        // Normalize values in data to [0,1]
        // Keeps width values in a smaller range, but may overweight outliers
        if (data == null) {
            System.err.printf("Must set the data before preprocessing it\n");
            System.exit(-3);
        }
        Instances newData = null;
        Normalize normalize = new Normalize();
        // normalize.setIgnoreClass(true);
        try {
            normalize.setInputFormat(data);
            newData = Filter.useFilter(data, normalize);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return newData;
    }// method

    void runClusterer() {
        try {
            m_clusterer.buildSubspaceClusterer(m_preprocessedDataSet);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /*-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----*/
    /**
     * Sets the class using classString.
     * 
     * @param classString
     * @throws Exception
     */
    void setClassAttribute(String classString) throws Exception {
        int theClass = 0;
        if (m_dataSet == null) {
            throw new Exception("Attempted to set class without first setting a data set.");
        }
        if (classString.length() != 0) {
            if (classString.compareTo("last") == 0)
                theClass = m_dataSet.numAttributes();
            else if (classString.compareTo("first") == 0)
                theClass = 1;
            else
                theClass = Integer.parseInt(classString);
        } else {
            // if the data set defines a class attribute, use it
            if (m_dataSet.classIndex() != -1) {
                theClass = m_dataSet.classIndex() + 1;
                System.err.println("Note: using class attribute from " + "dataset, i.e., attribute #" + theClass);
            }
        }
        if (theClass != -1) {
            if (theClass < 1 || theClass > m_dataSet.numAttributes())
                throw new Exception("Class attribute is out of range. Did you forget -c last ?");
            if (!m_dataSet.attribute(theClass - 1).isNominal())
                throw new Exception("Class attribute must be nominal!");
            m_dataSet.setClassIndex(theClass - 1);
        }
    }

    /*-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----*/
    /**
     * Set the clusterer using the class name.
     * 
     * @param clustererClassName
     *            A subspace clusterer class name.
     * @throws Exception
     *             If clusterer is not a valid class name.
     */
    void setClustererObject(String clustererClassName) throws Exception {
        m_clusterer = SubspaceClusterer.forName("weka.subspaceClusterer." + clustererClassName, null);
    }

    /**
     * /** Sets the data set to use in the clustering from a file name.
     * 
     * @param filename
     *            The name of an arff file containing data to cluster.
     * @throws Exception
     *             If there is a problem opening fileName or loading the data set.
     */
    void setDataSet(String filename) {
        DataSource source = null;
        try {
            source = new DataSource(filename);
            m_dataSet = source.getDataSet();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Parses metricClassesString. Uses reflection to create metric classes and adds them to m_metrics.
     * 
     * @param metricClassesString
     */
    void setMetricObjects(String metricClassesString) {
        String[] classStrings = metricClassesString.split(":");
        for (int i = 0; i < classStrings.length; i++) {
            try {
                Class<?> c = Class.forName("weka.clusterquality." + classStrings[i]);
                m_metricsObjects.add((ClusterQualityMeasure) c.newInstance());
            } catch (InstantiationException e1) {
                System.err.println("Not a valid subspace measure class: " + "weka.clusterquality." + classStrings[i]);
            } catch (IllegalAccessException e1) {
                System.err.println("Not a valid subspace measure class: " + "weka.clusterquality." + classStrings[i]);
            } catch (ClassNotFoundException e) {
                System.err.println("Not a valid subspace measure class: " + "weka.clusterquality." + classStrings[i]);
            }
        }
    }

    /*-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----*/
    /**
     * 
     * @param options
     * @throws Exception
     */
    void setOptions() throws Exception {
        try {
            if (Utils.getFlag('h', m_options)) {
                throw new Exception("Help requested.");
            }
            String scName = Utils.getOption("sc", m_options);
            if (scName.length() == 0) {
                System.err.println("No subspace clutstering algorithm specified. Specify an algorithm with -sc.");
            } else {
                this.setClustererObject(scName);
            }
            String dataSetFileName = Utils.getOption('t', m_options);
            if (dataSetFileName.length() == 0) {
                throw new Exception("No input file, use -t");
            } else {
                setDataSet(dataSetFileName);
                m_writer.setDataName(dataSetFileName);
            }
            String measureOptionString = Utils.getOption('M', m_options);
            if (measureOptionString.length() == 0) {
                System.err.println("No metrics set. Use -M to specify quality metrics.");
            } else {
                setMetricObjects(measureOptionString);
            }
            String trueFileName = Utils.getOption('T', m_options);
            if (trueFileName.length() == 0) {
                // System.err.println("No true cluster file set. Some metrics "
                // + "will not function without a true cluster file "
                // + "(CE and RNIA). Use -T to specify a true cluster file.");
            } else {
                setTrueClusters(trueFileName);
            }
            String classString = Utils.getOption('c', m_options);
            setClassAttribute(classString);
            String timeLimit = Utils.getOption("timelimit", m_options);
            if (timeLimit.length() > 0) {
                setTimeLimit(timeLimit);
            }
            String experimentName = Utils.getOption("label", m_options);
            if (experimentName.length() == 0) {
                throw new Exception("No experiment label, use -label");
            }
            m_writer.setKey(experimentName);
            // If it is not specified, leave it as an empty string.
            m_writer.setPath(Utils.getOption("path", m_options));
        } catch (Exception e) {
            throw new Exception('\n' + e.getMessage() + getOptionString());
        }
        // Set options for the clusterer
        ((OptionHandler) m_clusterer).setOptions(m_options);
    }

    /*-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----*/
    /**
     * Set the time limit for clustering. The time limit is only modified if t is greater than zero.
     * 
     * @param t
     *            A time in minutes.
     */
    void setTimeLimit(String minutes) {
        Long val = Long.parseLong(minutes);
        if (val > 0) {
            m_timeLimit = val;
        }
    }

    /*-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----*/
    /**
     * Sets the true clusters using the file referred to by fileName.
     * 
     * @param filename
     * @throws Exception
     */
    void setTrueClusters(String filename) throws Exception {
        File trueClusterFile = new File(filename);
        m_trueClusters = SubspaceClusterTools.getClusterList(trueClusterFile, getNumDims());
    }

    public int getNumDims() {
        return m_dataSet.numAttributes() - 1;
    }

    /*-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----*/
    void startTimer() {
        m_startTime = System.nanoTime();
    }

    /*-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----*/
    void stopTimer() {
        m_stoptTime = System.nanoTime();
    }
}// class