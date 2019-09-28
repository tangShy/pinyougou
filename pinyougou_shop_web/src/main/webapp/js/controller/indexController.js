app.controller('indexController', function ($scope, $controller, shopLoginService) {
    //读取当前登录人
    $scope.showLoginName = function () {
        shopLoginService.loginName().success(function (response) {
            $scope.loginName = response.loginName;
        });
    }
});