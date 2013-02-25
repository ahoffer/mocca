
hold off
shg

%Distrubtion variances
xx = 2;
yy = 2;
xy = 2;

%Create distribution data
A = mvnrnd([0 0] , [xx xy; xy yy], 300);

%Get principal components of data
coeff = pca(A);

%Plot distribution
h = plot(A(:,1),A(:,2),'b.');
hold on

%Do crazy stuff to plot principal components
temp=zeros(2,4);
temp(:, 2:2:end) = coeff;
scalefactor=10; %Make the lines longer
temp=scalefactor * temp;
X=temp(1:2:end);
Y=temp(2:2:end);
plot(X, Y, 'r', 'LineWidth', 2);

%Rotate the data to align with the first principal
%component

B = A*coeff;
plot(B(:,1), B(:,2), 'g.');

axis('square')
grid on;
xlabel('X Axis');
ylabel('Y Axis');
xlim([-10, 10]);
ylim([-10 10])
pause(0.1)

