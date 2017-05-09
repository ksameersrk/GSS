'use strict';

angular.module('gssApp')
    .config(function ($routeProvider) {
        $routeProvider
            .when('/', {
                templateUrl: 'app/startSimulation/startSimulation.html',
                controller: 'StartSimulationCtrl',
            });
    });