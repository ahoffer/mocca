function plotquality(beta_min, beta_max, clusters_max_size, dims_max, z_axis_upper_bound, plotlog)

%plotquality(0.01, 0.8, 1000, 100, 200, false);  

  frame_count = 0; 

  [cluster_cardinality, dims] = meshgrid(round(linspace(2, clusters_max_size, 50)), round(linspace(1, dims_max, 50)));
 
  for beta = linspace(beta_min, beta_max, 100)
   
    %Calcuate formula value
    quality = cluster_cardinality ./ dims .^ beta;

    %Set up labels and scales
    if (plotlog)
      values = log10(quality);
      zlabel_str = ('log 10 of quality');
    else
      values = quality;
      zlabel_str = 'quality';
    end

    %Plot!
    surf(cluster_cardinality, dims, values);
    
    %Manually manage the axises because auto-scaled z axis is annoying
    %when the plot is animated
    axis([1, clusters_max_size, 1, dims_max, 1, z_axis_upper_bound], 'manual');

    %Clamp the color map
    caxis([0, z_axis_upper_bound]);
    
    %Increase font size 
    ax = gca();
    set(ax, 'fontsize', 15);
    
    %Set other plotting params
    shading interp;
    titlestring = sprintf('Plot of quality for beta=%f', beta);
    title(titlestring);
    xlabel('clusters size');
    ylabel('cluster dimensions');
    zlabel(zlabel_str);

    drawnow;
    
    %Save images to a files
    fname = sprintf('test%03d', frame_count);
    frame_count = frame_count + 1;
    fname
    print(gcf(), '-dpng', fname);
    
  end
