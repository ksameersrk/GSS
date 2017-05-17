'use strict';

angular.module('gssApp', [
    'ngCookies',
    'ngResource',
    'ngSanitize',
    'ngRoute',
    'ngDialog',
    'angularUtils.directives.dirPagination',
    'ui.bootstrap',
    'monospaced.elastic',
    'chart.js'
])
    .run(["$rootScope", "$location", "$window", "$timeout", "$http", "$cookies", function ($rootScope, $location, $window, $timeout, $http, $cookies) {

        $http.defaults.useXDomain = true;
        $http.defaults.headers.common['Access-Control-Allow-Credentials'] = true;
        $http.defaults.headers.common['Access-Control-Allow-Origin'] = "*";
        $http.defaults.headers.common['Access-Control-Allow-Methods'] = "GET, POST, PUT, DELETE, OPTIONS";
        $http.defaults.headers.common['Access-Control-Allow-Headers'] = "Origin, X-Requested-With, Content-Type, Accept, Authorization";
        // to set auth token value in the localstorage
        $rootScope.appUrl = location.origin;
        console.log("Welcome !");
    }])
    .config(["$routeProvider", "$locationProvider", "$httpProvider", function ($routeProvider, $locationProvider, $httpProvider) {
        $routeProvider
            .otherwise({
                redirectTo: '/'
            });

        $locationProvider.html5Mode({
                 enabled: true,
                 requireBase: false
          });

        $httpProvider.defaults.useXDomain = true;
        $httpProvider.defaults.headers.common['Access-Control-Allow-Credentials'] = true;
        $httpProvider.defaults.headers.common['Access-Control-Allow-Origin'] = "*";
        $httpProvider.defaults.headers.common['Access-Control-Allow-Methods'] = "GET, POST, PUT, DELETE, OPTIONS";
        $httpProvider.defaults.headers.common['Access-Control-Allow-Headers'] = "Origin, X-Requested-With, Content-Type, Accept, Authorization";

        $httpProvider.defaults.headers.common['Accept'] = 'application/json, text/javascript';
        $httpProvider.defaults.headers.common['Content-Type'] = 'application/json; charset=utf-8';
    }]);
'use strict';

angular.module('gssApp')
    .factory('authService', ["$q", "$rootScope", "$window", "$location", "$cookies", function ($q, $rootScope, $window, $location, $cookies) {
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
    }]);
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

'use strict';

angular.module('gssApp')
  .controller('LargeSavedSimulationCtrl', ["$rootScope", "$scope", "$http", "globalService", "$location", function ($rootScope, $scope, $http, globalService, $location) {
    $rootScope.selectedTab = 2;

    $scope.changeRoute = function() {
        $location.path("/submitSitescopeRequest")
    };

    console.log("came inside the maintainance request controller .... ");
    console.log("printing the root scope....");

    var url = 'http://localhost:5000/get_large_datasets';
    //$scope.url = globalService.getDomainUrl()+'/api/processes';
    $scope.response = null;
    $scope.pageSize = 10;
    $scope.sortType     = 'request_id'; // set the default sort type
    $scope.sortReverse  = true;  // set the default sort order
    $scope.currentPage=1;
    $scope.refreshFlag = true;
    $scope.searchInputValue = "";

    $scope.message = "";
    $scope.alertMessage = "";
    $scope.pageLoading = false;
    $scope.requests = [];
    $scope.mouse_over_data = "Some Random Data";
    $scope.current_row=null;

    $scope.lc_ae_path = {};
    $scope.lc_at_path = {};
    $scope.lc_ie_path = {};
    $scope.lc_it_path = {};
    $scope.lc_tp_path = {};
    $scope.pie_with_path = {};
    $scope.pie_without_path = {};
    $scope.pie_both_path = {};
    $scope.output_shown = false;

    $scope.scenario = "0";

    $scope.setCurrentRow = function(data)
    {
        $scope.pageLoading = true;
        $scope.output_shown = false;

        $scope.current_row=data;
        $scope.scenario = data.scenario;

        $scope.lc_ae_path.lineLabels = data.lc_ae_path.label;
        $scope.lc_ae_path.lineData = data.lc_ae_path.data;
        $scope.lc_ae_path.lineSeries = data.lc_ae_path.series;
        $scope.lc_ae_path.lineOptions = $scope.getOptions("Disk Name", "Power Consumption in Joules");

        $scope.lc_at_path.lineLabels = data.lc_at_path.label;
        $scope.lc_at_path.lineData = data.lc_at_path.data;
        $scope.lc_at_path.lineSeries = data.lc_at_path.series;
        $scope.lc_at_path.lineOptions = $scope.getOptions("Disk Name", "Time in Seconds");

        $scope.lc_ie_path.lineLabels = data.lc_ie_path.label;
        $scope.lc_ie_path.lineData = data.lc_ie_path.data;
        $scope.lc_ie_path.lineSeries = data.lc_ie_path.series;
        $scope.lc_ie_path.lineOptions = $scope.getOptions("Disk Name", "Power Consumption in Joules");

        $scope.lc_it_path.lineLabels = data.lc_it_path.label;
        $scope.lc_it_path.lineData = data.lc_it_path.data;
        $scope.lc_it_path.lineSeries = data.lc_it_path.series;
        $scope.lc_it_path.lineOptions = $scope.getOptions("Disk Name", "Time in Seconds");

        $scope.lc_tp_path.lineLabels = data.lc_tp_path.label;
        $scope.lc_tp_path.lineData = data.lc_tp_path.data;
        $scope.lc_tp_path.lineSeries = data.lc_tp_path.series;
        $scope.lc_tp_path.lineOptions = $scope.getOptions("Disk Name", "Power Consumption in Joules");

        //pie charts
        

        if($scope.scenario === '1')
        {
            $scope.pie_without_path.pieLabels = data.pie_without_path.label;
            $scope.pie_without_path.pieData = data.pie_without_path.data;
            $scope.pie_without_path.pieOptions = { legend: { display: true } };
        }
        else if($scope.scenario === "2")
        {
            $scope.pie_with_path.pieLabels = data.pie_with_path.label;
            $scope.pie_with_path.pieData = data.pie_with_path.data;
            $scope.pie_with_path.pieOptions = { legend: { display: true } };
        }
        else if($scope.scenario === "3")
        {
            $scope.pie_without_path.pieLabels = data.pie_without_path.label;
            $scope.pie_without_path.pieData = data.pie_without_path.data;
            $scope.pie_without_path.pieOptions = { legend: { display: true } };

            $scope.pie_with_path.pieLabels = data.pie_with_path.label;
            $scope.pie_with_path.pieData = data.pie_with_path.data;
            $scope.pie_with_path.pieOptions = { legend: { display: true } };

            $scope.pie_both_path.pieLabels = data.pie_both_path.label;
            $scope.pie_both_path.pieData = data.pie_both_path.data;
            $scope.pie_both_path.pieOptions = { legend: { display: true } };
        }
        
        /*
        $scope.pieLabels = data.pieChart.label;
        $scope.pieData = data.pieChart.data;
        $scope.pieOptions = { legend: { display: true } };

        $scope.lineLabels = data.lineChart.label;
        $scope.lineData = data.lineChart.data;
        $scope.lineSeries = data.lineChart.series;
        $scope.lineOptions = $scope.getOptions("Node number", "Power Consumption in Joules");
        */

        $scope.outputLogs = data.logs;

        $scope.pageLoading = false;
        $scope.output_shown = true;
    }

    $scope.getOptions=function(x_label, y_label){
        return {
                  scales: {
                    xAxes: [{
                      scaleLabel: {
                        display: true,
                        labelString: x_label
                      }
                    }],
                    yAxes: [{
                      scaleLabel: {
                        display: true,
                        labelString: y_label
                      }
                    }]
                  },
                  legend: { display: true } 
                };
    }

    $scope.closeAlert=function(){
        $scope.alertMessage=false;
        $rootScope.message=false;
    }

    $scope.getData=function(){
        $scope.pageLoading = true;
        $http.get(url).
        then(function(response) {
            console.log("response #: "+JSON.stringify(response.data));
            $scope.tableData = response.data.tableData;
            $scope.refreshFlag = true;
            $scope.pageLoading = false;
        }, function(response) {
            $scope.pageLoading = false;
            if (response && response.status === 500)
            {
                $scope.alertMessage =  "There is some exception while fetching the requests.";
            }
            else if (response && response.status === 401)
            {
                $scope.alertMessage =  "Invalid Ticket, Please login again";
                return $rootScope.redirectToSSO();
            }
            else if (response && response.status === 503)
            {
                $scope.alertMessage =  "There is some problem connecting to Auth Server";
            }
            else
            {
                $scope.alertMessage =  "Request failed - "+response.statusText;
            }

            $scope.status = response.status;
        });
    }

    $scope.getData();

    }]);
