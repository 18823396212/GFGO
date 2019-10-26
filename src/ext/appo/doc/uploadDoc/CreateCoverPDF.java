package ext.appo.doc.uploadDoc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import ext.appo.ecn.pdf.PdfUtil;
import ext.customer.common.MBAUtil;
import wt.fc.QueryResult;
import wt.org.WTUser;
import wt.part.WTPart;
import wt.project.Role;
import wt.session.SessionServerHelper;
import wt.util.WTException;
import wt.util.WTProperties;
import wt.workflow.engine.WfProcess;
import wt.workflow.work.WorkItem;
import wt.workflow.work.WorkflowHelper;

public class CreateCoverPDF {
	
	private static Font headfont;// 设置字体大小
    private static Font keyfont;// 设置字体大小
    private static Font textfont;// 设置字体大小

    static
    {
        //中文格式
        BaseFont bfChinese;
        try
        {
            // 设置中文显示
        	String font_cn = getChineseFont();
        	bfChinese = BaseFont.createFont(font_cn+",1",
					 BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);

            headfont = new Font(bfChinese, 18, Font.NORMAL);// 设置字体大小
            keyfont = new Font(bfChinese, 35, Font.BOLD);// 设置字体大小m
            textfont = new Font(bfChinese, 8, Font.NORMAL);// 设置字体大小
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    

    private static String WT_CODEBASE = "";
       static {
           WTProperties wtproperties;
           try {
               wtproperties = WTProperties.getLocalProperties();
               WT_CODEBASE = wtproperties.getProperty("wt.codebase.location");
           } catch (IOException e) {
               e.printStackTrace();
           }
       }
    
    /**
	 * 获取中文字体位置
	 * @return
	 *  @author 
	 */
	private static String getChineseFont(){
 
 
		//宋体（对应css中的 属性 font-family: SimSun; /*宋体*/）
		String font1 ="C:/Windows/Fonts/simsun.ttc";
 
 
		//判断系统类型，加载字体文件
		java.util.Properties prop = System.getProperties();
		String osName = prop.getProperty("os.name").toLowerCase();
		System.out.println(osName);
		if (osName.indexOf("linux")>-1) {
			font1="/usr/share/fonts/simsun.ttc";
		}
		if(!new File(font1).exists()){
			throw new RuntimeException("字体文件不存在,影响导出pdf中文显示！"+font1);
		}
		return font1;
	}
    
    
    Document document = new Document();// 建立一个Document对象

	  /**
     * 提供外界调用的接口，生成以head为表头，list为数据的pdf
     * @param head  //数据表头
     * @param list  //数据
     * @return        //excel所在的路径
     * @throws WTException 
     * @throws DocumentException 
     * @throws ParseException 
	 * @throws IOException 
	 * @throws MalformedURLException 
     */
    public <T> File generatePDFs(String name,WTPart part) throws WTException, DocumentException, ParseException, MalformedURLException, IOException{
   	
//        String saveFilePathAndName = "";
//    
        //获得存储的根目录
        //String savePath = new GetFilePlace().getFileDirFromProperties(FilePath);
    	Map<String,String> mapPath = DataConfigurationPacketTable.getSendDocDataBeans();
        String savePath = mapPath.get("mappingPath");
        
//        //获得当天存储的路径,不存在则生成当天的文件夹
//        String realSavePath = new GenerateFold().getFold(savePath);
        
        
        File file = new File(savePath,name);
        try
        {
            file.createNewFile();
        }
        catch (IOException e1)
        {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
            initFile(file);
        try
        {
            file.createNewFile();  //生成一个pdf文件
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        //new CreatePdf().generatePDFa(val1,val1.length);
        new CreateCoverPDF(file).generatePDF(part);
        
        return file;
    }
    
    /**
     * 提供外界调用的接口，生成以head为表头，list为数据的pdf
     * @param head  //数据表头
     * @param list  //数据
     * @return        //excel所在的路径
     * @throws WTException 
     * @throws DocumentException 
     * @throws ParseException 
	 * @throws IOException 
	 * @throws MalformedURLException 
     */
    public <T> File generateYLPDFs(String name,WTPart part) throws WTException, DocumentException, ParseException, MalformedURLException, IOException{
   	
//        String saveFilePathAndName = "";
//    
        //获得存储的根目录
        //String savePath = new GetFilePlace().getFileDirFromProperties(FilePath);
    	Map<String,String> mapPath = DataConfigurationPacketTable.getYLSendDocDataBeans();
        String savePath = mapPath.get("mappingPath");
        
//        //获得当天存储的路径,不存在则生成当天的文件夹
//        String realSavePath = new GenerateFold().getFold(savePath);
        
        
        File file = new File(savePath,name);
        try
        {
            file.createNewFile();
        }
        catch (IOException e1)
        {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
            initFile(file);
        try
        {
            file.createNewFile();  //生成一个pdf文件
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        //new CreatePdf().generatePDFa(val1,val1.length);
        new CreateCoverPDF(file).generatePDF(part);
        
        return file;
    }
    
    public void initFile(File file)
    {
        document.setPageSize(PageSize.A4);// 设置页面大小
        try
        {
            PdfWriter.getInstance(document, new FileOutputStream(file));
            document.open();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    /**
     * 文成文件
     * @param file 待生成的文件名
     */
    public CreateCoverPDF(File file)
    {
        document.setPageSize(PageSize.A4);// 设置页面大小
        try
        {
            PdfWriter.getInstance(document, new FileOutputStream(file));
            document.open();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    public CreateCoverPDF()
    {
        
    }
    
    public <T> void generatePDF(WTPart part) throws WTException, DocumentException, ParseException, MalformedURLException, IOException 
    {
    	boolean flag = SessionServerHelper.manager.setAccessEnforced(false); // 忽略权限
    	
    	String font_cn = getChineseFont();
    	 BaseFont bfChinese = BaseFont.createFont(font_cn+",1",
				 BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
      	 Font FontChinese = new Font(bfChinese, 35, Font.NORMAL);//加入document：
//    	 Chapter chapter2 = new Chapter(1);
//    	 Paragraph title12 = new Paragraph();
//    	 Section section2 = chapter2.addSection(title12);
//    	             /** 添加图片 */
//    	 section2.add(new Paragraph("图片添加: 饼图", FontChinese));
//    	 Image image = Image.getInstance("/ptc/Windchill_11.0/Windchill/temp/controlledChapter-20180619-2.png"); 
//    	 section2.add(image);
    	 
      	 PdfPTable table2 = new PdfPTable(2); //表格两列

    	 table2.setHorizontalAlignment(Element.ALIGN_CENTER); //垂直居中
    	 table2.setWidthPercentage(100);//表格的宽度为100%
    	 float[] wid1 ={0.75f,0.25f}; //两列宽度的比例
    	 table2.setWidths(wid1);
    	 table2.getDefaultCell().setBorderWidth(0); //不显示边框
    	 PdfPCell cell11 = new PdfPCell(new Paragraph("文件受控信息",FontChinese));
    	 cell11.setHorizontalAlignment(Element.ALIGN_CENTER);
    	 cell11.setBorder(0);
    	 cell11.setPaddingTop(15.0f);
    	 cell11.setPaddingBottom(8.0f);
    	 table2.addCell(cell11);
    	 //String imagepath = "/ptc/Windchill_11.0/Windchill/temp/controlledChapter-20180619-2.png";
    	 String imagepath = WT_CODEBASE  + File.separator + "config" + File.separator + "custom" + File.separator + "controlledChapter-20180619-2.png";
    	 Image image = Image.getInstance(imagepath);
    	 table2.addCell(image);


    	
    	PdfPTable table1 = createTable(1);
//    	Image image = Image.getInstance("/ptc/Windchill_11.0/Windchill/temp/controlledChapter-20180619-2.png"); 
//        image.scaleAbsolute(50f, 50f); 
//        image.setAlignment(Image.MIDDLE);
//        table1.addCell(createCell("文件受控信息"+image, keyfont, Element.ALIGN_CENTER, 1,false));   
    	Map<String,String> map = new HashMap<>();
    	QueryResult qr = WorkflowHelper.service.getWorkItems(part);
        while(qr.hasMoreElements()){
        WorkItem wi = (WorkItem)qr.nextElement();
        
        if(wi != null){
    	WfProcess process = PdfUtil.getProcess(wi);
    	
    	if(process != null){
    	WTUser creatorUs =  (WTUser)process.getCreator().getPrincipal();
    	String creator =  creatorUs.getFullName();
    	map.put("creator", creator== null ? "" :creator);
    	SimpleDateFormat sdf = new SimpleDateFormat(PdfUtil.SIMPLE_DATE_FORMAT);
    	
        map.put("createTimestamp", sdf.format(wi.getCreateTimestamp())== null ? "" :sdf.format(wi.getCreateTimestamp()));
        map.put("createor", "");
        
        Set<String> setReview = new HashSet<>();
     	//wi.getc
        Role role = Role.toRole("Assessor");
        List<WTUser> users = PdfUtil.getUsers(process, role);
        if(!users.isEmpty()) {
        	for(WTUser user : users){
        	 String	hquser = user.getFullName();	
        	 String sfoHquser = hquser== null ? "" :   hquser.toString();
        	 String countReview = sfoHquser.replaceAll("\\d+","");
             String[] arryReview = countReview.split("\\|");
             setReview.add(arryReview[0]);
        	}
        }
        
        Role role3 = Role.toRole("Normalizer");
        List<WTUser> users3 = PdfUtil.getUsers(process, role3);
        if(!users3.isEmpty()) {
        	for(WTUser user : users3){
        	 String	hquser = user.getFullName();	
        	 String sfoHquser = hquser== null ? "" :   hquser.toString();
        	 String countReview = sfoHquser.replaceAll("\\d+","");
             String[] arryReview = countReview.split("\\|");
             setReview.add(arryReview[0]);
        	}
        }
        map.put("Assessor",StringUtils.strip(setReview.toString(),"[]"));
      
    	Set<String> setApproval = new HashSet<>();
     	//wi.getc
        Role role2 = Role.toRole("APPROVER");
        List<WTUser> users2 = PdfUtil.getUsers(process, role2);
        if(!users2.isEmpty()) {
        	for(WTUser user : users2){
        	 String	hquser = user.getFullName();	
        	 String sfoHquser = hquser== null ? "" :   hquser.toString();
        	 String countApproval= sfoHquser.replaceAll("\\d+","");
             String[] arryApproval = countApproval.split("\\|");
             setApproval.add(arryApproval[0]);
             
        	}
        }
        
        map.put("APPROVER",StringUtils.strip(setApproval.toString(),"[]"));
        	}
         }
        }
        
        table1.addCell(createCell("", headfont, Element.ALIGN_CENTER,1,false));
    	table1.addCell(createCell("文件编号："+part.getNumber(), headfont, Element.ALIGN_LEFT,1,false));      	
    	table1.addCell(createCell("", headfont, Element.ALIGN_CENTER,1,false));   
    	table1.addCell(createCell("文件名称："+part.getName(), headfont, Element.ALIGN_LEFT,1,false));   
    	table1.addCell(createCell("", headfont, Element.ALIGN_CENTER,1,false));
    	String partVersion = part.getVersionInfo().getIdentifier().getValue();
        String partIteration = part.getIterationInfo().getIdentifier().getValue();
    	table1.addCell(createCell("文件版本："+partVersion+"."+partIteration, headfont, Element.ALIGN_LEFT,1,false));   
    	table1.addCell(createCell("", headfont, Element.ALIGN_CENTER,1,false));
    	String createTimestamp=StringUtils.strip(map.get("createTimestamp"),"[]") == null ? "" :  StringUtils.strip(map.get("createTimestamp"),"[]");
    	table1.addCell(createCell("发行日期："+createTimestamp, headfont, Element.ALIGN_LEFT,1,false));
    	table1.addCell(createCell("", headfont, Element.ALIGN_CENTER,1,false));
    	String creator=StringUtils.strip(map.get("creator"),"[]") == null ? "" :  StringUtils.strip(map.get("creator"),"[]");
    	table1.addCell(createCell("拟制人："+creator, headfont, Element.ALIGN_LEFT,1,false));   
    	table1.addCell(createCell("", headfont, Element.ALIGN_CENTER,1,false));
    	String Assessor=StringUtils.strip(map.get("Assessor"),"[]") == null ? "" :  StringUtils.strip(map.get("Assessor"),"[]");
    	table1.addCell(createCell("审核人："+Assessor, headfont, Element.ALIGN_LEFT,1,false)); 
    	table1.addCell(createCell("", headfont, Element.ALIGN_CENTER,1,false));
    	String Approver=StringUtils.strip(map.get("APPROVER").toString(),"[]") == null ? "" :  StringUtils.strip(map.get("APPROVER").toString(),"[]");
    	table1.addCell(createCell("批准人："+Approver, headfont, Element.ALIGN_LEFT,1,false));
    	table1.addCell(createCell("", headfont, Element.ALIGN_CENTER,1,false));
    	table1.addCell(createCell("", headfont, Element.ALIGN_CENTER,1,false));
    	table1.addCell(createCell("郑重声明：未经批准，不得复制", headfont, Element.ALIGN_CENTER,1,false));   
        
        try
        {
            //将表格添加到文档中
        	document.add(table2);
        	document.add(table1);
        }
        catch (DocumentException e)
        {
            e.printStackTrace();
        }finally {
			SessionServerHelper.manager.setAccessEnforced(flag); // 添加权限
		}
        
        //关闭流
        document.close();
    }
    
    
    int maxWidth = 520;
    
    /**
     * 创建一个表格对象
     * @param colNumber  表格的列数
     * @return              生成的表格对象
     */
    public PdfPTable createTable(int colNumber)
    {
        PdfPTable table = new PdfPTable(colNumber);
        try
        {
            table.setTotalWidth(maxWidth);
            table.setLockedWidth(true);
            table.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.getDefaultCell().setBorder(1);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return table;
    }
    
    
    /**
     * 为表格添加一个内容
     * @param value           值
     * @param font            字体
     * @return                添加的文本框
     */
    public PdfPCell createCell(String value, Font font)
    {
        PdfPCell cell = new PdfPCell();
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPhrase(new Phrase(value, font));
        return cell;
    }

    /**
     * 为表格添加一个内容
     * @param value           值
     * @param font            字体
     * @param align            对齐方式
     * @param colspan        占多少列
     * @return                添加的文本框
     */
    public PdfPCell createCell(String value, Font font, int align, int colspan)
    {
        PdfPCell cell = new PdfPCell();
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setHorizontalAlignment(align);
        cell.setColspan(colspan);
        cell.setPhrase(new Phrase(value, font));
        return cell;
    }
    
    /**
     * 为表格添加一个内容
     * @param value           值
     * @param font            字体
     * @param align            对齐方式
     * @param colspan        占多少列
     * @param boderFlag        是否有有边框
     * @return                添加的文本框
     */
    public PdfPCell createCell(String value, Font font, int align, int colspan,
            boolean boderFlag)
    {
        PdfPCell cell = new PdfPCell();
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setHorizontalAlignment(align);
        cell.setColspan(colspan);
        cell.setPhrase(new Phrase(value, font));
        cell.setPadding(3.0f);
        if (!boderFlag)
        {
            cell.setBorder(0);
            cell.setPaddingTop(15.0f);
            cell.setPaddingBottom(8.0f);
        }
        return cell;
    }
	
    /**
     * 根据文件类别生成文件的名字,文件的命名规则是:文件目录/生成时间-uuid（全球唯一编码）.文件类别
     * @param fileDir  文件的存储路径
     * @param fileType 文件的类别
     * @return                 文件的名字  
     */
    public String generateFileName(String fileDir,String fileType)
    {
        String saveFileName = "";
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmssSS");
        saveFileName += format.format(Calendar.getInstance().getTime());
        
        UUID uuid = UUID.randomUUID();  //全球唯一编码
        
        saveFileName += "-" + uuid.toString();
        saveFileName += "." + fileType;
        
        saveFileName = fileDir + File.separator + saveFileName;
        
        return saveFileName;
    }
}
