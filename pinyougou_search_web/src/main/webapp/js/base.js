//定义不带分页的模块
var app = angular.module('pinyougou', []);
/*$sce服务写成过滤器*/
app.filter('trustHtml', ['$sce', function ($sce) {
    return function (data) {//传入参数时被过滤的内容
        return $sce.trustAsHtml(data);//返回的是过滤后的内容
    };
}]);
