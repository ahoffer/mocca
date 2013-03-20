package weka.subspaceClusterer;

import weka.core.Instance;
import weka.core.Instances;
import Jama.Matrix;

public class MatrixUtils {

	// Pick a small values that means zero.
	public static double epsilon = 1E-8;

	public static Matrix center(Matrix input, Matrix columnMeans) {
		int rows = input.getRowDimension();
		int cols = input.getColumnDimension();
		Matrix mat = new Matrix(rows, cols);
		double val, mean;
		for (int i = 0; i < rows; ++i) {
			for (int j = 0; j < cols; ++j) {
				val = input.get(i, j);
				mean = columnMeans.get(0, j);
				mat.set(i, j, val - mean);
			}// end for
		}// end for
		return mat;
	}// end method

	public static Matrix columnMeans(Matrix input) {

		int cols = input.getColumnDimension();
		int rows = input.getRowDimension();
		Matrix rowVector = new Matrix(1, cols);
		double sum;
		for (int j = 0; j < cols; ++j) {
			sum = 0;
			for (int i = 0; i < rows; ++i) {
				sum += input.get(i, j);
			}// end for
			rowVector.set(0, j, sum / rows);
		}// end for
		return rowVector;
	}// end method

	public static Matrix covariance(Matrix input) {
		// PRECONDITION: MATRIX MUST BE MEAN-CENTERED
		// Allocate the covariance matrix
		int rows = input.getRowDimension();
		int cols = input.getColumnDimension();
		Matrix covMatrix = new Matrix(cols, cols);

		// Compute the covariance matrix
		double covar_ij;
		Matrix colVec1, colVec2;
		for (int i = 0; i < cols; ++i) {
			for (int j = i; j < cols; ++j) {

				colVec1 = input.getMatrix(0, rows - 1, i, i);
				colVec2 = input.getMatrix(0, rows - 1, j, j);
				covar_ij = dotProduct(colVec1, colVec2) / (rows - 1);
				covMatrix.set(i, j, covar_ij);
				covMatrix.set(j, i, covar_ij);
			}// end for
		}// end for

		return covMatrix;
	}// end method

	public static double dotProduct(Matrix colVec1, Matrix colVec2) {
		double sum = 0;
		int rows1 = colVec1.getRowDimension();
		int rows2 = colVec2.getRowDimension();
		if (rows1 != rows2) {
			System.err.println("Dimension error in Pca.dotProduct.");
		}// end if

		for (int i = 0; i < rows1; ++i) {
			sum += colVec1.get(i, 0) * colVec2.get(i, 0);
		}// end for

		return sum;
	}// end method

	public static Instances toInstances(Instances template, Matrix values) {
		double array[][] = values.getArray();
		weka.core.Instances output = new Instances(template, template.numInstances());

		for (int i = 0; i < values.getRowDimension(); ++i) {
			for (int j = 0; j < values.getColumnDimension(); ++j) {
				output.instance(i).setValue(j, array[i][j]);
			}// end for
		}// end for

		return output;
	}// end method

	public static Matrix toMatrix(Instances input) {
		int cols = input.numAttributes();
		int rows = input.numInstances();
		Matrix mat = new Matrix(rows, cols);
		for (int i = 0; i < rows; ++i) {
			Instance inst = input.instance(i);
			for (int j = 0; j < cols; ++j) {
				mat.set(i, j, inst.value(j));
			}// end for
		}// end for
		return mat;
	}// end method

	public static boolean[] lessThanOrEqualTo(double[] input, double value) {
		int length = input.length;
		boolean result[] = new boolean[length];
		for (int i = 0; i < length; ++i) {
			result[i] = (input[i] <= value) ? true : false;
		}
		return result;
	}

	public static boolean[] greaterThanOrEqualTo(double[] input, double value) {
		int length = input.length;
		boolean result[] = new boolean[length];
		for (int i = 0; i < length; ++i) {
			result[i] = (input[i] >= value) ? true : false;
		}
		return result;
	}

	public static int countTrueValues(boolean[] input) {
		int count = 0;
		for (boolean b : input) {
			if (b) {
				count++;
			}// end if
		}// end for
		return count;
	}// end method

	public static int[] getSequence(int numberOfElements) {

		// Return a sequence that ranges from 0 to numberofElements-1
		int sequence[] = new int[numberOfElements];

		for (int i = 0; i < numberOfElements; ++i) {
			sequence[i] = i;
		}// end for

		return sequence;

	}// end method

	/*
	 * Return a sub-matrix given the rows indexes as input
	 */
	public static Matrix getRowsByIndex(Matrix input, int[] indexes) {
		int cols = input.getColumnDimension();
		return input.getMatrix(indexes, 0, cols - 1);

	}// end method

}// end class