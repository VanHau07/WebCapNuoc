$(function(){
	setEventFileUpload();
	
	$("#fAssetCRUD").find('button[data-action]').click(function (event) {
		event.preventDefault();/*event.stopPropagation();*/
		
		var $obj = $(this);
		var action = $obj.data('action');
		
		switch (action) {
		case 'accept':
			var formData = new FormData();
			formData.append('_id', $('#fAssetCRUD').find('input[name="_id"]').val());
			formData.append('title', $('#fAssetCRUD').find('#title').val());
			formData.append('content', encodeURIComponent(tinyMCE.editors["content"].getContent()));
			formData.append('loai-ts', $('#fAssetCRUD').find('#loai-ts').val());
			formData.append('loai-ts-text', $('#fAssetCRUD').find('#loai-ts').find('option:selected').text());		
			formData.append('summaryContent', $('#fAssetCRUD').find('#summaryContent').val());
			formData.append('user_asset', $('#fAssetCRUD').find('#user_asset').val());
			formData.append('price', $('#fAssetCRUD').find('#price').val());
			formData.append('hthuc', $('#fAssetCRUD').find('#hthuc').val());
			formData.append('attachFileName', $('#fAssetCRUD').find('input[name="attachFileName"]').val());
			formData.append('attachFileNameSystem', $('#fAssetCRUD').find('input[name="attachFileNameSystem"]').val());
			formData.append('status', $('#fAssetCRUD').find('#status').val());
			formData.append('status-text', $('#fAssetCRUD').find('#status').find('option:selected').text());	
			formData.append('deposit', $('#fAssetCRUD').find('#deposit').val());
			formData.append('tg_xem_tn', $('#fAssetCRUD').find('#tg_xem_tn').val());
			formData.append('tg_xem_dn', $('#fAssetCRUD').find('#tg_xem_dn').val());
			formData.append('tg_ban_tn', $('#fAssetCRUD').find('#tg_ban_tn').val());
			formData.append('tg_ban_dn', $('#fAssetCRUD').find('#tg_ban_dn').val());
			formData.append('tg_nop_tn', $('#fAssetCRUD').find('#tg_nop_tn').val());
			formData.append('tg_nop_dn', $('#fAssetCRUD').find('#tg_nop_dn').val());
			formData.append('pthuc', $('#fAssetCRUD').find('#pthuc').val());
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
			    			            			disabledAllControlsInForm('fAssetCRUD');
			    			            			$obj.prop('disabled', true);
			    			            			alertDLSuccess(jQuery.type(res.responseData) == 'object'? res.responseData.info: createObjectError(res).html(), function(){
			    			            			});
			    			            				$('#fAssetCRUD').find('button[data-action="back"]').trigger('click');
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
					if($('#fAsset').find('#grid').length > 0){
						try{
							$('#fAsset').find('#grid').data("kendoGrid").dataSource.read();
						}catch(err){}
					}						
					break;

		default:
			break;
		}
		
	});
	
});

function setEventFileUpload(){	
	$("#fAssetCRUD").find('input[type="file"][name="attachFile"]').fileupload({
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