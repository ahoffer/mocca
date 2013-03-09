package weka.subspaceClusterer;

import i9.subspace.base.ArffStorage;
import java.util.Enumeration;
import java.util.Vector;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.Utils;

public class Mocca extends SubspaceClusterer implements OptionHandler {
	private static final long serialVersionUID = 5624336775621682596L;

	public static void main(String[] argv) {
		runSubspaceClusterer(new Mocca(), argv);
	}

	private double alpha = 0.08;
	private double beta = 0.35;
	private double epsilon = 0.05;
	// Zero means "do not use PCA"
	private double gamma = 0.00;
	private double instanceOverlapThreshold = 0.50;

	private double subspaceOverlapThreshold = 0.20;

	private double width = 100.0;

	@Override
	public void buildSubspaceClusterer(Instances data) throws Exception {

		// Create an ARFF storage object from the Instances object
		// What is the advantage of that?
		ArffStorage arffstorage = new ArffStorage(data);

		// TODO
		// Call setSubscapceClusster(List<Cluster>)
		// setSubspaceClustering();

		// Print results
		toString();
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
		return "alpha=" + alpha + "; beta=" + beta + "; epsilon=" + epsilon
				+ "; subspace overlap threshold=" + subspaceOverlapThreshold
				+ "; instance overlap threshold=" + instanceOverlapThreshold
				+ "; width=" + width + "; gamma=" + gamma;
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
		vector.addElement(new Option("\talpha (default = 0.08)", "alpha", 1,
				"-a <double>"));
		vector.addElement(new Option("\tbeta (default = 0.35)", "beta", 1,
				"-b <double>"));
		vector.addElement(new Option("\tepsilon (default = 0.05)", "epsilon",
				1, "-e <double>"));
		vector.addElement(new Option(
				"\tsubspace overlap threshold (default = 0.90)",
				"subsapceOverlapThreshold", 1, "-s <double>"));
		vector.addElement(new Option(
				"\tinstance overlap threshold (default = 0.2)",
				"instanceOverlapThreshold", 1, "-i <double>"));
		vector.addElement(new Option("\twidth (default = 1.0)", "width", 1,
				"-w <double>"));
		vector.addElement(new Option("\tgamma (default = 0.00)", "gamma", 1,
				"-g <double>"));
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
		if (g > 0 && g < 1) {
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

	// TODO: Figure out how to use this feature
	// @Override
	// public TechnicalInformation getTechnicalInformation() {
	// TechnicalInformation info = new TechnicalInformation(Type.ARTICLE);
	//
	// info.
	//
	// // TODO Auto-generated method stub
	// return null;
	// }
}
