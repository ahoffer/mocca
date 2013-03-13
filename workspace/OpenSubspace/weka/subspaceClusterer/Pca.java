package weka.subspaceClusterer;

import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Center;
import weka.core.Instance;
import weka.core.Instances;
import Jama.EigenvalueDecomposition;
import Jama.Matrix;

public class Pca {

	// Instances removeClassAttribute(Instances input) throws Exception {
	//
	// int classIdx = input.classIndex();
	// if (classIdx >= 0) {
	// // get rid of the class column
	//
	// int[] todelete = new int[1];
	// todelete[0] = classIdx;
	// Remove removeFilter = new Remove();
	// removeFilter.setAttributeIndicesArray(todelete);
	// removeFilter.setInvertSelection(false);
	// removeFilter.setInputFormat(input);
	// return Filter.useFilter(input, removeFilter);
	// } else {
	// return input;
	// }
	// }// end method

	// Instance variables

	// Constructor
	private Pca() {

	}

	// public PCA(double[][] input) {
	// means = new double[input[0].length];
	// double[][] cov = getCovariance(input, means);
	// covMatrix = new Matrix(cov);
	// eigenstuff = covMatrix.eig();
	// eigenvalues = eigenstuff.getRealEigenvalues();
	// eigenvectors = eigenstuff.getV();
	// double[][] vecs = eigenvectors.getArray();
	// int numComponents = eigenvectors.getColumnDimension(); // same as num
	// // rows.
	// principleComponents = new TreeSet<PrincipleComponent>();
	// for (int i = 0; i < numComponents; i++) {
	// double[] eigenvector = new double[numComponents];
	// for (int j = 0; j < numComponents; j++) {
	// eigenvector[j] = vecs[i][j];
	// }
	// principleComponents.add(new PrincipleComponent(eigenvalues[i],
	// eigenvector));
	// }
	// }// end method

	public static Instances center(Instances input) throws Exception {
		Center filter = new Center();
		filter.setInputFormat(input);
		return Filter.useFilter(input, filter);
	}

	public static Matrix covariance(Instances input) throws Exception {
		// Allocate the covariance matrix
		// PRECONDITION: There is no class column in the input
		int dims = input.numAttributes();
		int numInst = input.numInstances();
		Matrix covMatrix = new Matrix(dims, dims);

		// Center the data by subtracting the column means
		Instances centered = center(input);

		// now compute the covariance matrix
		for (int i = 0; i < dims; i++) {
			for (int j = i; j < dims; j++) {
				double cov_ij, sum = 0;
				for (int k = 0; k < numInst; k++) {
					Instance inst = centered.instance(k);
					sum += inst.value(i) * inst.value(j);
				}// end for k
				cov_ij = sum / (double) (numInst - 1);
				covMatrix.set(i, j, cov_ij);
				covMatrix.set(j, i, cov_ij);
			}// end for j
		}// end for k
		return covMatrix;
	}// end method

	public static Matrix principalComponents(Instances input) throws Exception {
		Matrix cov = covariance(input);
		int covRank = cov.rank(); // Rank should tell us how many non-zero
									// eigenvalues we can expect.
		EigenvalueDecomposition eigs = cov.eig();

		// Count number of principal components
		double[] eigenvalues = eigs.getRealEigenvalues();
		int numNonZeroEigenVals = 0;
		for (int i = 0; i < eigenvalues.length; ++i) {
			if (eigenvalues[i] > 1e-12) {
				numNonZeroEigenVals++;
			}
		}

		if (numNonZeroEigenVals != covRank) {
			System.out.println("SOMETHING WHACKY IN PCA");
		}

		Matrix eigenvectors = eigs.getV();

		// Strip out eigvenvectors whose corresponding eigenvalues are zero.
		int size = eigenvectors.getRowDimension();
		if (size != eigenvectors.getColumnDimension()) {
			System.out.println("EIGEN VECTORS SHOULD BE SQUARE MATRIX");
		}

		Matrix pc = eigenvectors.getMatrix(0, size - 1, size - covRank,
				size - 1);
		return pc;
	}// end method

}// end class