'use strict';

// Development specific configuration
// ==================================
module.exports = {
	//Server API
  apiServer : "localhost",
  
  ip:       process.env.OPENSHIFT_NODEJS_IP ||
            process.env.IP ||
            undefined,

  // Server port
  port:     process.env.OPENSHIFT_NODEJS_PORT ||
            process.env.PORT ||
            5000,
};
