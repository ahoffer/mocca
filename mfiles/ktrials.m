function num_trials=ktrials(epsilon, alpha, beta, num_dims)

  num_trials = round( 1 + 4/alpha * (num_dims/log(4))^(log(alpha)/log(beta)) * log(1/epsilon) );