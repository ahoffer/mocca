package weka.subspaceClusterer;

import i9.subspace.base.Cluster;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.Utils;
import Jama.Matrix;

public class Mocca extends SubspaceClusterer implements OptionHandler {
	private static final long serialVersionUID = 5624336775621682596L;

	public static void main(String[] argv) {
		runSubspaceClusterer(new Mocca(), argv);
	}

	/********* ALGORITHM PARAMETERS ************/
	private double alpha = 0.08;
	private double beta = 0.35;
	/********** LOOP VARIANT STATE **************/
	ArrayList<Cluster> clusters = new ArrayList<Cluster>();
	/********* LOOP INVARIANTS *************/
	Instances dataAsInstances;
	int discrimSetSize;
	private double epsilon = 0.05;
	private double gamma = 0.00; // Zero means "do not use PCA"

	private double instanceOverlapThreshold = 0.50;
	int minNumInstances;
	int numDims;
	int numInstances;
	int numTrials;
	int rotationSetSize;
	private StatUtils stats;
	private double subspaceOverlapThreshold = 0.20;

	private double width = 100.0;

	@Override
	public void buildSubspaceClusterer(Instances data) throws Exception {
		// NOTE: The class column has already been removed from the instances

		// Set instance variables
		this.dataAsInstances = data;
		stats = new StatUtils(1);
		numDims = data.numAttributes();
		numInstances = data.numInstances();
		numTrials = calcNumTrials();
		minNumInstances = Utils.round(alpha * numInstances);
		discrimSetSize = calcDiscrimSetSize();
		rotationSetSize = (int) Math.round(gamma * numInstances);

		// gammaIsValid SHOULD ONLY BE CALLED AFTER xxxSetSize variables are
		// initialized
		if (!gammaIsValid()) {
			throw new Exception("Gamma is invalid.");
		}

		doMocca();
		setSubspaceClustering(clusters);

		// Print results
		toString();
	}

	public int calcDiscrimSetSize() {
		double s_est = Math.log10(numDims / Math.log(4)) / Math.log10(1 / beta);
		int temp = Utils.round(s_est);
		// Need at least two discriminating points to find a cluster
		return Math.max(2, temp);
	}

	public int calcNumTrials() {
		double d = numDims;
		double ln4 = Math.log(4);
		double log10alpha = Math.log10(alpha);
		double log10beta = Math.log10(beta);

		double est = 1 + 4 / alpha * Math.pow(d / ln4, log10alpha / log10beta) * Math.log(1 / epsilon);
		return Utils.round(est);
	}

