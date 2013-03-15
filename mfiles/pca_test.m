%matrix = [ 2.5, 2.4; 0.5, 0.7; 2.2, 2.9 ; 1.9, 2.2 ; 3.1, 3.0; 2.3, 2.7 ; 2, 1.6 ; 1, 1.1; 1.5, 1.6; 1.1, 0.9];

matrix = [9,0,8,2; 8,8,7,7; 10,9,4,0]





 %COEFF is a p-by-p matrix, each column containing coefficients for one 
 %principal component. The columns are in order of decreasing component variance

 %SCORE, the principal component scores; that is, the representation of X in the
 %principal component space. Rows of SCORE correspond to observations, columns
 %to components
 
 %LATENT, a vector containing the eigenvalues of the covariance matrix of X
[COEFF, Xlated_data, eigvals] = pca(matrix)