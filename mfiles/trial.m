function [mycluster, subspace]=trial(data, width, discrim_set_idx)

  %--Create subspace vector--
  %Create combinations of every point in the discriminating set
  combos = nchoosek(discrim_set_idx, 2);
  
  %Assume every column in the data represents one dimension
  dims = size(data, 2);
  num_points = size(data, 1);

  %Span vector holds the sum of absolute differences between pairs
  %of points in the discriminating set
  span = zeros(1, dims);

  %Iterate over every point combination and sum the absolute
  %differences
  %<<Is there a way to do this without a loop?>>
  for ii = 1:size(combos, 1)
    r1 = combos(ii, 1);
    r2 = combos(ii, 2);
    diff = data(r1,:) - data(r2,:);
    span = span + abs(diff);
  end

  %Subspace is a logical (1s and 0s) vector
  %If the discriminating set congregates in a dimension,
  %The value of the element is 1. Otherwise the value is 0.
  subspace = span < width;

  %--Find the clusters--
  %Create vectors of max/min values of the columns in the
  %discriminating set. 
  disriminating_points = data(discrim_set_idx, :);
  max_vals = repmat(max(disriminating_points), num_points, 1);
  min_vals = repmat(min(disriminating_points), num_points, 1);

  %The fullspace cluster is a logical matrix.
  %It is the set of instances that congregate in all dimensions.
  x1 = data <= (max_vals + width);
  x2 = data >= (min_vals - width);
  fullspace_cluster =  x1 & x2;

  %The subspace cluster is is less restrictive than the fullspace
  %cluster. It is the logical OR of the fullspace cluster with the
  %logical NOT of the subspace vector.
  subspace_cluster = fullspace_cluster | repmat(~subspace, num_points, 1);

  %Find the indexes of the instances in the subspace cluster
  %That is, find the rows where all the columns are true
  %All the values are true if every element in the row is 1
  cluster_rows = sum(subspace_cluster, 2) == dims;
  mycluster = find(cluster_rows);
  