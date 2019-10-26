<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ page import="ext.appo.part.workflow.ReadExcelData"%>
<%@ page import="ext.appo.part.beans.ProductLineConfigBean"%>
<%@ page import="ext.lang.PIStringUtils"%>
<%
    String productLine = request.getParameter("productLine");
	// 读取‘所属产品线’属性配置
	List<ProductLineConfigBean> productLineConfigArray = ReadExcelData.getProductLineConfig() ;
	for(ProductLineConfigBean bean : productLineConfigArray){
		if(bean.getEnumName().equalsIgnoreCase(productLine)){
			if(PIStringUtils.isNotNull(bean.getNodeName())){
				productLine = bean.getNodeName() ;
			}
			break ;
		}
	}
    if(productLine != null || !("".equals(productLine))){
    	String readExcel = ReadExcelData.readExcelReturn(productLine);
    	out.print(readExcel);
    }

%>