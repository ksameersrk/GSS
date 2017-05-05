'use strict';

angular.module('gssApp')
    .config(function ($routeProvider) {
        $routeProvider
            .when('/submitRequest', {
                templateUrl: 'app/startSimulation/startSimulation.html',
                controller: 'StartSimulationCtrl',
                resolve: {
                    message: function (authService) {
                        return authService.isSessionValid();
                    }
                }
            })
    });