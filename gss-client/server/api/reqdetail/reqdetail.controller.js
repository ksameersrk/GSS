'use strict';

var _ = require('lodash');

// Get list of reqdetails
exports.index = function(req, res) {
  	res.json({
   "status":"SUCCESS",
   "total_count":1,
   "maintenance_request":{
      "justification":"timepass",
      "status":"New",
      "request_id":122,
      "user_id":"ntantry",
      "user_name":"Nagaraja Tantry",
      "user_email":"Nagaraja_Tantry@intuit.com",
      "requested_mode":"enable",
      "last_modified_datetime":"ads",
      "created_datetime":"2015-08-23 20:36:21",
      "scheduled_time":"2015-08-24 12:30:00",
      "hosts":[
         {
            "status":"New",
            "host_id":156,
            "host_name":"dummy",
            "host_ip_address":"10.144.0.2",
            "pre_request_mode":"dummy",
            "post_request_mode":232,
            "status_reason":"dasas",
            "model_handle":"dummy"
         },
         {
            "status":"New",
            "host_id":157,
            "host_name":"dummy",
            "host_ip_address":"10.144.0.5",
            "pre_request_mode":"dummy",
            "post_request_mode":"null",
            "status_reason":"null",
            "model_handle":"dummy"
         },
         {
            "status":"New",
            "host_id":158,
            "host_name":"dummy",
            "host_ip_address":"10.144.0.0",
            "pre_request_mode":"dummy",
            "post_request_mode":"as",
            "status_reason":"hghghg",
            "model_handle":"dummy"
         }
      ]
   }
			}
	);
};