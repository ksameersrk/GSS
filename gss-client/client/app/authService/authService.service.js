'use strict';

angular.module('gssApp')
    .factory('authService', function ($q, $rootScope, $window, $location, $cookies) {
        $rootScope.appUrl = location.origin;
        var authInterceptorServiceFactory = {};

        var _request = function (config) {
            config.headers = config.headers || {};
            return config;
        }


        authInterceptorServiceFactory.authenticateUser = function () {
            return $q.resolve("Resolved IV Value");
        }

        authInterceptorServiceFactory.isSessionValid = function(){
         return true;
       }

        authInterceptorServiceFactory.request = _request;
        return authInterceptorServiceFactory;
    });