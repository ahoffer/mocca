function myrun
%fname = '../data/DB1500.arff';
fname = '../data/N30.arff';

  [dataName, attributeName, attributeType, data] = arffread(fname);
  scrubbed_data = data(:, 1:end-1);
  %scrubbed_data = data(:, 1:9);

  k = 10000;
  width = 10;
  alpha = 0.1;
  beta = 0.3;
  num_clusters = 5;
  max_subspace_overlap = .9;
  max_object_overlap = 0.8;
  epsilon = 0.05;
  num_dims = columns(scrubbed_data);
  
  k = ktrials(epsilon, alpha, beta, num_dims)
  s = s_est(num_dims, beta)
  
  % discrim_set = scrubbed_data(10:12, :);
  % [subspace, mycluster]=trial(scrubbed_data(1:75, :), width, discrim_set);

  results = cluster(scrubbed_data, width, k, s, alpha, beta, num_clusters, max_subspace_overlap, max_object_overlap);
  %savecsv('results.txt', results, num_dims);
  
  results.cardinality
  results.quality
  

  
