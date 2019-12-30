'use strict';

describe('Service: globalService', function () {

  // load the service's module
  beforeEach(module('gssApp'));

  // instantiate service
  var globalService;
  beforeEach(inject(function (_globalService_) {
    globalService = _globalService_;
  }));

  it('should do something', function () {
    expect(!!globalService).toBe(true);
  });

});
