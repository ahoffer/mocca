function value=objectoverlap(object1, object2)
  
  %Input are row vectors
  value = columns(intersect(object1, object2)) / (columns(object1) + columns(object2));