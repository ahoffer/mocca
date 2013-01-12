function shuffled_array=shuffle(min_elements, max_value)
% Return a row vector whose length is larger of min_elements and max_value.
% The value of each element is between [1, max_value] (inclusive)
  
  
  % Using randperm instead of randi reduces likelihood that the 
  % same value appears multiple times in the array.
  shuffled_array = randperm(max_value);

  while(columns(shuffled_array) < min_elements)
    shuffled_array = [shuffled_array randperm(max_value)];
  end