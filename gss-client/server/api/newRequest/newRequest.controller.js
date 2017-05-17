'use strict';

var _ = require('lodash');

// Get list of newRequests
exports.index = function(req, res) {
  res.json({
  	"status":"success", 
  	"status_message":"Succesfully submitted the new request."
  	
  });
};