package weka.subspaceClusterer;

import i9.subspace.base.Cluster;

import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.Future;

import weka.clusterquality.ClusterQualityMeasure;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.Utils;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

/**
 * A class for evaluating subspace clustering models. Most of this code was
 * already in OpenSubspace. This class is a refactored version of
 * SubspaceClusterEvaluation. I modified the code, to be more understandable (to
 * me at least). I also took time limit functionality from
 * weka.gui.explorer.SubspaceClustererPanel. Now, clustering algorithms are run
 * in a thread. If the time limit is reached, before the algorithm completes, it
 * is interrupted. I also modified the output format for the results. The output
 * from a successful execution of this class is
 * <p/>
 * 
 * [clusterer name][TAB][scheme][TAB][data set][TAB][run time][TAB] [quality
 * metric][TAB][quality metric][TAB]...
 * </p>
 * 
 * -t < name of the data set file > <br/>
 * <ul>
 * Specify the data set file.
 * <p/>
 * </ul>
 * 
 * -T < true clusters file > <br/>
 * <ul>
 * Specify a file containing the true clusters. This is needed for CE and RNIA
 * metrics.
 * <p/>
 * </ul>
 * 
 * -c < class index > <br/>
 * <ul>
 * Set the class attribute.
 * <p/>
 * </ul>
 * 
 * -M < cluster quality measures > <br/>
 * <ul>
 * Specify subspace cluster quality measures in package weka.clusterquality to
 * apply to clustering results.
 * <p/>
 * separate measures with ':' e.g. -M F1Measure:Entropy:CE
 * <p/>
 * </ul>
 * 
 * -sc < subspace clusterer > <br/>
 * <ul>
 * Subspace clustering algorithms in package weka.subspaceClusterer.
 * <p/>
 * </ul>
 * 
 * -timelimit < time limit in minutes > <br/>
 * <ul>
 * Specify the time limit on clustering in minutes (whole numbers only). Applies
 * only to clustering, not the time to evaluate the results.
 * <p/>
 * </ul>
 * 
 * -outfile < output file > <br/>
 * <ul>
 * Specify a file path to append the results of the clustering. If a file is not
 * specified, then output is written to stdout.
 * <p/>
 * </ul>
 * 
 * @author Dave Hunn (david.c.hunn@gmail.com)
 * @version Revision: 0.85
 */
public class MySubspaceClusterEvaluation {

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

			Thread.sleep(4000);
			System.out.println("Slow!");
			
