%Funtion
%function plotktrials(grid_size, alpha_min, alpha_max, beta_min, beta_max, dims_max, dims_steps, epsilon_max, epsilon_steps, z_axis_upper_bound, plotlog)

%{
grid_size =
alpha_min =
alpha_max =  
beta_min = 
beta_max = 
dims_max = 
dims_steps =
epsilon_max = 
epsilon_steps = 
z_axis_upper_bound = 
plotlog = true
%}

%Whole space of alpha and beta, increasing dimensionality, fixed epsilon
%Demonstrates large values of ktrials 
grid_size = 50;
alpha_min = 0;
alpha_max = 1;
beta_min = 0;
beta_max = 1;
dims_max = 1000;
dims_steps = 1;
epsilon_max = 0.05;
epsilon_steps = 1;
z_axis_upper_bound = 300;
plotlog = true;

%plotktrials(grid_size, alpha_min, alpha_max, beta_min, beta_max, dims_max, dims_steps, epsilon_max, epsilon_steps, z_axis_upper_bound, plotlog);

%Whole space of alpha and beta, fixed dimensionality, increasing epsilon
dims_max = 10;
dims_steps = 1;
epsilon_max = 0.99;
epsilon_steps = 100;

%Reasonable alpha and beta range 
grid_size = 20;
alpha_min = 0.05;
alpha_max = 0.25;
beta_min = 0.15;
beta_max = 0.45;
dims_max = 50;
dims_steps = 1;
epsilon_max = 0.1;
epsilon_steps = 1;
z_axis_upper_bound = 10;
plotlog = true;

plotktrials(grid_size, alpha_min, alpha_max, beta_min, beta_max, dims_max, dims_steps, epsilon_max, epsilon_steps, z_axis_upper_bound, plotlog);

%avconv -f image2 -i test%03d.png -r 24 video.avi

%avconv -y -f avi -c:v h264 -i test%03d.tif -f avi -me_method full -c:v libx264 -c:a n -b 512k -qcomp 0.5 -r 50 -s 640x480 output_file.avi 