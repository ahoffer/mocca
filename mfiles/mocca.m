function results=mocca(data, width, alpha, beta, ...
  subspace_overlap_threshold,...
  object_overlap_threshold, ...
  num_trials, ...
  discrim_set_size, ...
  use_pca,...
  rot_set_size,...
  num_pcs)

%  Subspace Overlap: Range is (0,1). This parameter allows the user to
%  control the extent to which the subspaces spanned by two clusters may
%  differ and yet still be considered close enough to check for object
%  overlap.
%
%  Object Overlap: Range is (0,1). This parameter controls how much the
%  objects or points of two clusters may overlap with one another. Object
%  overlap is only considered if the subspaces spanned by the two clusters
%  are close enough as determined by subspace overlap.

%--Initialize variables-- These varaibles must be OUTSIDE the num_trials
%loop
num_saved_clusters = 0; %#ok<*NASGU>
num_objs = rows(data);
num_dims=columns(data);
min_cluster_cardinality = round(alpha * num_objs);
found_at_least_one_cluster = false;

if use_pca
  %TODO: Prune unwanted principal components-- run the idea past Clark.
  %Usually this is controlld by alpha, some percetange of significance,
  %past which PCs are ignored.
  fprintf('mocca.m - num_pcs not implemented\n');
end

for k = 1:num_trials
  if use_pca
    %Randomly select rotation set and discriminating set
    rot_set=randi(num_objs, 1, rot_set_size);
    discrim_set=randi(rot_set_size, 1, discrim_set_size);
    
    %Find the principal components
    rot_objs=data(rot_set, :);
    coeff=pca(rot_objs);
    
    %The most significant principal component is the first column Re-order
    %the coeff matrix so that the LEAST significant PC is the first column.
    rot_mat=fliplr(coeff);
    transformed_data=data*rot_mat;
    discrim_objs=transformed_data(discrim_set, :);

  else
    %Randomly select discriminating objects
    discrim_set = randi(num_objs, 1, discrim_set_size);
    discrim_objs=data(discrim_set, :);
    transformed_data=data;
  end
  
  %--Search for a new cluster--
  [clstr.subspace, clstr.objects] = trial(transformed_data, width, discrim_objs);
  
  %--If the subspace is null, we failed to detect a cluster. Return to top
  %of loop
  if notnull(clstr.subspace)
    
    %--Compute cluster quality--
    clstr.cardinality = columns(clstr.objects);
    
    %----Test for minimum number of points----
    if clstr.cardinality > min_cluster_cardinality
      clstr.discrim_set = discrim_set;
      clstr.num_congregating_dims = sum(clstr.subspace);
      clstr.quality =...
        quality(clstr.cardinality, clstr.num_congregating_dims, beta);
      
      
      %--Decide to accept or reject the cluster--
      %------------------------------------------ --If this is the first
      %cluster, save it
      if (num_saved_clusters < 1)
        found_at_least_one_cluster = true;
        results(1) = clstr;
        num_saved_clusters = 1; %#ok<*NASGU>
      else
        %----Test for similarity to other subspaces----
        
        %NOTE: Ask Clark about his case. There could be a cluster that
        %overlaps in fewer dimesions but has a larger normalized overlap.
        %Does that present challenges?
        %
        %For example:
        %   subspace A = [0 1 1 1 1] subspace B = [1 0 0 0 0] subspace C =
        %   [1 1 0 1 1]
        %
        %   A overlap B = 1/min(1,4) =   1 = 100% A overlap C = 3/min(5,4)
        %   = 3/4 = 75%
        
        
        %TODO: Ask Clark about this defintion of subsapce overlap.
        %Determine normalized overlap between clusters A and B is defined:
        %
        %   (#dims in common) / min(#dims in A, #dims in B)
        %
        
        %Create a column vector whose values are the number of overlapping
        %dimensions between the current cluster and the recorded clusters.
        %For example:
        %
        %  subspace of current cluster = [1 0 1] subsapce of recorded
        %  clusters =
        %     [0 1 1] [1 1 1] [1 0 1] [0 0 1]
        %
        % Resulting column vector =
        %     [1] [2] [3] [1]
        
        %NOTE: results is a structure for results.subspace returns a comma
        %sepated list. That list can be turned into a row vector by
        %enclosing the list in brackets [ ]. The actual layout of the
        %matrices puts the subspaces in columns concatenated together.
        subspace_curr=repmat([clstr.subspace]', 1, num_saved_clusters);
        subspace_recorded=reshape([results.subspace], num_dims, []);
        temp=subspace_curr & subspace_recorded;
        overlapping_dims=sum(temp);
        
        %Create vectors for the number of dims in the current cluster
        num_dims_current_cluster=...
          repmat(clstr.num_congregating_dims, 1, num_saved_clusters);
        num_dims_recorded_clusters=[results.num_congregating_dims];
        
        %Create "divisor" vector
        divisor = min(num_dims_current_cluster, num_dims_recorded_clusters);
        
        %Create normalized overlap column vector
        normalized_overlap=overlapping_dims ./ divisor;
        
        %--If any value in the normalized overlap vector is greater than
        %the overlap threshold, then the current cluster overlaps at least
        %one recorded cluster's subspace
        temp = normalized_overlap > subspace_overlap_threshold;
        overlapping_cluster_indexes=find(temp);
        
        %----Test for similarity to other clusters' point sets---- Subspace
        %overlap cannot, by itself, disquality a cluster. Current cluster
        %is disqualified if its subspace overlaps too much with a cluster
        %whose AND there is too much object overlap.
        
        %IDEA: I could use bit vectors to record which objects are in a
        %cluster. Then I could use vector operations to determine overlap.
        %For example:
        %
        %  cluster objects A = [1 1 0 0 0 1] cluster objects B = [1 0 0 0 0
        %  1] sum(A & B)
        %
        %Clusters A and B have two object in common. This works well for
        %clusters with a small number of objects. BETTER IDEA! Use sparse
        %arrays instead of regular vectors. But wait, would that really be
        %any better? Sparse arrays are probably implemented as some kind of
        %linked data structure and I bet you loose the benefit of fast
        %vector operatrions on linked data structures.
        
        %TODO: Mention in student report that a bit vector is a fine way to
        %describer a lower dimensional subspace, but at some point it
        %becomes dumb. If there are 12,000 dimensions, then it would be
        %much easier to store the list of congregating dimeions instead of
        %representing the subsapce as a bit vector.
        
        %----Test for similarity to other clusters' point sets----
        %         objects_too_similar = false; max_num_common = 0;
        %         normalized_common = 0;
        %
        
        
        %TODO: Implement as parallel for. Instead of saving values to un-
        %synchronized varaibles, store each value in a vector indexed by
        %the loop variable, j. For example: vec(j) = num_common. The value
        %of j.
        
        max_num_common=0;
        normalized_common=0;
        num_overlapping_subspaces=columns(overlapping_cluster_indexes);
        
        for j = 1:num_overlapping_subspaces
          idx=overlapping_cluster_indexes(j);
          other_clstr=results(idx);
          
          % FAST intersection code. As long the lists of objects in the
          % clusters
          %are sorted, use this trick to speed up set intersections by
          %several orders of magnitude
          %
          % a = randi(1000,100,1); b = randi(1000,100,1); intersection =
          % a(ismembc(a,b))'
          %
          object_intersection=...
            clstr.objects(ismembc(clstr.objects, other_clstr.objects));
          
          num_common=columns(object_intersection);
          if (num_common > max_num_common);
            max_num_common=num_common;
            smallest_cardinality = ...
              min(clstr.cardinality, other_clstr.cardinality);
            normalized_common=num_common / smallest_cardinality;
          end %if-test
        end %for-loop
        
        if normalized_common < object_overlap_threshold
          
          %-----Record cluster----- Results is a (1 x n) row vector of
          %individual clusters That is how structures work
          
          results(num_saved_clusters+1) = clstr;
          
        end %if-test for first cluster
        
        num_saved_clusters = columns(results);
        
        %Progress report
        if mod(k, 1000) == 0
          fprintf('%d of %i num_trials\n', k, num_trials)
        end %if-test for printing progress
      end %if-test subspace/object overlap
    end %if-test for min. cluster size
  end %if-test subspace not null
  
  %--Clean out the old cluster object--
  clstr = [];
  
end %for loop

%Weird error about results not assigned
if ~found_at_least_one_cluster
  results = [];
end