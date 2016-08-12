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
        resp.sendRedirect("success_page.html");

    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //跳转链接
        resp.sendRedirect("simple_upload.html");
    }
}
