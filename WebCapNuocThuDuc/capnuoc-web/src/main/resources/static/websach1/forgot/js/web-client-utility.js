var ROOT_PATH = '';
var _csrf_value = '';var _csrf_name = '';var tokenTransaction = '';
var callback = null;

var isRedirectToLogin = false;
var timer = null;
var transactionMain = '';var transactionSub = '';
var methodSub = '';

var KENDOUI_PAGESIZE = 10;var KENDOUI_PAGESIZES = [5, 10, 20, 50, 100];var KENDOUI_BUTTONCOUNT = 5;var kendoGridHeight = 350;
var kendoGridHeightSmall = 250;
var KENDOUI_PAGESIZE_NO_SCROLL_Y = 5;
var kendoGridMessages = {
//		itemsPerPage: 'dòng trên trang',
		itemsPerPage: '',
		previous: 'Trang trước',
		next: 'Trang sau',
		refresh: 'Tải lại trang',
		last: 'Trang cuối',
		first: 'Trang đầu'
//		, empty: 'Không có dữ liệu'
		, empty: ''
//		, display: '<span class="text-lowercase"> {0} - {1} của tổng số {2} dòng</span>'
		, display: '<span class="text-lowercase">{0}-{1}/{2}</span>'
}

function initSetCSRF(xhrObj){xhrObj.setRequestHeader('X-CSRF-TOKEN', _csrf_value)};

function refreshCaptcha(objImg){$.ajax({type: "POST",datatype: "plain/text",url: ROOT_PATH + "/common/generatingCaptcha",cache: false,beforeSend: function(req) {initSetCSRF(req);},success:function(res) {objImg.attr('src', "data:image/jpg;base64, " + res + "");}});}

var HEADER_RESULT_TYPE_NAME = "RESULT_TYPE";
var HEADER_RESULT_TYPE_JSON = "JSON";
var HEADER_RESULT_TYPE_JSON_ARRAY = "JSON_ARRAY";
var HEADER_RESULT_TYPE_JSON_GRID = "JSON_GRID";
var HEADER_RESULT_TYPE_NORMAL = "PAGE";
var HEADER_RESULT_TYPE_PAGE_POPUP = "PAGE-POPUP";
var HEADER_RESULT_TYPE_PAGE_AREA = "PAGE-AREA";

function initSetCSRF(xhrObj){xhrObj.setRequestHeader('X-CSRF-TOKEN', _csrf_value)};
function initAjaxJsonRequest(xhrObj) {initSetCSRF(xhrObj); xhrObj.setRequestHeader(HEADER_RESULT_TYPE_NAME, HEADER_RESULT_TYPE_JSON);}
function initAjaxJsonArrayRequest(xhrObj) {initSetCSRF(xhrObj); xhrObj.setRequestHeader(HEADER_RESULT_TYPE_NAME, HEADER_RESULT_TYPE_JSON_ARRAY);}
function initAjaxJsonGridRequest(xhrObj) {initSetCSRF(xhrObj); xhrObj.setRequestHeader(HEADER_RESULT_TYPE_NAME, HEADER_RESULT_TYPE_JSON_GRID);}
function initNormalRequest(xhrObj) {initSetCSRF(xhrObj); xhrObj.setRequestHeader(HEADER_RESULT_TYPE_NAME, HEADER_RESULT_TYPE_NORMAL);}
function initPagePopupRequest(xhrObj) {initSetCSRF(xhrObj); xhrObj.setRequestHeader(HEADER_RESULT_TYPE_NAME, HEADER_RESULT_TYPE_PAGE_POPUP);}
function initPageNormalArea(xhrObj) {initSetCSRF(xhrObj); xhrObj.setRequestHeader(HEADER_RESULT_TYPE_NAME, HEADER_RESULT_TYPE_PAGE_AREA);}

function showLoading() {$('.preloader').fadeIn();}
function hideLoading(){$('.preloader').fadeOut();}
$.fn.serializeIncludeDisabled = function () {
    var disabled = this.find(":input:disabled").removeAttr("disabled");
    var serialized = this.serialize();
    disabled.attr("disabled", "disabled");
    return serialized;
};
$.fn.serializeObject = function() {
	var disabled = this.find(":input:disabled").removeAttr("disabled");
	
    var o = {};
    var a = this.serializeArray();
    $.each(a, function() {
        if (o[this.name]) {
            if (!o[this.name].push) {
                o[this.name] = [o[this.name]];
            }
            o[this.name].push(this.value || '');
        } else {
            o[this.name] = this.value || '';
        }
    });
    disabled.attr("disabled", "disabled");
    return o;
};

