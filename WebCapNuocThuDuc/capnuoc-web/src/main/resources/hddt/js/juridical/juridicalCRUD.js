$(function(){
	setEventFileUpload();
	
	$("#fJuridicalCRUD").find('button[data-action]').click(function (event) {
		event.preventDefault();/*event.stopPropagation();*/
		
		var $obj = $(this);
		var action = $obj.data('action');
		
		switch (action) {
		case 'accept':
			var formData = new FormData();
			formData.append('_id', $('#fJuridicalCRUD').find('input[name="_id"]').val());
			formData.append('title', $('#fJuridicalCRUD').find('#title').val());
			formData.append('attachFileName', $('#fJuridicalCRUD').find('input[name="attachFileName"]').val());
			formData.append('attachFileNameSystem', $('#fJuridicalCRUD').find('input[name="attachFileNameSystem"]').val());
			
			var xhrSign = new XMLHttpRequest();
			xhrSign.upload.addEventListener('progress', function(e) {
				
			});
			if(xhrSign.upload) {
				
			};
			xhrSign.onreadystatechange = function(e) {
				if(4 == this.readyState && this.status == 200) {
					var res = xhrSign.response;
	            	if(res){
	            		if(res.errorCode == 0) {
	            			hideLoading();

	            			var responseData = xhrSign.response.responseData;
	            			var confirmText = responseData['CONFIRM'];
	    					tokenTransaction = responseData['TOKEN'];
	    					
	    					formData.append('tokenTransaction', tokenTransaction);
	    					
	    					alertConfirm(confirmText,
    							function(e){
		    						var xhr = new XMLHttpRequest();
			    					xhr.upload.addEventListener('progress', function(e) {
			    						
			    					});
			    					if(xhr.upload) {
			    						
			    					};
			    					xhr.onreadystatechange = function(e) {
			    						if(4 == this.readyState && this.status == 200) {
			    							var res = xhr.response;
			    			            	if(res){
			    			            		if(res.errorCode == 0) {
			    			            			disabledAllControlsInForm('fJuridicalCRUD');
			    			            			$obj.prop('disabled', true);
			    			            			alertDLSuccess(jQuery.type(res.responseData) == 'object'? res.responseData.info: createObjectError(res).html(), function(){
			    			            			});
			    			            				$('#fJuridicalCRUD').find('button[data-action="back"]').trigger('click');
			    			            		}else{
							            			$obj.prop('disabled', false);
							            			alertDLSuccess(createObjectError(res).html(), function(){});
							            			hideLoading();
							            		}
			    			            	}else{
							            		$obj.prop('disabled', false);
							            		alertDLSuccess('unknown error!!!', function(){});
												hideLoading();
							            	}
			    						}
			    					}
			    					xhr.onerror = function() {
			    						
			    					}
			    					xhr.ontimeout = function() {
			    						
			    					}
			    					
			    					var urlPost = ROOT_PATH + '/admin/main/' + transactionMain + '/saveData';
			    					xhr.open("POST", urlPost, true);
			    					xhr.responseType = 'json';
			    					xhr.setRequestHeader('X-CSRF-TOKEN', _csrf_value)
			    					xhr.setRequestHeader(HEADER_RESULT_TYPE_NAME, HEADER_RESULT_TYPE_JSON);
			    					
			    					xhr.setRequestHeader("Cache-Control", "no-cache");
			    					xhr.setRequestHeader("X-Requested-With", "XMLHttpRequest");
			    					xhr.send(formData);
	    						}
	    						, function(e){}
	    					);
	    					
//	            			alert(tokenTransaction);
//	            			alertDLSuccess('Ký hợp đồng thành công.', function(){
//	            				objData = {};
//	            				objData['_id'] = $('#fEOfficeEContractSign').find('input[name="_id"]').val();
//	            				submitFormRenderArea(ROOT_PATH + '/Main?transaction=EOfficeEContractSign&method=main', objData, $('#divSubContent'));
//	            			});
	            			
	            		}else{
	            			$obj.prop('disabled', false);
	            			alertDLSuccess(createObjectError(res).html(), function(){});
	            			hideLoading();
	            		}
	            	}else{
	            		$obj.prop('disabled', false);
	            		alertDLSuccess('unknown error!!!', function(){});
						hideLoading();
	            	}
				}
			}
			xhrSign.onerror = function() {
				
			}
			xhrSign.ontimeout = function() {
				
			}
			
			var urlPost = ROOT_PATH + '/admin/main/' + transactionMain + '/checkDataToSave';
			xhrSign.open("POST", urlPost, true);
			xhrSign.responseType = 'json';
			xhrSign.setRequestHeader('X-CSRF-TOKEN', _csrf_value)
			xhrSign.setRequestHeader(HEADER_RESULT_TYPE_NAME, HEADER_RESULT_TYPE_JSON);
			
			xhrSign.setRequestHeader("Cache-Control", "no-cache");
			xhrSign.setRequestHeader("X-Requested-With", "XMLHttpRequest");
			xhrSign.send(formData);
			
			break;
		case 'back':
					$('#divMainContent').show();
					$('#divSubContent').hide(function(){$(this).empty();});
					if($('#fJuridical').find('#grid').length > 0){
						try{
							$('#fJuridical').find('#grid').data("kendoGrid").dataSource.read();
						}catch(err){}
					}						
					break;

		default:
			break;
		}
		
	});
	
});

function setEventFileUpload(){	
	$("#fJuridicalCRUD").find('input[type="file"][name="attachFile"]').fileupload({
		dataType: 'json',
		url: ROOT_PATH + '/admin/common/processUploadFileTmp',
		sequentialUploads: true,
		singleFileUploads: true,
		beforeSend: function(xhr, data) {
			initAjaxJsonRequest(xhr);
			showLoading();
		},
		add: function (e, data) {
			data.submit();
			
			objTmp = this;
			$(objTmp).closest("div.row").find('input[type="text"]').val('');
		},
		progressall: function (e, data) {
		},
		done: function (e, data) {
		},
		success:function(res) {
			hideLoading();
			if(res) {
				if(res.errorCode == 0) {
//					if($.isArray(res.responseData) && res.responseData.length > 0){
//						var item = res.responseData[0];
						var item = res.responseData;
						$(objTmp).closest("div.row").find('input[type="text"][name="attachFileName"]').val(item['OriginalFilename']);
						$(objTmp).closest("div.row").find('input[type="text"][name="attachFileNameSystem"]').val(item['SystemFilename']);
//					}
				}else{
					alertDLSuccess(createObjectError(res).html(), function(){});
				}				
			}else{
				alertDLSuccess('unknown error!!!', function(){});
				hideLoading();
			}
		},
		processfail: function (e, data) {
			hideLoading();
	    },
		error: function (e, data) {
			hideLoading();
			alertDLSuccess('Lỗi trong quá trình tải tập tin...', function(){});
		}
	});
}