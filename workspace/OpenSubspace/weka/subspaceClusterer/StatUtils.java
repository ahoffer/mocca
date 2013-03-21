package weka.subspaceClusterer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
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

	public int[] intersection(int[] list1, int[] list2) {
		// PRECONDITION: Both lists are sorted lowest to highest.
		ArrayList<Integer> results;
		int[] toSearch, toIterate;
		int size1, size2;
		size1 = list1.length;
		size2 = list2.length;

		// Iterate over the shortest O(n)
		// Search over the longest O(log n)
		if (size1 > size2) {
			toSearch = list1;
			toIterate = list2;
			results = new ArrayList<Integer>(size1);
		} else {
			toSearch = list2;
			toIterate = list1;
			results = new ArrayList<Integer>(size2);
		}

		for (int each : toIterate) {
			if (binarySearch(toSearch, each)) {
				results.add(Integer.valueOf(each));
			}
		}
		return toArray(results);
	}// method

	public boolean binarySearch(int[] sortedList, int target) {

		int high = sortedList.length - 1;
		int low = 0;
		int currentVal, mid;

		while (high >= low) {
			mid = (low + high) / 2;
			currentVal = sortedList[mid];
			if (target == currentVal) {
				return true;
			}

			if (currentVal > target) {
				// Too big. Look smaller.
				high = mid - 1;
			} else {
				// Too small. Look bigger.
				low = mid + 1;
			}
		}// end while

		return false;

	}// end method

	public int intersection(List<Integer> list1, List<Integer> list2) {
		// Iterate over the shortest O(n)
		// Create hash map of the longest
		int size1, size2, count;
		List<Integer> toIterate;
		HashSet<Integer> hash;

		size1 = list1.size();
		size2 = list2.size();
		count = 0;

		if (size1 > size2) {
			hash = new HashSet<Integer>(list1);
			toIterate = list2;

		} else {
			hash = new HashSet<Integer>(list2);
			toIterate = list1;
		}

		for (Integer each : toIterate) {
			if (hash.contains(each)) {
				count++;
			}// end if
		}// end for
		return count;
	}// end method

}// end class
