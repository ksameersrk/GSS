'use strict';

angular.module('gssApp')

  .factory('focus', function($timeout, $window) {
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
  })

  .directive('eventFocus', function(focus) {
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
  })
  
   
    .controller('StartSimulationCtrl', function ($scope, $http, globalService, $location, $filter, ngDialog, $rootScope, $q, $window, $routeParams) {

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
    });