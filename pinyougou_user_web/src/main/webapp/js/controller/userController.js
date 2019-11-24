//控制层
app.controller('userController', function ($scope, $controller, userService) {

    //注册
    $scope.reg = function () {
        if ($scope.entity.password != $scope.password) {
            alert("两次输入的密码不一致，请重新输入");
            return;
        }
        userService.add($scope.entity, $scope.smscode).success(
            function (response) {
                alert(response.message);
                if(response.message!="验证码输入错误！"){
                    //确认后清空注册信息
                    $scope.entity={};
                    $scope.password="";
                    $scope.smscode="";
                }
            }
        );
    }

    $scope.entity = {phone:""};
    //发送验证码
    $scope.sendCode = function () {
        if($scope.entity.phone == null || $scope.entity.phone == ""){
            alert("请输入手机号！");
            return;
        }
        userService.sendCode($scope.entity.phone).success(
            function (response) {
                alert(response.message);
            }
        );
    }

});	
