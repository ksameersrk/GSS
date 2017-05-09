'use strict';

angular.module('gssApp')
  .controller('MainCtrl', function ($scope, $http, globalService, $location) {

    console.log("Inside the Main controller....");

    $scope.changeRoute = function() {
        $location.path("/submitRequest")
    };
    $scope.closeAlert = function(){
            $scope.alertMessage=false;
    }

  });
