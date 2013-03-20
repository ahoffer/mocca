function [ bool ] = notnull( v )
%NOTNULL Return logical NOT of function isnull
%
%   Vector [0 0 0] returns FALSE
%   Vector [ ]  return FALSE
%
%   Vector [0 1 0] returns TRUE
%   Vector [1 1 1] returns TRUE

    bool = not(isnull(v));

end