'use strict';

angular.module('gssApp')
    .config(["$routeProvider", function ($routeProvider) {
        $routeProvider
            .when('/largeSavedSimulation', {
                templateUrl: 'app/largeSavedSimulation/largeSavedSimulation.html',
                controller: 'LargeSavedSimulationCtrl',
                resolve: {
                    message: ["authService", function (authService) {
                        return authService.isSessionValid();
                    }]
                }
            });
    }]);

'use strict';

angular.module('gssApp')
  .controller('MainCtrl', ["$scope", "$http", "globalService", "$location", function ($scope, $http, globalService, $location) {

    console.log("Inside the Main controller....");

    $scope.changeRoute = function() {
        $location.path("/submitRequest")
    };
    $scope.closeAlert = function(){
            $scope.alertMessage=false;
    }

  }]);

'use strict';

angular.module('gssApp')
    .config(["$routeProvider", function ($routeProvider) {
        $routeProvider
            .when('/', {
                templateUrl: 'app/startSimulation/startSimulation.html',
                controller: 'StartSimulationCtrl',
            });
    }]);
'use strict';

angular.module('gssApp')

  .factory('focus', ["$timeout", "$window", function($timeout, $window) {
    return function(id) {
      // timeout makes sure that is invoked after any other event has been triggered.
      // e.g. click events that need to run before the focus or
      // inputs elements that are in a disabled state but are enabled when those events
      // are triggered.
      $timeout(function() {
        var element = $window.document.getElementById(id);
        if(element)
          element.focus();
      });
    };
  }])

  .directive('eventFocus', ["focus", function(focus) {
    return function(scope, elem, attr) {
      elem.on(attr.eventFocus, function() {
        focus(attr.eventFocusId);
      });
      
      // Removes bound events in the element itself
      // when the scope is destroyed
      scope.$on('$destroy', function() {
        element.off(attr.eventFocus);
      });
    };
  }])
  
   
    .controller('StartSimulationCtrl', ["$scope", "$http", "globalService", "$location", "$filter", "ngDialog", "$rootScope", "$q", "$window", "$routeParams", function ($scope, $http, globalService, $location, $filter, ngDialog, $rootScope, $q, $window, $routeParams) {

        $rootScope.selectedTab = 1;

        //setting the default value for the view
        var reqJson;
        $scope.pageLoading = false;
        $scope.workload_type = "";
        $scope.output_logs = "";
        $scope.output_shown = false;
        $scope.changeRoute = function (location) {
            $location.path("/submitRequest")
        };

        $scope.isStringNotEmpty = function(value) {
            return value !== "";
        }
        $scope.submitForm = function (isValid) {
            // check to make sure the form is completely valid
            $scope.errors = [];
            var defer = $q.defer();

            
            if($scope.num_nodes === null || $scope.num_nodes === undefined || $scope.num_nodes === "") {
                $scope.errors.push({"key": "VALIDATION_ERROR","value": "Please select number of nodes."}); 
            }

            if($scope.scenario === null || $scope.scenario === undefined || $scope.scenario === "") {
                $scope.errors.push({"key": "VALIDATION_ERROR","value": "Please select scenarios."}); 
            }
            else
            {
                if($scope.scenario != "1" && ($scope.ssd_disk_type === null || $scope.ssd_disk_type === undefined || $scope.ssd_disk_type === "")) {
                    $scope.errors.push({"key": "VALIDATION_ERROR","value": "Please select SSD disk type."}); 
                }
                else if($scope.scenario === "1")
                {
                    $scope.ssd_disk_type = "0"; //default
                }
            }

            if($scope.num_of_operations === null || $scope.num_of_operations === undefined || $scope.num_of_operations === "") {
                $scope.errors.push({"key": "VALIDATION_ERROR","value": "Please select number of operations."}); 
            }

            if($scope.num_of_replicas === null || $scope.num_of_replicas === undefined || $scope.num_of_replicas === "") {
                $scope.errors.push({"key": "VALIDATION_ERROR","value": "Please select number of replicas."}); 
            }

            if($scope.caching_mechanism === null || $scope.caching_mechanism === undefined || $scope.caching_mechanism === "") {
                $scope.errors.push({"key": "VALIDATION_ERROR","value": "Please select caching mechanism."}); 
            }

            if($scope.disk_type === null || $scope.disk_type === undefined || $scope.disk_type === "") {
                $scope.errors.push({"key": "VALIDATION_ERROR","value": "Please select disk type."}); 
            }

            if($scope.workload_type === null || $scope.workload_type === undefined || $scope.workload_type === "") {
                $scope.errors.push({"key": "VALIDATION_ERROR","value": "Please select workload type."}); 
            }
            else
            {
                if($scope.workload_type === 'predefined_workload' && ($scope.predefined_workload_type == null || $scope.predefined_workload_type === undefined)) {
                    $scope.errors.push({"key": "VALIDATION_ERROR","value": "Please select predefined workload type."});   
                }
                else
                {
                    if($scope.predefined_workload_type == null || $scope.predefined_workload_type === undefined)
                    {
                        $scope.predefined_workload_type = "0";  
                    }
                    
                }

                if($scope.workload_type === 'manual' && ($scope.manual_textarea == null || $scope.manual_textarea === undefined)) {
                    $scope.errors.push({"key": "VALIDATION_ERROR","value": "Please select predefined manual type."});   
                }
                else
                {
                    if($scope.manual_textarea == null || $scope.manual_textarea === undefined)
                    {
                        $scope.manual_textarea = "";
                    }
                    
                }
            }

            
            

            $scope.lc_ae_path = {};
            $scope.lc_at_path = {};
            $scope.lc_ie_path = {};
            $scope.lc_it_path = {};
            $scope.lc_tp_path = {};
            $scope.pie_with_path = {};
            $scope.pie_without_path = {};
            $scope.pie_both_path = {};
            $scope.output_shown = false;

            console.log($scope.num_nodes);
            console.log($scope.scenario);
            console.log($scope.num_of_operations);
            console.log($scope.num_of_replicas);
            console.log($scope.caching_mechanism);
            console.log($scope.disk_type);
            console.log($scope.workload_type);
            console.log($scope.predefined_workload_type);
            console.log($scope.manual_textarea);

            $scope.alertMessage = $scope.errors.length > 0;

            var reqObj = {};

            reqObj.totalNoOfNodes = $scope.num_nodes;
            reqObj.scenario = $scope.scenario;
            reqObj.numberOfOperations = $scope.num_of_operations;
            reqObj.noOfReplicas = $scope.num_of_replicas;
            reqObj.cachingMechanism = $scope.caching_mechanism;
            reqObj.hddDiskType = $scope.disk_type;
            reqObj.ssdDiskType = $scope.ssd_disk_type;
            reqObj.workloadType = $scope.workload_type;
            reqObj.predefindedWorkloadNumber = $scope.predefined_workload_type;
            reqObj.manualTextarea = $scope.manual_textarea;

            console.log("Printing the request json Outside");
            reqJson = JSON.stringify(reqObj);
            console.log(reqJson);

            if ($scope.errors.length === 0){
                defer.resolve("Succesfully executed");
                console.log("Came inside if");
                defer.promise.then(function(string){
                    $scope.pageLoading =  true;                   
                    reqJson = JSON.stringify(reqObj);
                    console.log("Printing the request json");
                    console.log(reqJson);

                    $http.post('http://localhost:5000/start_simulation', reqJson).
                        success(function (data) {
                            console.log("Success : data : "+JSON.stringify(data));
                            

                            /*
                            //pie chart
                            $scope.pieLabels = ["Download Sales", "In-Store Sales", "Mail-Order Sales"];
                            $scope.pieData = [30, 50, 10];

                            //line chart
                            $scope.lineLabels = [0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0];
                            $scope.lineData = [
                              [7e-05, 0.000165, 0.0, 0.012063, 3.42879, 0.0, 19.364202, 0.0, 0.002183, 0.011806]
                            ];
                            $scope.lineSeries = ["ABC"];
                            

                            var options = {
                              scales: {
                                xAxes: [{
                                  scaleLabel: {
                                    display: true,
                                    labelString: 'Disks Number'
                                  }
                                }],
                                yAxes: [{
                                  scaleLabel: {
                                    display: true,
                                    labelString: 'Power Consumption in Joules'
                                  }
                                }]
                              },
                              legend: { display: true } 
                            };
                            */

                            /*
                            lc_ae_path = rootPath+'line_chart_active_energy.json'
                            lc_at_path = rootPath+'line_chart_active_time.json'
                            lc_ie_path = rootPath+'line_chart_idle_energy.json'
                            lc_it_path = rootPath+'line_chart_idle_time.json'
                            lc_tp_path = rootPath+'line_chart_total_power.json'
                            pie_with_path = rootPath+'pieChartActiveVsSpundownWithStagingDisk.json'
                            pie_without_path = rootPath+'pieChartActiveVsSpundownWithoutStagingDisk.json'
                            pie_both_path = rootPath+'pieChartWithVsWithoutSSD.json'
                            */

                            //$scope.lc_ae_path_options = $scope.getOptions("Node number", "Power Consumption in Joules");

                            //line charts
                            

                            $scope.lc_ae_path.lineLabels = data.lc_ae_path.label;
                            $scope.lc_ae_path.lineData = data.lc_ae_path.data;
                            $scope.lc_ae_path.lineSeries = data.lc_ae_path.series;
                            $scope.lc_ae_path.lineOptions = $scope.getOptions("Disk Name", "Power Consumption in Joules");

                            $scope.lc_at_path.lineLabels = data.lc_at_path.label;
                            $scope.lc_at_path.lineData = data.lc_at_path.data;
                            $scope.lc_at_path.lineSeries = data.lc_at_path.series;
                            $scope.lc_at_path.lineOptions = $scope.getOptions("Disk Name", "Time in Seconds");

                            $scope.lc_ie_path.lineLabels = data.lc_ie_path.label;
                            $scope.lc_ie_path.lineData = data.lc_ie_path.data;
                            $scope.lc_ie_path.lineSeries = data.lc_ie_path.series;
                            $scope.lc_ie_path.lineOptions = $scope.getOptions("Disk Name", "Power Consumption in Joules");

                            $scope.lc_it_path.lineLabels = data.lc_it_path.label;
                            $scope.lc_it_path.lineData = data.lc_it_path.data;
                            $scope.lc_it_path.lineSeries = data.lc_it_path.series;
                            $scope.lc_it_path.lineOptions = $scope.getOptions("Disk Name", "Time in Seconds");

                            $scope.lc_tp_path.lineLabels = data.lc_tp_path.label;
                            $scope.lc_tp_path.lineData = data.lc_tp_path.data;
                            $scope.lc_tp_path.lineSeries = data.lc_tp_path.series;
                            $scope.lc_tp_path.lineOptions = $scope.getOptions("Disk Name", "Power Consumption in Joules");

                            //pie charts
                            

                            if($scope.scenario === '1')
                            {
                                $scope.pie_without_path.pieLabels = data.pie_without_path.label;
                                $scope.pie_without_path.pieData = data.pie_without_path.data;
                                $scope.pie_without_path.pieOptions = { legend: { display: true } };
                            }
                            else if($scope.scenario === "2")
                            {
                                $scope.pie_with_path.pieLabels = data.pie_with_path.label;
                                $scope.pie_with_path.pieData = data.pie_with_path.data;
                                $scope.pie_with_path.pieOptions = { legend: { display: true } };
                            }
                            else if($scope.scenario === "3")
                            {
                                $scope.pie_without_path.pieLabels = data.pie_without_path.label;
                                $scope.pie_without_path.pieData = data.pie_without_path.data;
                                $scope.pie_without_path.pieOptions = { legend: { display: true } };

                                $scope.pie_with_path.pieLabels = data.pie_with_path.label;
                                $scope.pie_with_path.pieData = data.pie_with_path.data;
                                $scope.pie_with_path.pieOptions = { legend: { display: true } };

                                $scope.pie_both_path.pieLabels = data.pie_both_path.label;
                                $scope.pie_both_path.pieData = data.pie_both_path.data;
                                $scope.pie_both_path.pieOptions = { legend: { display: true } };
                            }
                            
                            /*
                            $scope.pieLabels = data.pieChart.label;
                            $scope.pieData = data.pieChart.data;
                            $scope.pieOptions = { legend: { display: true } };

                            $scope.lineLabels = data.lineChart.label;
                            $scope.lineData = data.lineChart.data;
                            $scope.lineSeries = data.lineChart.series;
                            $scope.lineOptions = $scope.getOptions("Node number", "Power Consumption in Joules");
                            */

                            $scope.outputLogs = data.logs;

                            $scope.pageLoading = false;
                            $scope.output_shown = true;

                        }).error(function (response, status, headers, config) {
                            console.log("response : "+response+" : "+status);
                            $scope.errors = response.errors;
                            
                    })});
            }
        };

        $scope.getOptions=function(x_label, y_label){
            return {
                      scales: {
                        xAxes: [{
                          scaleLabel: {
                            display: true,
                            labelString: x_label
                          }
                        }],
                        yAxes: [{
                          scaleLabel: {
                            display: true,
                            labelString: y_label
                          }
                        }]
                      },
                      legend: { display: true } 
                    };
        }
    
        $scope.closeAlert = function () {
            $scope.alertMessage = false;
        }

        $scope.displayModal = function () {

            ngDialog.open({template: 'app/modal/modal.html', scope: $scope});
        }


        $scope.resetForm = function (maintainanceForm) {
            $scope.alertMessage = false;
            $scope.workload_type = "";
            $scope.output_logs = "";
            $scope.output_shown = false;
            $scope.lc_ae_path = {};
            $scope.lc_at_path = {};
            $scope.lc_ie_path = {};
            $scope.lc_it_path = {};
            $scope.lc_tp_path = {};
            $scope.pie_with_path = {};
            $scope.pie_without_path = {};
            $scope.pie_both_path = {};
            $scope.output_shown = false;

            $scope.num_nodes="";
            $scope.scenario="";
            $scope.num_of_operations="";
            $scope.num_of_replicas="";
            $scope.caching_mechanism="";
            $scope.disk_type="";
            $scope.workload_type="";
            $scope.predefined_workload_type="";
            $scope.manual_textarea="";
        }


        $scope.setWorkloadType = function(d_type){
            $scope.workload_type = d_type;
            console.log("Workload type: "+$scope.workload_type);
        }
    }]);
