package com.chen.servlet;

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
 * Created by CHEN on 2016/8/10.
 * 进度条
 */
@WebServlet("/progressbar/progressbar_upload")
public class ProgressbarUploadFile extends HttpServlet{
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Map map = new HashMap();
        req.setCharacterEncoding("utf-8");
        DiskFileItemFactory factory = new DiskFileItemFactory();

        factory.setRepository(new File(req.getServletContext().getRealPath("/temp")));
        factory.setSizeThreshold(1024*1024) ;
        ServletFileUpload upload = new ServletFileUpload(factory);
        try {
            //可以上传多个文件
            List<FileItem> list = (List<FileItem>)upload.parseRequest(req);

            if(list.size()<0) {

            } else {
                for(FileItem item : list){
                    if(!item.isFormField()){
                        String name = item.getName() ;
                        String fileSuffix  = name.substring(name.lastIndexOf(".")+1,name.length());
                        String oldName = name.replaceAll("." + fileSuffix,"");
                        String fileName = ""+System.currentTimeMillis();
                        String newName = fileName + "." + fileSuffix;
                        OutputStream out = new FileOutputStream(new File(req.getServletContext().getRealPath("/file"),newName));
                        InputStream in = item.getInputStream() ;
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
        }catch (Exception e) {
            System.out.println("出错了：" + e.getMessage());
        }
        
    }
}
