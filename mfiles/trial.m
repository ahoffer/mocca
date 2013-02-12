function [subspace, mycluster]=trial(data, width, discrim_points)
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
%       [n x d]
%       The data set to cluster.
%       Rows correspond to points.
%       Column correspond to dimensions.
%
%   width
%       The width as specificed in the SEPC algorithm.
%
%   discrim_points
%       [p, d]
%       The actual point values of the discriminating set

%--Create max and min values in each dimension from the discriminating set.
maxs = max(discrim_points)
mins = min(discrim_points)

%--Create the extents of the discriminating points in all dimensions.
discrim_set_span = maxs - mins

%--Create subspace vector--
subspace = discrim_set_span <= width

%--If the entire subspace is zero, it means the discriminating set does
%not congregate in any dimension. If the algorithm is allowed to continue
%then the "allowance" varaible calculated below becomes worse than
%meaningless. The allowance takes on negative values which leads to
%a bounding box with negative volume. The real bummer occurs when the
%logical NOT of the subscapce vector is logically OR-ed with the set of
%congreating points. The results is that every point in the data sets is
%included in the cluster.
%Problem solves with an early return.
if ~any(subspace)
    subspace = [];
    mycluster = [];
    return
end

%--Find congregating points--
%Create matrices of max/min values
num_points = rows(data);
allowance = width - discrim_set_span;
upper_bounds = maxs + allowance;
lower_bounds = mins - allowance;

%The fullspace cluster is a logical matrix.
%If a point is inside the hypercude in a particular dimension,
%then value of the matrix for that point and dimesion is 1.
upper = repmat(upper_bounds, num_points, 1);
lower = repmat(lower_bounds, num_points, 1);
% size(lower)
% size(upper)
% size(data)
% num_points
fullspace_cluster = (data <= upper) & (data >= lower);

%The subspace cluster is is less restrictive than the fullspace
%cluster. It is the logical OR of the fullspace cluster with the
%logical NOT of the subspace vector.
subspace_cluster = fullspace_cluster | repmat(~subspace, num_points, 1);

%Find the indexes of the rows where all the values are true
congregating_points = all(subspace_cluster, 2);
mycluster = find(congregating_points)';
