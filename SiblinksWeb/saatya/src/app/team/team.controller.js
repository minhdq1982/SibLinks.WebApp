brotControllers.controller('TeamCtrl', ['$scope', '$rootScope', '$log', '$location', '$sce', 'TeamMentorService','myCache','VideoService',
    function ($scope, $rootScope, $log, $location, $sce, TeamMentorService,myCache,VideoService) {


        var limit = "50";
        var offset = "0";
        $scope.type = "subs";
        brot.signin.statusStorageHtml();
        $scope.login = 0;

        var userId = localStorage.getItem('userId');
        init();

        function init() {
            if(isEmpty(userId)){
                userId = -1;
            }
            selectMentorTop(limit, offset, $scope.type,userId);

        }

        $scope.selectType = function (type) {
            $scope.type = type + "";
            selectMentorTop(limit, offset, type);
        }
        $scope.isSubscribe = 0;

        $scope.setSubscribeMentor = function (mentorId) {
            if(isEmpty(userId)){
                return ;
            }
            VideoService.setSubscribeMentor(userId, mentorId+"").then(function (data) {
                if(data.data.status =="true") {
                    if (data.data.request_data_type == "subs") {
                        $scope.isSubscribe = 1;
                    }
                    else {
                        $scope.isSubscribe = -1;
                    }
                }
            });
        };

        function selectMentorTop(limit, offset, type) {
            TeamMentorService.getTopMentorsByType(limit, offset, type,userId).then(function (data) {
                var listTopMentors =[];

                var subjects = myCache.get("subjects");
                if (data.data.status) {
                    $scope.countAll = data.data.request_data_result.length;

                    if ($scope.countAll == null) {
                        $scope.errorData = DATA_ERROR.noDataFound;
                    }
                    else {
                        var data_result = data.data.request_data_result;
                        for (var i = 0; i < data_result.length; i++) {

                            var mentor = {};
                            mentor.userid = data_result[i].userid;
                            mentor.lastName = data_result[i].lastName;
                            mentor.firstName= data_result[i].firstName;
                            mentor.accomplishments= data_result[i].accomplishments;
                            mentor.bio= data_result[i].bio;
                            mentor.numsub= data_result[i].numsub;
                            mentor.imageUrl = data_result[i].imageUrl;
                            mentor.numlike= data_result[i].numlike;
                            mentor.numvideos= data_result[i].numvideos;
                            mentor.numAnswers= data_result[i].numAnswers;
                            mentor.isSubs= data_result[i].isSubs;
                            mentor.defaultSubjectId = data_result[i].defaultSubjectId;
                            mentor.listSubject = getSubjectNameById(data_result[i].defaultSubjectId, subjects);
                            mentor.numAnswers = data_result[i].numAnswers;
                            listTopMentors.push(mentor);
                        }
                        $scope.listTopmentors = listTopMentors;
                        $scope.showItem = true;
                    }


                }
            });
        }

    }]);