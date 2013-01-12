function myrun

  [dataName, attributeName, attributeType, data] = arffread('../data/S1500.arff');
  scrubbed_data = data(:, 1:end-1);
  
  %cluster(data, width, ktrials, discrim_set_size, beta, num_clusters, subspace_overlap, object_overlap)
  results = cluster(scrubbed_data, 50, 10000, 2, .9, 15, -1, -1);
  savecsv('results.txt', results, columns(scrubbed_data));