String.prototype.trim = function() {
	return this.replace(/^\s+|\s+$/g,"");
}

alertify.set('notifier','position', 'top-right');
alertify.defaults.closable = false;
alertify.defaults.transition = "fade";
alertify.defaults.theme.ok = "btn btn-sm btn-primary btn-ses";
alertify.defaults.theme.cancel = "btn btn-sm btn-danger";

function alertDLSuccess(msg, callback){
	if(isRedirectToLogin){
		isRedirectToLogin = false;
		callback = function(){
			window.location.href = ROOT_PATH;	
		}
	}
	
	alertify.alert(
		'<b class="white text-uppercase fw-800">Thông báo </b>'
		, msg
		, undefined == callback? function(e){}: callback
	);
}

function alertConfirm(msg, callbackOK, callbackCancel){
	alertify.confirm('<b class="white text-uppercase fw-800">Xác nhận</b>', msg, undefined == callbackOK? function(e){}: callbackOK, undefined == callbackCancel? function(e){}: callbackCancel);
}

function notificationDLSuccess(msg, callback){
	showNotificationComplete(msg, 'success');
	if(isRedirectToLogin){
		isRedirectToLogin = false;
		setTimeout(() => {
			window.location.href = ROOT_PATH;
		}, 3000);
	}
//	if(isRedirectToLogin){
//		isRedirectToLogin = false;
//		callback = function(){
//			window.location.href = ROOT_PATH;	
//		}
//	}
//	
//	alertify.alert(
//		'<b class="white text-uppercase fw-800">Thông báo </b>'
//		, msg
//		, undefined == callback? function(e){}: callback
//	);
	
}

//jQuery plugin to prevent double click
jQuery.fn.preventDoubleClick = function() {
	$(this).on('click', function(e) {
		var $el = $(this);
		if ($el.data('clicked')) {
			// Previously clicked, stop actions
			e.preventDefault();e.stopPropagation();
		} else {
			// Mark to ignore next click
			$el.data('clicked', true);
			// Unmark after 1 second
			window.setTimeout(function() {
				$el.removeData('clicked');
			}, 1000)
		}
	});
	return this;
};

function isDoubleClicked(element) {
    //if already clicked return TRUE to indicate this click is not allowed
    if (element.data("isclicked")) return true;

    //mark as clicked for 1 second
    element.data("isclicked", true);
    setTimeout(function () {
        element.removeData("isclicked");
    }, 1000);

    //return FALSE to indicate this click was allowed
    return false;
}

function createObjectError(objJson){
	isRedirectToLogin = false;
	if(jQuery.type(objJson) == 'object'){
		if(objJson.errorCode == 401)
			isRedirectToLogin = true;
	}
	var objDiv = $('<div>');if(objJson.responseData != null)objDiv.append($('<div>').addClass('bootbox-body').html(0 != objJson.errorCode? '<span class="text-danger">' + objJson.responseData + '</span>': objJson.responseData));if(objJson.errorMessages != null && objJson.errorMessages.length > 0){objDiv.append($('<hr>').css('margin-bottom', '5px').css('margin-top', '5px'));var objDivSub = $('<div>').addClass('required');objDivSub.append('<ul class="p-l-20">');for(var i=0; i<objJson.errorMessages.length; i++){objDivSub.find('ul').eq(0).append($('<li class="small">').html(objJson.errorMessages[i]));}objDiv.append(objDivSub);}return objDiv;
}
function showPopupWithURLAndData(url, data, isSmall, callback){var idDlg = 'modelPopup'; if(isSmall) idDlg = 'modelPopupMedium'; this.callback = null; if(callback) this.callback = callback;$('#' + idDlg + ' .modal-content').empty();var dataPost = '';if(data != null){Object.keys(data).forEach(function(key, index) {dataPost += key + '=' + data[key] + '&';});}$.ajax({type: "POST",dataType: "html",url: url,data: dataPost,beforeSend: function(req) {initPagePopupRequest(req);/*showLoading();*/},success : function(data) {$('#' + idDlg + ' .modal-content').html(data);hideLoading();},error:function (xhr, ajaxOptions, thrownError){$('#' + idDlg + ' .modal-content').html('<div class="modal-header" style="background-color: #ececec;"><h4 class="modal-title text-uppercase fw-700" id="dlg_title">SYSTEM-ERROR</h4></div><div class="modal-body"><div class="row"><div class="col-sm-12"><div class="col-xs-11 margin-top-only margin-bottom-only center-block"><label class="col-form-label control-label fw-700 text-uppercase text-red-important">Lỗi hệ thống...</label></div></div></div></div><div class="modal-footer"><button type="button" id="btDlgClose" class="btn btn-danger btn-sm " data-dismiss="modal"><i class="fa fa-power-off mR-5"></i> Đóng</button></div>');hideLoading();} });$('#' + idDlg).modal("show");}
function submitFormRenderArea(url, formData, objRender, callback){
	objRender.empty();
	this.callback = null; if(callback) this.callback = callback
	$.ajax({
		url: url,
		type: 'POST',
		data: formData,
//		async: true,
		cache: false,		
//		contentType: 'application/json; charset=utf-8',
//		contentType: 'text/plain',
//		contentType: false,		
//		enctype: 'multipart/form-data',		
//		processData: false,
		dataType : 'html',
		traditional: true,
		beforeSend: function(req){
			initPageNormalArea(req);
			showLoading();
		},
		beforeSubmit: function(req){
			initPageNormalArea(req);
			showLoading();
		},
		success: function(res){
			hideLoading();			
			objRender.html(res);
		},
		error:function (xhr, ajaxOptions, thrownError){
            hideLoading();
        }
	});
}

