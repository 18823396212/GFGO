package ext.appo.util;
import java.io.FileInputStream;
import java.io.IOException;
 
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
 
public class DownLoadUtil {
     
    protected ServletContext m_application;
    private boolean m_denyPhysicalPath;
    protected HttpServletResponse m_response;
    private String m_contentDisposition;
    protected HttpServletRequest m_request;
     
    public final void initialize(ServletConfig servletconfig,
            HttpServletRequest httpservletrequest,
            HttpServletResponse httpservletresponse) throws ServletException {
        m_application = servletconfig.getServletContext();
        m_request = httpservletrequest;
        m_response = httpservletresponse;
    }
 
    public final void initialize(PageContext pagecontext)
            throws ServletException {
        m_application = pagecontext.getServletContext();
        m_request = (HttpServletRequest) pagecontext.getRequest();
        m_response = (HttpServletResponse) pagecontext.getResponse();
    }
 
    public final void initialize(ServletContext servletcontext,
            HttpSession httpsession, HttpServletRequest httpservletrequest,
            HttpServletResponse httpservletresponse, JspWriter jspwriter)
            throws ServletException {
        m_application = servletcontext;
        m_request = httpservletrequest;
        m_response = httpservletresponse;
    }
     
    public void setContentDisposition(String s) {
        m_contentDisposition = s;
    }
    public void downloadFile(String s) throws IOException {
        downloadFile(s, null, null);
    }
 
    public void downloadFile(String s, String s1) throws IOException {
        downloadFile(s, s1, null);
    }
 
    public void downloadFile(String s, String s1, String s2) throws IOException{
        downloadFile(s, s1, s2, 65000);
    }
    public void downloadFile(String s, String s1, String s2, int i)
            throws IOException {
        if (s == null) {
            throw new IllegalArgumentException("File '" + s
                    + "' not found (1040).");
        }
        if (s.equals(""))
            throw new IllegalArgumentException("File '" + s
                    + "' not found (1040).");
        if (!isVirtual(s) && m_denyPhysicalPath)
            throw new SecurityException("Physical path is denied (1035).");
        if (isVirtual(s))
            s = m_application.getRealPath(s);
        java.io.File file = new java.io.File(s);
        FileInputStream fileinputstream = new FileInputStream(file);
        long l = file.length();
        int k = 0;
        byte abyte0[] = new byte[i];
        if (s1 == null)
            m_response.setContentType("application/x-msdownload");
        else if (s1.length() == 0)
            m_response.setContentType("application/x-msdownload");
        else
            m_response.setContentType(s1);
        m_response.setContentLength((int) l);
        m_contentDisposition = m_contentDisposition != null ? m_contentDisposition
                : "attachment;";
        if (s2 == null)
            m_response.setHeader("Content-Disposition", m_contentDisposition
                    + " filename=" + toUtf8String(getFileName(s)));
        else if (s2.length() == 0)
            m_response.setHeader("Content-Disposition", m_contentDisposition);
        else
            m_response.setHeader("Content-Disposition", m_contentDisposition
                    + " filename=" + toUtf8String(s2));
        while ((long) k < l) {
            int j = fileinputstream.read(abyte0, 0, i);
            k += j;
            m_response.getOutputStream().write(abyte0, 0, j);
        }
        fileinputstream.close();
    }
 
    private boolean isVirtual(String s) {
        if (m_application.getRealPath(s) != null) {
            java.io.File file = new java.io.File(m_application.getRealPath(s));
            return file.exists();
        } else {
            return false;
        }
    }
 
    private String getFileName(String s) {
        int i = 0;
        i = s.lastIndexOf(47);
        if (i != -1)
            return s.substring(i + 1, s.length());
        i = s.lastIndexOf(92);
        if (i != -1)
            return s.substring(i + 1, s.length());
        else
            return s;
    }
 
    public static String toUtf8String(String s) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c >= 0 && c <= 255) {
                sb.append(c);
            } else {
                byte[] b;
                try {
                    b = Character.toString(c).getBytes("utf-8");
                } catch (Exception ex) {
                    //System.out.println(ex);
                    b = new byte[0];
                }
                for (int j = 0; j < b.length; j++) {
                    int k = b[j];
                    if (k < 0)
                        k += 256;
                    sb.append("%" + Integer.toHexString(k).toUpperCase());
                }
            }
        }
        return sb.toString();
    }
 
}
