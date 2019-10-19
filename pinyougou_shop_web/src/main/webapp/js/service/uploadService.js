app.service("uploadService",function ($http) {
   this.uploadFile = function () {
       var formData = new formData();
       formData.append("file", file.files[0]);
       return $http({
           url:'../upload.do',
           data:formData,
           method:'post',
           headers:{'Content-Type':undefined},
           transformRequest:angular.identity
       })
       /**
        * anjularjs 对于 post 和 get 请求默认的 Content-Type header 是 application/json。
        * 通过设置‘Content-Type’: undefined，
        * 这样浏览器会帮我们把 Content-Type 设置为 multipart/form-data.
        * 通过设置 transformRequest: angular.identity ，anjularjs transformRequest function
        * 将序列化我们的 formdata object.
        * */
   }
});