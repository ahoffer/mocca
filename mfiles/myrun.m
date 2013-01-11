function myrun
  global x;
  x = [ 
  50,75,0;
  100,100,0;
  150,85,5000;
  0,0,0;
  100,100,100;
  10,10,10];

  [dataName, attributeName, attributeType, data] = arffread("default.arff");
  
  sepc(data, 50, .2, 0.8)
