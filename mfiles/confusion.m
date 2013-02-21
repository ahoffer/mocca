function confusion(objects, actual_classes)

predicated_classes = zeros(size(actual_classes));
predicated_classes(objects, 1) = 1;
classes = unique(actual_classes);
fprintf('\t\tValue\t\tCount\t\tPercent\n');
for idx = 1:length(classes)
  aClass = classes(idx, 1);
  mask = repmat(aClass, size(actual_classes)) == actual_classes; 
  actuals_cardinality=sum(mask); %TBD
  matches=predicated_classes & mask;
  num_matches = sum(matches);
  percentage = num_matches/length(objects);
  fprintf('\t\t\t%d\t\t%d\t\t\t%g%%\n', aClass, num_matches, percentage*100);
end

fprintf('\nActual class of objects in the cluster\n');
tabulate(actual_classes(objects, 1))