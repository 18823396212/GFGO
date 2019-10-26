package ext.appo.ecn.config;

import java.util.Map;

import org.apache.log4j.Logger;

import wt.fc.ObjectReference;
import wt.log4j.LogR;
import wt.util.WTException;
import wt.util.WTMessage;

import com.ptc.core.components.descriptor.ModelContext;
import com.ptc.core.components.rendering.PickerRenderConfigs;
import com.ptc.core.meta.common.TypeInstanceIdentifier;
import com.ptc.core.meta.common.impl.WCTypeInstanceIdentifier;
import com.ptc.core.meta.server.TypeIdentifierUtility;
import com.ptc.jca.mvc.components.AbstractGenericPickerConfig;
import com.ptc.jca.mvc.components.AbstractPickerConfig;
import com.ptc.netmarkets.util.beans.NmCommandBean;

import ext.appo.ecn.resource.changeNoticeActionsRB;

public class UserPickerConfig extends AbstractGenericPickerConfig{

	private static final String CLASSNAME = UserPickerConfig.class.getName() ;
	private static final Logger LOG = LogR.getLogger(CLASSNAME) ;
	
	// 搜索类型
	public static final String OBJECT_TYPE = "wt.org.WTUser";
	
	// 回调函数
	public static final String PICKER_CALLBACK = "CusPickerInputComponentCallback";
	
	// Picker ID
	public static final String PICKER_ID = "userPicker" ;
	
	// 资源文件
	public static final String RESOURCE = changeNoticeActionsRB.class.getName() ;
	public static final String SEARCH_HEAD_TITLE = "SEARCH_HEAD_TITLE";
	
	@Override
	public String getDisplayAttribute(NmCommandBean paramNmCommandBean) throws WTException {
		return "fullName" ;
	}

	@Override
	public String getSuggestMinChars(NmCommandBean paramNmCommandBean) {
		return "2";
	}

	@Override
	public String getInline(NmCommandBean paramNmCommandBean)throws WTException {
		return "false";
	}

	@Override
	public String getMultiSelect(NmCommandBean paramNmCommandBean)throws WTException {
		return "false";
	}

	@Override
	public String getPickerCallback() throws WTException {
		return PICKER_CALLBACK;
	}

	@Override
	public String getPickerTitle(NmCommandBean paramNmCommandBean)throws WTException {
		return getPickerTitleLabel(paramNmCommandBean);
	}
	
	public static String getPickerTitleLabel(NmCommandBean paramNmCommandBean) throws WTException {
		return WTMessage.getLocalizedMessage(RESOURCE, SEARCH_HEAD_TITLE, null, paramNmCommandBean.getLocale());
	}

	@Override
	public String getPickerId() {
		return PICKER_ID;
	}
	
	public static void setPickerProperties(String param, NmCommandBean nmcommandBean, Map<Object, Object> paramMap) throws WTException {
		PickerRenderConfigs.setDefaultPickerProperty(paramMap, PickerRenderConfigs.PICKER_ID, PICKER_ID);
		PickerRenderConfigs.setDefaultPickerProperty(paramMap, PickerRenderConfigs.PICKER_TITLE, getPickerTitleLabel(nmcommandBean));
		PickerRenderConfigs.setDefaultPickerProperty(paramMap, PickerRenderConfigs.OBJECT_TYPE, OBJECT_TYPE);
		PickerRenderConfigs.setDefaultPickerProperty(paramMap, PickerRenderConfigs.READ_ONLY_TEXTBOX, "false");
		PickerRenderConfigs.setDefaultPickerProperty(paramMap, PickerRenderConfigs.INCLUDE_TII, "true");
		PickerRenderConfigs.setDefaultPickerProperty(paramMap, PickerRenderConfigs.PICKER_CALLBACK, PICKER_CALLBACK);
		PickerRenderConfigs.setDefaultPickerProperty(paramMap, PickerRenderConfigs.PICKER_ATTRIBUTES, "fullName") ;
		PickerRenderConfigs.setDefaultPickerProperty(paramMap, PickerRenderConfigs.SHOW_SUGGESTION, "true");
		PickerRenderConfigs.setDefaultPickerProperty(paramMap, PickerRenderConfigs.SUGGEST_SERVICE_KEY, "StandardUserPickerSuggestable");
		PickerRenderConfigs.setDefaultPickerProperty(paramMap, PickerRenderConfigs.SUGGEST_MIN_CHARS, "2");
		PickerRenderConfigs.setDefaultPickerProperty(paramMap, PickerRenderConfigs.WIDTH, "20");
		if(LOG.isDebugEnabled()){
			LOG.debug(paramMap);
		}
	}
	
	public static void setDefaultValue(ModelContext modelContext, Map<Object, Object> paramMap) throws WTException {
		String defaultValue = "" ;
		String defaultHiddenValue = "" ;
		
		if( modelContext != null ){
			Object rawValue = modelContext.getRawValue();
			if (rawValue != null) {
				if( rawValue instanceof WCTypeInstanceIdentifier){
					ObjectReference objRef = TypeIdentifierUtility.getObjectReference((WCTypeInstanceIdentifier) modelContext.getRawValue());
					TypeInstanceIdentifier typeInstanceIdentifier = TypeIdentifierUtility.getTypeInstanceIdentifier(objRef);
					
					defaultValue = typeInstanceIdentifier.toExternalForm() ;
					defaultHiddenValue = typeInstanceIdentifier.toExternalForm() ;
				}else {
					defaultValue = rawValue.toString() ;
					defaultHiddenValue = rawValue.toString() ;
				}
			}
		}
		
		paramMap.put(PickerRenderConfigs.DEFAULT_HIDDEN_VALUE, defaultHiddenValue) ;
		paramMap.put(PickerRenderConfigs.DEFAULT_VALUE, defaultValue) ;
	}
	
	@Override
	public ToolbarAttributeConfig[] getTableSuggestAttributeAndLabel() throws WTException {
		AbstractPickerConfig.ToolbarAttributeConfig[] arrayOfToolbarAttributeConfig = {
				new AbstractPickerConfig.ToolbarAttributeConfig("name", "Add by Name"),
				new AbstractPickerConfig.ToolbarAttributeConfig("fullName", "Add by Full Name"),
				new AbstractPickerConfig.ToolbarAttributeConfig("eMail", "Add by eMail") };

		return arrayOfToolbarAttributeConfig;
	}
}
