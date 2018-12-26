brotControllers.controller('ChangePasswordCtrl', 
		['$scope', '$rootScope', '$location', '$timeout', '$routeParams', '$log', 'StudentService',
        function($scope, $rootScope, $location, $timeout, $routeParams, $log, StudentService) {

  var token = $routeParams.token;
  $scope.show = 0;
  $scope.notFound = 0;

  StudentService.confirmToken(token).then(function(data) {
    if(data.data.request_data_result[0].count == 1) {
      $scope.notFound = 1;
    }
  });

  $scope.changePassword = function(newPass, confirmPass) {
    if(newPass == confirmPass && newPass !== undefined && confirmPass !== undefined) {
      StudentService.changePassword(token, newPass).then(function(data) {
    	 if(data.data='true') {
    		 $log.info(data.data.request_data_result[0]);
    	     $scope.show = 1;
    	 } else {
    		 
    	 }
        
      });
    } else {
      $rootScope.myVar = !$scope.myVar;
      $timeout(function () {
        $rootScope.myVar = false;
      }, 2500);
      return;
    }
  };

  $scope.goHomePage = function() {
    window.location.href = '/';
  };

}]);