'use strict';

angular.module('gssApp')
    .config(["$routeProvider", function ($routeProvider) {
        $routeProvider
            .when('/submitRequest', {
                templateUrl: 'app/startSimulation/startSimulation.html',
                controller: 'StartSimulationCtrl',
                resolve: {
                    message: ["authService", function (authService) {
                        return authService.isSessionValid();
                    }]
                }
            })
    }]);
'use strict';

angular.module('gssApp')
  .factory('Modal', ["$rootScope", "$modal", function ($rootScope, $modal) {
    /**
     * Opens a modal
     * @param  {Object} scope      - an object to be merged with modal's scope
     * @param  {String} modalClass - (optional) class(es) to be applied to the modal
     * @return {Object}            - the instance $modal.open() returns
     */
    function openModal(scope, modalClass) {
      var modalScope = $rootScope.$new();
      scope = scope || {};
      modalClass = modalClass || 'modal-default';

      angular.extend(modalScope, scope);

      return $modal.open({
        templateUrl: 'components/modal/modal.html',
        windowClass: modalClass,
        scope: modalScope
      });
    }

    // Public API here
    return {

      /* Confirmation modals */
      confirm: {

        /**
         * Create a function to open a delete confirmation modal (ex. ng-click='myModalFn(name, arg1, arg2...)')
         * @param  {Function} del - callback, ran when delete is confirmed
         * @return {Function}     - the function to open the modal (ex. myModalFn)
         */
        delete: function(del) {
          del = del || angular.noop;

          /**
           * Open a delete confirmation modal
           * @param  {String} name   - name or info to show on modal
           * @param  {All}           - any additional args are passed staight to del callback
           */
          return function() {
            var args = Array.prototype.slice.call(arguments),
                name = args.shift(),
                deleteModal;

            deleteModal = openModal({
              modal: {
                dismissable: true,
                title: 'Confirm Delete',
                html: '<p>Are you sure you want to delete <strong>' + name + '</strong> ?</p>',
                buttons: [{
                  classes: 'btn-danger',
                  text: 'Delete',
                  click: function(e) {
                    deleteModal.close(e);
                  }
                }, {
                  classes: 'btn-default',
                  text: 'Cancel',
                  click: function(e) {
                    deleteModal.dismiss(e);
                  }
                }]
              }
            }, 'modal-danger');

            deleteModal.result.then(function(event) {
              del.apply(event, args);
            });
          };
        }
      }
    };
  }]);

