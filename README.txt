MOCCA
====
MOCCA is short for Monte Carlo Clustering Algorithm. It is an evolution of the DOC clustering algorithm. My work for is to incorporate principle component analysis into the algorithm to detect correlated clusters. This implmenetation is built on top of Opensubspace, which in turn is built on top of Weka.

DIRECTORIES
===========
data - data sets, and expected results
mfiles - Matlab/Octave files
misc - files not easily categoried
workspace - eclipse workspace


SNIPPETS
========
Example of making it run on windows:

  java.exe -classpath "C:\Users\ahoffer\Documents\GitHub\sepc\workspace\OpenSubspace;C:\Users\ahoffer\Documents\GitHub\sepc\workspace\OpenSubspace\lib\*" weka.subspaceClusterer.Mocca -m 10000 -w 01 -i 0.3 -s 0.95 -b 0.35 -g 0 -M F1Measure:Accuracy -t breast.arff -c last

--Notice that the directory which contains the jars ("OpenSubspace\lib\*") ends in an asterisk to indicate all the jar files should be included.
--The .class files are in the directory "OpenSubspace".
--Presumes the file breast.arff resides in the curerent directory.

Here is a version run out of the eclipse project directory with relative paths:

java -cp ".;.\lib\*" weka.subspaceClusterer.Mocca -m 5000 -w 1 -i 0.3 -b 0.35 -g 0.2 -t breast.arff -c last


To run the evaluationa afrom the Eclipse project directory:

java -cp ".:lib/*" weka.subspaceClusterer.MySubspaceClusterEvaluation -sc Mocca -t "../../data/breast.arff" -c last -g 0 -w 0.5 -i 0.8 -s 0.95 -maxiter 1000 -a 0.1 -e 0.5 -b 0.35 -label 1 -M F1Measure:Accuracy -path "."