function encodeObjJsonBase64UTF8(obj){
	return base64.encode(base64._utf8_encode(JSON.stringify(obj))).replace(/\+/g, "@");
}
function decodeBase64UTF8ToObj(strBase64){
	return JSON.parse(base64._utf8_decode(base64.decode(base64._utf8_encode(strBase64))));
}

function disabledAllControlsInForm(idForm){
	$('#' + idForm + ' input[type="text"]').attr('readonly', 'readonly');
	$('#' + idForm + ' textarea').attr('readonly', 'readonly'); 

//	$('#' + idForm + ' select').attr('disabled', 'disabled');
//	$('#' + idForm + ' .SumoSelect').addClass('disabled');

	$('#' + idForm + ' input[type="file"]').attr('disabled', 'disabled');
	$('#' + idForm + ' input[type="checkbox"]').attr('disabled', 'disabled');
	$('#' + idForm + ' input[type="radio"]').attr('disabled', 'disabled');
	
	$('#' + idForm + ' select:not(.sumo)').attr('disabled', 'disabled');
	$('#' + idForm + ' select.sumo').find('option').attr('disabled', 'disabled');
	$('#' + idForm).find('div.SumoSelect').find('div.multiple').find('ul.options li.opt').addClass('disabled');
	$('#' + idForm).find('div.SumoSelect').find('div.selall').find('p.select-all').addClass('partial');
	
	try {
		setTimeout(function(){
			$('#' + idForm + ' .date').datetimepicker('destroy');
			$('#' + idForm + ' .date').find('input').datetimepicker('destroy');
		}, 1000);
	}catch(err) {}
}

