<%@ page import="java.io.*,java.util.*, javax.servlet.*" %>
<%@ page import="javax.servlet.http.*" %>
<%@ page import="org.apache.commons.fileupload.*" %>
<%@ page import="org.apache.commons.fileupload.disk.*" %>
<%@ page import="org.apache.commons.fileupload.servlet.*" %>
<%@ page import="org.apache.commons.io.output.*" %>
<%@ page import="ext.generic.excel.check.util.CheckPdfSignature" %>

<%
   File file ;
   int maxFileSize = 5000 * 1024;
   int maxMemSize = 5000 * 1024;
   String filePath = "C:/Users";
   String pdfFileType = request.getParameter("pdfFileType");

   // ��֤�ϴ�����������
   String contentType = request.getContentType();
   if ((contentType.indexOf("multipart/form-data") >= 0)) {

      DiskFileItemFactory factory = new DiskFileItemFactory();
      // �����ڴ��д洢�ļ������ֵ
      factory.setSizeThreshold(maxMemSize);
      // ���ش洢�����ݴ��� maxMemSize.
      factory.setRepository(new File("c:\\temp"));

      // ����һ���µ��ļ��ϴ��������
      ServletFileUpload upload = new ServletFileUpload(factory);
      // ��������ϴ����ļ���С
      upload.setSizeMax( maxFileSize );
      try{ 
         // ������ȡ���ļ�
         List fileItems = upload.parseRequest(request);

         // �����ϴ����ļ�
         Iterator i = fileItems.iterator();
         while ( i.hasNext () ) 
         {
            FileItem fi = (FileItem)i.next();
            if ( !fi.isFormField () )	
            {
            // ��ȡ�ϴ��ļ��Ĳ���
            String fieldName = fi.getFieldName();
            String fileName = fi.getName();
            boolean isInMemory = fi.isInMemory();
            long sizeInBytes = fi.getSize();
            // д���ļ�
            
            file = new File("C:/Users/check.pdf") ;
            
            fi.write(file);
            
           
            String result = CheckPdfSignature.checkSignature(pdfFileType);
            out.println("<script>parent.callback('"+result+"')</script>");  

            }
         }
      }catch(Exception ex) {
         System.out.println(ex);
      }
   }
%>
