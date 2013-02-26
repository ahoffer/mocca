%Test SEPC on randomly data

%Bring Figure window to the foreground
shg;

%Multivariate Gaussian distrubtion variances
xx = 4;
yy = 2;
%xy = 2;

%Iterature over the covariance of the X and Y values to provide
%different amount of correlation (+, 0, -)
for xy = linspace(-2, 2, 15)
  %Reset the drawing context
  hold off;
   
  %Create distribution data
  A = mvnrnd([0 0], [xx xy; xy yy], 100);
  
  %Get principal components of data
  coeff = pca(A);
  
  %Get SEPC clsuters
  results=sepc(A,1, 0.1, 0.3, 0.01, .4, 0.1, false);
  
  %Get number of clusters and a color map for the cluster
  num_clus= columns(results);
  cmap = colormap(prism(num_clus));
  fprintf('Number of clusters %d\n', num_clus);
  
  %Iterature over the clusters and draw each one in a different color.
  for i=1:num_clus
    
    %X and Y values of each point in the cluster
    clusX = A(results(i).objects, 1);
    clusY = A(results(i).objects, 2);
    
    %Assign a color (RGB 1x3) triple to the clsuter
    color_vec=cmap(i,:);
    
    %Plot the cluster
    if results(i).num_congregating_dims == 2      
      scatter(clusX, clusY, 80, color_vec, 'filled', 'o');      
    else
      scatter(clusX, clusY, 80, color_vec, '+');      
    end
        
    %Format the plot
    axis('square')
    %grid on;
    xlabel('X Axis');
    ylabel('Y Axis');
    xlim([-5, 6]);
    ylim([-5 5]);
    
    %Turn on hold because we want to plot all the clusters
    hold on
  end
  
  %Plot all the dat as empty circles
  h = scatter(A(:,1),A(:,2),'ko');
  
  %Do crazy stuff to plot principal components
  temp=zeros(2,4);
  temp(:, 2:2:end) = coeff;
  scalefactor=10; %Make the lines longer
  temp=scalefactor * temp;
  X=temp(1:2:end);
  Y=temp(2:2:end);
  plot(X, Y, 'r', 'LineWidth', 1);
  
  %Rotate the data to align with the first principal component
  %PCs are column vectors, ordered in significant from left to right
  %B = A*coeff;
  %plot(B(:,1), B(:,2), 'g.');
  
  %Pause until keystroke
  w = waitforbuttonpress;
  key = get(gcf,'CurrentCharacter');
  disp(key);
  if key == 'e'
    closereq
    return
  end
end

closereq