'use strict';

angular.module('gssApp')
  .service('globalService', function () {
    // AngularJS will instantiate a singleton by calling "new" on this function
    var domainMap=new Array();
    domainMap["http://localhost:9000"]="http://localhost:9000";

    this.getDomainUrl=function(){
      var browserurl= window.location.origin;
      return domainMap[browserurl];
    }
  });
