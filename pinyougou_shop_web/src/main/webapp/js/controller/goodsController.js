//控制层
app.controller('goodsController', function ($scope, $controller, goodsService, itemCatService, typeTemplateService) {

    $controller('baseController', {$scope: $scope});//继承

    //读取列表数据绑定到表单中  
    $scope.findAll = function () {
        goodsService.findAll().success(
            function (response) {
                $scope.list = response;
            }
        );
    }

    //分页
    $scope.findPage = function (page, rows) {
        goodsService.findPage(page, rows).success(
            function (response) {
                $scope.list = response.rows;
                $scope.paginationConf.totalItems = response.total;//更新总记录数
            }
        );
    }

    //查询实体
    $scope.findOne = function (id) {
        goodsService.findOne(id).success(
            function (response) {
                $scope.entity = response;
            }
        );
    }

    //增加商品
    $scope.add = function () {
        $scope.entity.tbGoodsDesc.introduction = editor.html();//得到富文 本编辑器的内容并为 introduction 属性赋值
        goodsService.add($scope.entity).success(
            function (response) {
                if (response.success) {
                    alert("保存成功！")
                    $scope.entity = {};
                    editor.html('');//清空富文本编辑器
                } else {
                    alert(response.message);
                }
            }
        );
    }


    //批量删除
    $scope.dele = function () {
        //获取选中的复选框
        goodsService.dele($scope.selectIds).success(
            function (response) {
                if (response.success) {
                    $scope.reloadList();//刷新列表
                    $scope.selectIds = [];
                }
            }
        );
    }

    $scope.searchEntity = {};//定义搜索对象

    //搜索
    $scope.search = function (page, rows) {
        goodsService.search(page, rows, $scope.searchEntity).success(
            function (response) {
                $scope.list = response.rows;
                $scope.paginationConf.totalItems = response.total;//更新总记录数
            }
        );
    }

    //1.查询一级分类
    $scope.findByParentId = function (parentId) {
        itemCatService.findByParentId(parentId).success(
            function (response) {
                $scope.itemCat1List = response;
            }
        )
    }

    // $watch（）函数代表监控参数 1 对应的变量的变化，当变量发生改变时会执行第二个参数对应的函数
    // 此函数中参数 2 代表变量未发生变化前的值，参数 1 代表变量发生变化后的新值(就是你选择的哪 个值)
    // 监控一级分类
    $scope.$watch("entity.goods.category1Id", function (newValue, oldValue) {
        itemCatService.findByParentId(newValue).success(
            function (response) {
                $scope.itemCat2List = response;
            }
        )
    })
    // 监控二级分类
    $scope.$watch("entity.goods.category2Id", function (newValue, oldValue) {
        itemCatService.findByParentId(newValue).success(
            function (response) {
                $scope.itemCat3List = response;
            }
        )
    })
    // 监控三级分类
    $scope.$watch("entity.goods.category3Id", function (newValue, oldValue) {
        //1.当选择三级分类时在，后的模板 id 发生改变
        itemCatService.findOne(newValue).success(
            function (response) {
                $scope.entity.goods.typeTemplateId = response.typeId;
            }
        )
    })

    $scope.$watch("entity.goods.typeTemplateId", function (newValue, oldValue) {
            typeTemplateService.findOne(newValue).success(
                function (response) {
                    $scope.typeTemplate = response;
                    //转换typeTemplate 对象中的 brand_ids 这个 json 串
                    $scope.typeTemplate.brandIds = JSON.parse($scope.typeTemplate.brandIds);
                }
            )

        }
    )
});	
