package ext.appo.part.dataUtilities;
import com.ptc.carambola.rendering.HTMLComponent;
import com.ptc.core.components.descriptor.ModelContext;
import com.ptc.core.components.factory.AbstractDataUtility;
import com.ptc.core.components.factory.dataUtilities.AttributeDataUtilityHelper;
import com.ptc.core.components.rendering.guicomponents.*;
import com.ptc.windchill.linkeddata.linkedDataResource;
import com.ptc.windchill.suma.supplier.Manufacturer;

import ext.appo.ecn.pdf.PdfUtil;
import ext.appo.part.util.EffecitveBaselineUtil;
import ext.pi.core.PIAttributeHelper;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import wt.fc.IconDelegate;
import wt.fc.IconDelegateFactory;
import wt.fc.WTObject;
import wt.lifecycle.State;
import wt.log4j.LogR;
import wt.part.QuantityUnit;
import wt.part.WTPart;
import wt.part.WTPartAlternateLink;
import wt.part.WTPartMaster;
import wt.part.WTPartSubstituteLink;
import wt.part.WTPartUsageLink;
import wt.session.SessionHelper;
import wt.util.IconSelector;
import wt.util.WTException;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class BaseLineDataUtility extends AbstractDataUtility {

	private static final Logger LOGGER = LogR.getLogger(BaseLineDataUtility.class.getName());
	private static final String ICON_DEFAULT = "netmarkets/images/part.gif";

	public Object getDataValue(String componentId, Object datum, ModelContext mc) throws WTException {


		System.out.println("+++++++BaseLineDataUtility++++++++++");
		System.out.println("+++++++datum++++++++++"+datum);


		GUIComponentArray components = new GUIComponentArray();
		String[] arry = datum.toString().split("___");
		System.out.println("arry==============================="+arry.toString());
		System.out.println(arry.toString());
		WTPart part = (WTPart)EffecitveBaselineUtil.getObjectByOid(arry[4]);
		String partVersion = part.getVersionInfo().getIdentifier().getValue();
	    String partIteration = part.getIterationInfo().getIdentifier().getValue();        
	    State s = State.toState(arry[11]);
	    String partState = s.getDisplay(SessionHelper.getLocale());
	    String usageLinkOid = arry[10].toString();
	    System.out.println("usageLinkOid========================="+usageLinkOid);
		String upperPartNumber = "";
	  
//		if(arry[14] == null && arry[15] == null) {
			
				if (arry[2].toString().equals("1")){
					if (StringUtils.equals(componentId, "partNo")) {
						
						IconComponent icon = getIcon(part);
						icon.setId(componentId + "_icon");
						components.addGUIComponent(icon);

						TextDisplayComponent text = new TextDisplayComponent(componentId+"_text", part.getNumber());
						components.addGUIComponent(text);
						
						 return components;
					}else if(StringUtils.equals(componentId, "partName")) {
						 return part.getName();
					}else if(StringUtils.equals(componentId, "substitutePartNumber")) {
						
						String substitutePartNumber = "";
						if(arry[14] == null || arry[14] == "" || arry[14].contains("null")) {
							return "";
						}else {
							String[] subArry = arry[14].split("\\,");
							for(int j=0;j<subArry.length;j++) {
		            			String number = subArry[j].toString();
		            			WTPart subpart = EffecitveBaselineUtil.getPartByNumberAndVersion(number);
		            			if(subpart != null) {
		            				substitutePartNumber += subpart.getNumber()+","+subpart.getName()+";";
		            			}
		            		}
						}
						
						return substitutePartNumber;
					}else if(StringUtils.equals(componentId, "overallPartNumber")) {
						String overallPartNumber = "";
						if(arry[15] == null || arry[15] == "" || arry[15].contains("null")) {
							return "";
						}else {
							String[] aftArry = arry[15].split("\\,");
		            		for(int j=0;j<aftArry.length;j++) {
		            			String number = aftArry[j].toString();
		            			WTPart aftpart = EffecitveBaselineUtil.getPartByNumberAndVersion(number);
		            			if(aftpart != null) {
		            				overallPartNumber	+= aftpart.getNumber()+","+aftpart.getName()+";";
		            			}
		            		}
						}
						
						return overallPartNumber;
					}else if(StringUtils.equals(componentId, "partVersion")) {
						 return partVersion+"."+partIteration+" ("+part.getViewName()+")";
					}else if(StringUtils.equals(componentId, "state")) {
						 return partState;
					}else if(StringUtils.equals(componentId, "number")) {
						 return "";
					}else if(StringUtils.equals(componentId, "unit")) {
						 return "";
					}else if(StringUtils.equals(componentId, "placeNumber")) {
					     return "";
					}else if(StringUtils.equals(componentId, "zdckdj")) {
						 return "";
					}else if(StringUtils.equals(componentId, "BOMRemarkInfo")) {
						return "";
					}else if(StringUtils.equals(componentId, "createTime")) {
					    System.out.println("printingTime============================"+arry[13]);
					    String printingTime = arry[13].toString();
						return printingTime;
					}else if(StringUtils.equals(componentId, "upperPartNumber")) {
						WTPartUsageLink usageLink = (WTPartUsageLink)EffecitveBaselineUtil.getObjectByOid(usageLinkOid);
						if(usageLink!=null) {
						   if(usageLink != null){
							   WTPart usedBy = usageLink.getUsedBy();
							   String usedByOid =EffecitveBaselineUtil.getOidByObject(usedBy);
							   if(usedBy != null) {
								   	upperPartNumber = usedBy.getNumber();
								   	String url = "<a href=\"javascript:window.open('app/#ptc1/tcomp/infoPage?oid=" + usedByOid + "');void(0);\">" + upperPartNumber + "</a>";
					        		HTMLComponent htmlComponent = new HTMLComponent(url);
					        		return htmlComponent;
							   }
						   }
						}
						   return "";
		//				   return upperPartNumber;
					}else if(StringUtils.equals(componentId, "upperPartVersion")) {
						String upperPartVersion = "";
						WTPartUsageLink usageLink = (WTPartUsageLink)EffecitveBaselineUtil.getObjectByOid(usageLinkOid);
						if(usageLink!=null) {
						 	if(usageLink != null){
						 		WTPart usedBy = usageLink.getUsedBy();
						 		if(usedBy != null) {
						 			String upperVersion = usedBy.getVersionInfo().getIdentifier().getValue();
								  	String upperIteration = usedBy.getIterationInfo().getIdentifier().getValue();  
								  		upperPartVersion = upperVersion+"."+upperIteration;
							   }
						   }
						}
						 return upperPartVersion;
					}
				}else{
					WTPartUsageLink  link=(WTPartUsageLink)EffecitveBaselineUtil.getObjectByOid(arry[10]);
					Double amount = link.getQuantity().getAmount();
		        	QuantityUnit unit =   link.getQuantity().getUnit();
					if (StringUtils.equals(componentId, "partNo")) {
						String url = "<a href=\"javascript:window.open('app/#ptc1/tcomp/infoPage?oid=" + arry[4] + "');void(0);\">" + part.getNumber() + "</a>";
						IconComponent icon = getIcon(part);
						icon.setId(componentId + "_icon");
						icon.addJsAction("onclick", url);
						components.addGUIComponent(icon);

//		        		HTMLComponent htmlComponent = new HTMLComponent(url);
						TextDisplayComponent text = new TextDisplayComponent(componentId+"_text", part.getNumber());
						components.addGUIComponent(text);
		        		return components;
		//				 return part.getNumber();
					}else if(StringUtils.equals(componentId, "partName")) {
						    //注释代码用于做链接，把htmlComponent作为返回值即可
//							String url = "<a href=\"javascript:window.open('app/#ptc1/tcomp/infoPage?oid=" + arry[4] + "');void(0);\">" + part.getName() + "</a>";
//							HTMLComponent htmlComponent = new HTMLComponent(url);
						 return part.getName();
					}else if(StringUtils.equals(componentId, "substitutePartNumber")) {
						
						String substitutePartNumber = "";
						if(arry[14] == null || arry[14] == "" || arry[14].contains("null")) {
							return "";
						}else {
							String[] subArry = arry[14].split("\\,");
							for(int j=0;j<subArry.length;j++) {
		            			String number = subArry[j].toString();
		            			WTPart subpart = EffecitveBaselineUtil.getPartByNumberAndVersion(number);
		            			if(subpart != null) {
		            				substitutePartNumber += subpart.getNumber()+","+subpart.getName()+";";
		            			}
		            		}
						}
						
						return substitutePartNumber;
					}else if(StringUtils.equals(componentId, "overallPartNumber")) {
						String overallPartNumber = "";
						if(arry[15] == null || arry[15] == "" || arry[15].contains("null")) {
							return "";
						}else {
							String[] aftArry = arry[15].split("\\,");
		            		for(int j=0;j<aftArry.length;j++) {
		            			String number = aftArry[j].toString();
		            			WTPart aftpart = EffecitveBaselineUtil.getPartByNumberAndVersion(number);
		            			if(aftpart != null) {
		            				overallPartNumber	+= aftpart.getNumber()+","+aftpart.getName()+";";
		            			}
		            		}
						}
						
						return overallPartNumber;
					}else if(StringUtils.equals(componentId, "partVersion")) {
						 return partVersion+"."+partIteration+" ("+part.getViewName()+")";
					}else if(StringUtils.equals(componentId, "state")) {
					 	 return partState;
					}else if(StringUtils.equals(componentId, "number")) {
						 return  String.valueOf(amount) == null ? "" :   String.valueOf(amount);
					}else if(StringUtils.equals(componentId, "unit")) {
						 return String.valueOf(unit.getDisplay(Locale.CHINA)) == null ? "" :   String.valueOf(unit.getDisplay(Locale.CHINA));
					}else if(StringUtils.equals(componentId, "placeNumber")) {
						 String placeNumber = EffecitveBaselineUtil.getReferenceDesignatorSet(link);
						 return placeNumber == null ? "" :   placeNumber;
					}else if(StringUtils.equals(componentId, "zdckdj")) {
						String stockGrade = (String)PdfUtil.getIBAObjectValue(link, "stockGrade");
						return stockGrade == null ? "" :   stockGrade;
					}else if(StringUtils.equals(componentId, "BOMRemarkInfo")) {
						String bomNote = (String)PdfUtil.getIBAObjectValue(link, "bom_note");
						return bomNote == null ? "" :  bomNote;
					}else if(StringUtils.equals(componentId, "createTime")) {
						 System.out.println("printingTime============================"+arry[13]);
						 String printingTime = arry[13].toString();
						 return printingTime;
					}else if(StringUtils.equals(componentId, "upperPartNumber")) {
						return "";
					}else if(StringUtils.equals(componentId, "upperPartVersion")) {
						return "";
					}
				}
//		}
//		if(arry[14] != null) {
//					WTPartSubstituteLink sublink =  (WTPartSubstituteLink)EffecitveBaselineUtil.getObjectByOid(arry[14]);
//				if(sublink!= null){
//					WTPartMaster sm = sublink.getSubstitutes();
//					if (StringUtils.equals(componentId, "partNo")) {
//						return "";
//					}else if(StringUtils.equals(componentId, "partName")) {
//						return "";
//					}else if(StringUtils.equals(componentId, "substitutePartNumber")) {
//						
//						return sm.getNumber();
//					}else if(StringUtils.equals(componentId, "substituteType")) {
//						return "局部替代部件";
//					}else if(StringUtils.equals(componentId, "partVersion")) {
//						return "";
//					}else if(StringUtils.equals(componentId, "state")) {
//						return "";
//					}else if(StringUtils.equals(componentId, "number")) {
//						return "";
//					}else if(StringUtils.equals(componentId, "unit")) {
//						return "";
//					}else if(StringUtils.equals(componentId, "placeNumber")) {
//						return "";
//					}else if(StringUtils.equals(componentId, "zdckdj")) {
//						return "";
//					}else if(StringUtils.equals(componentId, "BOMRemarkInfo")) {
//						return "";
//					}else if(StringUtils.equals(componentId, "createTime")) {
//						return "";
//					}else if(StringUtils.equals(componentId, "upperPartNumber")) {
//						return "";
//					}else if(StringUtils.equals(componentId, "upperPartVersion")) {
//						return "";
//					}
//				}
//		}
//		if(arry[15] != null) {
//			WTPartAlternateLink sublink =  (WTPartAlternateLink)EffecitveBaselineUtil.getObjectByOid(arry[15]);
//			WTPartMaster sm = sublink.getAlternates();
//			if (StringUtils.equals(componentId, "partNo")) {
//				return "";
//			}else if(StringUtils.equals(componentId, "partName")) {
//				return "";
//			}else if(StringUtils.equals(componentId, "substitutePartNumber")) {
//				
//				return sm.getNumber();
//			}else if(StringUtils.equals(componentId, "substituteType")) {
//				return "全局替代部件";
//			}else if(StringUtils.equals(componentId, "partVersion")) {
//				return "";
//			}else if(StringUtils.equals(componentId, "state")) {
//				return "";
//			}else if(StringUtils.equals(componentId, "number")) {
//				return "";
//			}else if(StringUtils.equals(componentId, "unit")) {
//				return "";
//			}else if(StringUtils.equals(componentId, "placeNumber")) {
//				return "";
//			}else if(StringUtils.equals(componentId, "zdckdj")) {
//				return "";
//			}else if(StringUtils.equals(componentId, "BOMRemarkInfo")) {
//				return "";
//			}else if(StringUtils.equals(componentId, "createTime")) {
//				return "";
//			}else if(StringUtils.equals(componentId, "upperPartNumber")) {
//				return "";
//			}else if(StringUtils.equals(componentId, "upperPartVersion")) {
//				return "";
//			}
//		}

		return "";
	}
	
	/**
	 * Get the Icon from obj
	 * 
	 * @param obj
	 * @return
	 * @throws WTException
	 */
	private IconComponent getIcon(WTObject obj) {
		LOGGER.debug("----------------------[getIcon]");
		String imgURL = ICON_DEFAULT;
		try {
			IconDelegate delegate = IconDelegateFactory.getInstance().getIconDelegate(obj);
			IconSelector selector = delegate.getStandardIconSelector();
			while (!selector.isResourceKey()) {
				delegate = delegate.resolveSelector(selector);
				selector = delegate.getStandardIconSelector();
			}
			imgURL = selector.getIconKey();
			LOGGER.debug("imgURL is " + imgURL);
		} catch (Exception e) {
			LOGGER.error("getIcon failed!", e);
		}
		return new IconComponent(imgURL);
	}
	

}
