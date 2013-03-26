package weka.subspaceClusterer;

import java.util.Arrays;
import java.util.List;
import i9.subspace.base.Cluster;

public class MoccaCluster extends Cluster {

	private static final long serialVersionUID = 1L;
	double quality;
	int numCongregatingDims;

	/*-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----*/

	public MoccaCluster(boolean[] subspace, List<Integer> objects, int numCongregatingDims, double beta) {
		// Forgot to copy subspace vector and it caused strange results.
		// Currently I do not need to copy the objects parameter because the
		// list object is recreated on every iteration.

		super(Arrays.copyOf(subspace, subspace.length), objects);
		this.numCongregatingDims = numCongregatingDims;
		this.quality = quality(getCardinality(), numCongregatingDims, beta);
	}

	/*-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----*/

	@Override
	public String toStringWeka() {
		// TODO Auto-generated method stub
		return String.format("%,4.2f", quality) + " " + (super.toStringWeka());
	}// end method

	/*-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----*/

	/*
	 * Normalized overlap is defined as the number of points two clusters have
	 * in common, divided by the cardinality of the smaller cluster.
	 */
	public double getClusterOverlapScore(MoccaCluster otherCluster) {
		int overlap, smallerCardinality;
		double normalizedOverlap;

		overlap = MoccaUtils.intersection(m_objects, otherCluster.m_objects);
		smallerCardinality = Math.min(getCardinality(), otherCluster.getCardinality());
		normalizedOverlap = overlap / smallerCardinality;
		return normalizedOverlap;
	}

	public double getSubspaceOverlapScore(MoccaCluster otherCluster) {

		// PRECONDITION: Length of subspace arrays must be identical
		int overlap, smallerNumDims;
		double normalizedOverlap;

		overlap = 0;
		for (int i = 0; i < m_subspace.length; ++i) {
			if (m_subspace[i] && otherCluster.m_subspace[i]) {
				overlap++;
			}
		}// for

		smallerNumDims = Math.min(numCongregatingDims, otherCluster.numCongregatingDims);
		normalizedOverlap = overlap / smallerNumDims;

		return normalizedOverlap;

	}// method

	int getCardinality() {
		return m_objects.size();
	}

	/*-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----*/

	public static double quality(int cardinality, int numCongregatingDims, double beta) {

		return cardinality * Math.pow(1.0 / beta, numCongregatingDims);
	}

}// end class
