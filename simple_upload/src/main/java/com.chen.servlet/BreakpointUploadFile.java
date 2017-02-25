package com.chen.servlet;

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
 * Created by CHEN on 2016/8/10.
 * 断电续传
 */
@WebServlet("/breakpoint/breakpoint_upload")
public class BreakpointUploadFile extends HttpServlet{
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
                    filename=new String(item.getString().getBytes("iso-8859-1"),"utf-8");//对文件名进行修改
                    break;
                }
            }
            for(FileItem item:list) {

                if(!item.isFormField()) {
                    File file=new File(req.getServletContext().getRealPath("/file")+"/"+filename);//对文件进行遍历找到目标文件
                    OutputStream out;
                    InputStream in = item.getInputStream() ;
                    if(file.exists()) {
                        out=new FileOutputStream(file,true);//true代表续写
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

                    item.delete();
                    break;
                }
            }
        }

    }
}
