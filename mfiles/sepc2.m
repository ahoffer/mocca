function results=sepc2(data, width, alpha, beta, epsilon, num_clusters)
 
  results = [];
  recorded_clusters = 0;
 %Define internal constants
  dims = size(data, 2);

  %Compute number of trials to perform
  k_est = 1 + 4/alpha * (dims/log(4))^(log(alpha)/log(beta)) * log(1/epsilon);
  ktrials = round(k_est);

  %TEMP
  ktrials=5000
  
  %Computer size of the discriminating set
  s_est = log(dims/log(4))/log(1/beta);

  %Use 2 as smallest discrinating set. If s=1, pairwise 
  %combinations later in the algorithim do not make any sense
  %and will cause a run-time error
  discrim_set_size = max(3, round(s_est));
  
  while (recorded_clusters < num_clusters)
  
    %--Find the best cluster--
    [mycluster, subspace, quality] = cluster(data, width, ktrials, discrim_set_size, beta);
    %Change cluster from a column vector to a row vector
    mycluster = mycluster';
    cluster_size = size(mycluster,2);
   
    %Octave does not like jagged arrays. 
    num_points = size(data,1);
    %Pad cluster indexes to make all clusters a uniform size.
    %num_points+1 to avoid overwriting the last element in the array
    mycluster(1,num_points+1) = -1;
    
    %Create results array
    results = [results; quality, subspace, cluster_size, mycluster];

    %Update number of recorded clusters
    recorded_clusters = size(results,1);
    
  end %end loop
