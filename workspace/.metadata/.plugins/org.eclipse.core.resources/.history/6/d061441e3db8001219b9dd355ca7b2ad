package weka.subspaceClusterer;

import Jama.EigenvalueDecomposition;
import Jama.Matrix;

public class Pca {
    /*-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----*/
    // Instance variables
    Matrix input;
    Matrix principleComponents;

    // Matrix rotation;
    /*-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----*/
    // Constructor
    public Pca(Matrix input) {
        this.input = input;
        eval();
    }

    public int getColumnDimension() {
        return principleComponents.getColumnDimension();
    }

    public int getRowDimension() {
        return principleComponents.getRowDimension();
    }

    /*-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----*/
    public Pca eval() {
        Matrix centered = MatrixUtils.center(input, MatrixUtils.columnMeans(input));
        Matrix covar = MatrixUtils.covariance(centered);
        // Rank tell us the maximum number of non-zero eigenvalues to expect.
        int covRank = covar.rank();
        // Calculate the eigenvectors and eigenvalues of the covariance
        EigenvalueDecomposition eigenDecomp = covar.eig();
        // Count number of principal components
        double[] eigenvalues = eigenDecomp.getRealEigenvalues();
        int numNonZeroEigenVals = 0;
        for (int i = 0; i < eigenvalues.length; ++i) {
            if (MoccaUtils.greaterThanZero(eigenvalues[i])) {
                numNonZeroEigenVals++;
            }// end if
        }// end for
        // Verify number of non zero eigenvalues is the same as the rank of
        // the covariance matrix
        if (numNonZeroEigenVals != covRank) {
            System.err.printf("Contrainst violation: nNumber of non-zeroSOMETHING WHACKY IN PCA");
        }
        /*
         * The principal components will be column vectors in the eigenvector matrix. The eigenvector matrix will always
         * be a (pxp) matrix, same as the covariance matrix. ***I think*** The principal components are listed in order
         * of increasing significance. This works well for my purposes because the least significant components are
         * orthogonal the dimension were the data congregates.
         */
        Matrix eigenvectors = eigenDecomp.getV();
        /*
         * The eigenvectors matrix can contain garbage. If the eigenvalue is smaller than some epsilon, it must be
         * considered zero and the corresponding eigenvector discarded.
         * 
         * getMatrix(Initial row index, Final row index, Initial column index, Final column index)
         */
        // Using the principal compmonents as a roation matrix is different than usuing the principal components
        // individually. To get a rotation matrix, do not truncate the matrix that contains the eigen vectors, otherwise
        // there will be fewer dimensions after rotation than before.
        //
        // int size = eigenvectors.getRowDimension();
        // int last = size - 1;
        // int firstCol = size - covRank;
        // principleComponents = eigenvectors.getMatrix(0, last, firstCol, last);
        principleComponents = eigenvectors;
        // Return this instance
        return this;
    }// end method

    /*-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----*/
    public Matrix rotate(Matrix input) {
        /*
         * Let D be the data matrix and T (transform) be the principle components.
         */
        // 1) D * T
        // By default, pcs are ordered from least to mostsignificant.
        return input.times(principleComponents);
        // 2) D * fliplr(T)
        // Re-order the pcs so most significant pc is the first column.
        // return input.times(MatrixUtils.fliplr(getRotationMatrix()));
    }// end method
}// end class
