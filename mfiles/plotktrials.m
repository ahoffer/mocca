function plotktrials(grid_size, alpha_min, alpha_max, beta_min, beta_max, dims_max, dims_steps, epsilon_max, epsilon_steps, z_axis_upper_bound, plotlog)
  
  frame_count = 0; 

  %xx is alpha, yy is beta
  [xx, yy] = meshgrid(linspace(alpha_min, alpha_max, grid_size), linspace(beta_min, beta_max, grid_size));
  A = ones(grid_size, grid_size);
  B = 4 ./ xx;
  D = log(xx) ./ log(yy);

  %I bet the following line can be nuked
  axis([alpha_min, alpha_max, beta_min, beta_max, 0, 400], 'manual');
 
  for epsilon = linspace(0, epsilon_max, epsilon_steps)
   
    %Calcuate formula values
    E = log(1/epsilon) * ones(grid_size,grid_size);

    for dims = linspace(0, dims_max, dims_steps)
 
      %Dimensions are whole numbers
      dims = round(dims)
  
      %Calcuate formula values
      C = dims ./ log(4) * ones(grid_size,grid_size);
      ktrials = A + B .* (C.^D) .* E;

      %Set up labels and scales
      if (plotlog)
        kvalues = log10(ktrials);
        zlabel_str = ('log 10 of ktrials');
      else
        kvalues = ktrials;
        zlabel_str = 'ktrials';
      end

      %Plot!
      surf(xx, yy, kvalues);
      
      %Manually manage the axises because auto-scaled z axis is annoying
      %when the plot is animated
      axis([alpha_min, alpha_max, beta_min, beta_max, 0, z_axis_upper_bound], 'manual');

      %Clamp the color map
      caxis([0, z_axis_upper_bound]);
      
      %Increase font size 
      ax = gca();
      set(ax, 'fontsize', 15);
      
      %Set other plotting params
      shading interp;
      titlestring = sprintf('Plot of k trials for dims=%u, epsilon=%f', dims, epsilon);
      title(titlestring);
      xlabel('alpha');
      ylabel('beta');
      zlabel(zlabel_str);

      %Save images to a files
      fname = sprintf('test%03d', frame_count);
      frame_count = frame_count + 1;
      fname
      print(gcf(), '-dpng', fname);
      
    end %end inner loop
  end %end outer loop
  
