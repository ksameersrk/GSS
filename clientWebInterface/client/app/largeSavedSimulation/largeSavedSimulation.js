'use strict';

angular.module('gssApp')
    .config(function ($routeProvider) {
        $routeProvider
            .when('/largeSavedSimulation', {
                templateUrl: 'app/largeSavedSimulation/largeSavedSimulation.html',
                controller: 'LargeSavedSimulationCtrl',
                resolve: {
                    message: function (authService) {
                        return authService.isSessionValid();
                    }
                }
            });
    });
