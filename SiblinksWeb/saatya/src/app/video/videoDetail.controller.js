brotControllers.controller('VideoDetailCtrl', ['$scope', '$rootScope', '$routeParams','$http', '$location', '$sce', '$timeout',
    '$window', 'VideoService', 'SubjectServices', 'CategoriesService', 'SubCategoryVideoService', 'StudentService', 'CommentService','videoDetailService',
    function($scope, $rootScope, $routeParams, $http, $location, $sce, $timeout, $window,
             VideoService, SubjectServices, CategoriesService, SubCategoryVideoService, StudentService,
             CommentService, videoDetailService){
        $scope.userId = localStorage.getItem('userId');


        var videoid = $routeParams.videoid;

        $scope.listVideos = null;
        $scope.clickD = 1;

        $scope.keySearch = $rootScope.keySearch;

        $scope.addressCurrent = $location.absUrl();
        $scope.rated = false;

        var n = $scope.addressCurrent.indexOf('#');
        var nextVideo;
        var player;
        var idRemove, editCommentId, listDiscuss = [];

        init();

        function init() {

            if(isEmpty(videoid)){
                return ;
            }

            videoDetailService.getVideoDetailById(videoid).then(function(data){
                if(data.data.status == 'true') {
                    if( data.data.request_data_result.length==0){
                        $scope.errorInfo = "Video not found";
                    }
                    else {
                        $scope.videoInfo = data.data.request_data_result[0];
                        $scope.rateNum = Math.round($scope.videoInfo.averageRating);
                        var url = data.data.request_data_result[0].url;
                        var videoid = url.match(/(?:https?:\/{2})?(?:w{3}\.)?youtu(?:be)?\.(?:com|be)(?:\/watch\?v=|\/)([^\s&]+)/);
                        if(videoid != null) {
                            onYouTubeIframeAPIReady(videoid[1]);
                        }
                    }

                }

            });

            videoDetailService.getCommentVideoById(videoid).then(function(data){
                if(data.data.status == 'true') {
                    if( data.data.request_data_result.length==0){
                        $scope.nocommentInfo = "Have no comment";
                    }
                    else {
                        $scope.comments = data.data.request_data_result;
                    }

                }

            });

            videoDetailService.updateViewVideo(videoid).then(function (data) {
                if(data.status == 'false') {
                    console.log("Update view error");
                }
            });

            if(!isEmpty($scope.userId)) {
                videoDetailService.updateVideoHistory(videoid, $scope.userId).then(function (data) {
                    if (data.status == 'false') {
                        console.log("Update view error");
                    }
                });
            }

        }

        $scope.convertTime= function (str) {
            return convertUnixTimeToTime(str);
        }

        $scope.rateFunction = function (rate) {
            //$scope.rateNum = rate;
            if(isEmpty($scope.userId)||$scope.userId=="-1"){
                $scope.errorVideo = "Please login!";
                return ;
            }
            VideoService.checkUserRatingVideo($scope.userId, $scope.subCategoryId).then(function (data) {
                if (data.data.status) {
                    if (data.data.request_data_result.length > 0) {
                        $scope.rated = true;
                        $scope.errorVideo = "You are rated!";
                    } else {
                        VideoService.rateVideo($scope.userId, videoid, rate).then(function (data) {
                            if (data.data.status) {
                                $scope.rateNum = rate;
                            }
                        });
                    }
                }
            });

        }
        $scope.range = function(min, max, step) {
            step = step || 1;
            var input = [];
            for (var i = min; i <= max; i += step) {
                input.push(i);
            }
            return input;
        };


        function onYouTubeIframeAPIReady(youtubeId) {
            player = new YT.Player('video', {
                height: '430',
                width: '100%',
                videoId: youtubeId,
                events: {
                    'onReady': onPlayerReady,
                    'onStateChange': onPlayerStateChange
                },
                playerVars: {
                    showinfo: 0,
                    autohide: 1,
                    theme: 'dark'
                }
            });
        }

        function onPlayerReady(event) {
            event.target.playVideo();
        }

        function onPlayerStateChange(event) {
            if(event.data === 0) {
                window.location.href = '#/videos/detailVideo/' + videoid;
            }
        }



        $scope.deleteComment = function(cid) {
            $('#deleteItem').modal('show');
            idRemove = cid;
        };

        $scope.editComment = function(cid, discuss) {
            editCommentId = cid;
            $scope.discussOld = discuss;
            $('#editDiscuss').modal('show');
            CKEDITOR.instances['discuss'].setData(decodeURIComponent($scope.discussOld));
        };

        $scope.addComment = function () {
            var content = $('#add-comment').val();
            if(isEmpty(content)){
                return ;
            }
            if(isEmpty($scope.userId)) {
                $scope.errorVideo ="Please login";
                return;
            }

            videoDetailService.addCommentVideo($scope.userId, content, videoid).success(function(data) {
                if(data.status == 'true') {
                }
            });
        }

        $scope.updateComment = function() {
            var content = CKEDITOR.instances['discuss'].getData();
            CommentService.editComment(editCommentId, content).then(function(data) {
                if(data.data.request_data_result) {
                    listDiscuss = [];
                    for(var i = 1; i <= $scope.clickD; i++) {
                        getDiscuss(i);
                    }
                }
            });
        };

        $scope.profilePage = function(userId, type) {
            $location.url('/studentProfile/' + userId + '/' + 1);
        };

        $scope.showVideo = function(id_video, index) {
            $location.path('/detailVideo/' +  id_video +'/'+$scope.type +'x'+ index );
        };

        $scope.showPageSubject = function(event, subjectId) {
            var ele = event.currentTarget;
            $rootScope.keySearch = "";
            CategoriesService.getCategories(subjectId);
           // $location.path('/video_tutorial').search('subjectId', subjectId);
            $(ele).parent().find('.active').removeClass('active');
            $(ele).addClass('active');
        };

        $scope.showPageTopicOfSubject = function(subjectId, topicId) {
            $rootScope.keySearch = "";
            $location.path('/listVideo/'+ subjectId +'/'+ topicId +'/'+ 1);
        };

        $scope.loadMoreDisscusion = function() {
            //Get discussiones
            $scope.clickD++;
            SubCategoryVideoService.getDiscussesOnVideo($scope.subCategoryId, $scope.clickD, 4, 'Y')
                .success(function(data) {
                    if(data.status == 'true') {
                        var objectDiss = {};
                        objectDiss = data.request_data_result;
                        for(var i = 0; i < objectDiss.length; i++) {
                            objectDiss[i].content = decodeURIComponent(objectDiss[i].content);
                            objectDiss[i].content = $sce.trustAsHtml(objectDiss[i].content);
                            if(objectDiss[i].idFacebook == null && objectDiss[i].idGoogle == null) {
                                objectDiss[i].image = StudentService.getAvatar(objectDiss[i].authorId);
                            } else {
                                objectDiss[i].image = StudentService.getAvatar(objectDiss[i].authorId);
                            }
                            objectDiss[i].timestamp = convertUnixTimeToTime(objectDiss[i].timestamp);
                            $scope.discussiones.push(objectDiss[i]);
                        }
                    } else {
                       // $location.url('/video_tutorial');
                    }

                    SubCategoryVideoService.getDiscussesOnVideo($scope.subCategoryId, $scope.clickD + 1, 4, 'Y')
                        .success(function(data) {
                            if(data.request_data_result.length === 0) {
                                $scope.featureD = 1;
                            }
                        });
                });
        };

        $('#main .wrap_search').find('.search').keypress(function(event) {
            if(event.keyCode == 13) {
                var searchText = $('#main .wrap_search').find('.search').val();
                $rootScope.keySearch = searchText;
                $scope.$apply(function () {
                    $location.path('/searchVideo/' + searchText + '/' + 1);
                });
            }
        });

        var discussNew = '';
        $scope.addVideoComment = function(video_title) {
            if($scope.joinDiscussion != null) {
                SubCategoryVideoService.addComment($scope.userId, $scope.joinDiscussion, $scope.subCategoryId).success(function(data) {
                    if(data.status == 'true') {
                        SubCategoryVideoService.getDiscussesOnVideo($scope.subCategoryId, 1, 4, 'Y').success(function(data) {
                            if(data.status == 'true') {
                                $scope.discussiones = data.request_data_result;

                                for(var i in $scope.discussiones) {
                                    if($scope.discussiones[i].idFacebook == null && $scope.discussiones[i].idGoogle == null) {
                                        $scope.discussiones[i].image = StudentService.getAvatar($scope.discussiones[i].authorId);
                                    } else {
                                        $scope.discussiones[i].image = StudentService.getAvatar($scope.discussiones[i].authorId);
                                    }
                                    $scope.discussiones[i].timestamp = convertUnixTimeToTime($scope.discussiones[i].timestamp);
                                }
                            }
                        });
                    }
                });
                $scope.joinDiscussion = null;
            } else {
                $rootScope.myVar = !$scope.myVar;
                $timeout(function () {
                    $rootScope.myVar = false;
                    $('#divProgress').addClass('hide');
                }, 2500);
            }
        };

        $scope.addClassHide = function() {
            if($scope.userId == null) {
                return 'hide';
            }
            return '';
        };

        $scope.markVideoAsWatched = function() {
            // markVideoAs Watched
            VideoService.markVideoAsWatched( $scope.userId, $scope.subCategoryId);
        };

        $scope.showCKEditor = function(event) {
            showCKEditor = true;
            var ele = event.currentTarget;
            $('.content .discus .boxCkComment').toggle('slow');
            $('.content .discus .ckComment').removeClass('hide');
            $('.content .discus .go').addClass('hide');
            $('.content .discus .btnHide').removeClass('hide');
        };

        $scope.cancel = function() {
            revertCKeditor(showCKEditor);
        };

        $scope.sendDiscus = function() {
            var content = CKEDITOR.instances["txtDiscus"].getData();
            if(content.length > 0) {
                SubCategoryVideoService.addComment($scope.userId, content, $scope.subCategoryId).success(function(data) {
                    if(data.status == 'true') {
                        SubCategoryVideoService.getDiscussesOnVideo($scope.subCategoryId, 1, 4, 'Y').success(function(data) {
                            if(data.status == 'true') {
                                $scope.discussiones = data.request_data_result;
                                for(var i in $scope.discussiones) {
                                    if($scope.discussiones[i].idFacebook == null && $scope.discussiones[i].idGoogle == null) {
                                        $scope.discussiones[i].image = StudentService.getAvatar($scope.discussiones[i].authorId);
                                    } else {
                                        $scope.discussiones[i].image = StudentService.getAvatar($scope.discussiones[i].authorId);
                                    }
                                    $scope.discussiones[i].timestamp = convertUnixTimeToTime($scope.discussiones[i].timestamp);
                                    $scope.discussiones[i].content = decodeURIComponent($scope.discussiones[i].content);
                                    $scope.discussiones[i].content = $sce.trustAsHtml($scope.discussiones[i].content);
                                }
                            }
                        });
                        revertCKeditor(showCKEditor);
                    }
                });
            } else {
                $rootScope.myVar = !$scope.myVar;
                $timeout(function () {
                    $rootScope.myVar = false;
                    $('#divProgress').addClass('hide');
                }, 2500);
            }
        };

        $scope.deleteItem = function() {
            CommentService.deleteComment(idRemove).then(function(data) {
                if(data.data.status) {
                    listDiscuss = [];
                    for(var i = 1; i <= $scope.clickD; i++) {
                        getDiscuss(i);
                    }
                }
            });
        };
    }]);