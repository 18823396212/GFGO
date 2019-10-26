<%@ include file="/netmarkets/jsp/components/beginWizard.jspf" %>

<%@ taglib uri="http://www.ptc.com/windchill/taglib/mvc" prefix="mvc"%>


<%-- <jsp:include page="/netmarkets/jsp/ext/generic/reviewprincipal/chooseUser/comboxRole.jsp" flush="true" />--%>

<jsp:include page="${mvc:getComponentURL('reviewprincipal.builder.ChooseWTUserTableBuilder')}" flush="true"/>

<%@ include file="/netmarkets/jsp/util/end.jspf"%>
  

  	
  	
		    		   