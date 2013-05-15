%Reset the drawing context
hold off;

%Init random number generators
v = [11, 22, 33];
rand ("state", v);
randn ("state", v); 
 
%Variance 
xx = 1;
yy = 0.1;
 
%Covariance
xy = .31620;

%Positive definite contraints
% |cov(x,y)| < sqroot(var(x) * var(y)) 
%
% or put another way
%
% xy < squareroot(xx * yy)

%Must be real positive number to keep covar matrix positive definite
max_covar=sqrt(xx*yy) 
 
%Distribution mean values
mu = [0, 0];
 
%Create distribution data
A =  mvrnd  (mu, [xx xy; xy yy], 100);

%Format the plot
axis('square')

%Plot all the dat as empty circles
X = A(:,1);
Y = A(:,2);
h = scatter(X,Y);

%Axes
xlabel('Dim 0');
ylabel('Dim 1');
axis([-6, 6, -6, 6], 'square');

%Print correlation coeff
mytext = sprintf("corrcoef = %0.2f", corr(X,Y));
text(-1,6.5,mytext);