var _vArraysImage = [];
function initTinyMCEForMail(objId){
	_vArraysImage = [];
	tinymce.init({
        selector: 'textarea#' + objId,
        height: 400,
        forced_root_block: 'div',
        plugins: [
            "advlist anchor autolink codesample fullscreen image ",	/*help*/
            " lists link media noneditable preview",
            " searchreplace table template visualblocks wordcount paste  code imagetools "	/*image*/
        ],
        toolbar: "fullscreen preview | insertfile a11ycheck undo redo | fontselect fontsizeselect | bold italic | forecolor backcolor | template codesample | alignleft aligncenter alignright alignjustify | bullist numlist outdent indent | removeformat | link code image ",	/*image*/
        fontsize_formats: '8pt 9pt 10pt 11pt 12pt 14pt 16pt 18pt 20pt 22pt 24pt 26pt 36pt',
        
        statusbar: true,
        branding: false,	// To disable "Powered by TinyMCE"
        editor_selector : "mceEditor",
        language : "vi",
        mode : "textareas"
        , relative_urls: true
        , remove_script_host: false
        , convert_urls : 0
        , images_upload_handler: function (blobInfo, success, failure) {
        	var xhr, formData;

        	xhr = new XMLHttpRequest();
        	xhr.withCredentials = false;
        	xhr.open('POST', ROOT_PATH + '/admin/common/processUploadFile');
        	  
        	xhr.setRequestHeader('X-CSRF-TOKEN', _csrf_value);
        	xhr.setRequestHeader(HEADER_RESULT_TYPE_NAME, HEADER_RESULT_TYPE_JSON);
        	xhr.responseType = 'json';

        	xhr.onload = function() {
        		var json;

        		if (xhr.status === 403) {
        			$('div.tox-dialog__busy-spinner').hide();	//ẨN MÀN HÌNH WAITING
        			alertDLSuccess('HTTP Error: ' + xhr.status);
        			return;
        		}

        		if (xhr.status < 200 || xhr.status >= 300) {
        			$('div.tox-dialog__busy-spinner').hide();	//ẨN MÀN HÌNH WAITING
        			alertDLSuccess('HTTP Error: ' + xhr.status);
        	      return;
        	    }
        	    var json = xhr.response;
        	    if(typeof json === 'object'){
        	    		var obj = json.responseData;
        	    		success(window.location.origin + ROOT_PATH + '/e-images/' + obj['SystemFilename']);
        	    		
        	    		_vArraysImage.push(obj['SystemFilename']);
        	    }
        	  };

        	  xhr.onerror = function () {
        		  $('div.tox-dialog__busy-spinner').hide();	//ẨN MÀN HÌNH WAITING
        		  alertDLSuccess('HTTP Error: ' + xhr.status);
        	  };

        	  formData = new FormData();
        	  formData.append('file', blobInfo.blob(), blobInfo.filename());

        	  xhr.send(formData);
              
        },
        
        setup: function (editor) {
            editor.on('init', function (e) {
            });
        }
        
        
    });
	
	if (tinymce.editors.length > 0) {
//	    tinymce.execCommand('mceFocus', true, textArea_id );       
	    tinymce.execCommand('mceRemoveEditor',true, objId);        
	    tinymce.execCommand('mceAddEditor',true, objId);
	}
}


//https://stackoverflow.com/questions/26888276/tinymce-enable-button-while-in-read-only-mode
function initTinyMCEOnlyView(objId){
	tinymce.init({
	    selector: '#' + objId,
	    menubar: false,
	   // readonly: 1, 
	    branding: false,
	    height: 500,
	    max_height: 400,
	    //theme: 'modern',
	    toolbar: false,
	    //toolbar_items_size: 'small',
	    plugins: [' fullscreen'],		//preview
	    toolbar: ' fullscreen',		//preview
	    image_advtab: true,
	    templates: [
	    ],
	    content_css: [

	    ],
		setup: function (editor) {
			editor.on('init', function (e) {
				editor.setMode("readonly");
			});
		}
		, relative_urls: true
	    , remove_script_host: false
	});
	
	if (tinymce.editors.length > 0) {       
	    tinymce.execCommand('mceRemoveEditor',true, objId);        
	    tinymce.execCommand('mceAddEditor',true, objId);
	}
}

function initTinyMCEForMailHideMenuBar(objId){
	tinymce.init({
        selector: '#' + objId,
        height: 400,
        forced_root_block: 'div',
        plugins: [
            "advlist anchor autolink codesample fullscreen ",	/*help*/
            " lists link media noneditable preview",
            " searchreplace table template visualblocks wordcount paste  code imagetools "	/*image*/
        ],
        toolbar: "fullscreen preview | insertfile a11ycheck undo redo | fontselect fontsizeselect | bold italic | forecolor backcolor | template codesample | alignleft aligncenter alignright alignjustify | bullist numlist outdent indent | removeformat | link code",	/*image*/
        fontsize_formats: '8pt 9pt 10pt 11pt 12pt 14pt 16pt 18pt 20pt 22pt 24pt 26pt 36pt',
        menubar:false,
        statusbar: true,
        branding: false,	// To disable "Powered by TinyMCE"
        editor_selector : "mceEditor",
        language : "vi",
        mode : "textareas",
        images_upload_url: 'postAcceptor.php',
        images_upload_handler: function (blobInfo, success, failure) {
        	setTimeout(function () {
	            success('http://moxiecode.cachefly.net/tinymce/v9/images/logo.png');
        	}, 2000);
        },
        
        setup: function (editor) {
            editor.on('init', function (e) {
            });
        }
        
        
    });
	
	if (tinymce.editors.length > 0) {   
	    tinymce.execCommand('mceRemoveEditor',true, objId);        
	    tinymce.execCommand('mceAddEditor',true, objId);
	}
}