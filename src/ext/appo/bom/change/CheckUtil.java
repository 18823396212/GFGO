package ext.appo.bom.change;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import wt.change2.Changeable2;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.fc.ReferenceFactory;
import wt.method.RemoteAccess;
import wt.method.RemoteMethodServer;
import wt.occurrence.Occurrence;
import wt.occurrence.OccurrenceHelper;
import wt.part.PartUsesOccurrence;
import wt.part.ReferenceDesignatorSet;
import wt.part.ReferenceDesignatorSetDelegateFactory;
import wt.part.WTPart;
import wt.part.WTPartMaster;
import wt.part.WTPartUsageLink;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.session.SessionHelper;
import wt.util.WTAttributeNameIfc;
import wt.util.WTException;
import wt.vc.config.LatestConfigSpec;
import wt.vc.views.ViewHelper;

import com.ptc.core.logging.Log;
import com.ptc.core.logging.LogFactory;
import com.ptc.core.meta.common.DataTypesUtility;
import com.ptc.core.meta.common.FloatingPoint;

public class CheckUtil implements RemoteAccess
{

	static ReferenceFactory rf = new ReferenceFactory();
	private static Log log = LogFactory.getLog(CheckUtil.class);

	
	public static String getreferenceDesignator(WTPartUsageLink usagelink) throws WTException
	{
		String s2="";
        Vector vector = new Vector();
        vector.add(usagelink);
        Vector vector1 = OccurrenceHelper.service.getPopulatedOccurrenceableLinks(vector);
        WTPartUsageLink wtpartusagelink1 = (WTPartUsageLink)vector1.elementAt(0);
        String s4 = wtpartusagelink1.getQuantity().getUnit().getDisplay();
        if(wtpartusagelink1.getUsesOccurrenceVector() != null)
        {
            Vector vector2 = wtpartusagelink1.getUsesOccurrenceVector();
            ArrayList arraylist = new ArrayList(vector2.size());
            ArrayList arraylist1 = new ArrayList(vector2.size());
            for(int i = 0; i < vector2.size(); i++)
            {
                PartUsesOccurrence partusesoccurrence = (PartUsesOccurrence)vector2.elementAt(i);
                String s5 = partusesoccurrence.getName();
                if(s5 != null)
                    arraylist1.add(s5);
            }

            ReferenceDesignatorSetDelegateFactory referencedesignatorsetdelegatefactory = new ReferenceDesignatorSetDelegateFactory();
            ReferenceDesignatorSet referencedesignatorset = referencedesignatorsetdelegatefactory.get(arraylist1);
            String s6 = referencedesignatorset.getConsolidatedReferenceDesignators();
            if(s6 != null)
                s2 = s6;
        }
        
        return s2;
    }

	
	/**
	 * 根据usagelink——》位号
	 * @param usageLink
	 * @return
	 * @throws WTException
	 */
	public static String getReferenceDesignators(WTPartUsageLink usageLink)
			throws WTException {
		if (usageLink == null) {
			return null;
		}
		QueryResult qr = OccurrenceHelper.service.getUsesOccurrences(usageLink);
		
		List<String> refDesignatorList = new ArrayList<String>(qr.size());
		while(qr.hasMoreElements()){
			Occurrence occurrence = (Occurrence)(qr.nextElement());
			String name = occurrence.getName();
			if (name != null){
				refDesignatorList.add(name);
			}
		}
		ReferenceDesignatorSetDelegateFactory rdsFactory = new ReferenceDesignatorSetDelegateFactory();
		ReferenceDesignatorSet rds = rdsFactory.get(refDesignatorList);
		String consolidatedRefDesignators = rds.getConsolidatedReferenceDesignators();

		return consolidatedRefDesignators;
	}

