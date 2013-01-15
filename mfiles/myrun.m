function myrun

  [dataName, attributeName, attributeType, data] = arffread('../data/S1500.arff');
  scrubbed_data = data(:, 1:end-1);
  
  ktrials = 5000;
  width = 15;
  discrim_set_size = 2;
  beta = 0.37;
  num_clusters = 12;
  max_subspace_overlap = 0.8;
  max_object_overlap = 0.8;
  
  results = cluster(scrubbed_data, width, ktrials, discrim_set_size, beta, num_clusters, max_subspace_overlap, max_object_overlap);
  
  savecsv('results.txt', results, columns(scrubbed_data));
