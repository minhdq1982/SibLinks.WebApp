brotControllers.controller('StudentProfileController', 
  ['$sce','$scope', '$modal', '$routeParams', '$http', '$location', 'StudentService', 'QuestionsService', 'MentorService', 'TeamMentorService', 'myCache',
                                       function ($sce, $scope, $modal, $routeParams, $http, $location, StudentService, QuestionsService, MentorService, TeamMentorService, myCache) {


    var userId = localStorage.getItem('userId'); 
    var userName = localStorage.getItem('userName'); 
    var userType = localStorage.getItem('userType');
    
    var isInit = true;
    $scope.baseIMAGEQ = NEW_SERVICE_URL + '/comments/getImageQuestion/';
    $scope.sections = [
      {name: 'My Questions'},
      {name: 'Essay'},
      {name: 'Setting'}
    ];
    $scope.selected = $scope.sections[0];


    init();

    function init(){
      getMentorInfo();
      getStudentProfile();
      getQuestionByUserId();
      getEssayProfile();
    }


    function getMentorInfo(){
      TeamMentorService.getTopMentorsByType(5, 0, 'subcribe', userId).then(function (data) {
          var data_result = data.data.request_data_result;
          var subjects = myCache.get("subjects");
          if (data_result) {
              var listTopMentors = [];
              for (var i = 0; i < data_result.length; i++) {
                  var mentor = {};
                  if(data_result[i].isSubs == 1){
                      mentor.userid = data_result[i].userid;
                      mentor.userName = data_result[i].userName;
                      mentor.imageUrl = data_result[i].imageUrl;
                      mentor.numsub = data_result[i].numsub;
                      mentor.numvideos = data_result[i].numvideos;
                      mentor.defaultSubjectId = data_result[i].defaultSubjectId;
                      mentor.listSubject= getSubjectNameById(data_result[i].defaultSubjectId, subjects);
                      mentor.numAnswers = data_result[i].numAnswers;
                      listTopMentors.push(mentor);
                  }
              }
              $scope.listMentorBySubs = listTopMentors;
          }            
      });
    }

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

    function getEssayProfile(){
      StudentService.getEssayProfile(userId, 0, 0).then(function(data){        
          $scope.essays = data.data.request_data_result;
      });
    }

    function getStudentProfile(){
      StudentService.getUserProfile(userId, userType).then(function(data){
        if (data.data.status) {
          $scope.student = data.data.request_data_result;       
          $scope.student.fullName = $scope.student.firstname + ' ' + $scope.student.lastName;
          $scope.student.imageUrl = $scope.student.imageUrl.indexOf('http') == -1 ? $scope.baseIMAGEQ + $scope.student.imageUrl : $scope.student.imageUrl;
          $scope.student.registrationTime = moment($scope.student.registrationTime).format('MMM, YYYY ', 'en');
          if ($scope.student.gender == "Male")
            $('#male').prop('checked', true);
          if ($scope.student.gender == "Male")
            $('#female').prop('checked', true);
          else
            $('#other').prop('checked', true);
          displaySetting();
        }
      });
    }

    function displaySetting(){
      $("#firstName").val($scope.student.firstname);
      $("#lastName").val($scope.student.lastName);
      $("#about").val($scope.student.description);
      $("#email").val($scope.student.email);
      $("#description").val($scope.student.description);
      $("#bio").val($scope.student.bio);
      var favorite = $scope.student.favorite.split(',');
      for (var i = 0; i < favorite.length; i++) {
        if (favorite[i] == 'Music') 
          $('#music').prop('checked', true);
        if (favorite[i] == 'Art') 
          $('#art').prop('checked', true);
        if (favorite[i] == 'Sport') 
          $('#sport').prop('checked', true);
      }
    }

    function getQuestionByUserId(){
      QuestionsService.getQuestionByUserId(userId, 10, 0, 'newest').then(function(data){
          if (data.data.status) {
              var result = data.data.request_data_result;
              if(isInit) {
                  listPosted = [];
              }
              var count = {
                  "count_question": result.question.length,
                  "count_answer": result.answers.length
              };
              for (var i = 0; i < result.question.length; i++) {
                  var objPosted = {};
                  var questionData = result.question[i];

                  objPosted.id = questionData.PID;
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
                  }
                  listPosted.push(objPosted);
              }
              var listQuestion = {"total_count": count, "ListQA": listPosted};
              $scope.askQuestion = listQuestion;
          }
      });
    }

    function detectMultiImage(imagePath) {
        if(isEmpty(imagePath)){
            return null;
        }
        if (imagePath != null && imagePath.indexOf(";") != -1) {
            return imagePath.split(";");
        }
        var listImage = [];
        listImage.push(imagePath)
        return listImage;
    };

   $scope.setMaster = function(section) {
        $scope.selected = section;
        
        if ($scope.selected.name == "Setting") {
          $("#setting").addClass('active');
          $("#essay").removeClass('active');
          $("#my-questions").removeClass('active');
        }
        else if ($scope.selected.name == "Essay") {
          $("#essay").addClass('active');
          $("#setting").removeClass('active');
          $("#my-questions").removeClass('active');
        }
        else{
          $("#my-questions").addClass('active');
          $("#essay").removeClass('active');
          $("#setting").removeClass('active');
        }
    }

    $scope.isSelected = function(section) {
        return $scope.selected === section;
    }

    $scope.onFileSelect = function ($files) {
        $filex = $files;
        if ($filex != null) {
            var fd = new FormData();
            if ($filex != null) {
                for (var i = 0; i < $filex.length; i++) {
                    file = $filex[i];
                    fd.append('uploadfile', file);
                }
            }
            fd.append('userid', userId);
            var url = NEW_SERVICE_URL + 'user/uploadFile';
            $http({
                method: 'POST',
                url: url,
                headers: {
                    'Content-Type': undefined
                },
                data: fd,
                transformRequest: function (data, headersGetterFunction) {
                    return data;
                }

            }).success(function (data) {
                var local = data.request_data_result;
                window.location.href = '#/studentProfile';       
                window.location.reload();
            })
            .error(function (data, status) {
                $('.waitingUp').addClass('hide');
                $scope.errorUpload = data.request_data_result;
                return;
            });
        }
    };

    $scope.changePassword = function(){
      var check = true;
      var error = '';
      if($('#oldpass').val() == ""){
        checked = false;
        error += "Password is required. \n";
      }
      if($('#newpass').val() == ""){
        checked = false;
        error += "New password is required. \n";
      }
      if ($('#cnewpass').val() != $('#newpass').val()) {
        checked = false;
        error += "Password is not matched. \n";
      }
      if (error.length > 0) {
        alert(error);
      }
      else{
        var user = {
          'username':userName,
          'password':$('#oldpass').val(),
          'newpassword':$('#newpass').val()
        }
        console.log(user);
        StudentService.changePassword(user).then(function(data){
          console.log(data.data.request_data_result);
          if (data.data.request_data_result == "Success") {
            alert("Change password successful.");
            $('#oldpass').val('');
            $('#newpass').val('');
            $('#cnewpass').val('');
          }
        });
      }
    };

    $scope.update = function(){
      var check = true;
      var error = '';

      var favorite = "";
      if ($('#music').is(':checked')) {
        favorite += $('#music').val();
      }
      if ($('#art').is(':checked')) {
        if (favorite.length > 0)
          favorite += ',' + $('#art').val();
        else
          favorite += $('#art').val();
      }
      if ($('#sport').is(':checked')) {
        if (favorite.length > 0)
          favorite += ',' + $('#sport').val();
        else
          favorite += $('#sport').val();
      }

      var gender = '';
      if ($('#male').is(':checked')) {
        gender = "Male";
      }
      else if ($('#female').is(':checked')) {
        gender = "Female";
      }
      else{
        gender = "Other";
      }


      if ($('#email').val()) {
        if (!isValidEmailAddress($('#email').val())) {
          check = false;
          error += "Email is not valid";
        }
      }      

      if (check) {        
        var student = {
          'userid': userId,
          'firstName': $('#firstName').val(),
          'lastName': $('#lastName').val(),
          'email': $('#email').val(),
          'gender':gender,
          'school': $('#school').val(),
          'bio': $('#bio').val(),
          'description':$('#about').val(),
          'favorite':favorite
        };
        console.log(student);
        StudentService.updateStudentProfile(student).then(function(data){
          if (data.data.request_data_result == "Success") {
            window.location.href = '#/studentProfile';       
            window.location.reload();
          }
          else{
            alert("Something went wrong. Please try again later.");
          }
        });
      }
      else{
        console.log(error);
      }
    }

    function isValidEmailAddress(emailAddress) {
      var pattern = /^([a-z\d!#$%&'*+\-\/=?^_`{|}~\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF]+(\.[a-z\d!#$%&'*+\-\/=?^_`{|}~\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF]+)*|"((([ \t]*\r\n)?[ \t]+)?([\x01-\x08\x0b\x0c\x0e-\x1f\x7f\x21\x23-\x5b\x5d-\x7e\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF]|\\[\x01-\x09\x0b\x0c\x0d-\x7f\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF]))*(([ \t]*\r\n)?[ \t]+)?")@(([a-z\d\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF]|[a-z\d\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF][a-z\d\-._~\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF]*[a-z\d\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])\.)+([a-z\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF]|[a-z\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF][a-z\d\-._~\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF]*[a-z\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])\.?$/i;
      return pattern.test(emailAddress);
    };
}]);
