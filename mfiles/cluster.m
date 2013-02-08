function results=cluster(data, width, ktrials, discrim_set_size, alpha, beta, num_clusters_threshold, subspace_overlap_threshold, object_overlap_threshold)

%  Subspace Overlap: Range is (0,1). This parameter allows the user to control the extent
%  to which the subspaces spanned by two clusters may differ and yet still 
%  be considered close enough to check for object overlap.
%
%  Object Overlap: Range is (0,1). This parameter controls how much the objects or points 
%  of two clusters may overlap with one another. Object overlap is only 
%  considered if the subspaces spanned by the two clusters are close enough 
%  as determined by subspace overlap.

  %--Initialize variables--
  %These varaibles must be OUTSIDE the ktrials loop
  num_saved_clusters = 0;
  num_dims = columns(data);
  num_points = rows(data);
  num_points_threshold = round(alpha * num_points);
  
  %--SEPC Algorithm--
  for k = 1:ktrials
    
    %--Create the set of discriminating points as a row vector
    indexes = randi(num_points, 1, discrim_set_size);
    discrim_points = data(indexes, :);
    [clstr.subspace, clstr.objects] = trial(data, width, discrim_points);

    %--Compute cluster quality--      
    num_congregating_dims = sum(clstr.subspace);
    clstr.cardinality = columns(clstr.objects);
    clstr.quality = quality(clstr.cardinality, num_congregating_dims, beta);
    clstr.num_congregating_dims = num_congregating_dims;

    %--Decide to accept or reject the cluster--
    %------------------------------------------
    %--If this is the first cluster, save it--
     if (num_saved_clusters < 1)   
      results(1) = clstr;
      num_saved_clusters = 1;
    else
      %----Test for similarity to other subspaces----
      dims_too_similar = false;        
      %Find the number of dimensions which are different between the recorded
      %subspaces and the current subspace. 
      %Sum along the rows to create an (n x 1) column vector.
      subspace_copies = repmat(clstr.subspace, num_saved_clusters, 1);

      max_overlapping_dims = 0;
      normalized_overlap = 0;
      for i = 1:num_saved_clusters
        num_overlapping_dims = sum(clstr.subspace & results(i).subspace);
        if num_overlapping_dims > max_overlapping_dims
          max_overlapping_dims = num_overlapping_dims;
          num_dims_other_clstr = results(i).num_congregating_dims;
          normalized_overlap = max_overlapping_dims / min(clstr.num_congregating_dims, num_dims_other_clstr)
        end
      end
      dims_too_similar = normalized_overlap > subspace_overlap_threshold;
     
      %Take the largest number in the column and normalize it by the number of dimension
      %in the lower dimensional subspace

        % [max_num_overlap, index] = max(num_overlapping_dims)
        % num_dims_other_clstr = results(index).num_congregating_dims

      %---------------------------------------------------------- 
      
      %----Test for similarity to other clusters' point sets----
      objects_too_similar = false;
      max_num_common = 0;
      normalized_common = 0;
      
      for j = 1:num_saved_clusters
        num_common = columns(intersect(clstr.objects, results(j).objects));
        if (num_common > max_num_common);
          max_num_common = num_common;
          normalized_common = num_common / min(clstr.cardinality, results(j).cardinality);
        end
      end    
      objects_too_similar = normalized_common > object_overlap_threshold;
      %----------------------------------------------------------    

      %----Test for minimum number of points----
      too_few_objects = clstr.cardinality < num_points_threshold;
      
      %-----Record or do not record a cluster-----
      capture = not(objects_too_similar && dims_too_similar || too_few_objects);

      %----Drop lowest quality cluster if necessary-----
      %If maximum number of cluster are saved, check quality and replace the 
      %lowest quality cluster, if necessary.
      if(capture && num_saved_clusters == num_clusters_threshold)
        %Get lowest quality cluster and its index
        [min_quality, min_row_idx] = min([results.quality]);
        if (clstr.quality > min_quality)
          %Delete lowest quality cluster
          results(min_row_idx) = [];
        else
          capture = false;
        end 
      end 
  
      %Update results. Append a row to the array.
      if (capture)
        results(num_saved_clusters+1) = clstr;
      end
    end 
    %Results is a (1 x n) row vector of individual clusters
    %That is how structures work
    num_saved_clusters = columns(results);
    
    %Progress report
    if mod(k, 1000) == 0
      fprintf('%d of %d ktrials\n', k, ktrials)
    end
    
  end %for loop
 