function discrim_set_size=s_est(num_dims, beta)

  discrim_set_size = round(log(num_dims/log(4)) / log(1/beta));