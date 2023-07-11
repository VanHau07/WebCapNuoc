$(document).ready(function () {
  // $('.navbar-nav li > a[href$="' + location.pathname + '"]').addClass('active')
  $('.navbar-nav>li:has(ul)').addClass('dropdown')

  $('a.dropdown-toggle').on('click', function (e) {
    if (!$(this).next().hasClass('show')) {
      $(this)
        .parents('.dropdown-menu')
        .first()
        .find('.show')
        .removeClass('show')
    }

    var $subMenu = $(this).next('.dropdown-menu')

    $subMenu.toggleClass('show')

    $(this)
      .parents('li.nav-item.dropdown.show')
      .on('hidden.bs.dropdown', function (e) {
        $('.dropdown-submenu .show').removeClass('show')
      })

    return false
  })

 $('.pl-main__owl .owl-carousel').owlCarousel({
    loop: true,
    margin: 10,
    nav: true,
    dots: false,
    responsive: {
      0: {
        items: 1,
      },
      600: {
        items: 1,
      },
      1000: {
        items: 1,
      },
    },
  })

  $('#contact-form').on('submit', function (event) {
    event.preventDefault()
    var formData = new FormData(this)
    $.ajax({
      url: '',
      method: 'POST',
      data: formData,
      contentType: false,
      cache: false,
      processData: false,
      success: function (data) {
        alert('Cảm ơn bạn đã liên hệ với chúng tôi.')
        window.location.reload()
      },
    })
  })

  // $('.data-id').click(function(){
  //    product_type=$(this).attr('product_type');
  //    $.ajax({
  //             url:"../includes/get-product.php",
  //             type:"post",
  //             data:"product_type="+product_type,
  //             async:true,
  //             success:function(kq){
  //               console.log(kq);
  //               $("#list-product").html(kq);
  //                 }
  //             });

  // });

  // $('.year-data').click(function(){
  //    year=$(this).attr('year-id');
  //    $.ajax({
  //             url:"../includes/get-year.php",
  //             type:"post",
  //             data:"year="+year,
  //             async:true,
  //             success:function(kq){
  //               $(".product-ex-2").html(kq);
  //                 }
  //             });

  // });

  $('.menu div:first-child').addClass('active')
  $('.nacc li:first-child').addClass('active')

  $('.url-link').click(function () {
    $('.url-link').removeClass('selected-color')
    $(this).addClass('selected-color')
  })

  $('#inline-popups li a').click(function () {
    id = $(this).attr('data-id')

    $.ajax({
      url: 'includes/get-question.php',

      type: 'post',

      data: 'id=' + id,

      async: true,

      success: function (kq) {
        $('#test-popup').html(kq)
      },
    })
  })

  $('.box-recruitment').click(function () {
    recid = $(this).attr('data-id')

    $.ajax({
      url: '../includes/get-recruitment.php',

      type: 'post',

      data: 'recid=' + recid,

      async: true,

      success: function (kq) {
        console.log(recid)
        $('#form-cv').html(kq)
      },
    })
  })

  $('#mail-chimp').click(function () {
    if ($('#mail-list').val() == '') {
      alert('Vui lòng nhập đầy đủ thông tin!')
    } else {
      mail = $('#mail-list').val()

      $.ajax({
        url: 'includes/mail-chimp.php',

        type: 'post',

        data: 'mail=' + mail,

        async: true,

        success: function (kq) {
          alert('Đăng ký thành công!')

          $('#mail-list').val('')
        },
      })
    }
  })

  $('.hspl_mobile .owl-carousel').owlCarousel({
    loop: true,
    margin: 10,
    nav: true,
    dots: false,
    responsive: {
      0: {
        items: 1,
      },
      600: {
        items: 3,
      },
      1000: {
        items: 5,
      },
    },
  })

  $('#send-signup').click(function () {
    if (
      $('#fullname').val() == '' ||
      $('#number').val() == '' ||
      $('#field').val() == '0'
    ) {
      alert('Vui lòng nhập đầy đủ thông tin')
    } else {
      var action = 'fetch_data'

      var fullname = $('#fullname').val()

      var number = $('#number').val()

      var field = $('#field').val()

      $.ajax({
        url: 'includes/import-mess.php',

        method: 'POST',

        data: {
          action: action,
          fullname: fullname,
          number: number,
          field: field,
        },

        success: function (data) {
          alert('Gửi thông tin thành công! Chúng tôi sẽ sớm phản hồi lại.')

          $('#fullname').val('')

          $('#number').val('')

          $('#field').val('')

          location.reload()
        },
      })
    }
  })

  if ($(window).width() > 900) {
    $('.mainmenu li a').removeAttr('data-toggle', 'dropdown')
  } else {
    // change functionality for larger screens
  }

  $('#company .owl-carousel').owlCarousel({
    interval: 3000,
    loop: true,
	mouseDrag: false,
    margin: 10,
    autoWidth:true,
    dots: false,
    nav: true,
    autoplay: true,

    responsive: {
      0: {
        items: 1,
      },

      600: {
        items: 1,
      },

      1000: {
        items: 4,
      },
    },
  })

  $('.product-loop .owl-carousel').owlCarousel({
    loop: false,

    margin: 10,

    dots: false,

    nav: true,

    autoplay: true,

    responsive: {
      0: {
        items: 1,
      },

      600: {
        items: 1,
      },

      1000: {
        items: 4,
      },
    },
  })

  $('#categories-slide .owl-carousel').owlCarousel({
    loop: true,

    dots: false,

    nav: false,

    autoplay: true,

    responsive: {
      0: {
        items: 1,
      },

      600: {
        items: 1,
      },

      1000: {
        items: 1,
      },
    },
  })

  $('#page-personal .owl-carousel').owlCarousel({
    interval: 3000,

    loop: true,

    margin: 10,

    dots: false,

    nav: true,

    autoplay: true,

    responsive: {
      0: {
        items: 1,
      },

      600: {
        items: 1,
      },

      1000: {
        items: 5,
      },
    },
  })

  $('#thinhtrigroup .owl-carousel').owlCarousel({
    loop: true,
    margin: 10,
    nav: true,
    dots: false,
    responsive: {
      0: {
        items: 1,
      },
      600: {
        items: 2,
      },
      1000: {
        items: 2,
      },
    },
  })

  $('.slider-for').slick({
    slidesToShow: 1,
    slidesToScroll: 1,
    arrows: false,
    fade: true,
    asNavFor: '.slider-nav',
  })
  $('.slider-nav').slick({
    slidesToShow: 4,
    slidesToScroll: 1,
    asNavFor: '.slider-for',
    dots: true,
    focusOnSelect: true,
  })

  $('a[data-slide]').click(function (e) {
    e.preventDefault()
    var slideno = $(this).data('slide')
    $('.slider-nav').slick('slickGoTo', slideno - 1)
  })

  $('.btn-schedule').click(function () {
    $('html, body').animate(
      {
        scrollTop: $('section.schedule').offset().top,
      },
      2000
    )
  })

  $('#view-pdf .owl-carousel').owlCarousel({
    loop: true,
    nav: true,
    dots: false,
    responsive: {
      0: {
        items: 1,
      },
      600: {
        items: 1,
      },
      1000: {
        items: 1,
      },
    },
  })

  $(window).scroll(function () {
    if ($(this).scrollTop() > 100) {
      $('#scroll').fadeIn()
    } else {
      $('#scroll').fadeOut()
    }
  })

  $('#scroll').click(function () {
    $('html, body').animate({ scrollTop: 0 }, 1000)

    return false
  })

  // Inline popups

  $('#inline-popups').magnificPopup({
    delegate: 'a',

    removalDelay: 500, //delay removal by X to allow out-animation

    callbacks: {
      beforeOpen: function () {
        this.st.mainClass = this.st.el.attr('data-effect')
      },
    },

    midClick: true, // allow opening popup on middle mouse click. Always set it to true if you don't provide alternative source.
  })

  // Hinge effect popup

  $('a.hinge').magnificPopup({
    mainClass: 'mfp-with-fade',

    removalDelay: 1000, //delay removal by X to allow out-animation

    callbacks: {
      beforeClose: function () {
        this.content.addClass('hinge')
      },

      close: function () {
        this.content.removeClass('hinge')
      },
    },

    midClick: true,
  })

  $('#tile-1 ul li .nav-link:eq(0)').addClass('active')

  $('.zoom-gallery').magnificPopup({
    delegate: 'a',

    type: 'image',

    closeOnContentClick: false,

    closeBtnInside: false,

    mainClass: 'mfp-with-zoom mfp-img-mobile',

    image: {
      verticalFit: true,

      titleSrc: function (item) {
        return (
          item.el.attr('title') +
          ' &middot; <a class="image-source-link" href="' +
          item.el.attr('data-source') +
          '" target="_blank">Xem chi tiết</a>'
        )
      },
    },

    gallery: {
      enabled: true,
    },

    zoom: {
      enabled: true,

      duration: 300, // don't foget to change the duration also in CSS

      opener: function (element) {
        return element.find('img')
      },
    },
  })

  $('.count').each(function () {
    $(this)
      .prop('Counter', 0)
      .animate(
        {
          Counter: $(this).text(),
        },
        {
          duration: 2500,
          easing: 'swing',
          step: function (now) {
            $(this).text(Math.ceil(now))
          },
        }
      )
  })

  // Acc
  $(document).on('click', '.naccs .menu div', function () {
    var numberIndex = $(this).index()

    if (!$(this).is('active')) {
      $('.naccs .menu div').removeClass('active')
      $('.naccs ul li').removeClass('active')

      $(this).addClass('active')
      $('.naccs ul')
        .find('li:eq(' + numberIndex + ')')
        .addClass('active')

      var listItemHeight = $('.naccs ul')
        .find('li:eq(' + numberIndex + ')')
        .innerHeight()
      $('.naccs ul').height(listItemHeight + 'px')
    }
  })

  var $slider = $('.slider'),
    $slideBGs = $('.slide__bg'),
    diff = 0,
    curSlide = 0,
    numOfSlides = $('.slide').length - 1,
    animating = false,
    animTime = 500,
    autoSlideTimeout,
    autoSlideDelay = 6000,
    $pagination = $('.slider-pagi')

  function createBullets() {
    for (var i = 0; i < numOfSlides + 1; i++) {
      var $li = $("<li class='slider-pagi__elem'></li>")
      $li.addClass('slider-pagi__elem-' + i).data('page', i)
      if (!i) $li.addClass('active')
      $pagination.append($li)
    }
  }

  createBullets()

  function manageControls() {
    $('.slider-control').removeClass('inactive')
    if (!curSlide) $('.slider-control.left').addClass('inactive')
    if (curSlide === numOfSlides)
      $('.slider-control.right').addClass('inactive')
  }

  function autoSlide() {
    autoSlideTimeout = setTimeout(function () {
      curSlide++
      if (curSlide > numOfSlides) curSlide = 0
      changeSlides()
    }, autoSlideDelay)
  }

  autoSlide()

  function changeSlides(instant) {
    if (!instant) {
      animating = true
      manageControls()
      $slider.addClass('animating')
      $slider.css('top')
      $('.slide').removeClass('active')
      $('.slide-' + curSlide).addClass('active')
      setTimeout(function () {
        $slider.removeClass('animating')
        animating = false
      }, animTime)
    }
    window.clearTimeout(autoSlideTimeout)
    $('.slider-pagi__elem').removeClass('active')
    $('.slider-pagi__elem-' + curSlide).addClass('active')
    $slider.css('transform', 'translate3d(' + -curSlide * 100 + '%,0,0)')
    $slideBGs.css('transform', 'translate3d(' + curSlide * 50 + '%,0,0)')
    diff = 0
    autoSlide()
  }

  function navigateLeft() {
    if (animating) return
    if (curSlide > 0) curSlide--
    changeSlides()
  }

  function navigateRight() {
    if (animating) return
    if (curSlide < numOfSlides) curSlide++
    changeSlides()
  }

  $(document).on('mousedown touchstart', '.slider', function (e) {
    if (animating) return
    window.clearTimeout(autoSlideTimeout)
    var startX = e.pageX || e.originalEvent.touches[0].pageX,
      winW = $(window).width()
    diff = 0

    $(document).on('mousemove touchmove', function (e) {
      var x = e.pageX || e.originalEvent.touches[0].pageX
      diff = ((startX - x) / winW) * 70
      if ((!curSlide && diff < 0) || (curSlide === numOfSlides && diff > 0))
        diff /= 2
      $slider.css(
        'transform',
        'translate3d(' + (-curSlide * 100 - diff) + '%,0,0)'
      )
      $slideBGs.css(
        'transform',
        'translate3d(' + (curSlide * 50 + diff / 2) + '%,0,0)'
      )
    })
  })

  $(document).on('mouseup touchend', function (e) {
    $(document).off('mousemove touchmove')
    if (animating) return
    if (!diff) {
      changeSlides(true)
      return
    }
    if (diff > -8 && diff < 8) {
      changeSlides()
      return
    }
    if (diff <= -8) {
      navigateLeft()
    }
    if (diff >= 8) {
      navigateRight()
    }
  })

  $(document).on('click', '.slider-control', function () {
    if ($(this).hasClass('left')) {
      navigateLeft()
    } else {
      navigateRight()
    }
  })

  $(document).on('click', '.slider-pagi__elem', function () {
    curSlide = $(this).data('page')
    changeSlides()
  })
})

