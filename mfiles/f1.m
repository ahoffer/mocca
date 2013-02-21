function fone = f1( actual_classes, clusters)

%Actual classes is a column vector
num_objects=length(actual_classes);
classes=unique(actual_classes);
num_hidden_clusters = length(classes);
num_found_clusters=columns(clusters);
mapping=zeros(num_found_clusters, 1);

%Create an array of structures to hold the indices of the objects
%in the hidden clusters
for j = 1:num_hidden_clusters
  the_class = classes(j);
  object2class=repmat(the_class, num_objects, 1) == actual_classes;
  hidden_clusters(j).objects = find(object2class);
  hidden_clusters(j).cardinality = sum(object2class);
end

for i = 1:num_found_clusters
  overlap = zeros(num_hidden_clusters, 1);
  for j = 1:num_hidden_clusters
    setA = clusters(i).objects;
    setB = hidden_clusters(j).objects;
    intersection_cardinality = sum(ismember(setA, setB), 2);
    value=intersection_cardinality / hidden_clusters(j).cardinality;
    overlap(j)=value;
  end
  [~, idx]=max(overlap);
  mapping(i)=classes(idx);
end

%IDEA: Imporve the results by looking at cluster quality for clusters
%that are assigned to the same classification and only map the cluster
%with the highst quality
fone=0;
for j = 1:num_hidden_clusters
  the_class = classes(j);
  clusters_indexes=find(repmat(the_class, num_found_clusters, 1) == mapping);
  OmOfH=unique([clusters(clusters_indexes).objects]);
  numerator=length(intersect(hidden_clusters(j).objects, OmOfH));
  recall=numerator/hidden_clusters(j).cardinality;
  precision=numerator/length(OmOfH);
  d = (recall+precision);
  if ~isnan(d) && d ~= 0
    fone=fone+(2*recall*precision)/d;
  end
end
fone=fone/num_hidden_clusters;
