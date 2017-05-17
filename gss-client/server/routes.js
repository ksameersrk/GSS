/**
 * Main application routes
 */

'use strict';

var errors = require('./components/errors');
var path = require('path');
var crypto = require('crypto');
var httpProxy = require('http-proxy');
var apiProxy = httpProxy.createProxyServer();
var config = require('./config/environment');
var bodyParser = require('body-parser');
var request = require('request');
var moment = require('moment-timezone');

module.exports = function(app) {

  app.use(bodyParser.urlencoded({ extended: false }));
  app.use(bodyParser.json());
  
  // All other routes should redirect to the index.html
  app.route('/')
    .get(function(req, res) {
      console.log("GET METHOD");
      res.sendfile(path.resolve(app.get('appPath') + '/index.html'));
    });

  app.route('/*')
    .get(function(req, res) {
      console.log("GET METHOD");
      res.sendfile(req.originalUrl);
    });
};