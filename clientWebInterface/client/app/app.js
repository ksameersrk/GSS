'use strict';

angular.module('gssApp', [
    'ngCookies',
    'ngResource',
    'ngSanitize',
    'ngRoute',
    'ngDialog',
    'angularUtils.directives.dirPagination',
    'ui.bootstrap',
    'monospaced.elastic',
    'chart.js'
])
    .run(function ($rootScope, $location, $window, $timeout, $http, $cookies) {

        $http.defaults.useXDomain = true;
        $http.defaults.headers.common['Access-Control-Allow-Credentials'] = true;
        $http.defaults.headers.common['Access-Control-Allow-Origin'] = "*";
        $http.defaults.headers.common['Access-Control-Allow-Methods'] = "GET, POST, PUT, DELETE, OPTIONS";
        $http.defaults.headers.common['Access-Control-Allow-Headers'] = "Origin, X-Requested-With, Content-Type, Accept, Authorization";
        // to set auth token value in the localstorage
        $rootScope.appUrl = location.origin;
        console.log("Welcome !");
    })
    .config(function ($routeProvider, $locationProvider, $httpProvider) {
        $routeProvider
            .otherwise({
                redirectTo: '/'
            });

        $locationProvider.html5Mode({
                 enabled: true,
                 requireBase: false
          });

        $httpProvider.defaults.useXDomain = true;
        $httpProvider.defaults.headers.common['Access-Control-Allow-Credentials'] = true;
        $httpProvider.defaults.headers.common['Access-Control-Allow-Origin'] = "*";
        $httpProvider.defaults.headers.common['Access-Control-Allow-Methods'] = "GET, POST, PUT, DELETE, OPTIONS";
        $httpProvider.defaults.headers.common['Access-Control-Allow-Headers'] = "Origin, X-Requested-With, Content-Type, Accept, Authorization";

        $httpProvider.defaults.headers.common['Accept'] = 'application/json, text/javascript';
        $httpProvider.defaults.headers.common['Content-Type'] = 'application/json; charset=utf-8';
    });