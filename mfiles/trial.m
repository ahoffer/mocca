function [subspace, mycluster]=trial(data, width, discrim_set_idxs)
%INPUT/OUTPUT
%
%   subspace
%       A row vector whose length is equal to number of dimensions in the 
%       data set and whose values are "1" or "0". A value of "1" means 
%       the cluster congregates in that dimension. A value of "0" means it 
%       does not congregate in that dimesion.  For example, [1 1 0 0 1] 
%       describes a subspace where points congregate in the first, second 
%       and fifth dimensions. 
%
%   mycluster
%       The cluster that was found. It is a row vector whose elements are 
%       indexes into the data set. The indexes are 1-based, not zero-based. 
%       The vectors legnth is equal to the number of points in the cluster.
%       The variable is named "mycluster" because the work "cluster" conflicts
%       with the "cluster.m" file
%
%   data
%       The data set to cluster. 
%       Rows correspond to points. 
%       Column correspond to dimensions.
%
%   width
%       The width as specificed in the SEPC algorithm.
%
%   discrim_set_idxs
%       Indexes into rows of the data set. Sepcifies the points in the 
%       data to sed as the SEPC discriminating set. It is a row vector.
%
%   

  %Create combinations of every point in the discriminating set
  combos = nchoosek(discrim_set_idxs, 2);
  
  %Assume every column in the data represents one dimension
  num_dims = columns(data);
  num_points = rows(data);

  %Span vector holds the sum of absolute differences between pairs
  %of points in the discriminating set
  span = zeros(1, num_dims);

  %Iterate over every point combination and sum the absolute
  %differences
  %<<Is there a way to do this without a loop?>>
  for ii = 1:size(combos, 1)
    r1 = combos(ii, 1);
    r2 = combos(ii, 2);
    diff = data(r1,:) - data(r2,:);
    span = span + abs(diff);
  end

  %--Create subspace vector--
  subspace = span < width;

  %--Find congregating points--
  %Create vectors of max/min values of the columns in the
  %discriminating set. 
  disriminating_points = data(discrim_set_idxs, :);
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

  %Find the indexes of the rows where all the columns are true
  %All the values are true if every element in the row is 1
  congregating_points = sum(subspace_cluster, 2) == num_dims;
  mycluster = find(congregating_points)';
 