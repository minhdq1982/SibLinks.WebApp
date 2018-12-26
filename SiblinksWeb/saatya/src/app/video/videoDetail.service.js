brotServices.factory('videoDetailService', ['$http', function ($http) {
    var factory = {};


    factory.getVideoDetailById = function (vid) {
        var promise = $http({
            method: 'get',
            url: NEW_SERVICE_URL + 'videodetail/getVideoDetailById/' + vid
        });
        return promise;
    };
    factory.getCommentVideoById = function (vid) {
        var promise = $http({
            method: 'get',
            url: NEW_SERVICE_URL + 'videodetail/getCommentVideoById/' + vid
        });
        return promise;
    };


    factory.addCommentVideo = function(userId, content, videoId) {
        var promise = $http({
            method: 'POST',
            url: NEW_SERVICE_URL + 'comments/addComment',
            data: {
                "request_data_type":"video",
                "request_data_method":"add_comment",
                "request_data": {
                    "authorID": userId,
                    "content": encodeURIComponent(content),
                    "vid": videoId
                }
            }
        });
        return promise;
    };


    factory.updateViewVideo = function(vid) {
        var promise = $http({
            method: 'POST',
            url: NEW_SERVICE_URL + 'video/updateViewVideo',
            data: {
                "request_data_type": "video",
                "request_data_method": "updateViewVideo",
                "request_data": {
                    "vid": vid
                }
            }
        });
        return promise;
    };

    factory.updateVideoHistory = function( vid,userId) {
        var promise = $http({
            method: 'POST',
            url: NEW_SERVICE_URL + 'videodetail/updateVideoHistory',
            data: {
                "request_data_type": "video",
                "request_data_method": "updateVideoHistory",
                "request_data": {
                    "vid": vid,
                    "uid":userId
                }
            }
        });
        return promise;
    };
    factory.updateWatchedVideo = function(uid, vid) {
        var promise = $http({
            method: 'POST',
            url: NEW_SERVICE_URL + 'video/updateVideoWatched',
            data: {
                "request_data_type": "video",
                "request_data_method": "updateVideoWatched",
                "request_data": {
                    "uid": uid,
                    "vid": vid
                }
            }
        });
        return promise;
    };


    return factory;
}]);