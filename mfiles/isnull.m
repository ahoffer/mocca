function [ bool ] = isnull( v )
%ISNULL Return true if the vector is all zeroes
%
%   Vector [0 0 0] returns true
%   Vector [ ]  return true
%
%   Vector [0 1 0] returns false
%   Vector [1 1 1] returns false

    bool = ~any(v);

end

