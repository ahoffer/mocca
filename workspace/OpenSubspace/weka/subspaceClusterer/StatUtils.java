package weka.subspaceClusterer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import Jama.Matrix;

public class StatUtils {

	public static double one = 0.99999;

	// Return the largest value from each column
	public static Matrix max(Matrix input) {
		int cols = input.getColumnDimension();
		Matrix maxs = new Matrix(1, cols);
		double largest, val;
		for (int j = 0; j < cols; ++j) {
			largest = Double.NEGATIVE_INFINITY;
			for (int i = 0; i < input.getRowDimension(); ++i) {
				val = input.get(i, j);
				if (val > largest) {
					largest = val;
				}// end if
				maxs.set(0, j, largest);
			}// end for
		}// end for
		return maxs;
	}// end method

	// Return the largest value from each column
	public static Matrix min(Matrix input) {
		int cols = input.getColumnDimension();
		Matrix mins = new Matrix(1, cols);
		double smallest, val;
		for (int j = 0; j < cols; ++j) {
			smallest = Double.POSITIVE_INFINITY;
			for (int i = 0; i < input.getRowDimension(); ++i) {
				val = input.get(i, j);
				if (val < smallest) {
					smallest = val;
				}// end if
				mins.set(0, j, smallest);
			}// end for
		}// end for
		return mins;
	}// end method

	Random random;

	// Set loop invariants
	public StatUtils(int seed) throws Exception {
		random = new Random(seed);
	}

	public int[] select(int[] input, int size) {
		return Arrays.copyOfRange(input, 0, size);
	}

	public int[] shuffle(int[] array) {
		// TODO: I'm too lazy to code up a Fisher-Yates shuffle. It would be
		// good practice, though. Then I could jump the java collection classes.
		//
		ArrayList<Integer> listCopy = toList(array);

		// Shuffle the list
		Collections.shuffle(listCopy);

		return toArray(listCopy);

	}// end method

	public int[] toArray(List<Integer> input) {

		int size = input.size();
		int array[] = new int[size];
		for (int i = 0; i < size; ++i) {
			array[i] = input.get(i).intValue();
		}// end for

		return array;

	}// end method

	public ArrayList<Integer> toList(int input[]) {
		ArrayList<Integer> list = new ArrayList<Integer>();
		for (int value : input) {
			list.add(value);
		}
		return list;
	}

	public int[] sampleNoReplacment(int input[], int sampleSize) {
		int[] shuffled = shuffle(input);
		return select(shuffled, sampleSize);
	}// end method

	// TODO: In what class does this method belong?
	// Return true if the object is inside the min/max bounds
	public boolean inside(double object[], double minimum[], double maximum[]) {
		for (int i = 0; i < object.length; ++i) {
			if (object[i] > maximum[i] || object[i] < minimum[i]) {
				return false;
			}// end if
		}// end for
		return true;
	}// end method

}// end class