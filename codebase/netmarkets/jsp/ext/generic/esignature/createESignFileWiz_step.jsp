<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.ptc.com/windchill/taglib/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.ptc.com/windchill/taglib/components" prefix="jca"%>
<%@ taglib uri="http://www.ptc.com/windchill/taglib/core" prefix="wc"%>


<fmt:setLocale value="${localeBean.locale}"/>
<fmt:setBundle basename="ext.generic.esignature.esignatureResource"/>
<!-- get localized column headers -->
<fmt:message var="msg" key="CREATE_ESIGN_FILE_MSG"/>
${msg}
