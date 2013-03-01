function results=mocca_api(data, width, alpha, beta, epsilon, ...
  subspace_overlap_threshold, ...
  object_overlap_threshold, ...
  use_pca,...
  rotation_set_size,...
  num_pcs)

num_dims=columns(data);
num_trials=ktrials(epsilon, alpha, beta, num_dims);
discrim_set_size=max(2, s_est(num_dims, beta));
num_objs=rows(data);
results = [];

%Validate inputs
if rotation_set_size > num_objs
  fprintf('Rotation set size, %d, cannot be larger the number of objects in the data set, %d.\nExiting...\n', rotation_set_size, num_objs);
  return
end

if num_pcs >= num_dims
  fprintf('Number of principal components, %d, cannot exceed number of dimensions, %d.\nExiting...\n', num_pcs, num_dims);
  return
end

if num_pcs >= rotation_set_size
  fprintf('Need at least %d objects to get %d principal compponents. Only %d objects requested.\nExiting...\n', num_pcs+1, num_pcs, rotation_set_size);
  return
end

if discrim_set_size >= rotation_set_size
  fprintf('Size of discriminating set, %d, cannot be >= size of rotation set, %d\nExiting...\n', discrim_set_size, rotation_set_size);
  return
end
%fprintf('Number of trials=%d\n', num_trials);

results=mocca(data, width, alpha, beta, ...
  subspace_overlap_threshold, ...
  object_overlap_threshold, ...
  num_trials, ...
  discrim_set_size,...
  use_pca,...
  rotation_set_size,...
  num_pcs);

end
