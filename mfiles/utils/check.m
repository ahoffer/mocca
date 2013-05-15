filenames=dir('.');
for i = 1:length(filenames)
  fname = filenames(i).name;
  is_m_file = fname (end) == 'm';
  
  if is_m_file
    checkcode(fname)
  end
end