function A=pad(A, newlength)
  
  if (columns(A) < newlength)
    A(1, newlength) = 0;
  end