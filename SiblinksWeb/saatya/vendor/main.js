jQuery(document).ready(function($) {

    //*******Home page
    $(".item-img").mouseenter(function(){
        $(this).find(".item-hover").show(); //show click button when hover tutorial
    });
    $(".item-img").mouseleave(function(){
        $(this).find(".item-hover").hide(); //hide click button when hover tutorial
    });
    // $(".add-img").click(function(){
    //     $(".media-manager").css({"left":0}); //show manager upload when click on add more image
    // })

    $(".navbar-toggle").click(function(){
        $(".mobimenu").toggle(); //show menu on mobile
    })

    $(".media-uploaded li img").click(function(){
        $(this).next(".choose-uploaded").addClass("active"); //Click on image uploaded then add class active to sign choosed img  
    })
    $(".choose-uploaded").click(function(){
        $(this).removeClass("active"); //Click again on image uploaded then remove class active
    })

    //*** Ask a question  
    $(".new-ask").click(function(){
        $(".form-ask-question").css({"left":0}); //Click on new ask button (in ask a question page) then show form to add new question
    })

    $(".item-hover").click(function(){ //click on each images will show slide of all image
        $(".popup-images").css({"left":0});
    })

    $(".close-popup-images").click(function(){ //click on outside of wrapper slide then close popup images
        $(".popup-images, .form-ask-question").css({"left":"100%"});
    })

    $(".total-question i").click(function(){ //show sort question in top of ask a question
        $(".sort-answer").toggle();
    })
    $(".sort-answer li").click(function(){ //hide sort question in top of ask a question
        $(".sort-answer").hide();
    })

    $(".profile-user").click(function(){ //Show profile of user when click on arrow button on header
        $(".user-info").toggle();
    })

    $(".video-thumnail").mouseenter(function(){
        $(this).find(".hover-video").show(); //show click button video when hover tutorial
    });
    $(".video-thumnail").mouseleave(function(){
        $(this).find(".hover-video").hide(); //hide click button video when hover tutorial
    });

    $(".mentors-infor-img-wrapper, .feature-thumnail").mouseenter(function(){
        $(this).find(".mentors-img-hover, .hover-video").show(); //show img mentor then show to icon to go to mentors details
    });
    $(".mentors-infor-img-wrapper, .feature-thumnail").mouseleave(function(){
        $(this).find(".mentors-img-hover, .hover-video").hide(); //show img mentor then show to icon to go to mentors details
    });
    $(".edit-details-question").click(function(){
        $(".edit-question").toggle();
    })
    $(".sort").click(function(){
        $(".sort-answer").toggle();
    })

    $(window).on("load",function(){
      $(".series-video-list").mCustomScrollbar({
          theme:"light"
      });
    })

    //Bxslider - Slider images of ask a question page

    var $j = jQuery.noConflict();

    var realSlider= $j("ul#bxslider").bxSlider({
        speed:1000,
        pager:false,
        nextText:'',
        prevText:'',
        infiniteLoop:false,
        hideControlOnEnd:true,
        onSlideBefore:function($slideElement, oldIndex, newIndex){
            changeRealThumb(realThumbSlider,newIndex);

        }

    });

    var realThumbSlider=$j("ul#bxslider-pager").bxSlider({
        minSlides: 5,
        maxSlides: 5,
        slideWidth: 100,
        slideMargin: 15,
        moveSlides: 1,
        pager:false,
        speed:1000,
        infiniteLoop:false,
        hideControlOnEnd:true,
        nextText:'<span>></span>',
        prevText:'<span><</span>',
        onSlideBefore:function($slideElement, oldIndex, newIndex){
            /*$j("#sliderThumbReal ul .active").removeClass("active");
             $slideElement.addClass("active"); */

        }
    });
    // $(".popup-images").reloadSlider();
    linkRealSliders(realSlider,realThumbSlider);

    if($j("#bxslider-pager li").length<5){
        $j("#bxslider-pager .bx-next").hide();
    }


    // sincronizza sliders realizzazioni
    function linkRealSliders(bigS,thumbS){

        $j("ul#bxslider-pager").on("click","a",function(event){
            event.preventDefault();
            var newIndex=$j(this).parent().attr("data-slideIndex");
            bigS.goToSlide(newIndex);
        });
    }

    //slider!=$thumbSlider. slider is the realslider
    function changeRealThumb(slider,newIndex){

        var $thumbS=$j("#bxslider-pager");
        $thumbS.find('.active').removeClass("active");
        $thumbS.find('li[data-slideIndex="'+newIndex+'"]').addClass("active");

        if(slider.getSlideCount()-newIndex>=4)slider.goToSlide(newIndex);
        else slider.goToSlide(slider.getSlideCount()-4);

    }
    //End Bxslider - Slider images of ask a question page



    // Get images has choosen
    function handleFileSelect(evt) {
        var files = evt.target.files; // FileList object

        // Loop through the FileList and render image files as thumbnails.
        for (var i = 0, f; f = files[i]; i++) {

            // Only process image files.
            if (!f.type.match('image.*')) {
                continue;
            }

            var reader = new FileReader();

            // Closure to capture the file information.
            reader.onload = (function(theFile) {
                return function(e) {
                    // Render thumbnail.
                    var div = document.createElement('div');
                    div.innerHTML = ['<img class="thumb" src="', e.target.result,
                        '" title="', escape(theFile.name), '"/><div class="remove-image"><img src="../images/remove-img.png"></div>'].join('');
                    document.getElementById('list').insertBefore(div, null);
                    $(".media-manager").hide();

                };
            })(f);


            // Read in the image file as a data URL.
            reader.readAsDataURL(f);
        }

    }
    //document.getElementById('files').addEventListener('change', handleFileSelect, false);
})