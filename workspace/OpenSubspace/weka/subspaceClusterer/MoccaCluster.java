package weka.subspaceClusterer;

import java.util.List;

import i9.subspace.base.Cluster;

public class MoccaCluster extends Cluster {

	private static final long serialVersionUID = 1L;
	double quality;

	public MoccaCluster(boolean[] subspace, List<Integer> objects, double quality) {
		super(subspace, objects);
		this.quality = quality;
	}

	@Override
	public String toStringWeka() {
		// TODO Auto-generated method stub
		return String.valueOf(quality) + " " + (super.toStringWeka());
	}// end method

}// end class
