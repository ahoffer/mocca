function [clusters, subspaces, qualities]=sepc(data, width, alpha, beta)
 
  %Define internal constants
  epsilon = 0.01;
  dims = columns(data);

  %Compute number of trials to perform
  k_est = 1 + 4/alpha * (dims/log(4))^(log(alpha)/log(beta));
  ktrials = round(k_est)

  %Computer size of the discriminating set
  s_est = log(dims/log(4))/log(1/beta);

  %Use 2 as smallest discrinating set. If s=1, pairwise 
  %combinations later in the algorithim do not make any sense
  %and will cause a run-time error
  s = max(2, round(s_est))

  %--Randomize instances in the dataset--
  %
  %Values should range from 1 to number of instances in the data set because
  %the values will be used to index the data set
  instances = rows(data);

  %The number of samples needed depends on the size of each sample and the 
  %number of trials
  shuffle_size = ktrials * s;
 
 
 
  [best_cluster, best_subspace, best_quality]=cluster(data, width, ktrials, s, beta);
  disp("-----------------------")
  quality=best_quality
  congregating_dims=columns(best_subspace)
  %subspace=best_subspace
  isntances=columns(best_cluster)
  %instances=best_cluster
 
end  
