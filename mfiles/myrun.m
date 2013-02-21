function [out, all_results]=myrun
javaaddpath('C:\Program Files\Weka-3-7\weka.jar');
wekaOBJ = loadARFF('iris.arff');
[mdata,featureNames,targetNDX,stringVals,relationName] = weka2matlab(wekaOBJ);
data=mdata(:, 1:end-1);
actual_classes=mdata(:, end);

width=1.5;
alpha=.2;
beta=.2;
epsilon=0.01;
subspace_overlap_threshold=.9;
object_overlap_threshold=.5;

idx=0;
for width = linspace(0,4,500)
  result=sepc(data, width, alpha, beta, epsilon, subspace_overlap_threshold, object_overlap_threshold);
  if ~isempty(result)
    idx = idx+1;
    fone=f1(actual_classes, result);
    out(idx, 1) = width;
    out(idx, 2) = fone;
    all_results(idx).result=result;
  end
end
