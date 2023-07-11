$(function(){
	_gridMain.kendoGrid({
		dataSource: new kendo.data.DataSource({
			transport: {
				read: {
					type: 'POST',
                    url: ROOT_PATH + '/admin/main/footer/search',
                    dataType: 'json',
                    data: function(){return getDataSearch();},
                    beforeSend: function(req){
                    	initAjaxJsonGridRequest(req);
                	},
				}
			},
			requestEnd: function (e) {
               	if (e.type === "read" && e.response) {
               		if(e.response.errorCode == 0){
               		}else{
//           			showNotificationComplete(createObjectError(e.response).html(), 'warning');
//           			alertDLSuccess(createObjectError(e.response).html(), function(){});
               			notificationDLSuccess(createObjectError(e.response).html(), function(){});
               		}
               	}
           	},
            schema: {
				data: "rows",
                total: "total",
                model: {
					fields: {
					}
				}
			},
//			pageSize: KENDOUI_PAGESIZE_NO_SCROLL_Y,
			pageSize: 10,
			serverPaging: true,
			serverSorting: true,
           	serverFiltering: true,
           	change: function(e) {
            },
		}),
		selectable: true, scrollable: true, 
 		sortable: {mode: "single", allowUnsort: true},
		sortable: true,
// 		filterable: { mode: "row"},
		filterable: false, resizable: true,
		serverSorting: false,
//		height: kendoGridHeight,
		pageable: {
			refresh: true,
			pageSizes: true,
			buttonCount: KENDOUI_BUTTONCOUNT,
			messages: {
				itemsPerPage: kendoGridMessages.itemsPerPage,
				previous: kendoGridMessages.previous,
				next: kendoGridMessages.next,
				refresh: kendoGridMessages.refresh,
				last: kendoGridMessages.last,
				first: kendoGridMessages.first,
				empty: kendoGridMessages.empty,
				display: kendoGridMessages.display
			},
			pageSizes: KENDOUI_PAGESIZES,
			numeric: true
		},
		dataBinding: function () {
            record = (this.dataSource.page() - 1) * this.dataSource.pageSize();
        },
        columns: [
			{field: 'STT', width: '60px', encoded: false, headerTemplate: '<a class="k-link" href="javascript:void(0);">STT</a>',
  				attributes: {'class': 'table-cell text-center'}, sortable: false, 
  				headerAttributes: {'class': 'table-header-cell text-center'}, template: "#= ++record #",
  			},
  			{field: 'CopyRight', width: '200px', encoded: false, headerTemplate: '<a class="k-link" href="javascript:void(0);">Bản quyền</a>',
				attributes: {'class': 'table-cell text-left'}, 
				headerAttributes: {'class': 'table-header-cell text-center'},
			},
			
			
			{field: 'StatusDesc', width: '120px', encoded: false, headerTemplate: '<a class="k-link" href="javascript:void(0);">Trạng thái</a>',
				attributes: {'class': 'table-cell text-left'}, 
				headerAttributes: {'class': 'table-header-cell text-center'},
			},
			{field: 'CreateDate', width: '130px', encoded: false, headerTemplate: '<a class="k-link" href="javascript:void(0);">Ngày tạo</a>',
				attributes: {'class': 'table-cell', style: 'text-align: center;'}, 
				headerAttributes: {'class': 'table-header-cell', style: 'text-align: center;',},
			},
			{field: 'CreateUserFullName', width: '130px', encoded: false, headerTemplate: '<a class="k-link" href="javascript:void(0);">Người tạo</a>',
				attributes: {'class': 'table-cell', style: 'text-align: left;'}, 
				headerAttributes: {'class': 'table-header-cell', style: 'text-align: center;',},
			},
		],
        filter: function(arg){arraySelected = [];},
		sort: function(arg){arraySelected = [];},
		dataBound: function(e) {
			_gridMain.find('div table tbody tr td').each(function(idx, obj){
				$(obj).attr('title', $(obj).html())
			});
			
			$("#fFooter").find('button[data-action="detail"], button[data-action="hotnews"]').prop('disabled', true);
			$("#fFooter").find('button[data-action="edit"]').prop('disabled', true);
			$("#fFooter").find('button[data-action="active"]').prop('disabled', true);
			$("#fFooter").find('button[data-action="de-active"]').prop('disabled', true);
			$("#fFooter").find('button[data-action="delete"]').prop('disabled', true);
		},
	});
	
	_gridMain.find('table[role="grid"]').find('tbody').undelegate('tr', 'click');
	_gridMain.find('table[role="grid"]').find('tbody').delegate('tr', 'click', function(e){
		var item = _gridMain.data("kendoGrid").dataItem($(this).closest("tr"));
		$("#fFooter").find('button[data-action="detail"], button[data-action="hotnews"]').prop('disabled', false);
		
		var grid = _gridMain.data("kendoGrid");
		var rowData = grid.dataItem(grid.select());
		if(null == rowData) return;
		
		var isActive = null == rowData['ActiveFlag']? '': rowData['ActiveFlag'];
		
		$("#fFooter").find('button[data-action="edit"]').prop('disabled', !('N' == isActive) );
		$("#fFooter").find('button[data-action="active"]').prop('disabled', !('N' == isActive) );
		$("#fFooter").find('button[data-action="de-active"]').prop('disabled', 'N' == isActive );
		$("#fFooter").find('button[data-action="delete"]').prop('disabled', !('N' == isActive) );
	});
	
	$("#fFooter").find('button[data-action]').click(function (event) {
		event.preventDefault();/*event.stopPropagation();*/
		var dataAction = $(this).data('action');
		var dataController = $(this).data('controller');
		
		var actionCheck = '|detail|edit|de-active|active|delete|';
		var grid = _gridMain.data("kendoGrid");
    	var rowData = grid.dataItem(grid.select());
    	if(actionCheck.indexOf('|' + dataAction + '|') != -1 && null == rowData){
    		alertDLSuccess('Vui lòng chọn dòng dữ liệu để thực hiện.', function(){});
    		return;
    	}
		
		var objData = {};
		if(rowData != null){
			objData['_id'] = rowData['_id'];
		}
		switch (dataAction) {
		case 'search':
			_gridMain.data("kendoGrid").dataSource.read();
			break;
		case 'create':
		case 'detail':
		case 'edit':
			$('#divSubContent').show();$('#divMainContent').hide();
//			submitFormRenderArea(ROOT_PATH + '/Main?transaction=ServiceContractCreate&method=main', objData, $('#divSubContent'));
			submitFormRenderArea(ROOT_PATH + '/admin/main/' + dataController + '/init', objData, $('#divSubContent'));
			break;
		case 'active':
		case 'de-active':
		case 'delete':
		case 'hotnews':
			objData = {_id: rowData['_id']};
			$.ajax({
				type: "POST",
				datatype: "json",
				url: ROOT_PATH + '/admin/main/' + dataController + '/checkDataToSave',
				data: objData,
				beforeSend: function(req) {
					initAjaxJsonRequest(req);
		        	showLoading();
				},
				success:function(res) {
					hideLoading();
					if(res) {
						if(res.errorCode == 0) {
							var responseData = res.responseData;
							var confirmText = responseData['CONFIRM'];
							tokenTransaction = responseData['TOKEN'];
							
							objData['tokenTransaction'] = tokenTransaction;
							
							alertConfirm(confirmText,
								function(e){
									$.ajax({
										type: "POST",
										datatype: "json",
										url: ROOT_PATH + '/admin/main/' + dataController + '/saveData',
										data: objData,
										beforeSend: function(req) {
											initAjaxJsonRequest(req);
								        	showLoading();
										},
										success:function(res) {
											hideLoading();
											if(res) {
												if(res.errorCode == 0) {
													_gridMain.data("kendoGrid").dataSource.read();
												}else{
													alertDLSuccess(createObjectError(res).html(), function(){});
												}
											}else{
												alertDLSuccess('unknown error!!!', function(){});
												hideLoading();
											}
										},
										error:function (xhr, ajaxOptions, thrownError){
											alertDLSuccess(xhr.status + " - " + xhr.responseText, function(){});
								            hideLoading();
								        }
									});
								},
								function(e){}
							)
						}else{
							alertDLSuccess(createObjectError(res).html(), function(){});
						}
					}else{
						alertDLSuccess('unknown error!!!', function(){});
						hideLoading();
					}
				},
				error:function (xhr, ajaxOptions, thrownError){
					alertDLSuccess(xhr.status + " - " + xhr.responseText, function(){});
		            hideLoading();
		        }
			});
			break;

		default:
			break;
		}
		
	});
	
});

function getDataSearch(){
	var dataPost = {};
	dataPost['title'] = $("#fFooter").find('#title').val();
	return dataPost;
}