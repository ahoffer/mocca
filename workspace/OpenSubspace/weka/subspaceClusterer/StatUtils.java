package weka.subspaceClusterer;

import java.util.Random;

import weka.core.Instances;
import weka.filters.Filter;

public class StatUtils {

	// Instance variables
	weka.filters.unsupervised.instance.Resample sampler;
	Random random;

	public static double one = 0.99999;

	// Set loop invariants
	public StatUtils(int seed, Instances data) throws Exception {
		sampler = new weka.filters.unsupervised.instance.Resample();
		sampler.setRandomSeed(seed); // Make successive runs repeatable.
		sampler.setInputFormat(data);
		sampler.setNoReplacement(true);
		random = new Random(seed);
	}

	public Instances subSampleAmount(Instances dataSet, int num) {
		// Damn you Weka. You win this round.
		Instances shuffleCopy = new Instances(dataSet);
		shuffleCopy.randomize(random);
		return new Instances(dataSet, 0, num);
	} // end

	public Instances subSamplePercentage(Instances dataSet, double percentage)
			throws Exception {

		if (percentage >= one) {
			return new Instances(dataSet);
		}

		sampler.setSampleSizePercent(percentage);
		return Filter.useFilter(dataSet, sampler);
	}// end method

}// end class