	public static String getBomQuantity(WTPartUsageLink usagelink) throws WTException
	{
		String Quantity = "";
		FloatingPoint floatingpoint = new FloatingPoint(usagelink.getQuantity().getAmount(), -1);
		String s3 = DataTypesUtility.toString(floatingpoint, SessionHelper.getLocale());
		if (s3 != null && s3.trim().length() > 0)
			Quantity = s3;
		else
			Quantity = "1";
		return Quantity;
	}





	
	public static WTPart getLatestPartByNumber(String partNumber, String view) throws WTException
	{
		if (partNumber == null || partNumber.length() == 0)
			return null;

		QuerySpec qs = new QuerySpec(WTPart.class);
		SearchCondition scNumber = new SearchCondition(WTPart.class, WTPart.NUMBER, SearchCondition.EQUAL, partNumber.toUpperCase());
		SearchCondition scLatestIteration = new SearchCondition(WTPart.class, WTAttributeNameIfc.LATEST_ITERATION, SearchCondition.IS_TRUE);
		qs.appendWhere(scNumber);
		qs.appendAnd();
		qs.appendWhere(scLatestIteration);
		if (view != null && view.length() > 0)
		{
			qs.appendAnd();
			SearchCondition scView = new SearchCondition(WTPart.class, "view.key", SearchCondition.EQUAL, PersistenceHelper.getObjectIdentifier(ViewHelper.service.getView(view)));
			qs.appendWhere(scView);
		}
		QueryResult qr = PersistenceHelper.manager.find(qs);
		if (qr != null && qr.hasMoreElements())
			qr = (new LatestConfigSpec()).process(qr);

		if (qr != null && qr.hasMoreElements())
			return (WTPart) qr.nextElement();
		return null;
	}


	

	
	public static String getPDMBizUnit(String unit)
	{
		for (int i=0;i<erp.length;i++)
		{
			if (unit.equalsIgnoreCase(erp[i]))
			{
				return pdm[i];
			}
		}
		
		return unit;
	}
	
	public static String getERPBizUnit(String unit)
	{
		for (int i=0;i<pdm.length;i++)
		{
			if (unit.equalsIgnoreCase(pdm[i]))
			{
				return erp[i];
			}
		}
		
		return unit;
	}
	
	public static HashMap getAllWTPartMasterID() throws WTException
	{
		
		HashMap hashMap=new HashMap();
		QuerySpec qs = new QuerySpec(WTPartMaster.class);

		QueryResult qr = PersistenceHelper.manager.find(qs);
		
		while(qr.hasMoreElements())
		{
			WTPartMaster wtPartMaster = (WTPartMaster) qr.nextElement();
			
			if(PersistenceHelper.isPersistent(wtPartMaster))
			{
				hashMap.put(String.valueOf(wtPartMaster.getPersistInfo().getObjectIdentifier().getId()),wtPartMaster.getNumber());
			}
		}

		return hashMap;
	}
	
	public static void main(String[] args) throws WTException
	{
		String userName = null;
		String passWord = null;
		String n = null;
		String f = null;

		if (args == null)
			return;
		for (int i = 0; i < args.length; i += 2)
		{
			if (i + 1 < args.length)
			{
				if (("-u".equals(args[i])))
				{
					userName = args[i + 1];
				} else if ("-p".equals(args[i]))
				{
					passWord = args[i + 1];
				} else if ("-n".equals(args[i]))
				{
					n = args[i + 1];
				}else if ("-f".equals(args[i]))
				{
					f = args[i + 1];
				}
			}
		}

		if (userName == null)
			userName = "wcadmin";
		if (passWord == null)
			passWord = "wcadmin";
		RemoteMethodServer server = RemoteMethodServer.getDefault();
		server.setUserName(userName);
		server.setPassword(passWord);
		
		HashMap hm = getAllWTPartMasterID();
		
		System.out.println(hm.get("abcc"));
		String number = (String) hm.get("abcc");
		if (number == null)
		{
			System.out.println("OK");
		} else
		{
			System.out.println(number);
		}
		
//		ShowUtil.PrintHashMaps(hm);
		
		
		String string=getPDMBizUnit("Each");
		String string2=getERPBizUnit("ea");
		System.out.println(string);
		System.out.println(string2);
	}
	
	public static String pdm[]={"ea","m","cm","mm","l","ml","kg","g"};
	public static String erp[]={"Each","m","cm","mm","l","ml","kg","g"};

}
