function results=cluster(data, width, ktrials, discrim_set_size, beta, num_clusters, subspace_overlap, object_overlap)
  
%  Subspace Overlap: This parameter allows the user to control the extent
%  to which the subspaces spanned by two clusters may differ and yet still 
%  be considered close enough to check for object overlap.
%
%  Object Overlap: This parameter controls how much the objects or points 
%  of two clusters may overlap with one another. Object overlap is only 
%  considered if the subspaces spanned by the two clusters are close enough 
%  as determined by subspace overlap.
  
  
  %--Initialize variables--
  %These varaibles must be OUTSIDE the ktrials loop
  results = zeros;
  discrim_set_idxs = [];
  recorded_clusters = 0;
  num_dims = columns(data);
  num_points = rows(data);
  shuffle_idx = 1;
  
  %--Randomize (indexes of) points in the dataset--
  %The number of samples needed for discriminating sets depends on the size 
  %of each sample and the number of trials
  shuffle_size = ktrials * discrim_set_size;
  shuffled_array = shuffle(shuffle_size, num_points);

  %--SEPC Algorithm--
  for i = 1:ktrials

    %--Create discriminating set without duplicates--
    %<<Should we pull discriminating points out of the pool after they successfully discover a cluster?>>
   discrim_set_idxs = [];
    while (columns(discrim_set_idxs) < discrim_set_size)
      discrim_set_idxs(1, end+1) = shuffled_array(1, shuffle_idx);
      shuffle_idx = shuffle_idx +1;
      discrim_set_idxs = unique(discrim_set_idxs);
    end   

    %--Create cluster from the discriminating set--
    [subspace, mycluster] = trial(data, width, discrim_set_idxs);  

    %--Computer cluster quality--      
    num_congregating_dims = sum(subspace);
    cluster_cardinality = columns(mycluster);
    quality = cluster_cardinality / (beta ^ num_congregating_dims);
   
    %--Record or do not record a cluster--
    %If input parameter <num_clusters> is -1,  record all clusters
    %If there are fewer cluster than <num_clusters>,  store the cluster
      capture = (num_clusters == -1) || (recorded_clusters < num_clusters);
    
    if (~capture && recorded_clusters > 0)
      %Store the cluster if it is better than any previous cluster
        [min_quality, min_row_idx] = min(results(:, 1));
      if (quality > min_quality)
        %Delete lowest quality cluster
          results(min_row_idx, :) = []; 
        %Definitely capture the cluster
          capture = true;
      end %inner if
    end %outer if
     
    %Record the cluster
    if (capture)
      %Matlab does not like jagged arrays. Pad cluster indexes to make all
      %clusters a uniform size. Use num_points+1 to avoid overwriting the last
      %element in the array
        mycluster(1,num_points+1) = -1;
    
      %Update results. Append a row to the array.
        if(recorded_clusters == 0)
          results = [quality, subspace, cluster_cardinality, mycluster];
        else 
          results = [results; quality, subspace, cluster_cardinality, mycluster];
        end
    
      %Update number of recorded clusters
        recorded_clusters = rows(results);
    end %if statement
  end %for loop

