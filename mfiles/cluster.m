function [best_cluster, best_subspace, best_quality]=cluster(data, width, ktrials, discrim_set_size, beta)
  
  warning ('off', 'Octave:broadcast');

  %--Randomize (indexes of) points in the dataset--
  %
  %Values should range from 1 to number of points because
  num_points = size(data, 1);

  %The number of samples needed for discriminating sets depends on the size 
  %of each sample and the number of trials
  shuffle_size = ktrials * discrim_set_size;

  %Create the shuffle vector
  %Note: Discriminating sets should not contain duplicate points.
  %Using randperm instead of randi reduces likelihood that the 
  %same point appears multiple times in the same discriminating set.
  shuffle = randperm(num_points);
  while(size(shuffle, 2) < shuffle_size)
    shuffle = [shuffle randperm(num_points)];
  end

  %Set values outside of the trials loop
  start = 1;
  best_quality = 0;
  best_cluster = [];
  best_subspace = [];

  %SEPC Algorithm
  for i = 1:ktrials
    %--Create discriminating set--
    stop = start + discrim_set_size - 1;
    discrim_set_idx = shuffle(1, start:stop);
    start = stop + 1;

    %--Create cluster from the discriminating set--
    [cluster, subspace] = trial(data, width, discrim_set_idx);  

    %--Computer cluster quality--      
    num_congregating_dims = sum(subspace);
    cluster_cardinality = size(cluster, 1);
    quality = cluster_cardinality / (beta ^ num_congregating_dims);

    %--Update best cluster--
    if (quality > best_quality)
      best_quality = quality;
      best_cluster = cluster;
      best_subspace = subspace;
    end

  end %for loop

