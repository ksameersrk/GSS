/**
 * Express configuration
 */

'use strict';

var express = require('express');
var favicon = require('serve-favicon');
var morgan = require('morgan');
var compression = require('compression');
var methodOverride = require('method-override');
var cookieParser = require('cookie-parser');
var errorHandler = require('errorhandler');
var path = require('path');
var config = require('./environment');
var url  = require('url');

module.exports = function(app) {
  var env = app.get('env');

  app.set('views', config.root + '/server/views');
  app.engine('html', require('ejs').renderFile);
  app.set('view engine', 'html');
  app.use(compression());
  app.use(methodOverride());
  app.use(cookieParser());
  
  if ('production' === env) {
    app.use(favicon(path.join(config.root, 'public', 'favicon.ico')));
    app.use('/',function (req, res, next) {
      console.log("In production, req : "+req.method);
      next();
    });

    app.use(express.static(path.join(config.root, 'public')));
    app.set('appPath', config.root + '/public');
    app.use(morgan('dev'));
  }

  if ('development' === env || 'test' === env) {
    //app.use(require('connect-livereload')());
    app.use('/',function (req, res, next) {
      if(req.originalUrl === '/' && req.method === "GET"){
        console.log("Cookies: ", req.cookies["userId"]);
        if(req.cookies["userId"]){
          console.log("entered if");
          next();
        }else{
          console.log("entered else");
          //Dev redirection
          res.redirect('http://iocmaintenance-dev.sso.intuit.com');

          //Pro redirection
          //res.redirect('https://iocmaintenance.sso.intuit.com');
        }
      }else{
        console.log("entered post")
        next();
      }
    });
    app.use(express.static(path.join(config.root, '.tmp')));
    app.use(express.static(path.join(config.root, 'client')));
    app.set('appPath', 'client');
    app.use(morgan('dev'));
    app.use(errorHandler()); // Error handler - has to be last
  }
};