'use strict';

angular.module('gssApp')
    .controller('NavbarCtrl', ["$scope", "$location", "$routeParams", "$window", "$rootScope", "$cookies", function ($scope, $location, $routeParams, $window, $rootScope, $cookies) {
        $scope.menu = [{
            'title': 'Home',
            'link': '/'
        }];

        $scope.isCollapsed = true;

        $scope.isActive = function (route) {
            return route === $location.path();
        };
    }]);
'use strict';

angular.module('gssApp')
  .controller('SidebarCtrl', ["$scope", "$location", "$rootScope", function ($scope, $location, $rootScope) {
  	$rootScope.selectedTab = 1;
    $scope.selectTab = function(tab){
      // console.log("before selectedTab value "+tab +" "+$rootScope.selectedTab);
      $rootScope.selectedTab = tab;
      sessionStorage.setItem("selectedTab", $rootScope.selectedTab);
      console.log("after selectedTab value "+sessionStorage.getItem("selectedTab"));
    }
  }]);
angular.module('gssApp').run(['$templateCache', function($templateCache) {
  'use strict';

  $templateCache.put('app/largeSavedSimulation/largeSavedSimulation.html',
    "<div ng-show=!loginFlag id=wrapper><div id=page-wrapper style=\"min-height: 1000px\"><div ng-show=pageLoading><div ng-include=\"'components/loading/loading.html'\"></div></div><div class=\"main-header clearfix\"><div class=page-title><h3 id=page-title class=no-margin>Large Simulation visualization</h3></div></div><div class=row><div class=col-lg-12><div class=\"alert alert-info alert-dismissible col-md-4 col-md-offset-4\" role=alert ng-show=alertMessage><button type=button class=close data-dismiss=alert aria-label=Close ng-click=closeAlert()><span aria-hidden=true>&times;</span></button> {{alertMessage}}</div></div></div><div class=row><div class=col-lg-12><div class=\"alert alert-info alert-dismissible col-md-4 col-md-offset-4\" role=alert ng-show=message><button type=button class=close data-dismiss=alert aria-label=Close ng-click=closeAlert()><span aria-hidden=true>&times;</span></button> {{message}}</div></div></div><div class=col-md-12 ng-hide=alertMessage ng-show=\"tableData.length > 0\"><div><div class=\"ibox float-e-margins\"><div class=ibox-content style=\"border-width: 0px 0; background-color: #f8f8f8\"><div class=row><div class=\"col-sm-4 m-b-xs\"><div data-toggle=buttons class=btn-group><label class=\"btn btn-sm btn-white\" ng-class=\"{checked: pageSize == 10 }\"><input type=radio id=option1 name=options ng-model=pageSize value=10>10</label><label class=\"btn btn-sm btn-white\" ng-class=\"{checked: pageSize == 25 }\"><input type=radio id=option2 name=options ng-model=pageSize value=25>25</label><label class=\"btn btn-sm btn-white\" ng-class=\"{checked: pageSize == 50 }\"><input type=radio id=option3 name=options ng-model=pageSize value=50>50</label><label class=\"btn btn-sm btn-white\" ng-class=\"{checked: pageSize == 100 }\"><input type=radio id=option4 name=options ng-model=pageSize value=100>100</label></div></div><div class=\"col-sm-3 pull-right\"><div class=input-group><div class=input-group-addon><i class=\"fa fa-search\"></i></div><input class=form-control ng-model=searchInputValue placeholder=Search></div></div></div></div><div class=table-responsive><table class=\"table table-striped table-bordered\"><thead><tr><td ng-click=\"sortType = 'request_id'; sortReverse = !sortReverse\">Simulation Description <span ng-show=\"sortType == 'request_id' && !sortReverse\" class=\"fa fa-caret-down\"></span> <span ng-show=\"sortType == 'request_id' && sortReverse\" class=\"fa fa-caret-up\"></span></td><td ng-click=\"sortType = 'user_email'; sortReverse = !sortReverse\">Total No. of Nodes <span ng-show=\"sortType == 'user_email' && !sortReverse\" class=\"fa fa-caret-down\"></span> <span ng-show=\"sortType == 'user_email' && sortReverse\" class=\"fa fa-caret-up\"></span></td><td ng-click=\"sortType = 'requested_mode'; sortReverse = !sortReverse\">Scenarios <span ng-show=\"sortType == 'requested_mode' && !sortReverse\" class=\"fa fa-caret-down\"></span> <span ng-show=\"sortType == 'requested_mode' && sortReverse\" class=\"fa fa-caret-up\"></span></td><td ng-click=\"sortType = 'scheduled_time'; sortReverse = !sortReverse\">No. Of Operations <span ng-show=\"sortType == 'scheduled_time' && !sortReverse\" class=\"fa fa-caret-down\"></span> <span ng-show=\"sortType == 'scheduled_time' && sortReverse\" class=\"fa fa-caret-up\"></span></td><td ng-click=\"sortType = 'created_datetime'; sortReverse = !sortReverse\">No. of Replicas <span ng-show=\"sortType == 'created_datetime' && !sortReverse\" class=\"fa fa-caret-down\"></span> <span ng-show=\"sortType == 'created_datetime' && sortReverse\" class=\"fa fa-caret-up\"></span></td><td ng-click=\"sortType = 'status'; sortReverse = !sortReverse\">Caching Mechanism <span ng-show=\"sortType == 'status' && !sortReverse\" class=\"fa fa-caret-down\"></span> <span ng-show=\"sortType == 'status' && sortReverse\" class=\"fa fa-caret-up\"></span></td><td ng-click=\"sortType = 'justification'; sortReverse = !sortReverse\">HDD type <span ng-show=\"sortType == 'justification' && !sortReverse\" class=\"fa fa-caret-down\"></span> <span ng-show=\"sortType == 'justification' && sortReverse\" class=\"fa fa-caret-up\"></span></td><td ng-click=\"sortType = 'user_name'; sortReverse = !sortReverse\">SDD type <span ng-show=\"sortType == 'user_name' && !sortReverse\" class=\"fa fa-caret-down\"></span> <span ng-show=\"sortType == 'user_name' && sortReverse\" class=\"fa fa-caret-up\"></span></td></tr></thead><tbody><tr dir-paginate=\"row in tableData | orderBy:sortType:sortReverse | filter:searchInputValue |\n" +
    "                itemsPerPage:pageSize\" pagination-id=host current-page=currentPage><td><a class=lnkClss data-placement=left ng-click=setCurrentRow(row)>{{row.title}}</a></td><td>{{row.totalNumberOfNodes}}</td><td>{{row.scenario}}</td><td>{{row.noOfOperation}}</td><td>{{row.noOfReplicas}}</td><td>{{row.cachingMechanism}}</td><td>{{row.sddType}}</td><td>{{row.hddType}}</td></tr></tbody></table><dir-pagination-controls pagination-id=host on-page-change=pageChangeHandler(newPageNumber)></dir-pagination-controls></div></div></div></div><div class=col-md-6 ng-show=output_shown><div class=\"panel panel-primary\" style=\"margin-top: 1%\"><div class=panel-heading style=\"text-align: center\">Active Energy Consumption of Disks</div><div class=panel-body><canvas id=line class=\"chart chart-line\" chart-data=lc_ae_path.lineData chart-labels=lc_ae_path.lineLabels chart-options=lc_ae_path.lineOptions chart-series=lc_ae_path.lineSeries></canvas></div></div></div><div class=col-md-6 ng-show=output_shown><div class=\"panel panel-primary\" style=\"margin-top: 1%\"><div class=panel-heading style=\"text-align: center\">Active Time of Disks</div><div class=panel-body><canvas id=line class=\"chart chart-line\" chart-data=lc_at_path.lineData chart-labels=lc_at_path.lineLabels chart-options=lc_at_path.lineOptions chart-series=lc_at_path.lineSeries></canvas></div></div></div><div class=col-md-6 ng-show=output_shown><div class=\"panel panel-primary\" style=\"margin-top: 1%\"><div class=panel-heading style=\"text-align: center\">Idle Energy Consumption of Disks</div><div class=panel-body><canvas id=line class=\"chart chart-line\" chart-data=lc_ie_path.lineData chart-labels=lc_ie_path.lineLabels chart-options=lc_ie_path.lineOptions chart-series=lc_ie_path.lineSeries></canvas></div></div></div><div class=col-md-6 ng-show=output_shown><div class=\"panel panel-primary\" style=\"margin-top: 1%\"><div class=panel-heading style=\"text-align: center\">Idle Time of Disks</div><div class=panel-body><canvas id=line class=\"chart chart-line\" chart-data=lc_it_path.lineData chart-labels=lc_it_path.lineLabels chart-options=lc_it_path.lineOptions chart-series=lc_it_path.lineSeries></canvas></div></div></div><div class=col-md-6 ng-show=output_shown><div class=\"panel panel-primary\" style=\"margin-top: 1%\"><div class=panel-heading style=\"text-align: center\">Total Energy Consumption of Disks</div><div class=panel-body><canvas id=line class=\"chart chart-line\" chart-data=lc_tp_path.lineData chart-labels=lc_tp_path.lineLabels chart-options=lc_tp_path.lineOptions chart-series=lc_tp_path.lineSeries></canvas></div></div><div class=\"panel panel-primary\" style=\"margin-top: 1%\"><div class=panel-heading style=\"text-align: center\">Output Logs</div><div class=panel-body style=\"text-align: center\">Click here for <a ng-href={{outputLogs}} target=_blank>logs</a></div></div></div><div class=col-md-6 ng-show=\"output_shown && (scenario === '1' || scenario === '3')\"><div class=\"panel panel-primary\" style=\"margin-top: 1%\"><div class=panel-heading style=\"text-align: center\">Power Consumption without Staging Disk</div><div class=panel-body><canvas id=doughnut class=\"chart chart-doughnut\" chart-data=pie_without_path.pieData chart-labels=pie_without_path.pieLabels chart-options=pie_without_path.pieOptions></canvas></div></div></div><div class=col-md-6 ng-show=\"output_shown && (scenario === '2' || scenario === '3')\"><div class=\"panel panel-primary\" style=\"margin-top: 1%\"><div class=panel-heading style=\"text-align: center\">Power Consumption with Staging Disk</div><div class=panel-body><canvas id=doughnut class=\"chart chart-doughnut\" chart-data=pie_with_path.pieData chart-labels=pie_with_path.pieLabels chart-options=pie_with_path.pieOptions></canvas></div></div></div><div class=col-md-6 ng-show=\"output_shown && scenario === '3'\"><div class=\"panel panel-primary\" style=\"margin-top: 1%\"><div class=panel-heading style=\"text-align: center\">Power Consumption with and without Staging Disk</div><div class=panel-body><canvas id=doughnut class=\"chart chart-doughnut\" chart-data=pie_both_path.pieData chart-labels=pie_both_path.pieLabels chart-options=pie_both_path.pieOptions></canvas></div></div></div></div></div>"
  );


  $templateCache.put('app/loading/loading.html',
    "<div class=grid><div class=cell><div class=card><span class=plus>Loading&#8230;</span></div></div></div>"
  );


  $templateCache.put('app/main/main.html',
    "<div ng-show=!loginFlag id=wrapper><!-- <div ng-include=\"'components/navbar/navbar.html'\"></div> --><wandering-cubes-spinner ng-show=refreshFlag></wandering-cubes-spinner><div id=page-wrapper ng-hide=refreshFlag><div class=row><div class=col-lg-12><div class=\"alert alert-warning alert-dismissible col-md-4\" role=alert ng-show=alertMessage><button type=button class=close data-dismiss=alert aria-label=Close ng-click=closeAlert()><span aria-hidden=true>&times;</span></button> {{alertMessage}}</div></div></div><div class=row><div class=listOpts ng-click=\"changeRoute('submitRequest')\" ng-hide=\"\"><ul class=appDiv><li><span>Create Maintainance Request</span></li></ul></div></div></div></div>"
  );


  $templateCache.put('app/startSimulation/startSimulation.html',
    "<div ng-show=!loginFlag id=wrapper><div id=page-wrapper ng-hide=refreshFlag><div ng-show=pageLoading><div ng-include=\"'components/loading/loading.html'\"></div></div><div ng-hide=pageLoading><div class=row><div class=col-lg-12><div tabindex=0 id=createErrorMessage class=\"alert alert-warning alert-dismissible col-md-6 col-md-offset-3\" role=alert ng-show=alertMessage><button type=button class=close data-dismiss=alert aria-label=Close ng-click=closeAlert()><span aria-hidden=true>&times;</span></button><div ng-repeat=\"item in errors\"><span ng-show=\"item.key === 'AMBIGUOUS_ERROR'\">{{item.ambiguityHost}}<ul><li ng-repeat=\"host in item.ambiguitySuggesstHosts\" class=order-items>{{host}}</li></ul></span> <span ng-show=\"item.key === 'VALIDATION_ERROR'\">{{item.value}}</span> <span ng-show=\"item.key === 'OTHERS'\">{{item.message}}</span></div></div></div></div><div ng-class=\"output_shown ? 'col-md-6' : 'col-md-10'\"><div class=\"panel panel-primary\" style=\"margin-top: 1%\"><div class=panel-heading style=\"text-align: center\">Enter Simulation Details</div><div class=panel-body><form class=form-horizontal name=maintainanceForm ng-submit=submitForm(maintainanceForm.$valid) novalidate><!-- novalidate prevents HTML5 validation since we will be validating ourselves --><div class=form-group><label class=\"col-sm-3 control-label\">No. of Nodes<sup>*</sup></label><p class=input-group><select name=num_nodes class=\"form-control m-b\" style=\"width:auto; margin: 0 10px\" ng-model=num_nodes><option disabled value=\"\">--No. of Nodes--</option><option value=3>03</option><option value=4>04</option><option value=5>05</option><option value=6>06</option><option value=7>07</option><option value=8>08</option><option value=9>09</option><option value=10>10</option><option value=11>11</option><option value=12>12</option><option value=13>13</option><option value=14>14</option><option value=15>15</option><option value=16>16</option></select></p></div><div class=form-group><label class=\"col-sm-3 control-label\">Scenarios<sup>*</sup></label><p class=input-group><select name=scenario class=\"form-control m-b\" style=\"width:auto; margin: 0 10px\" ng-model=scenario><option disabled value=\"\">--Staging Disk Scenarios--</option><option value=1>Without Staging Disk</option><option value=2>With Staging Disk</option><option value=3>With and Without Staging Disk</option></select></p></div><div class=form-group><label class=\"col-sm-3 control-label\">No. of operations<sup>*</sup></label><p class=input-group><select name=num_of_operations class=\"form-control m-b\" style=\"width:auto; margin: 0 10px\" ng-model=num_of_operations><option disabled value=\"\">--No. of operations--</option><option value=5>05</option><option value=10>10</option><option value=15>15</option><option value=20>20</option><option value=25>25</option><option value=30>30</option><option value=35>35</option><option value=40>40</option><option value=45>45</option><option value=50>50</option></select></p></div><div class=form-group><label class=\"col-sm-3 control-label\">No. of Replicas<sup>*</sup></label><p class=input-group><select name=num_of_replicas class=\"form-control m-b\" style=\"width:auto; margin: 0 10px\" ng-model=num_of_replicas><option disabled value=\"\">--No. of replicas--</option><option value=2>2</option><option value=3>3 (Openstack Swift Default)</option><option value=4>4</option></select></p></div><div class=form-group><label class=\"col-sm-3 control-label\">Caching Mechanism<sup>*</sup></label><p class=input-group><select name=caching_mechanism class=\"form-control m-b\" style=\"width:auto; margin: 0 10px\" ng-model=caching_mechanism><option disabled value=\"\">--Caching Mechanism--</option><option value=LRU>LRU</option><option value=FIFO>FIFO</option></select></p></div><div class=form-group><label class=\"col-sm-3 control-label\">Disk Type<sup>*</sup></label><p class=input-group><select name=disk_type class=\"form-control m-b\" style=\"width:auto; margin: 0 10px\" ng-model=disk_type><option disabled value=\"\">--HDD Disk Type--</option><option value=1>SeagateEnterpriseST6000VN0001</option><option value=2>HGSTUltrastarHUC109090CSS600</option><option value=3>ToshibaEnterpriseMG04SCA500E</option></select><select name=ssd_disk_type class=\"form-control m-b\" style=\"width:auto; margin: 0 10px\" ng-model=ssd_disk_type ng-show=\"scenario == '2' || scenario == '3'\"><option disabled value=\"\">--SDD Disk Type--</option><option value=1>HG6EnterpriseTHNSNJ512GCSU</option><option value=2>Seagate600ProEnterpriseST480FP0021</option><option value=3>IntelDCS3500EnterpriseSC2BB800G401</option></select></p></div><div class=form-group><label class=\"col-sm-3 control-label\">Workload Type<sup>*</sup></label><div class=col-md-6><div class=btn-group role=group><button class=\"btn btn-default\" type=button ng-model=workload_type ng-click=\"setWorkloadType('predefined_workload')\" ng-class=\"{'btn-selected-blue':workload_type=='predefined_workload'}\">Predefined</button> <button class=\"btn btn-default\" type=button ng-model=workload_type ng-click=\"setWorkloadType('manual')\n" +
    "                    \" ng-class=\"{ 'btn-selected-blue': workload_type === 'manual' }\">Manual Entry</button></div></div></div><div class=form-group ng-show=\"workload_type == 'predefined_workload'\"><label class=\"col-sm-3 control-label\">Predefined Workload Type<sup>*</sup></label><p class=input-group><select name=predefined_workload_type class=\"form-control m-b\" style=\"width:auto; margin: 0 10px\" ng-model=predefined_workload_type><option disabled value=\"\">--Predefined Workload Type--</option><option value=1>Read Intensive</option><option value=2>Write Intensive</option></select></p></div><div class=form-group ng-show=\"workload_type == 'manual'\"><label class=\"col-sm-3 control-label\">Operations<sup>*</sup></label><div class=col-md-6><textarea name=manual_textarea class=\"form-control msd-elastic\" ng-model=manual_textarea ng-minlength=1 data-role=none></textarea></div></div><div class=form-group style=\"margin-top: 8%\"><div class=\"col-sm-4 col-sm-offset-4\"><input type=button class=\"btn btn-warning\" ng-click=resetForm(maintainanceForm) value=Reset>&nbsp;&nbsp; <button class=\"btn btn-primary\" type=submit data-toggle=modal data-target=#myModal ng-disabled=maintainanceForm.$invalid event-focus=click event-focus-id=createErrorMessage>Submit</button></div></div></form></div></div></div></div><div class=col-md-6 ng-show=output_shown><div class=\"panel panel-primary\" style=\"margin-top: 1%\"><div class=panel-heading style=\"text-align: center\">Active Energy Consumption of Disks</div><div class=panel-body><canvas id=line class=\"chart chart-line\" chart-data=lc_ae_path.lineData chart-labels=lc_ae_path.lineLabels chart-options=lc_ae_path.lineOptions chart-series=lc_ae_path.lineSeries></canvas></div></div></div><div class=col-md-6 ng-show=output_shown><div class=\"panel panel-primary\" style=\"margin-top: 1%\"><div class=panel-heading style=\"text-align: center\">Active Time of Disks</div><div class=panel-body><canvas id=line class=\"chart chart-line\" chart-data=lc_at_path.lineData chart-labels=lc_at_path.lineLabels chart-options=lc_at_path.lineOptions chart-series=lc_at_path.lineSeries></canvas></div></div></div><div class=col-md-6 ng-show=output_shown><div class=\"panel panel-primary\" style=\"margin-top: 1%\"><div class=panel-heading style=\"text-align: center\">Idle Energy Consumption of Disks</div><div class=panel-body><canvas id=line class=\"chart chart-line\" chart-data=lc_ie_path.lineData chart-labels=lc_ie_path.lineLabels chart-options=lc_ie_path.lineOptions chart-series=lc_ie_path.lineSeries></canvas></div></div></div><div class=col-md-6 ng-show=output_shown><div class=\"panel panel-primary\" style=\"margin-top: 1%\"><div class=panel-heading style=\"text-align: center\">Idle Time of Disks</div><div class=panel-body><canvas id=line class=\"chart chart-line\" chart-data=lc_it_path.lineData chart-labels=lc_it_path.lineLabels chart-options=lc_it_path.lineOptions chart-series=lc_it_path.lineSeries></canvas></div></div></div><div class=col-md-6 ng-show=output_shown><div class=\"panel panel-primary\" style=\"margin-top: 1%\"><div class=panel-heading style=\"text-align: center\">Total Energy Consumption of Disks</div><div class=panel-body><canvas id=line class=\"chart chart-line\" chart-data=lc_tp_path.lineData chart-labels=lc_tp_path.lineLabels chart-options=lc_tp_path.lineOptions chart-series=lc_tp_path.lineSeries></canvas></div></div><div class=\"panel panel-primary\" style=\"margin-top: 1%\"><div class=panel-heading style=\"text-align: center\">Output Logs</div><div class=panel-body style=\"text-align: center\">Click here for <a ng-href={{outputLogs}} target=_blank>logs</a></div></div></div><div class=col-md-6 ng-show=\"output_shown && (scenario === '1' || scenario === '3')\"><div class=\"panel panel-primary\" style=\"margin-top: 1%\"><div class=panel-heading style=\"text-align: center\">Power Consumption without Staging Disk</div><div class=panel-body><canvas id=doughnut class=\"chart chart-doughnut\" chart-data=pie_without_path.pieData chart-labels=pie_without_path.pieLabels chart-options=pie_without_path.pieOptions></canvas></div></div></div><div class=col-md-6 ng-show=\"output_shown && (scenario === '2' || scenario === '3')\"><div class=\"panel panel-primary\" style=\"margin-top: 1%\"><div class=panel-heading style=\"text-align: center\">Power Consumption with Staging Disk</div><div class=panel-body><canvas id=doughnut class=\"chart chart-doughnut\" chart-data=pie_with_path.pieData chart-labels=pie_with_path.pieLabels chart-options=pie_with_path.pieOptions></canvas></div></div></div><div class=col-md-6 ng-show=\"output_shown && scenario === '3'\"><div class=\"panel panel-primary\" style=\"margin-top: 1%\"><div class=panel-heading style=\"text-align: center\">Power Consumption with and without Staging Disk</div><div class=panel-body><canvas id=doughnut class=\"chart chart-doughnut\" chart-data=pie_both_path.pieData chart-labels=pie_both_path.pieLabels chart-options=pie_both_path.pieOptions></canvas></div></div></div></div></div>"
  );


  $templateCache.put('components/loading/loading.html',
    "<div class=grid><div class=cell><div class=card><span class=plus>Loading&#8230;</span></div></div></div>"
  );


  $templateCache.put('components/modal/modal.html',
    "<div class=modal-header><button ng-if=modal.dismissable type=button ng-click=$dismiss() class=close>&times;</button><h4 ng-if=modal.title ng-bind=modal.title class=modal-title></h4></div><div class=modal-body><p ng-if=modal.text ng-bind=modal.text></p><div ng-if=modal.html ng-bind-html=modal.html></div></div><div class=modal-footer><button ng-repeat=\"button in modal.buttons\" ng-class=button.classes ng-click=button.click($event) ng-bind=button.text class=btn></button></div>"
  );


  $templateCache.put('components/navbar/navbar.html',
    "<div class=\"navbar navbar-default navbar-static-top\"><div class=container style=width:99%><center><div class=navbar-header><a href=\"/#/\" class=navbar-brand>Green Swift Simulation</a></div></center></div></div>"
  );


  $templateCache.put('components/sidebar/sidebar.html',
    "<nav ng-show=!loginFlag id=homeSideBar class=\"navbar-default navbar-static-side sidebarNav ng-scope\" role=navigation ng-controller=SidebarCtrl><div class=\"sidebar-collapse sidebar\"><ul class=nav id=side-menu><li ng-click=selectTab(1) ng-class=\"selectedTab === 1 ? 'active' : ''\"><a href=/#/submitRequest class=active>Realtime Simulation</a></li><li ng-click=selectTab(5) ng-class=\"selectedTab === 2 ? 'active' : ''\"><a href=/#/largeSavedSimulation class=active>Large Saved Simulation</a></li></ul></div></nav>"
  );

}]);

