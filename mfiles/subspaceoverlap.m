function value=subspaceoverlap(subspace1, subspace2)
% Parameters subspace1 and subspace2 are row vectors of the same length
% Return a number in the range [0, 1]. A value of 1 means the 
% two subspaces are the identical. A value of 0 means the subspaces
% do not intersct.
  value = -1;
  
  same_size = size(subspace1) == size(subspace2)
  if ( || rows(subspace1) > 1)
    disp 'ERROR COMPUTING SUBSPACE OVERLAP');
    return
  end
  
  dims = columns(suspace1);
  similarity = sum(not(xor(subspace1, subspace2)))
  value = similarity / dims;

