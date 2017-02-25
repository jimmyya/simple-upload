# simple_upload
这是一个上传的模块，用了四种上传的方法，简单，异步，进度条，断点。

你可以从我的博文中了解到更多的信息
http://blog.csdn.net/cjm812752853/article/details/52170611

[TOC]

##功能说明
我已经做了很多个功能模块了，现在想来整理一下做过的功能，一些web应用的基础功能。
第一个就是上传模块，说起来，上传看起来一件简单的事情，如果是以前，能上传就是一个了不起的功能了。可是现在不一样了，我们要给用户更好的体验，所以就有了我四个版本的上传模块，分别如下：
1. 简单
2. 异步
3. 进度条
4. 断点

##简单
简单上传就是一个传统的form表单上传，一个简单粗暴的上传方式。用户必须开着页面，等待上传，而且上传极有可能不成功，特别是大文件的时候。
但是也是有优点的，那就是代码量特少。这并不好，我一直觉得宁愿我多写两行代码，让用户更加方便，也不要省那两行。

###上传页面
simple_upload.html
```
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <title>简单提交页面</title>
</head>
<body>
    <form method="post" action="simple_upload" enctype="multipart/form-data">
        <input name="file" type="file" accept="image/gif,image.jpg"/>
        <input name="token" type="hidden"/>
        <input type="submit" value="提交"/>
    </form>
</body>
</html>
```
说明教程外链：[W3School](http://www.w3school.com.cn/html5/att_input_accept.asp)

###后台处理
我主要提供两种后端处理的代码，第一种就是使用org.apache.commons.fileupload.*
```
package com.chen.servlet;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

/**
 * Created by CHEN on 2016/8/10.
 */
@WebServlet("/simple_upload")
public class SimpleUploadFile extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //检查token，要是错误的token，禁止上传，返回错误页面之类的
        //TODO 很多代码

        //磁盘文件项目工厂
        DiskFileItemFactory factory=new DiskFileItemFactory();
        //设置文件阀值，当文件超过5M的时候，产生临时文件并存储在临时的目录中
        factory.setSizeThreshold(5*1024*1024);
        //设置缓存路径
        factory.setRepository(new File(req.getServletContext().getRealPath("/temp")));

        //上传的工具
        ServletFileUpload upload=new ServletFileUpload(factory);
        //设置上传的文件最大值
        upload.setSizeMax(50*1024*1024);

        try {
            List<FileItem> items=upload.parseRequest(req);
            if(items.size()<0) { //一个表单项都没有

            } else {
                for(FileItem item:items) {
                    if(item.isFormField()) {//表单项，而不是文件上传项

                    }else {//文件项
                        String filename=item.getName();
                        //后缀检查
                        String filesuffix=filename.substring(filename.lastIndexOf("."));
                        //TODO 很多代码

                        StringBuffer buffer=new StringBuffer(req.getServletContext().getRealPath("/file"));
                        buffer.append("/");
                        buffer.append(UUID.randomUUID().toString());
                        buffer.append(filesuffix);
                        filename= buffer.toString();
                        //
                        FileOutputStream out=new FileOutputStream(filename);
                        InputStream in=item.getInputStream();
                        //开始写文件
                        byte[] b=new byte[1024];
                        int len=0;
                        while((len=in.read(b))>0) {
                            out.write(b,0,len);
                        }
                        in.close();
                        out.close();
                        item.delete();//如果有临时文件就删除
                    }
                }
            }
        } catch (FileUploadException e) {
            //TODO 异常处理之类的
            e.printStackTrace();
        }
        //成功的页面
        resp.sendRedirect("success_page");

    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //跳转链接
        resp.sendRedirect("simple_upload.html");
    }
}

```
像上面的代码，是一个简单的上传模块。

##异步
有时候，用户可不想在那里傻傻的等待你跳转页面，他想要有其他事情同时可以做。那这时候，就要使用异步的方式，但是其实异步也和之前的无两样，只是把表单提取出来，提交而已。

###上传页面
```
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <title>异步上传页面</title>
</head>
<body>
    <form>
        <span id="message"></span>
        <input id="file" name="file" type="file"/>
        <input id="token" name="token" type="hidden"/>
    </form>
    <script src="https://cdn.bootcss.com/jquery/3.1.0/jquery.min.js"></script>
    <script src="/js/ajaxfileupload.js"></script>
    <script>
        $("#file").on("change",function() {

            $.ajaxFileUpload({
                url: "ajax_upload",
                fileElementId : 'file',//上传文件的ID
                dataType:"json",//返回数据格式
                success :function(data) {
                    $("#message").html(data['message']);
                },
                error:function(status) {
                    
                }
            });

        });


    </script>
</body>
</html>

```
###后台处理
后台处理基本差不多，就是把跳转换成了输出JSON。
```
package com.chen.servlet;

import net.sf.json.JSONObject;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.List;
import java.util.UUID;

/**
 * Created by CHEN on 2016/8/10.
 */
@WebServlet("/ajax_upload")
public class AjaxUploadFile extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("utf-8");
        resp.setCharacterEncoding("utf-8");
        //检查token，要是错误的token，禁止上传，返回错误页面之类的
        //TODO 很多代码

        //磁盘文件项目工厂
        DiskFileItemFactory factory=new DiskFileItemFactory();
        //设置文件阀值，当文件超过5M的时候，产生临时文件并存储在临时的目录中
        factory.setSizeThreshold(5*1024*1024);
        //设置缓存路径
        factory.setRepository(new File(req.getServletContext().getRealPath("/temp")));

        //上传的工具
        ServletFileUpload upload=new ServletFileUpload(factory);
        //设置上传的文件最大值
        upload.setSizeMax(50*1024*1024);

        try {
            List<FileItem> items=upload.parseRequest(req);
            if(items.size()<0) { //一个表单项都没有

            } else {
                for(FileItem item:items) {
                    if(item.isFormField()) {//表单项，而不是文件上传项

                    }else {//文件项
                        String filename=item.getName();
                        //后缀检查
                        String filesuffix=filename.substring(filename.lastIndexOf("."));
                        //TODO 很多代码

                        StringBuffer buffer=new StringBuffer(req.getServletContext().getRealPath("/file"));
                        buffer.append("/");
                        buffer.append(UUID.randomUUID().toString());
                        buffer.append(filesuffix);
                        filename= buffer.toString();
                        //
                        FileOutputStream out=new FileOutputStream(filename);
                        InputStream in=item.getInputStream();
                        //开始写文件
                        byte[] b=new byte[1024];
                        int len=0;
                        while((len=in.read(b))>0) {
                            out.write(b,0,len);
                        }
                        in.close();
                        out.close();
                        item.delete();//如果有临时文件就删除
                    }
                }
            }
        } catch (FileUploadException e) {
            //TODO 异常处理之类的
            e.printStackTrace();
        }
        //成功提示
        JSONObject data=new JSONObject();
        data.put("message","success");
        PrintWriter out=resp.getWriter();
        out.print(data);
        out.close();

    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //跳转链接
        resp.sendRedirect("simple_upload.html");
    }
}
```
##进度条
异步模块看起来已经是不错的了，但是 没有一个进度条之类的，就让用户在那里等着，直到上传成功，就突然跳出一句上传成功，感觉这样的交互，不好，所以在第三次中，我增加了一个进度条的功能，使用的是jQuery中的Uploadify。
详细文档：[Uploadify](http://www.uploadify.com/documentation/)
###上传页面
```
<%@ page language="java" contentType="text/html; charset=utf-8"%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">

<html>
	<head>
		<title>Upload</title>
		<!--装载文件-->
		<link href="uploadify-v3.1/uploadify.css" type="text/css" rel="stylesheet" />
		<script type="text/javascript" src="jquery.js"></script>
		<script type="text/javascript" src="uploadify-v3.1/jquery.uploadify-3.1.js"></script>
		<script type="text/javascript" src="uploadify-v3.1/swfobject.js"></script>
		<!--ready事件-->
		<script type="text/javascript">
		 	$(document).ready(function () {
				　	$("#uploadify").uploadify({
					 'debug':'true',
					 'method':'post',
					 'formData':{'chen':'chen'},//发送给服务端的参数，格式：{key1:value1,key2:value2}
					 'swf': 'uploadify-v3.1/uploadify.swf',
					  'uploader':'file_upload',
					　//'script':'upload!doUpload.action?name=yangxiang',
					　//'script': 'servlet/Upload?name=yangxiang',  
					　//'cancel' : 'uploadify-v3.1/uploadify-cancel.png',                  
					　'queueID' : 'fileQueue', //和存放队列的DIV的id一致  
					　//'fileDataName': 'fileupload', //必须，和以下input的name属性一致                   
					　'auto'  : false, //是否自动开始  
					　'multi': true, //是否支持多文件上传  
					  'buttonText': '选择文件', //按钮上的文字
					 'simUploadLimit' : 1, //一次同步上传的文件数目
					　//'sizeLimit': 19871202, //设置单个文件大小限制，单位为byte
					　'fileSizeLimit' : '6000MB',
					  'queueSizeLimit' : 10, //队列中同时存在的文件个数限制  
					　//'fileTypeExts': '*.jpg;*.gif;*.jpeg;*.png;*.bmp;*.iso',//允许的格式
					  //'fileTypeDesc': '支持格式:jpg/gif/jpeg/png/bmp/iso.', //如果配置了以下的'fileExt'属性，那么这个属性是必须的  
					　'onUploadSuccess': function ( fileObj, response, data) {  
					　		alert("文件:" + fileObj.name + "上传成功");
					　},  
					　'onUploadError': function(event, queueID, fileObj) {  
					　		alert("文件:" + fileObj.name + "上传失败");  
					　},  
					　'onCancel': function(event, queueID, fileObj){  
					　		alert("取消了" + fileObj.name);  
					　　　} 
				　});
			});
    	</script>
	</head>

	<body>
		<div id="fileQueue" style="width: 30%"></div>
		<input type="file" name="file" id="uploadify" />

		<p>
			<a href="javascript:jQuery('#uploadify').uploadify('upload','*')">开始上传</a>&nbsp;

			<a href="javascript:$('#uploadify').uploadify('cancel',$('.uploadifive-queue-item').first().data('file'))">取消上传</a>

			<a href="javascript:$('#uploadify').uploadify('cancel','*')">清空所有的上传文件</a>

			<a href="javascript:$('#uploadify').uploadify('stop','*')">暂停</a>

			<!-- 如果填入true则表示禁用上传按钮 -->
			<a href="javascript:$('#uploadify').uploadify('disable','true')">禁用</a>

			<a href="javascript:$('#uploadify').uploadify('debug')">调试</a>

		</p>
	</body>
</html>
```
###后台处理
```
import net.sf.json.JSONObject;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by CHEN on 2016/8/8.
 */
@WebServlet("/file_upload")
public class FileServlet extends HttpServlet {

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Map map = new HashMap();
        request.setCharacterEncoding("utf-8");
        DiskFileItemFactory factory = new DiskFileItemFactory();

        factory.setRepository(new File("I:\\file\\temp"));
        factory.setSizeThreshold(1024*1024) ;
        ServletFileUpload upload = new ServletFileUpload(factory);
        try {
            //可以上传多个文件
            List<FileItem> list = (List<FileItem>)upload.parseRequest(request);

            if(list.size()<0) {

            } else {
                for(FileItem item : list){
                    if(!item.isFormField()){
                        String name = item.getName() ;
                        String fileSuffix  = name.substring(name.lastIndexOf(".")+1,name.length());
                        String oldName = name.replaceAll("." + fileSuffix,"");
                        String fileName = ""+System.currentTimeMillis();
                        String newName = fileName + "." + fileSuffix;
                        OutputStream out = new FileOutputStream(new File("I:\\file\\",newName));
                        InputStream in = item.getInputStream() ;
                        int length = 0 ;
                        byte [] buf = new byte[1024] ;
                        while( (length = in.read(buf) ) != -1){
                            out.write(buf, 0, length);
                        }
                        in.close();
                        out.close();
                        /**将上传处理后的数据返回**/
                        map.put("fileSuffix",fileSuffix);
                        map.put("fileName",oldName);
                        map.put("filePath",fileName);
                        item.delete();
                        break;

                    }
                }

            }
        }catch (Exception e) {
            System.out.println("出错了：" + e.getMessage());
        }
        response.setContentType("text/xml; charset=UTF-8");
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Pragma", "no-cache");
        PrintWriter out = response.getWriter();
        JSONObject jsonObject = JSONObject.fromObject(map);
        String msg =  jsonObject.toString();
        out.print(msg);
        out.close();
    }

    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        doPost(req, resp);
    }
}
```
上面的代码顺带解决了前端传参给servlet的难题。

##断点续传
总的来说，这个功能看起来已经不错了，都有进度条了，很友好。可是还是不够了，假如用户上传2G的文件呢，上传到差不多90%了，可是手一抖，关了浏览器，那就什么都没有了。
在博客园，看到了一个很好的博文，吕大豹的《[支持断点续传的文件上传插件——Huploadify-V2.0来了](http://www.cnblogs.com/lvdabao/p/3498370.html)》
提出了很好的解决方案，那就是对上传的文件进行分割，每次只上传一部分，最后进行追加，合并。
代码如下
###上传页面
```
<!DOCTYPE html>
<html lang="zh-CN">
<head>
	<meta charset="UTF-8">
	<title>Title</title>
	<link rel="stylesheet" type="text/css" href="Huploadify.css"/>

</head>

<body>
<div id="upload"></div>
<button id="btn1">暂停</button>
<button id="btn2">上传</button>
<button id="btn3">取消</button>
<button id="btn4">disable</button>
<button id="btn5">ennable</button>
<button id="btn6">destroy</button>
<script type="text/javascript" src="jquery.js"></script>
<script type="text/javascript" src="jquery.Huploadify.js"></script>
<style type="text/css">
</style>
<script type="text/javascript">
	$(function(){

		var up = $('#upload').Huploadify({
			auto:true,
			fileTypeExts:'*.vmfk;*.jpg;*.png;*.exe;*.mp3;*.mp4;*.zip;*.doc;*.docx;*.ppt;*.pptx;*.xls;*.xlsx;*.pdf',
			multi:true,
			fileSizeLimit:99999999,
			breakPoints:true,
			saveInfoLocal:true,
			showUploadedPercent:true,//是否实时显示上传的百分比，如20%
			showUploadedSize:true,
			removeTimeout:9999999,
			uploader:'uploadfile',
			onUploadStart:function(){
				//up.settings('formData', {aaaaa:'1111111',bb:'2222'});
				var filename=$('.up_filename').eq(0).text();
				up.Huploadify('settings','formData', {'filename':filename});

			},
			onUploadSuccess:function(file){
				//alert('上传成功');
			},
			onUploadComplete:function(){
				//alert('上传完成');
			},
			/*getUploadedSize:function(file){
			 var data = {
			 data : {
			 fileName : file.name,
			 lastModifiedDate : file.lastModifiedDate.getTime()
			 }
			 };
			 var url = 'http://49.4.132.173:8080/admin/uploadfile/index/';
			 var uploadedSize = 0;
			 $.ajax({
			 url : url,
			 data : data,
			 async : false,
			 type : 'POST',
			 success : function(returnData){
			 returnData = JSON.parse(returnData);
			 uploadedSize = returnData.uploadedSize;
			 }
			 });
			 return uploadedSize;
			 }	*/
		});

		$('#btn1').click(function(){
			up.stop();
		});
		$('#btn2').click(function(){
			up.upload('*');
		});
		$('#btn3').click(function(){
			up.cancel('*');
		});
		$('#btn4').click(function(){
			//up.disable();
			up.Huploadify('disable');
		});
		$('#btn5').click(function(){
			up.ennable();
		});
		$('#btn6').click(function(){
			up.destroy();
		});

	});
</script>
</body>
</html>

```
基本是他的例子，有一点特殊的就是，我向后台传了一个文件名的参数。
###后台处理
```
import net.sf.json.JSONObject;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.servlet.ServletRequestContext;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by CHEN on 2016/8/8.
 */
@WebServlet("/hu_load/uploadfile")
public class UploadFile extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req,resp);
    }

    @Override
   protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("utf-8");
        resp.setCharacterEncoding("utf-8");
        Map map=new HashMap();
        DiskFileItemFactory factory=new DiskFileItemFactory();
        factory.setSizeThreshold(10*1024);
        factory.setRepository(new File(req.getServletContext().getRealPath("/temp")));

        ServletFileUpload upload=new ServletFileUpload(factory);
        upload.setHeaderEncoding("utf-8");

        List<FileItem> list= null;
        try {
            list = upload.parseRequest(new ServletRequestContext(req));
        } catch (FileUploadException e) {
            e.printStackTrace();
        }
        if(list.size()<0) {

        }else {
            String filename="";
            for(FileItem item:list) {
                if("".equals(filename)&&"filename".equals(item.getFieldName())) {
                    filename=new String(item.getString().getBytes("iso-8859-1"),"utf-8");
                    break;
                }
            }
            for(FileItem item:list) {

                if(!item.isFormField()) {
                    File file=new File("I:\\file\\"+filename);
                    OutputStream out;
                    InputStream in = item.getInputStream() ;
                    if(file.exists()) {
                        out=new FileOutputStream(file,true);
                    }else {
                        out = new FileOutputStream(file);
                    }
                    int length = 0 ;
                    byte [] buf = new byte[1024] ;
                    while( (length = in.read(buf) ) != -1){
                        out.write(buf, 0, length);
                    }
                    in.close();
                    out.close();
                    /**将上传处理后的数据返回**/
              /*  map.put("fileSuffix",fileSuffix);
                map.put("fileName",oldName);
                map.put("filePath",fileName);*/
                    item.delete();
                    break;
                }
            }
        }

        //可有可无
        resp.setContentType("text/xml; charset=UTF-8");
        resp.setHeader("Cache-Control", "no-cache");
        resp.setHeader("Pragma", "no-cache");
        PrintWriter out = resp.getWriter();
        JSONObject jsonObject = JSONObject.fromObject(map);
        String msg =  jsonObject.toString();
        out.print(msg);
        out.close();

    }
}
```
一开始我也没有办法获得前端传来的参数，后来我打开他的源码，看了这样的一个函数
```
//发送文件块函数
	var sendBlob = function(url,xhr,file,formdata){
	 	xhr.open(option.method, url, true);
		xhr.setRequestHeader("X-Requested-With", "XMLHttpRequest");

		var fd = new FormData();
		fd.append(option.fileObjName,file);
		if(formdata){
	  	for(key in formdata){
	  		fd.append(key,formdata[key]);
	  	}
	  }
		xhr.send(fd);
	}
```
一开始我对他进行了修改，改成了xhr.send("filename",filename)，然后后台用getPart()去接受参数，经过了很多次折腾。后台终于能拿到前端参数了，就像我写的那样，首先要遍历所有的FileItem，拿到文件名，然后才开始写文件。


##后言
上传功能算是结束了吗，不，还没有。还有类如拍照上传、拖拽上传等等没有实现，这些就留着你去思考了。提示一下，基本要依靠HTML5的特性。