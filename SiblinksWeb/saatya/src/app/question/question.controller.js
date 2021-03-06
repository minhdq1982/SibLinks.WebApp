brotControllers
    .controller(
        'QuestionController',
        [
            '$sce',
            '$route',
            '$scope',
            '$rootScope',
            '$timeout',
            '$http',
            '$routeParams',
            '$location',
            '$log',
            'SubjectServices',
            'QuestionsService',
            'CategoriesService',
            'MentorService',
            'AnswerService',
            'HomeService',
            'CommentService',
            'StudentService',
            'VideoService',
            'myCache',
            function ($sce, $route, $scope, $rootScope, $timeout,
                      $http, $routeParams, $location, $log,
                      SubjectServices, QuestionsService,
                      CategoriesService, MentorService,
                      AnswerService, HomeService, CommentService,
                      StudentService, VideoService, myCache) {
                var qid = $routeParams.question_id;
                var lastRoute = $route.current;
                $scope.clickS = 1;
                $scope.clickC = 1;
                $scope.countS = 0;

                $scope.limitAnswes = 3;
                var listPosted = [];

                var userId = localStorage.getItem('userId');
                $scope.userId = userId;
                var subject_id = $routeParams.subjectId;
                var categorieId = $routeParams.categorieId;
                $scope.subjectId = subject_id;
                $scope.categorieId = categorieId;
                $scope.baseIMAGEQ = NEW_SERVICE_URL + '/comments/getImageQuestion/';
                $scope.totalQuestion = 0;
                var oldQid = '-1';
                var ordertype = "";
                var LIMIT_TOP_MENTORS = 5;
                var LIMIT_TOP_VIDEOS = 5;
                var idRemove;
                var eventRemove;
                var questionForAnswer;

                var limit = 10;
                var offset = 0;
                var currentPage = 0;
                var isLoadMore = false;
                $scope.isDisplayMore = false;
                init();
                function init() {

                    ordertype = "newest";
                    //$scope.ordertype = "answered";
                    if (isEmpty(userId)) {
                        userId = -1;
                    }
                    getQuestions(userId, limit, offset, ordertype, oldQid);

                    QuestionsService.countQuestions(userId, ordertype).then(function (data) {
                        $scope.totalQuestion = data.data.request_data_result[0].numquestion;
                        if ($scope.totalQuestion == 0 || userId == "-1") {
                            window.location.href = '/#/ask_a_question/first-ask';
                        }
                    });

                    MentorService.getTopMentorsByLikeRateSubcrible(LIMIT_TOP_MENTORS, offset, 'subcribe', userId).then(function (data) {
                        var data_result = data.data.request_data_result;
                        var subjects = myCache.get("subjects");
                        if (data_result) {
                            var listTopMentors = [];
                            for (var i = 0; i < data_result.length; i++) {
                                var mentor = {};
                                mentor.userid = data_result[i].userid;
                                mentor.userName = data_result[i].userName;
                                mentor.lastName = data_result[i].lastName;
                                mentor.firstName = data_result[i].firstName;
                                mentor.imageUrl = data_result[i].imageUrl;
                                mentor.numlike = data_result[i].numlike;
                                mentor.numsub = data_result[i].numsub;
                                mentor.numvideos = data_result[i].numvideos;
                                mentor.defaultSubjectId = data_result[i].defaultSubjectId;
                                mentor.listSubject = getSubjectNameById(data_result[i].defaultSubjectId, subjects);
                                mentor.numAnswers = data_result[i].numAnswers;
                                listTopMentors.push(mentor);
                            }
                        }
                        $scope.listTopmentors = listTopMentors;
                    });

                    VideoService.getVideoByRate(LIMIT_TOP_VIDEOS, offset).then(function (data) {
                        if (data.data.status) {
                            var result_data = data.data.request_data_result;
                            if (result_data) {
                                var listVideoRate = [];
                                for (var i = 0; i < result_data.length; i++) {
                                    var element = result_data[i];
                                    var video = {};
                                    video.title = element.title;
                                    video.image = element.image;
                                    video.url = element.url;
                                    video.rating = element.rating;
                                    video.uid = element.uid;
                                    listVideoRate.push(video);
                                }
                            }
                            $scope.listVideoRate = listVideoRate;
                        }

                    });


                }


                function getQuestions(userId, limit, offset, type,oldQid) {

                    QuestionsService.getQuestionByUserId(userId, limit, offset, type,oldQid).then(function (data) {
                        if (data.data.status) {
                            var result = data.data.request_data_result;
                            if(!isLoadMore) {
                                listPosted = [];
                            }
                            for (var i = 0; i < result.question.length; i++) {
                                var objPosted = {};
                                var questionData = result.question[i];
                                objPosted.id = questionData.PID;
                                oldQid = questionData.PID;
                                objPosted.title = questionData.TITLE;
                                objPosted.subject = questionData.SUBJECT;
                                objPosted.name = questionData.FIRSTNAME;
                                objPosted.content = questionData.CONTENT;
                                objPosted.numviews = questionData.NUMVIEWS == null ? 0 : questionData.NUMVIEWS;
                                objPosted.time = convertUnixTimeToTime(questionData.TIMESTAMP);
                                objPosted.image = detectMultiImage(questionData.IMAGEPATH);
                                //objPosted.count = questionData.length;
                                if (result.answers[i] != null) {
                                    var answer = result.answers[i];
                                    var answer_result = answer.body.request_data_result;
                                    objPosted.count_answer = answer_result.length;
                                    var listAnswer = [];
                                    for (var y = 0; y < answer_result.length; y++) {

                                        var objAnswer = {};
                                        objAnswer.authorID = answer_result[y].authorID;
                                        objAnswer.aid = answer_result[y].aid;
                                        objAnswer.pid = answer_result[y].pid;
                                        objAnswer.name = answer_result[y].firstName + " " + answer_result[y].lastName;
                                        var answer_text = decodeURIComponent(answer_result[y].content);
                                        answer_text = $sce.trustAsHtml(answer_text);
                                        objAnswer.content = answer_text;
                                        objAnswer.avatar = answer_result[y].imageUrl;
                                        objAnswer.countLike = answer_result[y].countLike;
                                        if (answer_result[y].likeAnswer == null || answer_result[y].likeAnswer === "N") {
                                            objAnswer.like = "No Like";
                                        } else {
                                            objAnswer.like = "Liked";
                                        }
                                        objAnswer.time = convertUnixTimeToTime(answer_result[y].timeStamp);
                                        listAnswer.push(objAnswer);
                                        objPosted.answers = listAnswer;
                                    }

                                } else {
                                    objPosted.answers = null;
                                    objPosted.count_answer = "0";
                                }
                                listPosted.push(objPosted);
                            }
                            if(result.question.length == 0 ){
                                listPosted = [];
                                if(isLoadMore) {
                                   return;
                                }
                            }
                            $scope.askQuestion = listPosted;
                        }
                    });
                }

                function detectMultiImage(imagePath) {
                    if(isEmpty(imagePath)){
                        return null;
                    }
                    var result=[];
                    if (imagePath != null && imagePath.indexOf(";") != -1) {
                        for(var i = 0;i<imagePath.split(";"); i++){
                            var str = imagePath.split(";")[i];
                            if(!isEmpty(str)){
                                result.push(str);
                            }

                        }
                        return result;
                    }
                    var listImage = [];
                    listImage.push(imagePath)
                    return listImage;

                };
                function getSubjectNameById(strSubjectId, listcate) {
                    var subject = {};
                    var listSubject = [];
                    if (isEmpty(strSubjectId)) {
                        listSubject.push(subject);
                        return listSubject;
                    }
                    if (strSubjectId.indexOf(',') < -1) {
                        for (var y = 0; y < listcate.length; y++) {
                            if (listcate[y].subjectId == strSubjectId) {
                                subject.id = strSubjectId;
                                subject.name = listcate[y].subject
                                return listSubject.push(subject);
                            }

                        }
                    }
                    else {
                        var list = strSubjectId.split(',');
                        for (var i = 0; i < list.length; i++) {
                            for (var y = 0; y < listcate.length; y++) {
                                if (listcate[y].subjectId == list[i]) {
                                    subject = [];
                                    subject.name = listcate[y].subject;
                                    subject.id = listcate[y].subjectId;
                                    listSubject.push(subject);
                                }

                            }
                        }
                    }

                    return listSubject;

                }

                $scope.detailQuestion = function (id) {
                    window.location.href = '/#/ask_a_question/question_detail/' + id+"";
                }
                $scope.showFormAdd = function () {
                    $(".form-ask-question").css({"left": 0});
                }
                $scope.closePopupAskQuestion = function () {
                    $(".popup-images, .form-ask-question").css({"left": "100%"});
                }

                $scope.isShowOrder = false;

                $scope.orderQuestions = function (type) {
                    var limitOder = 10;
                    var offsetOder = 0;

                    if(type == ordertype){
                        $scope.isShowOrder = false;
                        return ;
                    }
                    $scope.isDisplayMore = false;
                    currentPage = 0;
                    oldQid = '-1';
                    ordertype = type + '';

                    if (isEmpty(userId)) {
                        userId = -1;
                    }
                    isLoadMore = false;
                    QuestionsService.countQuestions(userId, ordertype).then(function (data) {
                        $scope.totalQuestion = data.data.request_data_result[0].numquestion;
                        if($scope.totalQuestion != '0'){
                            getQuestions(userId, limitOder, offsetOder, ordertype,oldQid);
                        }
                        else {
                            $scope.askQuestion = [];
                        }
                    });

                    $scope.isShowOrder = false;
                }


                $scope.isShowImageHover = false;
                $scope.currentImage = "";
                $scope.toggleShowOrder = function () {
                    $scope.isShowOrder = $scope.isShowOrder === false ? true : false;
                };
                $scope.zoomImage = function (img) {
                    $scope.currentImage = ($scope.baseIMAGEQ + img + "");
                    $(".popup-images").css({"left": 0});
                }

                $scope.closeImage = function () {
                    $(".popup-images, .form-ask-question").css({"left": "100%"});
                }
                $scope.imageHoverIn = function () {
                    $scope.isShowImageHover = true;
                }
                $scope.imageHoverOut = function () {
                    $scope.isShowImageHover = false;
                }


                $scope.likeCount = 0;
                $scope.likeAnswer = function (aid) {
                    if (!isEmpty(userId) && userId != -1) {
                        AnswerService.likeAnswer(userId, aid + "").then(function (data) {
                            if (data.data.status == 'Fail') {
                                $scope.likeCount = -1;
                            }
                            else {
                                $scope.likeCount = 1;
                            }

                        });
                    }
                    else {
                        $scope.likeCount = 0;
                    }
                };

                $scope.isLoadMore = false;


                $scope.loadMorePost = function(ev) {
                    currentPage++;
                    isLoadMore = true;
                    if ( $scope.isDisplayMore) {
                        return;
                    }
                    $scope.isDisplayMore = true;

                    var newoffset = limit * currentPage;
                    console.log(newoffset);
                    if (newoffset > $scope.totalQuestion) {
                        $scope.isDisplayMore = true;
                        return ;
                    }
                    else {
                        $scope.isDisplayMore = false;
                        getQuestions(userId, limit, newoffset, ordertype,oldQid);
                    }


                };


                $scope.deletePost = function (event, pid) {
                    $scope.typeItem = 'Post';
                    $('#deleteItem').modal('show');
                    idRemove = pid;
                    eventRemove = event;
                };

                $scope.deleteAnswer = function (aid, q) {
                    questionForAnswer = q;
                    $scope.typeItem = 'Answer';
                    $('#deleteItem').modal('show');
                    idRemove = aid;
                    eventRemove = event;
                };


            }]);