var a,
  b,
  c,
  submitContent,
  captcha,
  locked,
  validSubmit = false,
  timeoutHandle

// Generating a simple sum (a + b) to make with a result (c)

function generateCaptcha() {
  a = Math.ceil(Math.random() * 10)

  b = Math.ceil(Math.random() * 10)

  c = a + b

  submitContent =
    '<span>' +
    a +
    '</span> + <span>' +
    b +
    '</span>' +
    ' = <input class="submit__input" type="text" maxlength="2" size="2" required />'

  $('.submit__generated').html(submitContent)

  init()
}

// Check the value 'c' and the input value.

// init the action handlers - mostly useful when 'c' is refreshed

function init() {
  $('form').on('submit', function (e) {
    e.preventDefault()

    if ($('.submit__generated').hasClass('valid')) {
      // var formValues = [];

      captcha = $('.submit__input').val()

      if (captcha !== '') {
        captcha = Number(captcha)
      }

      checkCaptcha()

      if (validSubmit === true) {
        validSubmit = false

        var name = document.getElementById('file').files[0].name

        names = $('#recipient-name').val()

        email = $('#recipient-email').val()

        phone = $('#recipient-phone').val()

        recruiment = $('#recipient-id').val()

        var form_data = new FormData()

        var ext = name.split('.').pop().toLowerCase()

        if (
          jQuery.inArray(ext, [
            'xls',
            'xlsx',
            'doc',
            'docx',
            'pdf',
            'jpg',
            'png',
          ]) == -1
        ) {
          alert('File hồ sơ không hợp lệ')
        }

        var oFReader = new FileReader()

        oFReader.readAsDataURL(document.getElementById('file').files[0])

        var f = document.getElementById('file').files[0]

        var fsize = f.size || f.fileSize

        if (fsize > 4000000) {
          alert('Dung lượng file quá lớn.')
        } else {
          //alert(recruiment);

          form_data.append('file', document.getElementById('file').files[0])

          form_data.append('names', names)

          form_data.append('email', email)

          form_data.append('phone', phone)

          form_data.append('recruiment', recruiment)

          $.ajax({
            url: '../includes/cv.php',

            method: 'POST',

            data: form_data,

            contentType: false,

            cache: false,

            processData: false,

            success: function (data) {
              console.log(data)

              $('#recipient-name').val('')

              $('#recipient-email').val('')

              $('#recipient-phone').val('')

              $('#file').val('')

              alert(
                'Gửi hồ sơ thành công. Chúng tôi sẽ liên hệ lại trong thời gian sớm nhất!'
              )

              $('.modal-backdrop').css({ display: 'none' })

              $('body').removeClass('modal-open')

              $('#recruitmentleModal').css({ display: 'none' })
            },
          })
        }
      }
    } else {
      return false
    }
  })

  // Captcha input result handler

  $('.submit__input').on(
    'propertychange change keyup input paste',
    function () {
      // Prevent the execution on the first number of the string if it's a 'multiple number string'

      // (i.e: execution on the '1' of '12')

      window.clearTimeout(timeoutHandle)

      timeoutHandle = window.setTimeout(function () {
        captcha = $('.submit__input').val()

        if (captcha !== '') {
          captcha = Number(captcha)
        }

        checkCaptcha()
      }, 150)
    }
  )

  // Add the ':active' state CSS when 'enter' is pressed

  $('body')
    .on('keydown', function (e) {
      if (e.which === 13) {
        if ($('.submit-form').hasClass('overlay')) {
          checkCaptcha()
        } else {
          $('.submit-form').addClass('enter-press')
        }
      }
    })

    .on('keyup', function (e) {
      if (e.which === 13) {
        $('.submit-form').removeClass('enter-press')
      }
    })

  // Refresh button click - Reset the captcha

  $('.submit-control i.fa-refresh').on('click', function () {
    if (!locked) {
      locked = true

      setTimeout(unlock, 500)

      generateCaptcha()

      setTimeout(checkCaptcha, 0)
    }
  })

  // Submit white overlay click

  $('.submit-form-overlay').on('click', function () {
    checkCaptcha()
  })
}

generateCaptcha()

/**

COPY HERE

*/
;(function ($) {
  /** change value here to adjust parallax level */

  var parallax = -0.5

  var $bg_images = $('.wp-block-cover-image')

  var offset_tops = []

  $bg_images.each(function (i, el) {
    offset_tops.push($(el).offset().top)
  })

  $(window).scroll(function () {
    var dy = $(this).scrollTop()

    $bg_images.each(function (i, el) {
      var ot = offset_tops[i]

      $(el).css('background-position', '50% ' + (dy - ot) * parallax + 'px')
    })
  })
})(jQuery)
