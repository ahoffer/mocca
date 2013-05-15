%Load Data
javaaddpath('C:\Program Files\Weka-3-7\weka.jar');
wekaOBJ = loadARFF('../data/genn.arff');
[mdata,featureNames,targetNDX,stringVals,relationName] = weka2matlab(wekaOBJ);
data=mdata(:, 1:end-1);
actual_classes=mdata(:, end);

rows(data)

%Test MOCCA
width = 0.1;
alpha = .1;
beta = .6;
epsilon = 0.05;
subspace_overlap_threshold=0.9;
object_overlap_threshold=0.4;
use_pca=true;
rotation_set_size=8;
num_pcs= -1;

results=mocca_api(data, width, alpha, beta, epsilon, ...
  subspace_overlap_threshold, ...
  object_overlap_threshold, ...
  use_pca,...
  rotation_set_size,...
  num_pcs);

%Clear the plot
hold off;

%Bring Figure window to the foreground
shg;

%Get number of clusters and a color map for the cluster
num_clus= columns(results);
cmap = colormap(prism(num_clus));
fprintf('Number of clusters %d\n', num_clus);

%Plot all the dat as empty circles
%set(gcf, 'Color', [0, 0, 0]);
whitebg([0.5,0.5,0.5])
h=scatter(data(:,1),data(:,2),'ko');

%Set background color
hold on;

for i=1:num_clus
  
  %X and Y values of each point in the cluster
  clusX = data(results(i).objects, 1);
  clusY = data(results(i).objects, 2);
  
  %Assign a color (RGB 1x3) triple to the clsuter
  color_vec=cmap(i,:);
  
  %Plot the cluster
  if results(i).num_congregating_dims == 2
    scatter(clusX, clusY, 80, color_vec, 'filled', 'o');
  else
    scatter(clusX, clusY, 150, color_vec, 'd');
  end
  
end