	private void doMocca() throws Exception {
		Matrix originalDataAsMatrix = MatrixUtils.toMatrix(dataAsInstances);
		Matrix dataToCluster;
		double[] upper = new double[numDims];
		double[] lower = new double[numDims];

		for (int k = 0; k < numTrials; k++) {

			int indexes[] = MatrixUtils.getSequence(numInstances);

			if (usePca()) {
				// Randomly select rotation set based on gamma
				int rotationIndexes[] = stats.sampleNoReplacment(indexes, rotationSetSize);

				// Get rotation objects
				Matrix roationObjs = MatrixUtils.getRowsByIndex(originalDataAsMatrix, rotationIndexes);

				// Find the principal components and rotate the data
				Pca pca = new Pca(roationObjs);
				dataToCluster = pca.rotate(originalDataAsMatrix);

				/*
				 * TODO: From this point on there is no need to use the Matrix
				 * class. Really, there is no need to use the Matrix class after
				 * PCA is complete. I bet I could hide all references to Jama
				 * matrices inside the MatUtils class and use Java native arrays
				 * for everything else.
				 */

			}// end if

			else {
				dataToCluster = originalDataAsMatrix.copy();
			}

			// Randomly select discriminating set
			// TODO: This is really inefficient. We only need a handful of
			// points but we are reshuffling the entire list of points.
			int discrimSetIndexes[] = stats.sampleNoReplacment(indexes, discrimSetSize);
			Matrix discrimObjs = MatrixUtils.getRowsByIndex(dataToCluster, discrimSetIndexes);

			// Create max and min values in each dimension from the
			// discriminating set.
			Matrix mins = StatUtils.min(discrimObjs);
			Matrix maxs = StatUtils.max(discrimObjs);

			// Find the subspace and number of congregating dimensions
			Matrix bounds = maxs.minus(mins);
			double boundsAsArray[] = bounds.getArray()[0];
			boolean subspace[] = MatrixUtils.lessThanOrEqualTo(boundsAsArray, width);
			int numCongregatingDims = MatrixUtils.countTrueValues(subspace);

			/*
			 * If the entire subspace is zero, it means the discriminating set
			 * does not congregate in any dimension. The trial has failed to
			 * find a cluster
			 */
			if (numCongregatingDims == 0) {
				// Return to top of loop to try again.
				break;
			}

			/*
			 * Calculate upper and lower bounds of the hyper volume that
			 * surrounds the cluster.
			 */
			double[] minimums = mins.getArray()[0];
			double[] maximums = maxs.getArray()[0];
			double sheath;
			for (int i = 0; i < numDims; ++i) {
				sheath = width - boundsAsArray[i];
				lower[i] = minimums[i] - sheath;
				upper[i] = maximums[i] + sheath;
			}

			/*
			 * Find congregating points
			 */
			ArrayList<Integer> objectIndexes = new ArrayList<Integer>(1000);
			double[][] objects = dataToCluster.getArray();
			for (int i = 0; i < numInstances; ++i) {
				double[] object = objects[i];
				// Only check bounds if the cluster congregates in this
				// dimension
				if (subspace[i]) {
					if (stats.inside(object, lower, upper)) {
						objectIndexes.add(Integer.valueOf(i));
					}// end if
				}// end if
			}// end for

			if (objectIndexes.isEmpty()) {
				// BAD!
				System.err.println("EMPTY CLUSTER!");
			}

			clusters.add(new Cluster(subspace, objectIndexes));

		}// end k trials loop
	}// end method

	private boolean gammaIsValid() {
		/*
		 * The rotation set size must equal to or greater than the rotation set
		 * size because the discriminating set is sampled (without replacement)
		 * from the rotation set. If the algorithm is not using PCA, then the
		 * value of gamma is relevant, and therefore always valid.
		 */
		return !usePca() || (rotationSetSize >= discrimSetSize && gamma <= 1);
	}

	public double getAlpha() {
		return alpha;
	}

	public double getBeta() {
		return beta;
	}

	public double getEpsilon() {
		return epsilon;
	}

	public double getGamma() {
		return gamma;
	}

	public double getInstanceOverlapThreshold() {
		return instanceOverlapThreshold;
	}

	@Override
	public String getName() {
		return "MOCCA";
	}

	/**
	 * Gets the current option settings for the OptionHandler.
	 * 
	 * @return String[] The list of current option settings as an array of
	 *         strings
	 */
	public String[] getOptions() {
		String[] options = new String[14]; // = 2 * the number of arguments
		int current = 0;
		options[current++] = "-a";
		options[current++] = "" + alpha;
		options[current++] = "-b";
		options[current++] = "" + beta;
		options[current++] = "-e";
		options[current++] = "" + epsilon;
		options[current++] = "-s";
		options[current++] = "" + subspaceOverlapThreshold;
		options[current++] = "-i";
		options[current++] = "" + instanceOverlapThreshold;
		options[current++] = "-w";
		options[current++] = "" + width;
		options[current++] = "-g";
		options[current++] = "" + gamma;
		return options;
	}

	@Override
	public String getParameterString() {
		return "alpha=" + alpha + "; beta=" + beta + "; epsilon=" + epsilon + "; subspace overlap threshold="
				+ subspaceOverlapThreshold + "; instance overlap threshold=" + instanceOverlapThreshold + "; width="
				+ width + "; gamma=" + gamma;
	}

