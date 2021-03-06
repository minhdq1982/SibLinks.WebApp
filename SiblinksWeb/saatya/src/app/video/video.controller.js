/**
 * Created by Tavv on 06/09/2016.
 */
brotControllers.controller('VideoCtrl', ['$scope', '$http', '$location', '$rootScope', '$timeout', '$log', 'HomeService', 'MentorService', 'TeamMentorService', 'StudentService', 'VideoService', 'myCache', 'QuestionsService',
    function ($scope, $http, $location, $rootScope, $timeout, $log, HomeService, MentorService, TeamMentorService, StudentService, VideoService, myCache, QuestionsService) {
    /* Video Subscription begin */
	$scope.listCategorySubscription = null;
	$scope.totalSubscription = 0;
	$scope.displayNumberOfVideoRecent = 6;
	$scope.displayNumberOfVideoWeek = 6;
	$scope.displayNumberOfVideoOlder = 6;
	$scope.coutWeek = 0;
	$scope.isSubscribe = 0;

	$scope.listRecentVideo = {};
	$scope.listWeekVideo = {};
	$scope.listOlderVideo = {};
	
	$scope.flagShowMoreRecent = true;
	$scope.flagShowMoreWeek = true;
	$scope.flagShowMoreOlder = true;
	
	$scope.showDivRecent = false;
	$scope.showDivWeek = false;
	$scope.showDivOlder = false;
	
	$scope.listMentorOlder = [];
	var checkExist = false;
	
	$scope.userId = localStorage.getItem('userId');
	
	VideoService.getListCategorySubscription().then(function(data) {
		$scope.listCategorySubscription = data.data.request_data_result;
		resetAttributes();
		if($scope.listCategorySubscription.length > 0) {
			VideoService.getListVideoSubscription($scope.userId, $scope.listCategorySubscription[0].subjectId).then(function(data) {
				$scope.listVideoSubscription = data.data.request_data_result;
				fillAllVideo($scope.listVideoSubscription);
				
				$scope.selected = $scope.listCategorySubscription[0].subjectId.toString();
			});	
		}
	});	
	
	
	$scope.showVideoWithCategory = function(event) {
		$scope.selected = event.target.id; 
		resetAttributes();
		
		VideoService.getListVideoSubscription($scope.userId, event.target.id).then(function(data) {
			$scope.listVideoSubscription = data.data.request_data_result;
			fillAllVideo($scope.listVideoSubscription);
		});	
	}
	
	
	$scope.isActive = function(index) {
        return $scope.selected === index.toString();
	};
	
	
	function resetAttributes() {
		$scope.displayNumberOfVideoRecent = 6;
		$scope.displayNumberOfVideoWeek = 6;
		$scope.displayNumberOfVideoOlder = 6;
		$scope.totalSubscription = 0;
		$scope.listRecentVideo = {};
		$scope.listWeekVideo = {};
		$scope.listOlderVideo = {};
		$scope.showDivRecent = false;
		$scope.showDivWeek = false;
		$scope.showDivOlder = false;
		$scope.flagShowMoreRecent = true;
		$scope.flagShowMoreWeek = true;
		$scope.flagShowMoreOlder = true;
		$scope.listMentorOlder = [];
	}

	
	function fillAllVideo(allVideo) {
		if(typeof allVideo[1] != 'undefined') {
			$scope.listRecentVideo = allVideo[1];
			$scope.totalSubscription += $scope.listRecentVideo.length;
			$scope.showDivRecent = true;
		}
		if(typeof allVideo[2] != 'undefined') {
			$scope.listWeekVideo = allVideo[2];
			$scope.totalSubscription += $scope.listWeekVideo.length;
			$scope.showDivWeek = true;
		}
		if(typeof allVideo[3] != 'undefined') {
			$scope.listOlderVideo = allVideo[3];
			$scope.totalSubscription += $scope.listOlderVideo.length;
			$scope.showDivOlder = true;
			
			for(var i = 0; i < $scope.listOlderVideo.length; i++) {
				checkExist = false;
				for(var j = 0; j < $scope.listMentorOlder.length; j++) {
					if($scope.listOlderVideo[i].authorID == $scope.listMentorOlder[j].authorID) {
						checkExist = true;
						break;
					}
				}
				if(!checkExist) {
					$scope.listMentorOlder.push({
						"authorID" : $scope.listOlderVideo[i].authorID,
						"authorName": $scope.listOlderVideo[i].author
					});
				}
			}
		}
	}
	
	$scope.showMoreVideoRecent = function(){
		if ($scope.listRecentVideo.length > 0 && $scope.displayNumberOfVideoRecent <= $scope.listRecentVideo.length) {
			$scope.displayNumberOfVideoRecent += 6;
		}
		
		if($scope.displayNumberOfVideoRecent > $scope.listRecentVideo.length) {
			$scope.flagShowMoreRecent = false;
		}
	}
	
	$scope.showMoreVideoWeek = function(){
		if ($scope.listWeekVideo.length > 0 && $scope.displayNumberOfVideoWeek <= $scope.listWeekVideo.length) {
			$scope.displayNumberOfVideoWeek += 6;
		}
		
		if($scope.displayNumberOfVideoWeek > $scope.listWeekVideo.length) {
			$scope.flagShowMoreWeek = false;
		}
	}
	
	$scope.showMoreVideoOlder = function(){
		if ($scope.listOlderVideo.length > 0 && $scope.displayNumberOfVideoOlder <= $scope.listOlderVideo.length) {
			$scope.displayNumberOfVideoOlder += 6;
		}
		
		if($scope.displayNumberOfVideoOlder > $scope.listOlderVideo.length) {
			$scope.flagShowMoreOlder = false;
		}
	}
	
	$scope.rangeSubscription = function(count){
		var ratings = []; 
		for (var i = 0; i < count; i++) { 
			ratings.push(i) 
		} 
		return ratings;
	}
	
	$scope.caculateTimeElapsed = function (time){
		var current = new Date().getTime();
		var inputTime = new Date(time);
		var secondElapsed = parseInt(Math.floor((current - inputTime)/1000));
		secondElapsed = (secondElapsed < 1)? 1 : secondElapsed;
		var text = '';

		if (years = parseInt((Math.floor(secondElapsed / 31536000))))
			text = years + (years > 1 ? ' years' : ' year') + ' ago';
		else if (months = parseInt((Math.floor(secondElapsed / 2592000))))
			text = months + (months > 1 ? ' months' : ' month') + ' ago';
		else if (weeks = parseInt((Math.floor(secondElapsed / 604800))))
			text = weeks + (weeks > 1 ? ' weeks' : ' week') + ' ago';
		else if (days = parseInt((Math.floor(secondElapsed / 86400))))
			text = days + (days > 1 ? ' days' : ' day') + ' ago';
		else if (hours = parseInt((Math.floor(secondElapsed / 3600))))
			text = hours + (hours > 1 ? ' hours' : ' hour') + ' ago';
		else if (minutes = parseInt((Math.floor(secondElapsed / 60))))
			text = minutes + (minutes > 1 ? ' minutes' : ' minute') + ' ago';
		else
			text = secondElapsed + (secondElapsed > 1 ? ' seconds' : ' second') + ' ago';

		return text;
	}	
	
	$scope.subscrible = function(mentorId) {
        if(isEmpty($scope.userId)){
            return ;
        }
        VideoService.setSubscribeMentor($scope.userId, mentorId).then(function (data) {
            if(data.data.status =="true") {
                if (data.data.request_data_type == "subs") {
                    $scope.isSubscribe = 1;
                    removeMentor(mentorId);
                }
                else {
                    $scope.isSubscribe = -1;
                }
            }
        });
	}
	
    function removeMentor(mentorId) {
        if ($scope.listMentorOlder !== undefined) {
        	for(var i = 0; i < $scope.listOlderVideo.length; i++) {
        		if($scope.listOlderVideo[i].authorID == mentorId) {
        			updateTopMentorSubscribled($scope.listOlderVideo[i]);
        		}
        	}
        	
        	for(var i = 0; i < $scope.listMentorOlder.length; i++) {
        		if($scope.listMentorOlder[i].authorID == mentorId) {
        			$scope.listMentorOlder.splice(i, 1);
        			if($scope.listMentorOlder.length == 0) {
        				$scope.showDivOlder = false;
        			}
        		}
        	}
        }
    };
    
    function updateTopMentorSubscribled(item) {
        if (item) {
            var listNewTopMentor = $scope.listMentorSubscribed;
            var subjects = myCache.get("subjects");
            var itemConvert = {};
            itemConvert.MentorName = item.author;
            itemConvert.MentorId = item.authorID;
            itemConvert.avatar = item.imageUrl;
            itemConvert.videoNew = 1;

            itemConvert.subjects = [];
            for(var i = 0; i < $scope.listCategorySubscription.length; i++) {
        		if($scope.listCategorySubscription[i].subjectId == item.subjectId) {
        			itemConvert.subjects.push($scope.listCategorySubscription[i].subject);
        			break;
        		}
        	}
            
            listNewTopMentor.unshift(itemConvert);
            $scope.listMentorSubscribed = listNewTopMentor;
        }
    }
	/* Video Subscription end */


        var video_recommended = [];
        var subjects = [];
        brot.signin.statusStorageHtml();
        $scope.login = 0;

        var userId = localStorage.getItem('userId');

        var nameOfUser = localStorage.getItem("nameHome");

        var ParseType = {
            RECOMMENDED: 0,
            RECENTLY: 1,
            RECOMMENDED_FOR_YOU: 2,
            SUBCRIBE: 3
        };

        var CountType = {
            HOME: 0,
            MENTOR: 1,
            SUBCRIPTION: 2,
            HISTORY: 3,
            FAVOURITE: 4
        };


        var limit = 6;
        var offset = 0;
        var currentPageRecommended = 0, currentPageRecently = 0;
        var isInitRecommended = true;
        var isInitRecently = true;
        var isInitRecommendedForYou = true;
        $scope.totalVideosRecommended = 0;
        $scope.totalVideosRecently = 0;
        var hasLoadMore = false;
        init();

        function init() {
            // isInitRecommended = true;
            // isInitRecently = true;
            if (userId !== null) {
                $scope.login = 1;
                getMentorSubscribed(userId);
                getVideoBySubject(userId, -1, 6, 0);
                getCountFactory(CountType.HOME);
            } else {
                getVideoBySubject(-1, -1, 6, 0);
            }
            getNewVideoMentorSubscribe(userId);
        }


        VideoService.getSubjects().then(function (response) {
            if (response.data.status) {
                var subjects = response.data.request_data_result;
                if (subjects) {
                    $scope.listSubjects = subjects;
                }
            }
        });

        function parseDataFactory(type, data) {
            switch (Number(type)) {
                case ParseType.RECOMMENDED:
                    return parseDataVideoRecommended(data);
                case ParseType.RECENTLY:
                    return parseDataVideoRecently(data);
                case ParseType.RECOMMENDED_FOR_YOU:
                    return parseDataVideoRecommendedForU(data);
                case ParseType.SUBCRIBE:
                    return parseDataVideoSubcribe(data);
                default:
                    return null;
            }
        }

        $scope.subjectId = -1;
        $scope.sortBySubject = function (subjectId) {
            resetAllData();
            $scope.subjectId = subjectId;
            if (userId) {
                getVideoBySubject(userId, subjectId, 6, 0);
            } else {
                getVideoBySubject(-1, subjectId, 6, 0);
            }
        };

        function getVideoBySubject(userid, subjectId, limit, offset) {
            if (userid) {
                VideoService.getVideoBySubject(userid, subjectId, limit, offset).then(function (response) {
                    var allData = response.data.request_data_result;
                    if (allData) {
                        var rs_recommended = allData.recommended;
                        if (rs_recommended) {
                            var resultListRecommended = parseDataFactory(ParseType.RECOMMENDED, rs_recommended);
                            $scope.listVideoRecommended = !hasLoadMore ? resultListRecommended : $scope.listVideoRecommended.concat(resultListRecommended);
                            $scope.totalVideosRecommended = $scope.listVideoRecommended.length;
                        }
                        var data_recommend_4_u = allData.recommended_for_you;
                        if (data_recommend_4_u) {
                            $scope.listVideoRecommendedForU = parseDataFactory(ParseType.RECOMMENDED_FOR_YOU, data_recommend_4_u);
                        }
                        var data_recently = allData.recently;
                        if (rs_recommended) {
                            var resultVideoRecently = parseDataFactory(ParseType.RECENTLY, data_recently);
                            if (resultVideoRecently != null) {
                                $scope.listVideoRecently = !hasLoadMore ? resultVideoRecently : $scope.listVideoRecently.concat(resultVideoRecently);
                                $scope.totalVideosRecently = $scope.listVideoRecently.length;
                            }
                        }
                    }
                });
                return subjectId;
            }
        }

        function parseDataVideoRecommendedForU(data) {
            var listVideoRecommended = [];
            var imgTemp = "/images/default_video.png";
            var result_data = data;
            for (var i = 0; i < result_data.length; i++) {
                var item = result_data[i];
                var videosByMentorName = [];
                if (item) {
                    var conditionOfLoop = item.length > 6 ? 6 : item.length;
                    for (var y = 0; y < conditionOfLoop; y++) {
                        var obj = item[y];
                        var videoOfMentor = {};
                        videoOfMentor.userId = obj.userid;
                        videoOfMentor.name = obj.name;
                        videoOfMentor.image = obj.image == null ? imgTemp : obj.image;
                        videoOfMentor.link = obj.url;
                        videoOfMentor.vid = obj.vid;
                        videoOfMentor.title = obj.title;
                        videoOfMentor.avatar = obj.imageUrl;
                        videoOfMentor.defaultSubjectId = obj.defaultSubjectId;
                        videoOfMentor.runningTime = obj.runningTime;
                        videoOfMentor.values_count = obj.values_count;
                        videoOfMentor.avgRating = obj.averageRating;
                        videoOfMentor.numViews = obj.numViews;
                        videoOfMentor.numComments = obj.numComments;
                        videoOfMentor.countSubscribe = obj.countSubscribe;
                        videoOfMentor.time = convertUnixTimeToTime(obj.timeStamp);
                        videosByMentorName.push(videoOfMentor);
                    }
                    listVideoRecommended.push(videosByMentorName);
                }
            }
            return listVideoRecommended;
        }

        function parseDataVideoRecommended(data) {
            if (data) {
                if (isInitRecommended) {
                    var listVideos = [];
                    for (var i = 0; i < data.length; i++) {
                        var objVideo = {};
                        var video = data[i];
                        objVideo.vid = video.vid;
                        objVideo.title = video.title;
                        objVideo.authorID = video.authorID;
                        objVideo.userName = video.name;
                        objVideo.description = video.description;
                        objVideo.image = video.image;
                        objVideo.link = video.url;
                        objVideo.time = video.runningTime;
                        objVideo.topicId = video.topicId;
                        objVideo.subjectId = video.subjectId;
                        objVideo.numViews = video.numViews;
                        objVideo.numRatings = video.numRatings;
                        objVideo.numComments = video.numComments;
                        objVideo.averageRating = video.averageRating;
                        objVideo.timeAgo = convertUnixTimeToTime(video.timeStamp);
                        listVideos.push(objVideo);
                    }
                    return listVideos;
                }
                return null;
            }

        }

        function parseDataVideoRecently(data) {
            if (data) {
                if (isInitRecently) {
                    var listVideosRecently = [];
                    for (var i = 0; i < data.length; i++) {
                        var objVideo = {};
                        var video = data[i];
                        objVideo.vid = video.vid;
                        objVideo.authorID = video.authorID;
                        objVideo.userName = video.name;
                        objVideo.title = video.title;
                        objVideo.image = video.image;
                        objVideo.link = video.url;
                        objVideo.numViews = video.numViews;
                        objVideo.subjectId = video.subjectId;
                        objVideo.numRatings = video.numRatings;
                        objVideo.averageRating = video.averageRating;
                        objVideo.numComments = video.numComments;
                        objVideo.timeAgo = convertUnixTimeToTime(video.timeStamp);
                        objVideo.enable = video.videoEnable;
                        listVideosRecently.push(objVideo);
                    }
                    return listVideosRecently;
                }
            }
            return null;
        }

        function parseDataVideoSubcribe(data) {
            if (data) {
                var listVideosSubcribe = [];
                for (var i = 0; i < data.length; i++) {
                    var objVideo = {};
                    var video = data[i];
                    objVideo.vid = video.vid;
                    objVideo.authorID = video.authorID;
                    objVideo.userName = video.userName;
                    objVideo.title = video.title;
                    objVideo.image = video.image;
                    objVideo.link = video.url;
                    objVideo.numViews = video.numViews;
                    objVideo.subjectId = video.subjectId;
                    objVideo.numRatings = video.numRatings;
                    objVideo.runningTime = video.runningTime;
                    objVideo.averageRating = video.averageRating;
                    objVideo.timeAgo = convertUnixTimeToTime(video.timeStamp);
                    listVideosSubcribe.push(objVideo);
                }
            }
            return listVideosSubcribe;
        };

        function search(nameKey, myArray) {
            for (var i = 0; i < myArray.length; i++) {
                if (myArray[i].name === nameKey) {
                    return myArray[i];
                }
            }
        };

        function getMentorSubscribed(userId) {
            if (userId) {
                VideoService.getMentorSubscribed(userId, 5, 0).then(function (response) {
                    if (response.data.status) {
                        var result = response.data.request_data_result;
                        if (result) {
                            var listMentorSubscribe = [];
                            for (var i = 0; i < result.length; i++) {
                                var element = result[i];
                                var subscribe = {};
                                subscribe.mentorId = element.userid;
                                subscribe.mentorName = element.name;
                                subscribe.subjectId = element.defaultSubjectId;
                                subscribe.subjects = convertToArray(element.subjects);
                                subscribe.avatar = element.imageUrl;
                                listMentorSubscribe.push(subscribe);
                            }
                            $scope.listMentorSubscribe = listMentorSubscribe;
                            return listMentorSubscribe;
                        }
                    }
                });
            }
        }


        TeamMentorService.getTopMentorsByType(5, 0, 'subcribe', userId).then(function (data) {
            var data_result = data.data.request_data_result;
            var subjects = myCache.get("subjects");
            if (data_result) {
                var listTopMentors = [];
                for (var i = 0; i < data_result.length; i++) {
                    var mentor = {};
                    if (data_result[i].isSubs == 1) {
                        mentor.userid = data_result[i].userid;
                        mentor.userName = data_result[i].userName;
                        mentor.imageUrl = data_result[i].imageUrl;
                        mentor.numsub = data_result[i].numsub;
                        mentor.numvideos = data_result[i].numvideos;
                        mentor.defaultSubjectId = data_result[i].defaultSubjectId;
                        mentor.listSubject = getSubjectNameById(data_result[i].defaultSubjectId, subjects);
                        mentor.numAnswers = data_result[i].numAnswers;
                        listTopMentors.push(mentor);
                    }
                }
                $scope.listMentorBySubs = listTopMentors;
            }
        });


        function convertToArray(strSubjects) {
            return strSubjects != null ? strSubjects.split(',') : null;
        }

        function getNewVideoMentorSubscribe(userId) {
            if (userId) {
                VideoService.getNewVideoMentorSubscribe(userId, 5, 0).then(function (response) {
                    if (response.data.status) {
                        var result = response.data.request_data_result;
                        if (result) {
                            var listMentorSubscribe = [];
                            for (var i = 0; i < result.length; i++) {
                                var element = result[i];
                                var subscribe = {};
                                subscribe.MentorName = element.mentorName;
                                subscribe.MentorId = element.MentorId;
                                subscribe.avatar = element.imageUrl;
                                subscribe.videoNew = element.values_count;
                                subscribe.subjects = convertToArray(element.subjects);
                                //console.log(subscribe.subjects);
                                listMentorSubscribe.push(subscribe);
                            }
                            $scope.listMentorSubscribed = listMentorSubscribe;
                            return listMentorSubscribe;
                        }
                    }
                });
            }
        }

        $scope.addCommentVideo = function (content, videoId) {
            //TODO: Handle event when user comment
            VideoService.addComment(userId, nameOfUser, content, videoId).then(function (response) {
                if (response.data.request_data_result[0]) {
                    console.log("Add Comment Successful");
                }
            });

        };

        function getCountAnsSubVideoMentor(mentorId, key) {
            if (mentorId) {
                VideoService.getCountFactory(mentorId, key).then(function (response) {
                    if (response.data.status) {
                        var result = response.data.request_data_result[0];
                        $scope.countResult = result;
                        return result;
                    }
                });
            }
        }

        function getVideoRecommendedForYou(userId) {
            if (userId) {
                VideoService.getVideoRecommendedForYou(userId).then(function (data) {
                    if (data.data.status) {
                        // var listVideoRecommended = {};
                        if (isInitRecommendedForYou) {
                            var listVideoRecommended = [];
                            var imgTemp = "http://ndl.mgccw.com/mu3/app/20140318/19/1395152622265/sss/4_small.png";
                            var result_data = data.data.request_data_result;
                            for (var i = 0; i < result_data.length; i++) {
                                var item = result_data[i];
                                var videosByMentorName = [];
                                if (item) {
                                    // var mentorName = Object.keys(item)[i];
                                    // nameOfMentor.push(mentorName);
                                    var conditionOfLoop = item.length > 6 ? 6 : item.length;
                                    for (var y = 0; y < conditionOfLoop; y++) {
                                        var obj = item[y];
                                        var videoOfMentor = {};
                                        videoOfMentor.userId = obj.userid;
                                        videoOfMentor.name = obj.name;
                                        videoOfMentor.image = obj.image  == null ? imgTemp : obj.image;
                                        videoOfMentor.link = obj.url;
                                        videoOfMentor.vid = obj.vid;
                                        videoOfMentor.title = obj.title;
                                        videoOfMentor.avatar = obj.imageUrl;
                                        videoOfMentor.defaultSubjectId = obj.defaultSubjectId;
                                        videoOfMentor.runningTime = obj.runningTime;
                                        videoOfMentor.values_count = obj.values_count;
                                        videoOfMentor.avgRating = obj.averageRating;
                                        videoOfMentor.numViews = obj.numViews;
                                        videoOfMentor.numComments = obj.numComments;
                                        videoOfMentor.countSubscribe = obj.countSubscribe;
                                        videoOfMentor.time = convertUnixTimeToTime(obj.timeStamp);
                                        videosByMentorName.push(videoOfMentor);
                                    }
                                    listVideoRecommended.push(videosByMentorName);
                                }
                            }
                            $scope.listVideoRecommended = listVideoRecommended;
                        }
                    }
                });
            }
        }


        function removeObject(index) {
            if ($scope.listVideoRecommendedForU !== undefined) {
                updateTopMentorBlock($scope.listVideoRecommendedForU[0][0]);
                $scope.listVideoRecommendedForU.splice(index, 1);
                if ($scope.listVideoRecommendedForU.length == 0) {
                    var target = angular.element(document.querySelector("div.video-member-title h5"));
                    console.log(target);
                }
            }
        };


        function getCountFactory(type) {
            var key = null;
            switch (Number(type)) {
                case CountType.HOME:
                    key = "home";
                    break;
                case CountType.MENTOR:
                    key = "mentor";
                    break;
                case CountType.SUBSCRIPTION:
                    key = "subcription";
                    break;
                case CountType.HISTORY:
                    key = "history";
                    break;
                case CountType.FAVOURITE:
                    key = "favourite";
                    break;
            }
            if (userId) {
                VideoService.getCountFactory(userId, key).then(function (response) {
                    if (response.data.status) {
                        var result = response.data.request_data_result[0];
                        $scope.countResult = result;
                        return result;
                    }

                    // var countByCategory = [];
                    // for (var i = 0; i < result.length; i++) {
                    //     var category = {};
                    //     category.history = result[i].video_watched;
                    //     category.subcription = result[i].subcribed;
                    //     category.favourite = result[i].video_like;
                    //     countByCategory.push(category);
                    // }
                    //
                    // $scope.countResult = countByCategory;
                });
            }
        }


        $scope.redirectPageFactory = function (page) {
            switch (page) {
                case 'top_mentor':
                    $location.path('/team');
                    break;
                case 'manage_subscription':
                    $location.path('/studentprofile/' + userId);
                    break;
            }
        };

        $scope.setSubscribeMentor = function (mentorId) {
            if (isEmpty(userId)) {
                return;
            }
            VideoService.setSubscribeMentor(userId, mentorId + "").then(function (response) {
                if (response.data.status == "true") {
                    if (response.data.request_data_type == "subs") {
                        $scope.isSubscribe = 1;
                        removeObject(0);
                        console.log("Set Subscribe Mentor Successful");
                    }
                    else {
                        $scope.isSubscribe = -1;
                    }
                }
            });
        };

        function updateTopMentorBlock(item) {
            if (item) {
                var listNewTopMentor = $scope.listMentorSubscribed;
                var subjects = myCache.get("subjects");
                var itemConvert = {};
                itemConvert.MentorName = item.name;
                itemConvert.MentorId = item.userId;
                itemConvert.avatar = item.avatar;
                itemConvert.videoNew = item.values_count;
                var subject_results = getSubjectNameById(item.defaultSubjectId, subjects);
                itemConvert.subjects = [];
                for (var i = 0; i < subject_results.length; i++) {
                    itemConvert.subjects.push(subject_results[i].name);
                }
                listNewTopMentor.unshift(itemConvert);
                $scope.listMentorSubscribed = listNewTopMentor;
            }
        }


        $scope.isLoadMoreRecommended = false;
        $scope.isLoadMoreRecently = false;
        $scope.loadMoreVideo = function (loadMoreWithBlock, subjectId) {
            hasLoadMore = true;
            switch (loadMoreWithBlock) {
                case 1 :
                    currentPageRecommended++;
                    if ($scope.isLoadMoreRecommended)
                        return;
                    //$scope.isLoadMoreRecommended = true;
                    var newoffsetRecommended = limit * currentPageRecommended;
                    isInitRecommended = true;
                    isInitRecently = false;
                    isInitRecommendedForYou = false;
                    if (newoffsetRecommended > $scope.totalVideosRecommended) {
                        newoffsetRecommended = $scope.totalVideosRecommended;
                        $scope.isLoadMoreRecommended = true;
                    } else {
                        $scope.isLoadMoreRecommended = false;
                        if (userId) {
                            getVideoBySubject(userId, subjectId, limit, newoffsetRecommended);
                        } else {
                            getVideoBySubject(-1, subjectId, limit, offset);
                        }
                    }
                    break;
                case 2 :
                    currentPageRecently++;
                    if ($scope.isLoadMoreRecently)
                        return;
                    $scope.isLoadMoreRecently = true;
                    var newoffsetRecently = limit * currentPageRecently;
                    isInitRecently = true;
                    isInitRecommended = false;
                    isInitRecommendedForYou = false;
                    if (newoffsetRecently > $scope.totalVideosRecently) {
                        newoffsetRecently = $scope.totalVideosRecently;
                        $scope.isLoadMoreRecently = true;
                    }
                    else {
                        $scope.isLoadMoreRecently = false;
                        if (userId) {
                            getVideoBySubject(userId, subjectId, limit, newoffsetRecently);
                        } else {
                            getVideoBySubject(-1, subjectId, limit, offset);
                        }
                    }
                    break;
                case 3 :

                    break;
            }


        };

        //MTDU
        var listVideos = '';
        if (myCache.get("listVideos") !== undefined) {
            $log.info("My cache already exists");
            $scope.listVideos = myCache.get("listVideos");
        } else {
            VideoService.getHistoryVideosList(localStorage.getItem('userId')).then(function (data) {
                if (data.data.status) {
                    $scope.listVideos = data.data.request_data_result;
                    for (var i = 0; i < $scope.listVideos.length; i++) {
                        $scope.listVideos[i].timeStamp = caculateTimeElapsed($scope.listVideos[i].timeStamp)
                    }
                    myCache.put("listVideos", $scope.listVideos);
                }
            });
        }

        $scope.numberOfHistoryVideo = 4;
        $scope.flagShowMoreButton = true;
        $scope.showMore = function () {
            if ($scope.listVideos.length > 0 && $scope.numberOfHistoryVideo < $scope.listVideos.length)
                $scope.numberOfHistoryVideo += 4;
            if ($scope.numberOfHistoryVideo > $scope.listVideos.length)
                $scope.flagShowMoreButton = false;
        }

        $scope.range = function (num) {
            return new Array(num);
        }

        $scope.resetFlagShowMore = function () {
            $scope.numberOfHistoryVideo = 4;
            $scope.flagShowMoreButton = true;
        }
        $scope.xxxxx = function () {

            window.location.href = '/#/videos';
           // window.location.reload();

        }


        function resetAllData() {
            if ($scope.listVideoRecommended) {
                $scope.listVideoRecommended = [];
                currentPageRecommended = 0;
                $scope.totalVideosRecommended = 0;
            }
            if ($scope.listVideoRecently) {
                $scope.listVideoRecently = [];
                currentPageRecently = 0;
                $scope.totalVideosRecently = 0;
            }
            if(!isInitRecommended){
                isInitRecommended = true;
            }
            if(!isInitRecently){
                isInitRecently = true;
            }
        }


    }]);