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

	public static void main(String[] argv) {
		runSubspaceClusterer(new Mocca(), argv);
	}

	@Override
	public void buildSubspaceClusterer(Instances data) throws Exception {
		// NOTE: The class column has already been removed from the instances

		// Set instance variables
		this.dataAsInstances = data;
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

	public double getMaxiter() {
		return maxiter;
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
		ArrayList<String> options = new ArrayList<String>();
		options.add("-a");
		options.add("" + alpha);
		options.add("-b");
		options.add("" + beta);
		options.add("-e");
		options.add("" + epsilon);
		options.add("-s");
		options.add("" + subspaceOverlapThreshold);
		options.add("-i");
		options.add("" + instanceOverlapThreshold);
		options.add("-w");
		options.add("" + width);
		options.add("-g");
		options.add("" + gamma);
		options.add("-m");
		options.add("" + maxiter);

		return MoccaUtils.toArray(options);
	}

	@Override
	public String getParameterString() {
		return "alpha=" + alpha + "; beta=" + beta + "; epsilon=" + epsilon + "; subspace overlap threshold="
				+ subspaceOverlapThreshold + "; instance overlap threshold=" + instanceOverlapThreshold + "; width="
				+ width + "; gamma=" + gamma + "maxiter=" + maxiter + ";";
	}

	public double getSubspaceOverlapThreshold() {
		return subspaceOverlapThreshold;
	}

	public double getWidth() {
		return width;
	}

	public String globalInfo() {
		return "Monte Carlo Cluster Analysis (MOCCA)";
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
		vector.addElement(new Option("\tgamma (default = 0.00)", "gamma", 1, "-g <double>"));
		vector.addElement(new Option("\tmaximum iteration (default = 10000)", "maxiter", 1, "-m <integer>"));
		return vector.elements();
	}

	public void setAlpha(double alpha) {
		if (alpha > 0.0 && alpha < 1.0)
			this.alpha = alpha;
	}

	/*-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----*/

	public void setBeta(double beta) {
		if (beta > 0.0 && beta < 1.0)
			this.beta = beta;
	}

	/*-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----*/

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

	public void setMaxiter(int maxiter) {
		if (maxiter > 0)
			this.maxiter = maxiter;
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

	/*-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----*/

	/*
	 * Setter
	 */
	public void setSubspaceOverlapThreshold(double maxOverlap) {
		subspaceOverlapThreshold = maxOverlap;
	}

	/*-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----*/

	/*
	 * Setter
	 */
	public void setWidth(double w) {
		if (w > 0.0)
			this.width = w;
	}

	/*-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----*/

	private void add(MoccaCluster newCluster) {

		double subspaceOverlap, clusterOverlap;

		/*
		 * Modifying a list invalidates any iterator objects created from it. Do
		 * not remove objects while iterating over the list. Add them to a
		 * "remove items list" and remove them before exiting.
		 */
		List<Cluster> removeList = new ArrayList<Cluster>();

		for (Cluster otherCluster : clusters) {
			MoccaCluster other = (MoccaCluster) otherCluster;
			subspaceOverlap = newCluster.getSubspaceOverlapScore(other);
			clusterOverlap = newCluster.getClusterOverlapScore(other);
			if (subspaceOverlap > subspaceOverlapThreshold && clusterOverlap > instanceOverlapThreshold) {
				// Keep the cluster with the highest quality
				if (newCluster.quality > other.quality) {
					// Keep new cluster, remove other cluster
					removeList.add(otherCluster);
				} else {
					// Do not keep the new cluster.
					return;
				}
			}// if
		}// for

		clusters.removeAll(removeList);
		// There is no sufficiently similar cluster with higher quality.
		// Add this cluster.
		clusters.add(newCluster);

	}// method

	/*-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----*/

	private void doMocca() throws Exception {

		// DECLARE
		double[] upper, lower;
		boolean[] subspace;
		Shuffler shuffler;
		Matrix pointsToCluster, originalDataAsMatrix;
		int numCongregatingDims;
		ArrayList<Integer> pointIndexes;

		// ALLOCATE
		upper = new double[numDims];
		lower = new double[numDims];
		subspace = new boolean[numDims];
		shuffler = new Shuffler(numInstances, 1);

		// INITIALIZE
		originalDataAsMatrix = MatrixUtils.toMatrix(dataAsInstances);
		numCongregatingDims = 0;

		// The original data is only modified if PCA-assist is used. There is no
		// need to create a copy.
		pointsToCluster = originalDataAsMatrix;

		// LOOP
		for (int k = 0; k < numTrials; k++) {

			if (usePca()) {
				// Randomly select rotation set based on gamma
				int rotationIndexes[] = shuffler.next(rotationSetSize);

				// Get rotation objects
				Matrix roationObjs = MatrixUtils.getRowsByIndex(originalDataAsMatrix, rotationIndexes);

				// Find the principal components and rotate the data
				Pca pca = new Pca(roationObjs);
				pointsToCluster = pca.rotate(originalDataAsMatrix);
			}// end if

			// Randomly select discriminating set
			int discrimSetIndexes[] = shuffler.next(discrimSetSize);
			Matrix discrimPoints = MatrixUtils.getRowsByIndex(pointsToCluster, discrimSetIndexes);

			// Determine the subspace where the points congregate, if any
			numCongregatingDims = findSubspace(discrimPoints.getArray(), subspace, lower, upper);
			if (numCongregatingDims > 0) {

				// Determine which points are in the cluster
				pointIndexes = findCongregatingPoints(pointsToCluster, subspace, lower, upper);

				// Create cluster object.
				MoccaCluster newCluster = new MoccaCluster(subspace, pointIndexes, numCongregatingDims, beta);

				// Add cluster if it meets certain criteria
				add(newCluster);

			}// if
		}// end k trials loop
	}// end method

	/*-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----*/

	private ArrayList<Integer> findCongregatingPoints(Matrix pointsToCluster, boolean[] subspace, double[] lowerBounds,
			double[] upperBounds) {

		ArrayList<Integer> pointsIndexes = new ArrayList<Integer>();
		double[][] points = pointsToCluster.getArray();

		congregate: for (int i = 0; i < numInstances; ++i) {

			// Get the actual point
			double[] point = points[i];

			// Check to see if the point is in the cluster
			for (int j = 0; j < numDims; ++j) {

				/*
				 * Only check bounds if the cluster congregates in this
				 * dimension. We don't care about dimensions that are not part
				 * of the subspace
				 */
				if (subspace[j]) {
					if (point[j] > upperBounds[j] || point[j] < lowerBounds[j]) {
						/*
						 * Point is not inside the hyper volume for congregating
						 * dimension j. Therefore the point is not part of the
						 * cluster.
						 * 
						 * Return to top of loop to examine next object in the
						 * data set.
						 */
						continue congregate;
					}// end if for bounds check
				}// end if for subspace check
			}// end for

			// The point is part of the cluster
			pointsIndexes.add(Integer.valueOf(i));

		}// end for
		return pointsIndexes;
	}// method

	/*-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----*/

	/*
	 * Return true if the discriminating points congregate in at least one
	 * dimension (i.. a cluster is found). Otherwise return false. Populate the
	 * parameters subspace, lower and upper if a cluster is found. If a cluster
	 * is not found, the values of those parameters is undefined.
	 */
	private int findSubspace(double[][] discrimObjs, boolean[] subspace, double[] lower, double[] upper) {

		int numCongregatingDims;
		double sheath;

		/*
		 * Create max and min values in each dimension from the discriminating
		 * set. Find the subspace and number of congregating dimensions
		 */
		double[] minimums = MoccaUtils.min(discrimObjs);
		double[] maximums = MoccaUtils.max(discrimObjs);
		double lengths[] = MoccaUtils.subtract(maximums, minimums);

		// subspace = MatrixUtils.lessThanOrEqualTo(lengths, width);

		// DEBUG CODE
		temp = MoccaUtils.lessThanOrEqualTo(lengths, width);
		for (int i = 0; i < temp.length; ++i) {
			subspace[i] = temp[i];
		}

		numCongregatingDims = MoccaUtils.countTrueValues(subspace);

		/*
		 * If the entire subspace is zero, it means the discriminating set does
		 * not congregate in any dimension. The trial has failed to find a
		 * cluster
		 */
		if (numCongregatingDims > 0) {

			/*
			 * Calculate upper and lower bounds of the hyper volume that
			 * surrounds the cluster.
			 */

			for (int i = 0; i < numDims; ++i) {
				sheath = width - lengths[i];
				lower[i] = minimums[i] - sheath;
				upper[i] = maximums[i] + sheath;
			}

		}// if
		return numCongregatingDims;
	}// method

	/*-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----*/

	private boolean gammaIsValid() {
		/*
		 * The rotation set size must equal to or greater than the rotation set
		 * size because the discriminating set is sampled (without replacement)
		 * from the rotation set. If the algorithm is not using PCA, then the
		 * value of gamma is relevant, and therefore always valid.
		 */
		return !usePca() || (rotationSetSize >= discrimSetSize && gamma <= 1);
	}

	private boolean usePca() {
		// If gamma is greater than zero, use PCA.
		return gamma > 0;
	}

	// DEBUG
	boolean[] temp;

	private static final long serialVersionUID = 5624336775621682596L;
	private double alpha = 0.08;
	private double beta = 0.35;
	private double epsilon = 0.05;
	private double gamma = 0.00; // Zero means "do not use PCA"
	private double instanceOverlapThreshold = 0.50;
	private int maxiter = 1000;
	private double subspaceOverlapThreshold = 0.20;
	private double width = 100.0;
	List<Cluster> clusters = new ArrayList<Cluster>();
	Instances dataAsInstances;
	int discrimSetSize;
	int minNumInstances;
	int numDims;
	int numInstances;
	int numTrials;
	int rotationSetSize;

} // end class
