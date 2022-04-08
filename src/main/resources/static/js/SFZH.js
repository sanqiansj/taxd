//function checkLog(){
//    var islogin ="<%=Session["islogin"]%>";
//    if (islogin == 'true') {
//        window.location.href = "/type/1-1";
//    }
//    else{
//        bootbox.alert({title:"错误提示",message:"请先登录用户."});
//    }
//}

function doSendMail(obj) {
    var email = $.trim($("#regname").val());
    //对邮箱地址校验
    if (!email.match(/.+@.+\..+/)){
        bootbox.alert({title:"错误提示",message:"邮箱地址格式不正确."});
        $("#regname").focus();
        return false;
    }
    $.post('/ecode','email='+ email, function (data){
        if (data == 'email-invalid'){
            bootbox.alert({title:"错误提示",message:"邮箱地址格式不正确."});
            $("#regname").focus();
            return false;
        }
        if (data == 'send-pass'){
            bootbox.alert({title:"信息提示",message:"邮箱验证码已成功发送，请查收."});
            $("#regname").attr('disabled',true);
            $(obj).attr('disabled',true);
            return false;
        }
        else {
            bootbox.alert({title:"错误提示",message:"邮箱验证码未成功发送."});
            return false;
        }
    });
    }

function doReg(){
    var regname = $.trim($("#regname").val());
    var regpass = $.trim($("#regpass").val());
    var regcode = $.trim($("#regcode").val());

    if (!regname.match(/.+@.+\..+/) || regpass.length<5) {
        bootbox.alert({title:"错误提示",message:"注册邮箱不正确或密码小于五位."});
        return false
    }
    else{
        var param = "username=" + regname;
        param+= "&password=" + regpass;
        param+= "&ecode=" +regcode;
        $.post('/user',param, function (data){
           if (data == "ecode-error"){
                bootbox.alert({title:"错误提示",message:"验证码无效."});
                $("#regcode").val('');
                $("#regcode").focus();
           }
           else if (data == "user-repeated"){
                bootbox.alert({title:"错误提示",message:"该用户已注册."});
                $("#regname").focus();
           }
           else if (data == "reg-pass"){
                bootbox.alert({title:"信息提示",message:"恭喜您，注册成功！."});
                setTimeout('location.reload();',1000);
           }
           else if (data == "reg-fail"){
                bootbox.alert({title:"错误提示",message:"注册失败，请联系管理员予以解决."});
           }
        });
    }
    }



function submit(){
    var companyName = $.trim($("#companyName").val());
    var patentName = $.trim($("#patentName").val());
    var filename = $("#input-b6a").val()

    if (filename == "" || filename == null || filename == undefined){
        bootbox.alert({title:"错误提示",message:"请先选择您需要上传的文件"});
        setTimeout('location.reload();',1000);
    }
    else if (companyName.length<1 ) {
        bootbox.alert({title:"错误提示",message:"请输入您的公司名称"});
    }
    else{
        $('#input-b6a').fileinput('upload'); //触发插件开始上传。
    }
    }


function doLogin(){
    var loginname = $.trim($("#loginname").val());
    var loginpass = $.trim($("#loginpass").val());
    var logincode = $.trim($("#logincode").val());

    if (loginpass.length<5) {
        bootbox.alert({title:"错误提示",message:"您输入的密码小于五位，请重新输入。"});
        return false
    }
    else{
        var param = "username=" + loginname;
        param+= "&password=" + loginpass;
        param+= "&vcode=" +logincode;
        $.post('/login',param, function (data){
           if (data == "vcode-error"){
                bootbox.alert({title:"错误提示",message:"验证码无效."});
                $("#logincode").val('');
                $("#logincode").focus();
           }
           else if (data == "login-pass"){
                bootbox.alert({title:"信息提示",message:"恭喜您，登录成功！."});
                setTimeout('location.reload();',1000);
           }
           else if (data == "login-fail"){
                bootbox.alert({title:"错误提示",message:"登录失败，请检查用户名与密码是否正确"});
           }
        });
    }
    }

function request (fileId,url) {
    const req = new XMLHttpRequest();
    req.open('POST', url, true);
    req.responseType = 'blob';
    req.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
    req.onload = function() {
        const data = req.response;
        const a = document.createElement('a');
        const blob = new Blob([data]);
        const blobUrl = window.URL.createObjectURL(blob);
        const filename = decodeURI(req.getResponseHeader('Content-Disposition').split('filename=')[1]);
        download(blobUrl,filename) ;
    };
    req.send('fileId='+ fileId);
}

function download(blobUrl,filename) {
    const a = document.createElement('a');
    a.style.display = 'none';
    a.download = filename;
    a.href = blobUrl;
    a.click();
    document.body.removeChild(a);
}

//function showLogin(){
////    $('#login').addClass("active");
////    $('#reg').removeClass("active");
////    $('#loginpanel').addClass("active");
////    $('#regpanel').removeClass("active");
//    $('mymodal').modal('show');
//}
//
//function showReg(){
////    $('#login').removeClass("active");
////    $('#reg').addClass("active");
////    $('#loginpanel').removeClass("active");
////    $('#regpanel').addClass("active");
//    $('mymodal').modal('show');
//}
//$.ajax({
//    url: "/uploadprocess",
//    type: 'POST,
//    data: data,
//    success: function(msg){
//        bootbox.alert({title:"信息提示",message:"恭喜您，登录成功！."});
//    }
//})