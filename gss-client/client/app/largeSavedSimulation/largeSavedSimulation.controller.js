'use strict';

angular.module('gssApp')
  .controller('LargeSavedSimulationCtrl', function ($rootScope, $scope, $http, globalService, $location) {
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

    });