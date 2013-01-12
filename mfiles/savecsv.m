function savecsv(filename, results, num_dims)

  %--Open file--
  myfile = fopen(filename, 'w');
  
  %--Store results--
  for r = 1:rows(results)
    quality = results(r,1);
    fprintf(myfile,'%2.1e,,', quality);
  
    stop = num_dims + 1;
    subspace = results(r, 2:stop);
    for i = 1:columns(subspace)
      dim = subspace(i);
      fprintf(myfile,'%d,', dim);
    end
    
    stop = stop + 1;
    cluster_size = results(r, stop);
    fprintf(myfile,',%d,,', cluster_size);

    stop = stop + 1;
    cluster = results(r, stop:stop+cluster_size-1);

    for i = 1:cluster_size
      fprintf(myfile, '%d,', cluster(i));
    end

    fprintf(myfile, '\n');
  end %outer loop

  fclose(myfile);

  