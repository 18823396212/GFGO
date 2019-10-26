<%@page import="ext.appo.util.dataUtitlies.AppoAttributeDataUtitly"%>
<%
String OptionsVal = new AppoAttributeDataUtitly().getOptionsVal(request);
out.write(OptionsVal); 
%>
