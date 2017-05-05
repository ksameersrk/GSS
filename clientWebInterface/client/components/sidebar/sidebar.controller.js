'use strict';

angular.module('gssApp')
  .controller('SidebarCtrl', function ($scope, $location, $rootScope) {
  	$rootScope.selectedTab = 1;
    $scope.selectTab = function(tab){
      // console.log("before selectedTab value "+tab +" "+$rootScope.selectedTab);
      $rootScope.selectedTab = tab;
      sessionStorage.setItem("selectedTab", $rootScope.selectedTab);
      console.log("after selectedTab value "+sessionStorage.getItem("selectedTab"));
    }
  });