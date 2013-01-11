function myrun2

  [dataName, attributeName, attributeType, data] = arffread('S1500.arff');
  scrubbed_data = data(:,1:end-1);

  max_clusters = 10;
  exclusive = false;
  dims = size(scrubbed_data, 2);
  myfile = fopen('results.txt', 'w');

  %Delete rows that belong to a cluster
  temp_copy = scrubbed_data;
  if (exclusive)
    temp_copy(found_points_idx, :) = [];
  end

  %Call SEPC
  results = sepc2(temp_copy, 50, 0.05, 0.5, 0.1, max_clusters);

  %--Store results--
  for r = 1:size(results,1)
    quality = results(r,1);
    fprintf(myfile,'%2.1e,,', quality);
  
    stop = dims+1;
    subspace = results(r, 2:stop);
    for i = 1:length(subspace)
      dim = subspace(i);
      fprintf(myfile,'%d,', dim);
    end

    stop = stop + 1;
    cluster_size = results(r, stop);
    fprintf(myfile,'%d,', cluster_size);

    stop = stop + 1;
    cluster = results(r, stop:stop+cluster_size-1);
    fprintf(myfile, ',%03d,,', cluster_size);

    for i = 1:cluster_size
      fprintf(myfile, '%d,', cluster(i));
    end

    fprintf(myfile, '\n');
  end %outer loop

fclose(myfile);
