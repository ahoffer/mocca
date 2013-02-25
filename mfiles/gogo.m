hold off
shg;
cmap = colormap(jet);
%Distrubtion variances
xx = 4;
yy = 2;
%xy = 2;

for xy = linspace(-2, 2, 3)
  hold off
  %Create distribution data
  A = mvnrnd([0 0] , [xx xy; xy yy], 100);
  
  %Get principal components of data
  coeff = pca(A);
  
  %Plot clusters
  results=sepc(A,1, 0.1, 0.3, 0.01, .4, 0.1);
  num_clus= columns(results);
  cmap = hsv(num_clus);
  for i=1:num_clus
    clusX = A(results(i).objects, 1);
    clusY = A(results(i).objects, 2);
    
    i
    results(i).objects
    
    
    color_vec=cmap(i,:);
    scatter(clusX, clusY, 50, color_vec, 'filled');
    axis('square')
    grid on;
    xlabel('X Axis');
    ylabel('Y Axis');
    xlim([-5, 6]);
    ylim([-5 5])
    hold on
    shg;
  end
  
  %Plot distribution
  h = scatter(A(:,1),A(:,2),'ko');
  %hold on
  
  %Do crazy stuff to plot principal components
  temp=zeros(2,4);
  temp(:, 2:2:end) = coeff;
  scalefactor=10; %Make the lines longer
  temp=scalefactor * temp;
  X=temp(1:2:end);
  Y=temp(2:2:end);
  plot(X, Y, 'r', 'LineWidth', 1);
  axis('square')
  grid on;
  xlabel('X Axis');
  ylabel('Y Axis');
  xlim([-10, 10]);
  ylim([-10 10])
  
  pause(0.1)
end
