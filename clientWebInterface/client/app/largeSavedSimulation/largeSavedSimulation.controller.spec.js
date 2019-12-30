'use strict';

describe('Controller: LargeSavedSimulationCtrl', function () {

  // load the controller's module
  beforeEach(module('gssApp'));

  var MaintainanceCtrl, scope;

  // Initialize the controller and a mock scope
  beforeEach(inject(function ($controller, $rootScope) {
    scope = $rootScope.$new();
    MaintainanceCtrl = $controller('LargeSavedSimulationCtrl', {
      $scope: scope
    });
  }));

  it('should ...', function () {
    expect(1).toEqual(1);
  });
});
