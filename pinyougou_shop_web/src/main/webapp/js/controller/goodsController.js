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
        //声明 entity 对象(为 specificationItems 集合初始化)
        // $scope.entity = {goods: {},tbGoodsDesc: {"itemImages": [], "specificationItems": []}};
        $scope.entity = {tbGoodsDesc: {"itemImages": [], "specificationItems": []}};

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

        // $scope.entity = {};
        // $scope.entity.tbGoodsDesc = {};
        //监控 entity.goods.typeTemplateId,根据模板 id 查询品牌列表
        $scope.$watch("entity.goods.typeTemplateId", function (newValue, oldValue) {
                typeTemplateService.findOne(newValue).success(
                    function (response) {
                        $scope.typeTemplate = response;
                        //转换typeTemplate 对象中的 brand_ids 这个 json 串
                        $scope.typeTemplate.brandIds = JSON.parse($scope.typeTemplate.brandIds);
                        //2.查看扩展属性列表
                        $scope.entity.tbGoodsDesc.customAttributeItems = JSON.parse($scope.typeTemplate.customAttributeItems);
                    }
                )
                //3.查询规格列表
                $scope.findSpecList(newValue);
            }
        )

        $scope.findSpecList = function (id) {
            typeTemplateService.findSpecList(id).success(
                function (response) {
                    $scope.specList = response;
                }
            )
        }

        //定义点击规格项时触发的函数
        $scope.updateSpecAttribute = function ($event, name, value) {
            var object = $scope.searchObjectByKey($scope.entity.tbGoodsDesc.specificationItems, "attributeName", name);
            if (object != null) {
                if ($event.target.checked) {  //如果被复选
                    object.attributeValue.push(value);
                } else {                      //如果未复选则删除
                    object.attributeValue.splice(object.attributeValue.indexOf(value), 1);
                    //如果 attributeValue 数组中一项都没有，删除此项
                    if (object.attributeValue.length == 0) {
                        $scope.entity.tbGoodsDesc.specificationItems.splice($scope.entity.tbGoodsDesc.specificationItems.indexOf(object), 1)
                    }
                }
            } else {
                $scope.entity.tbGoodsDesc.specificationItems.push({"attributeName": name, "attributeValue": [value]});
            }
        }

        //生成规格 列表
        $scope.createItemList = function () {
            //定义规格 新的根据用户勾选的选项生成的新列表
            $scope.entity.itemList = [{spec: {}, price: 0, num: 99999, status: '0', isDefault: '0'}];
            //得到用户勾选的所有内容
            var items = $scope.entity.tbGoodsDesc.specificationItems;
            //遍历其内容
            for (var i = 0; i < items.length; i++) {
                $scope.entity.itemList = addColumn($scope.entity.itemList, items[i].attributeName, items[i].attributeValue);
            }
        }

        //新增加列
        //不需要在前端页面调用的方法可以不加 $scope. 前缀
        addColumn = function (list, attributeName, attributeValue) {
            var newList = []; //构造新的集合
            for (var i = 0; i < list.length; i++) { //得到原始集合中的数据
                var oldRow = list[i];
                for (var j = 0; j < attributeValue.length; j++) { //根据老的集合项构造新的对象
                    var newRow = JSON.parse(JSON.stringify(oldRow)); //深克隆
                    // 重新对新的集合项赋值
                    newRow.spec[attributeName] = attributeValue[j]; //再将新项添加到新的集合中 newList.push(newRow);
                    //再将新项添加到新的集合中
                    newList.push(newRow);
                }
            }
            return newList;
        }
    }
);
