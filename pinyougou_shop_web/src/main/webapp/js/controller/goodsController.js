//控制层
//引入$location内置服务，用于在不同html之间传参，要求在? 前要有# ，否则，不能传递参数！
app.controller('goodsController', function ($scope, $controller, $location, goodsService, itemCatService, typeTemplateService, uploadService) {

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
    $scope.findOne = function () {
        //通过内置服务取得从 goods.html 页面传递过来的参数id
        var id = $location.search()['id'];
        if (id == null) {
            return;
        }
        goodsService.findOne(id).success(
            function (response) {
                $scope.entity = response;
                editor.html($scope.entity.tbGoodsDesc.introduction);//商品介绍
                $scope.entity.tbGoodsDesc.itemImages = JSON.parse($scope.entity.tbGoodsDesc.itemImages);//商品图片
                //扩展属性
                $scope.entity.tbGoodsDesc.customAttributeItems = JSON.parse($scope.entity.tbGoodsDesc.customAttributeItems);
                //规格列表
                $scope.entity.tbGoodsDesc.specificationItems = JSON.parse($scope.entity.tbGoodsDesc.specificationItems);
                //得到SKU商品信息
                //再一次的转换其中的spec对象
                var itemList = $scope.entity.items;
                for (var i = 0; i < itemList.length; i++) {
                    itemList[i].spec = JSON.parse(itemList[i].spec);
                }
            }
        );
    };

    //增加商品
    $scope.save = function () {
        var serviceObject;//服务层对象
        $scope.entity.tbGoodsDesc.introduction = editor.html();//得到富文 本编辑器的内容并为 introduction 属性赋值
        if ($scope.entity.tbGoods.id != null) {
            serviceObject = goodsService.update($scope.entity);//修改
        } else {
            serviceObject = goodsService.add($scope.entity);//增加
        }
        serviceObject.success(
            function (response) {
                if (response.success) {
                    alert("保存成功！");
                    // $scope.entity = {};
                    // editor.html('');//清空富文本编辑器
                    location.href = "goods.html";//确认保存后返回列表页
                } else {
                    alert(response.message);
                }
            }
        );
    };


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
    };

    $scope.searchEntity = {};//定义搜索对象
    //声明 entity 对象(为 specificationItems 集合初始化)
    $scope.entity = {tbGoods: {}, tbGoodsDesc: {"itemImages": [], "specificationItems": []}};

    //将正在上传的文件保存到上传的文件列表中
    $scope.add_image_entity = function () {
        $scope.entity.tbGoodsDesc.itemImages.push($scope.image_entity);
    };

    //列表中移除图片
    $scope.remove_image_entity = function (index) {
        $scope.entity.tbGoodsDesc.itemImages.splice(index,1);
    };

    //搜索
    $scope.search = function (page, rows) {
        goodsService.search(page, rows, $scope.searchEntity).success(
            function (response) {
                $scope.list = response.rows;
                $scope.paginationConf.totalItems = response.total;//更新总记录数
            }
        );
    };

    //进行文件上传
    $scope.uploadFile = function(){
        uploadService.uploadFile().success(
            function (response) {
                if(response.success){
                    $scope.image_entity.url = response.message;
                }else {
                    alert(response.message);
                }
            }
        )
    };


    //1.查询一级分类
    $scope.findByParentId = function (parentId) {
        itemCatService.findByParentId(parentId).success(
            function (response) {
                $scope.itemCat1List = response;
            }
        )
    };

    // $watch（）函数代表监控参数 1 对应的变量的变化，当变量发生改变时会执行第二个参数对应的函数
    // 此函数中参数 2 代表变量未发生变化前的值，参数 1 代表变量发生变化后的新值(就是你选择的哪 个值)
    // 监控一级分类
    $scope.$watch("entity.tbGoods.category1Id", function (newValue, oldValue) {
        itemCatService.findByParentId(newValue).success(
            function (response) {
                $scope.itemCat2List = response;
            }
        )
    });
    // 监控二级分类
    $scope.$watch("entity.tbGoods.category2Id", function (newValue, oldValue) {
        itemCatService.findByParentId(newValue).success(
            function (response) {
                $scope.itemCat3List = response;
            }
        )
    });
    // 监控三级分类
    $scope.$watch("entity.tbGoods.category3Id", function (newValue, oldValue) {
        //1.当选择三级分类时在，后的模板 id 发生改变
        itemCatService.findOne(newValue).success(
            function (response) {
                $scope.entity.tbGoods.typeTemplateId = response.typeId;
            }
        )
    });

    //监控 entity.tbGoods.typeTemplateId,根据模板 id 查询品牌列表
    $scope.$watch("entity.tbGoods.typeTemplateId", function (newValue, oldValue) {
        //1.查看对应分类下的品牌列表
        typeTemplateService.findOne(newValue).success(
            function (response) {
                $scope.typeTemplate = response;
                //转换typeTemplate 对象中的 brand_ids 这个 json 串
                $scope.typeTemplate.brandIds = JSON.parse($scope.typeTemplate.brandIds);
                //2.查看扩展属性列表
                if ($location.search()['id'] == null) {
                    $scope.entity.tbGoodsDesc.customAttributeItems = JSON.parse($scope.typeTemplate.customAttributeItems);
                }
            }
        );
        //3.查询规格列表
        $scope.findSpecList(newValue);
    });

    $scope.findSpecList = function (id) {
        typeTemplateService.findSpecList(id).success(
            function (response) {
                $scope.specList = response;
            }
        )
    };

    //定义点击规格项时触发的函数
    $scope.updateSpecAttribute = function ($event, name, value) {
        var object = $scope.searchObjectByKey($scope.entity.tbGoodsDesc.specificationItems, "attributeName", name);
        if (object != null) {
            if ($event.target.checked) {  //如果被复选
                object.attributeValue.push(value);
            } else {                      //如果未复选则删除
                object.attributeValue.splice(object.attributeValue.indexOf(value), 1);
                //如果 attributeValue 数组中一项都没有，删除此项
                if (object.attributeValue.length === 0) {
                    $scope.entity.tbGoodsDesc.specificationItems.splice($scope.entity.tbGoodsDesc.specificationItems.indexOf(object), 1)
                }
            }
        } else {
            $scope.entity.tbGoodsDesc.specificationItems.push({"attributeName": name, "attributeValue": [value]});
        }
    };

    //生成规格 列表
    $scope.createItemList = function () {
        //定义规格 新的根据用户勾选的选项生成的新列表
        $scope.entity.items = [{spec: {}, price: 0, num: 99999, status: '0', isDefault: '0'}];
        //得到用户勾选的所有内容
        var itemList = $scope.entity.tbGoodsDesc.specificationItems;
        //遍历其内容
        for (var i = 0; i < itemList.length; i++) {
            $scope.entity.items = addColumn($scope.entity.items, itemList[i].attributeName, itemList[i].attributeValue);
        }
    };

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
    };

    //定义审核状态
    $scope.status = ["未审核", "已审核", "审核未通过", "已关闭"];
    //定义分类列表
    $scope.itemCatList = [];
    //查询分类列表
    $scope.selectItemList = function () {
        itemCatService.findAll().success(
            function (response) {
                for (var i = 0; i < response.length; i++) {
                    $scope.itemCatList[response[i].id] = response[i].name;
                }
            }
        )
    };

    //检测属性的值
    $scope.checkAttributeValue = function (specName, optionName) {
        var items = $scope.entity.tbGoodsDesc.specificationItems;
        var object = $scope.searchObjectByKey(items, "attributeName", specName);
        if (object == null) {
            return false;
        } else {
            if (object.attributeValue.indexOf(optionName) >= 0) {
                return true;
            } else {
                return false;
            }
        }
    }
});
