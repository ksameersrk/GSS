'use strict';

angular.module('gssApp')
    .controller('NavbarCtrl', function ($scope, $location, $routeParams, $window, $rootScope, $cookies) {
        $scope.menu = [{
            'title': 'Home',
            'link': '/'
        }];

        $scope.isCollapsed = true;

        $scope.isActive = function (route) {
            return route === $location.path();
        };
    });