brotControllers
    .controller(
        'QuestionDetailCtrl',
        [
            '$sce',
            '$http',
            '$scope',
            '$rootScope',
            '$routeParams',
            '$location',
            '$timeout',
            '$log',
            '$window',
            'QuestionsService',
            'AnswerService',
            'SubjectServices',
            'CategoriesService',
            'HomeService',
            'CommentService',
            'StudentService',
            'myCache',
            function ($sce, $http, $scope, $rootScope, $routeParams,
                      $location, $timeout, $log, $window,
                      QuestionsService, AnswerService,
                      SubjectServices, CategoriesService,
                      HomeService, CommentService, StudentService,
                      myCache) {
                var userId = localStorage.getItem('userId');
                $scope.userId = userId;
                var question_id = $routeParams.question_id;
                $scope.limitAnswes = 5;
                $scope.baseIMAGEQ = NEW_SERVICE_URL + '/comments/getImageQuestion/';
                var oldImagePath="";
                var oldImagePathEdited="";
                var qidEdit;
                var imagePathOld_BK;

                init();
                var type = "new";

                function init() {
                    if (isEmpty(question_id)) {
                        return;
                    }

                    QuestionsService.getQuestionById(question_id).then(function (data) {
                        var obj = data.data.request_data_result;
                        var viewNew = parseInt(obj[0].numViews, 10) + 1;
                        $scope.subjects = myCache.get("subjects");
                        $scope.initCategory = {subject: obj[0].subject, subjectId: obj[0].subjectId};
                        //$('#autocompleteQuest_value').val(obj[0].content);
                        $scope.imagePathOld = detectMultiImage(obj[0].imagePath);
                        imagePathOld_BK = $scope.imagePathOld;
                        oldImagePath = obj[0].imagePath;

                        var question = {
                            "authorId": obj[0].authorID,
                            "question_id": obj[0].pid,
                            "question_text": obj[0].content,
                            "views": viewNew,
                            "question_creation_date": moment(obj[0].updateTime, 'YYYY-MM-DD h:mm:ss').startOf('hour').fromNow(),
                            "timeStamp": convertUnixTimeToTime(obj[0].TIMESTAMP),
                            "author": obj[0].userName,
                            "firstName": obj[0].firstName,
                            "lastName": obj[0].lastName,
                            "avatar": obj[0].imageUrl,
                            "subject": obj[0].subject,
                            "subjectId": obj[0].subjectId,
                            "numview": obj[0].numViews,
                            "numlike": obj[0].countLike,
                            "imagePath": detectMultiImage(obj[0].imagePath)

                        };
                        $scope.question = question;
                        $scope.arr = encodeURIComponent(question.question_text);


                    });
                    QuestionsService.updateViewQuestion(question_id, "view").then(function (data) {
                    });
                    var listAnswer = [];
                    QuestionsService.getAnswerByQid(question_id, type, "", "").then(function (data) {

                        var answers = data.data.request_data_result;
                        $scope.countAnswer = answers.length;
                        if (answers != null) {
                            for (var i = 0; i < answers.length; i++) {
                                var an = answers[i];
                                var answer_text = decodeURIComponent(an.content);
                                answer_text = $sce.trustAsHtml(answer_text);
                                an.answer_text = answer_text;
                                an.timeStamp = convertUnixTimeToTime(an.TIMESTAMP);
                                listAnswer.push(an);
                            }
                        } else {
                            $scope.countAnswer = 0;
                        }
                        $scope.listAnswer = listAnswer;
                    });

                    // get data when edit


                }


                $scope.removeImg = function (index) {
                    $scope.filesArray.splice(index, 1);
                    $scope.stepsModel.splice(index, 1);

                }

                $scope.removeImgOld = function (index) {
                    $scope.imagePathOld.splice(index, 1);

                    for(var i = 0 ; i< $scope.imagePathOld.length;i++) {
                        oldImagePathEdited += $scope.imagePathOld[i];
                        if (i < $scope.imagePathOld.length - 1) {
                            oldImagePathEdited += ";";
                        }
                    }

                }

                $scope.zoomImage = function (img) {
                    $scope.currentImage = ($scope.baseIMAGEQ + img + "");
                    $(".popup-images").css({"left": 0});
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


                $scope.likeCount = 0;
                $scope.likeAnswer = function (aid) {
                    if (!isEmpty(userId) && userId != -1) {
                        AnswerService.likeAnswer(userId, aid + "").then(function (data) {
                            if (data.data.status == 'Fail') {
                                $scope.likeCount = 1;
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

                $scope.editQuestion = function (qid) {
                    $scope.titlePopupAsk = "Edit question";
                    $scope.isEdit = true;
                    qidEdit = qid;
                    $scope.imagePathOld = imagePathOld_BK;
                    $scope.initCategory = {subject: $scope.question.subject, subjectId: $scope.question.subjectId};
                    $scope.initCategory.originalObject = $scope.initCategory;
                    $('#autocompleteQuest_value').val($scope.question.question_text);
                    $(".form-ask-question").css({"left": 0});

                }
                $scope.deleteQuestion = function (qid) {
                        QuestionsService.removePost(qid).then(function (data) {
                            if (data.data.request_data_result) {
                                window.location.href = '#/ask_a_question';
                            }
                        });

                }
                $scope.filesArray = [];
                $scope.removeImg = function (index) {
                    $scope.filesArray.splice(index, 1);
                    $scope.stepsModel.splice(index, 1);

                }
                $scope.selectedSubject = function (selected) {
                    $scope.initCategory = selected;
                };

                $scope.updateQuestion = function () {
                    // get question of student

                    if ($scope.initCategory == null || $scope.initCategory === undefined || $scope.initCategory.originalObject == null) {
                        $scope.askErrorMsg='Please choose category';
                        $("#autocompleteCate_value").focus();
                        $rootScope.myVarC = !$scope.myVarC;
                        $timeout(function () {
                            $rootScope.myVarC = false;
                        }, 2500);
                        return;
                    }
                    var questions = $('#autocompleteQuest_value').val();
                    if (!questions) {
                        $rootScope.myVarQ = !$scope.myVarQ;
                        $timeout(function () {
                            $rootScope.myVarQ = false;
                        }, 2500);
                        $scope.askErrorMsg='You enter text or upload for your question';
                        $("#autocompleteQuest_value").focus();
                        return;
                    }

                    if (isEmpty(userId) ||userId=='-1') {
                        $scope.askErrorMsg='Please login before edit a question';
                        $rootScope.myVarU = !$scope.myVarU;
                        $timeout(function () {
                            $rootScope.myVarU = false;
                        }, 2500);
                        return;
                    }
                    fd = new FormData();
                    if ($scope.filesArray != null) {
                        for (var i = 0; i < $scope.filesArray.length; i++) {
                            file = $scope.filesArray[i];
                            fd.append('file', file);
                        }
                    }

                    fd.append('qid', $scope.question.question_id);
                    fd.append('content', questions);
                    fd.append('oldImagePathEdited', oldImagePathEdited);
                    fd.append('oldImagePath', oldImagePath);

                    fd.append('subjectId', $scope.initCategory.originalObject.subjectId);
                    QuestionsService.updateQuestion(fd).then(function (data) {
                        if (data.data.status == "true") {
                            $(".popup-images, .form-ask-question").css({"left": "100%"});
                           // window.location.href = '/#/ask_a_question';
                            window.location.reload();
                        }
                        else {
                            $scope.askErrorMsg =data.data.request_data_result;
                        }
                    });


                };
                $scope.stepsModel = [];
                $scope.onFileSelect = function ($files) {

                    if ($files != null) {
                        for (var i = 0; i < $files.length; i++) {
                            var file = $files[i];
                            $scope.filesArray.push(file);
                            var reader = new FileReader();
                            reader.onload = $scope.imageIsLoaded;
                            reader.readAsDataURL(file);

                        }
                    }
                };

                $scope.imageIsLoaded = function (e) {
                    $scope.$apply(function () {
                        $scope.stepsModel.push(e.target.result);
                    });
                }

                $scope.redirectForum = function () {
                    // get question of student

                    if ($scope.initCategory == null || $scope.initCategory === undefined || $scope.initCategory.originalObject == null) {
                        $scope.askErrorMsg='Please choose category';
                        $("#autocompleteCate_value").focus();
                        $rootScope.myVarC = !$scope.myVarC;
                        $timeout(function () {
                            $rootScope.myVarC = false;
                        }, 2500);
                        return;
                    }
                    var questions = $('#autocompleteQuest_value').val();
                    if (!questions) {
                        $rootScope.myVarQ = !$scope.myVarQ;
                        $timeout(function () {
                            $rootScope.myVarQ = false;
                        }, 2500);
                        $scope.askErrorMsg='You enter text or upload for your question';
                        $("#autocompleteQuest_value").focus();
                        return;
                    }

                    if (isEmpty(userId) ||userId=='-1') {
                        $scope.askErrorMsg='Please login before you ask a question';
                        $rootScope.myVarU = !$scope.myVarU;
                        $timeout(function () {
                            $rootScope.myVarU = false;
                        }, 2500);
                        return;
                    }
                    fd = new FormData();
                    if ($scope.filesArray != null) {
                        for (var i = 0; i < $scope.filesArray.length; i++) {
                            file = $scope.filesArray[i];
                            fd.append('file', file);
                        }
                    }

                    fd.append('userId', userId);
                    fd.append('content', questions);

                    fd.append('subjectId', $scope.initCategory.originalObject.subjectId);
                    HomeService.addQuestion(fd).then(function (data) {
                        if (data.data.status == "true") {
                            $(".popup-images, .form-ask-question").css({"left": "100%"});
                            window.location.href = '/#/ask_a_question';
                            window.location.reload();
                        }
                        else {
                            $scope.askErrorMsg =data.data.request_data_result;
                        }
                    });


                };


                $scope.showEditQuestion = function () {
                    $(".edit-question").toggle();
                }
                $scope.showOrder = function () {
                    $('.sort-answer').toggle();
                }
                $scope.showFormAdd = function () {
                    $scope.titlePopupAsk = "Ask a question";
                    $scope.isEdit = false;
                    //$scope.initCategory = {};
                    $scope.$broadcast('angucomplete-alt:clearInput', "autocompleteCate");
                    $scope.imagePathOld = [];
                    $('#autocompleteQuest_value').val("");
                    $(".form-ask-question").css({"left": 0});
                }
                $scope.closePopupAskQuestion = function () {
                    $(".popup-images, .form-ask-question").css({"left": "100%"});
                }
            }]);