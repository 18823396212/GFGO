package ext.generic.change.validator;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import wt.doc.WTDocument;
import wt.fc.Persistable;
import wt.fc.WTReference;
import wt.log4j.LogR;
import wt.part.PartDocHelper;
import wt.part.WTPart;

import com.ptc.core.ui.validation.DefaultSimpleValidationFilter;
import com.ptc.core.ui.validation.UIValidationCriteria;
import com.ptc.core.ui.validation.UIValidationKey;
import com.ptc.core.ui.validation.UIValidationStatus;

import ext.generic.change.bean.MenuBean;
import ext.generic.change.constant.ChangeConstant;
import ext.generic.change.util.ChangeBusinessRuleUtil;
import ext.generic.change.util.ChangeRuleXMLConfigUtil;

/**
 * 部件修订按钮可见性配置
 * @author gyang
 *
 */
public class PartReviseActionFilter extends DefaultSimpleValidationFilter{

	private static String clazz = PartReviseActionFilter.class.getName() ;
	private static final Logger LOGGER = LogR.getLogger(clazz);
	@Override
	public UIValidationStatus preValidateAction(UIValidationKey uivalidationkey,UIValidationCriteria uivalidationcriteria) {
		UIValidationStatus uivalidationstatus = UIValidationStatus.ENABLED;
		WTReference wtreference = uivalidationcriteria.getContextObject();
		if (wtreference != null) {
			Persistable persistable = wtreference.getObject();
			try {
				Map<String, MenuBean> menuMap = ChangeRuleXMLConfigUtil.getInstance().getMenuMap();	//获取配置文件中的内容
				MenuBean menuBean = menuMap.get(ChangeConstant.NODE_MENU_PART);
				if ( persistable != null && persistable instanceof WTPart) {
					WTPart wtpart = (WTPart) persistable;	
					LOGGER.debug("part menuBean : = " + menuBean );
					boolean result = false;
					if(menuBean == null){
						result = true;
					}else{
						ChangeBusinessRuleUtil changeBusinessRuleUtil = new ChangeBusinessRuleUtil(menuBean);
						result = changeBusinessRuleUtil.showReviseMenu(wtpart) ;	//判断按钮是否可见
					}
					if ( result ) {
						uivalidationstatus =  UIValidationStatus.ENABLED;
					} else {
						uivalidationstatus = UIValidationStatus.HIDDEN;
					}
				}else if(persistable != null && persistable instanceof WTDocument){
					if(PartDocHelper.isReferenceDocument((WTDocument) persistable)){
						List<String> partStateList = menuBean.getStateList();
						if(partStateList.contains("ALL")){
							uivalidationstatus =  UIValidationStatus.ENABLED;
						}else{
							String lifecycleState = ((WTDocument) persistable).getLifeCycleState().toString();
							if ((lifecycleState != null) && (partStateList.contains(lifecycleState))) {
								uivalidationstatus =  UIValidationStatus.ENABLED;
							}else{
								uivalidationstatus = UIValidationStatus.HIDDEN;
							}
						}
					}else{
						uivalidationstatus = UIValidationStatus.HIDDEN;
					}
				}					
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return uivalidationstatus;
	}

}