			//System.out.println("FAST!");
			// DEBUG
			// clusterer.buildSubspaceClusterer(dataSet);
			return null;
		}// method

	}// Task class

	public static void main(String[] args) {
		MySubspaceClusterEvaluation eval = new MySubspaceClusterEvaluation();
		try {
			eval.evaluate(args);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}// main

	/** the clusterer */
	private SubspaceClusterer m_clusterer = null;

	/** The data set to perform the clustering on. */
	private Instances m_dataSet;

	// name of experiment. Used to name output files. Should be unique for every
	// run.
	private String m_experiment = null;

	/** The metrics to perform on the clustering result. */
	private ArrayList<ClusterQualityMeasure> m_metrics;

	// Used to measure the run time.
	private long m_startTime;

	private long m_stoptTime;

	/**
	 * A time limit in minutes. If the clustering is not finished after
	 * m_timeLimit minutes, then it will be interrupted.
	 */
	private long m_timeLimit;
	/**
	 * The true clusters hidden in the data set. This is required for some
	 * metrics (RNIA, and CE).
	 */
	private ArrayList<Cluster> m_trueClusters;

	/**
	 * Evaluates a clusterer with the options given.
	 * 
	 * @param options
	 *            An array of strings containing options for clustering.
	 */
	public void evaluate(String[] options) {

		runClusterer();

		// // Set options for the Evaluator
		// setOptions(options);
		// // Set options for the clusterer
		// if (m_clusterer instanceof OptionHandler) {
		// ((OptionHandler) m_clusterer).setOptions(options);
		// }
		//
		// runClusterer();

	}

	/**
	 * Calculates all quality metrics specified in m_metrics on the clustering
	 * result. Returns the results as a StringBuffer.
	 * 
	 * @return The results of applying quality metrics to the clustering result.
	 */
	private StringBuffer getClusteringQuality() {
		StringBuffer results = new StringBuffer();
		ArrayList<Cluster> clusterList = null;

		if (m_metrics == null || m_metrics.size() == 0) {
			results.append("No metrics set.");
		}
		if (m_clusterer.getSubspaceClustering() == null) {
			clusterList = new ArrayList<Cluster>();
		} else {
			clusterList = (ArrayList<Cluster>) m_clusterer.getSubspaceClustering();
		}

		// calculate each quality metric
		for (ClusterQualityMeasure m : m_metrics) {
			m.calculateQuality(clusterList, m_dataSet, m_trueClusters);
		}

		// print values
		for (ClusterQualityMeasure m : m_metrics) {
			String val = "";

			val = m.getName() + "=\t";
			if (m.getOverallValue() != null) {
				if (m.getOverallValue().equals(Double.NaN)) {
					val += "undef";
				} else {
					val += String.valueOf(m.getOverallValue());
				}
			} else {
				val = "";
				if (m.getCustomOutput() != null) {
					val += m.getCustomOutput();
					val = val.replace('\n', '\t');
				} else {
					val += "null";
				}
			}
			if (val != "") {
				results.append(val + "\t");
			}
		}

		return results;
	}

	/**
	 * TODO: update the option string to include the options I have added. Make
	 * up the help string giving all the command line options
	 * 
	 * @param clusterer
	 *            the clusterer to include options for
	 * @return a string detailing the valid command line options
	 */
	private String makeOptionString(SubspaceClusterer clusterer) {
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

		optionsText.append("-outfile <output file>\n");
		optionsText.append("\tSpecifies a file path to append the results of the\n");
		optionsText.append("\tclustering. If a file is not specified, then output\n");
		optionsText.append("\tis written to stdout.\n");

		// Get scheme-specific options
		if (clusterer instanceof OptionHandler) {
			optionsText.append("\nOptions specific to " + clusterer.getClass().getName() + ":\n\n");
			@SuppressWarnings("unchecked")
			Enumeration<Option> enu = ((OptionHandler) clusterer).listOptions();

			while (enu.hasMoreElements()) {
				Option option = (Option) enu.nextElement();
				optionsText.append(option.synopsis() + '\n');
				optionsText.append(option.description() + "\n");
			}
		}

		return optionsText.toString();
	}

	private long milliseconds() {

		return (m_stoptTime - m_startTime) / 1000;
	}

	/**
	 * 
	 * @param inst
	 *            The set of instances to remove the class label from.
	 * @return A set of instances sans class label.
	 */
	private Instances removeClass(Instances inst) {
		Remove af = new Remove();
		Instances retI = null;

		try {
			if (inst.classIndex() < 0) {
				retI = inst;
			} else {
				af.setAttributeIndices("" + (inst.classIndex() + 1));
				af.setInvertSelection(false);
				af.setInputFormat(inst);
				retI = Filter.useFilter(inst, af);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return retI;
	}

	/**
	 * 
	 * @return Returns true if the clusterer finishes within m_timeLimit.
	 */

	private void runClusterer() {

		ExecutorService executor = Executors.newSingleThreadExecutor();
		Future<Void> future = executor.submit(new Task(m_clusterer, removeClass(m_dataSet)));

		try {
			System.out.println("Started..");
			future.get(3, TimeUnit.SECONDS);
			System.out.println("Finished!");
		} catch (TimeoutException e) {
			// This is not an error. This is our timeout.
			System.out.println("Timeout!");
		} catch (InterruptedException e) {

			System.err.println("InterruptedException!");
			e.printStackTrace();
		} catch (ExecutionException e) {
			System.err.println("InterruptedException!");
			e.printStackTrace();
		}
		executor.shutdownNow();

	}

	/**
	 * Sets the class using classString.
	 * 
	 * @param classString
	 * @throws Exception
	 */
	public void setClassAttribute(String classString) throws Exception {
		int theClass = 0;

		if (m_dataSet == null) {
			throw new Exception("Attempted to set class without first setting" + " a data set.");
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
				throw new Exception("Class is out of range!");

			if (!m_dataSet.attribute(theClass - 1).isNominal())
				throw new Exception("Class must be nominal!");

			m_dataSet.setClassIndex(theClass - 1);
		}
	}

	/**
	 * Set the clusterer using the class name.
	 * 
	 * @param clusterer
	 *            A subspace clusterer class name.
	 * @throws Exception
	 *             If clusterer is not a valid class name.
	 */
	public void setClusterer(String clusterer) throws Exception {
		m_clusterer = SubspaceClusterer.forName("weka.subspaceClusterer." + clusterer, null);
	}

	/**
	 * set the clusterer
	 * 
	 * @param clusterer
	 *            the clusterer to use
	 */
	public void setClusterer(SubspaceClusterer clusterer) {
		m_clusterer = clusterer;
	}

	/**
	 * Sets the data set to use in the clustering from a file name.
	 * 
	 * @param fileName
	 *            The name of an arff file containing data to cluster.
	 * @throws Exception
	 *             If there is a problem opening fileName or loading the data
	 *             set.
	 */
	public void setDataSet(String fileName) throws Exception {
		DataSource source = new DataSource(fileName);
		m_dataSet = source.getDataSet();
	}

	public void setExperiment(String m_experiment) {
		this.m_experiment = m_experiment;
	}

	/**
	 * Parses metricClassesString. Uses reflection to create metric classes and
	 * adds them to m_metrics.
	 * 
	 * @param metricClassesString
	 */
	public void setMetrics(String metricClassesString) {
		if (m_metrics == null) {
			m_metrics = new ArrayList<ClusterQualityMeasure>();
		}

		String[] classStrings = metricClassesString.split(":");

		for (int i = 0; i < classStrings.length; i++) {
			try {
				Class<?> c = Class.forName("weka.clusterquality." + classStrings[i]);
				m_metrics.add((ClusterQualityMeasure) c.newInstance());
			} catch (InstantiationException e1) {
				System.err.println("Not a valid subspace measure class: " + "weka.clusterquality." + classStrings[i]);
			} catch (IllegalAccessException e1) {
				System.err.println("Not a valid subspace measure class: " + "weka.clusterquality." + classStrings[i]);
			} catch (ClassNotFoundException e) {
				System.err.println("Not a valid subspace measure class: " + "weka.clusterquality." + classStrings[i]);
			}
		}
	}

	/**
	 * 
	 * @param options
	 * @throws Exception
	 */
	public void setOptions(String options[]) throws Exception {
		try {
			if (Utils.getFlag('h', options)) {
				throw new Exception("Help requested.");
			}

			String scName = Utils.getOption("sc", options);
			if (scName.length() == 0) {
				System.err.println("No algorithm specified. Using the default"
						+ " (SEPC). Specify an algorithm with -sc.");
			} else {
				this.setClusterer(scName);
			}

			String dataSetFileName = Utils.getOption('t', options);
			if (dataSetFileName.length() == 0) {
				throw new Exception("No input file, use -t");
			} else {
				setDataSet(dataSetFileName);
			}

			String measureOptionString = Utils.getOption('M', options);
			if (measureOptionString.length() == 0) {
				System.err.println("No metrics set. Use -M to specify quality metrics.");
			} else {
				setMetrics(measureOptionString);
			}

			String trueFileName = Utils.getOption('T', options);
			if (trueFileName.length() == 0) {
				System.err.println("No true cluster file set. Some metrics "
						+ "will not function without a true cluster file "
						+ "(CE and RNIA). Use -T to specify a true cluster file.");
			} else {
				setTrueClusters(trueFileName);
			}

			String classString = Utils.getOption('c', options);
			setClassAttribute(classString);

			String timeLimit = Utils.getOption("timelimit", options);
			if (timeLimit.length() > 0) {
				setTimeLimit(Long.parseLong(timeLimit));
			}

			String experimentName = Utils.getOption("exp", options);
			if (experimentName.length() == 0) {
				throw new Exception("No experiment name, use -exp");
			}
			setExperiment(experimentName);

		} catch (Exception e) {
			throw new Exception('\n' + e.getMessage() + makeOptionString(m_clusterer));
		}
	}

	public void setStartTime(long startTime) {
		this.m_startTime = startTime;
	}

	/**
	 * Set the time limit for clustering. The time limit is only modified if t
	 * is greater than zero.
	 * 
	 * @param t
	 *            A time in minutes.
	 */
	public void setTimeLimit(long t) {
		if (t > 0) {
			m_timeLimit = t;
		}
	}

	/**
	 * Sets the true clusters using the file referred to by fileName.
	 * 
	 * @param fileName
	 * @throws Exception
	 */
	public void setTrueClusters(String fileName) throws Exception {
		File trueClusterFile = new File(fileName);
		int numDims = m_dataSet.numAttributes() - 1; // class is one of the
														// attributes
		m_trueClusters = SubspaceClusterTools.getClusterList(trueClusterFile, numDims);
	}

	private void start() {
		m_startTime = System.nanoTime();
	}

	private void stop() {
		m_stoptTime = System.nanoTime();
	}

}// class