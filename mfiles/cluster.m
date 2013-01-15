function results=cluster(data, width, ktrials, discrim_set_size, beta, num_clusters, max_subspace_overlap, max_object_overlap)
global pts
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
  num_saved_clusters = 0;
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
   
    %--Test for similarity to recorded clusters--
    %
    %If there is at least one other cluster, test its dissimilarity
    %to the current cluster
    
    too_similar_dims = false;  
    
    if (num_saved_clusters > 0)
      subspaces = results(:,2:num_dims+1);
      subspace_copies = repmat(subspace, num_saved_clusters, 1);
      
      %Find the number of dimension which are different between the recorded
      %subspaces and the current subspace and then sum along the rows to
      %create a column vector. 
      overlap = sum(not(xor(subspace_copies, subspaces)), 2);
      normalized_overlap = max(overlap) / num_dims;
      too_similar_dims = normalized_overlap > max_subspace_overlap;
    end      
    
    %Matlab does not like jagged arrays. Pad cluster indexes with 0s. 
    %Use num_points+1 to avoid overwriting the last element in the array
    mycluster = pad(mycluster, num_points+1);
    
    %Check for object overlap
    %Do not check if subspace overlap is too large - waste
    num_common = 0;
    normalized_common = 0;
    pts = 0;
   
    if (num_saved_clusters > 0)      
      cluster_copies = repmat(mycluster, num_saved_clusters, 1);
      saved_clusters = results(:, num_dims+3:end);
      %<<Need a Matlab guru to remove iteration>>
       for j = 1:rows(results)
        num_common = max(num_common, columns(intersect(mycluster, saved_clusters(j,end-1))));
        pts = results(j, num_dims+2);
        normalized_common = num_common / (cluster_cardinality + pts);
       end
    end  
    
    too_similar_objects = normalized_common > max_object_overlap;
    
    %--Record or do not record a cluster--
    %If input parameter <num_clusters> is -1,  record all clusters
    %If there are fewer cluster than <num_clusters>,  store the cluster
    capture = not(too_similar_objects || too_similar_dims);
    

    %If maximum number of cluster are saved, check quality and replace the 
    %lowest quality cluster, if necessary.
    if(capture && num_saved_clusters == num_clusters)
      %Get lowest quality cluster and its index
      [min_quality, min_row_idx] = min(results(:, 1));
      if (quality > min_quality)
        %Delete lowest quality cluster
        results(min_row_idx, :) = [];
      else
        capture = false;
      end 
    end 
      
    %Update results. Append a row to the array.
    if (capture)
      if(num_saved_clusters == 0)
        results = [quality, subspace, cluster_cardinality, mycluster];
      else 
        results = [results; quality, subspace, cluster_cardinality, mycluster];
      end
      %Update number of recorded clusters
      num_saved_clusters = rows(results);
    end

  end %for loop

  