	public double getSubspaceOverlapThreshold() {
		return subspaceOverlapThreshold;
	}

	public double getWidth() {
		return width;
	}

	public String globalInfo() {
		return "Monte Carlo Cluster Analysis (MOCCA): A Monte "
				+ "Carlo algorithm that performs sames a few points and determines the dimensions in which the congregate. "
				+ "The congregating dimeions are defined at the dimensions in which the points are not more farther apart "
				+ "than some arbitrary width. If the points cluster in any dimensions, the rest of the datat set is searched"
				+ "to find other instances which also congregate in the found suspace.";
	}

	/**
	 * Returns an enumeration of all the available options.
	 * 
	 * @return Enumeration An enumeration of all available options.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Enumeration listOptions() {
		Vector vector = new Vector();

		// Option(description, name, numArguments, synopsis)
		vector.addElement(new Option("\talpha (default = 0.08)", "alpha", 1, "-a <double>"));
		vector.addElement(new Option("\tbeta (default = 0.35)", "beta", 1, "-b <double>"));
		vector.addElement(new Option("\tepsilon (default = 0.05)", "epsilon", 1, "-e <double>"));
		vector.addElement(new Option("\tsubspace overlap threshold (default = 0.90)", "subsapceOverlapThreshold", 1,
				"-s <double>"));
		vector.addElement(new Option("\tinstance overlap threshold (default = 0.2)", "instanceOverlapThreshold", 1,
				"-i <double>"));
		vector.addElement(new Option("\twidth (default = 1.0)", "width", 1, "-w <double>"));
		vector.addElement(new Option("\tgamma )(default = 0.00)", "gamma", 1, "-g <double>"));
		return vector.elements();
	}

	public void setAlpha(double alpha) {
		if (alpha > 0.0 && alpha < 1.0)
			this.alpha = alpha;
	}

	public void setBeta(double beta) {
		if (beta > 0.0 && beta < 1.0)
			this.beta = beta;
	}

	public void setEpsilon(double epsilon) {
		if (epsilon > 0.0 && epsilon < 1.0)
			this.epsilon = epsilon;
	}

	public void setGamma(double g) {
		if (g >= 0.0 && g <= 1.0) {
			gamma = g;
		}
	}

	public void setInstanceOverlapThreshold(double maxOverlap) {
		if (maxOverlap > 0.0)
			subspaceOverlapThreshold = maxOverlap;
	}

	public void setOptions(String[] options) throws Exception {

		String optionString = Utils.getOption("a", options);
		if (optionString.length() != 0) {
			setAlpha(Double.parseDouble(optionString));
		}

		optionString = Utils.getOption("b", options);
		if (optionString.length() != 0) {
			setBeta(Double.parseDouble(optionString));
		}

		optionString = Utils.getOption("e", options);
		if (optionString.length() != 0) {
			setEpsilon(Double.parseDouble(optionString));
		}

		optionString = Utils.getOption("s", options);
		if (optionString.length() != 0) {
			setSubspaceOverlapThreshold(Double.parseDouble(optionString));
		}

		optionString = Utils.getOption("i", options);
		if (optionString.length() != 0) {
			setInstanceOverlapThreshold(Double.parseDouble(optionString));
		}

		optionString = Utils.getOption("w", options);
		if (optionString.length() != 0) {
			setWidth(Double.parseDouble(optionString));
		}

		optionString = Utils.getOption("g", options);
		if (optionString.length() != 0) {
			setGamma(Double.parseDouble(optionString));
		}
	}

	public void setSubspaceOverlapThreshold(double maxOverlap) {
		subspaceOverlapThreshold = maxOverlap;
	}

	public void setWidth(double w) {
		if (w > 0.0)
			this.width = w;
	}

	private boolean usePca() {
		// If gamma is greater than zero, use PCA.
		return gamma > 0;
	}

} // end class
