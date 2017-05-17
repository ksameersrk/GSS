'use strict';

// Production specific configuration
// =================================
module.exports = {
  // Server IP
  apiServer : "localhost:5000",

  ip:       process.env.OPENSHIFT_NODEJS_IP ||
            process.env.IP ||
            undefined,

  // Server port
  port:     process.env.OPENSHIFT_NODEJS_PORT ||
            process.env.PORT ||
            80,
};