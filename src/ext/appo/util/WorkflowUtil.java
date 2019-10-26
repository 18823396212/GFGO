package ext.appo.util;

import java.util.Enumeration;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import com.ptc.core.meta.common.TypeIdentifier;
import com.ptc.core.meta.server.TypeIdentifierUtility;
import com.ptc.windchill.enterprise.part.commands.PartDocServiceCommand;

import wt.doc.WTDocument;
import wt.epm.EPMDocument;
import wt.fc.ObjectVector;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.lifecycle.LifeCycleException;
import wt.lifecycle.LifeCycleHelper;
import wt.lifecycle.LifeCycleManaged;
import wt.lifecycle.State;
import wt.maturity.MaturityException;
import wt.maturity.MaturityHelper;
import wt.maturity.Promotable;
import wt.maturity.PromotionNotice;
import wt.method.RemoteMethodServer;
import wt.part.WTPart;
import wt.query.QueryException;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.team.WTActorRoleHolder2;
import wt.team.WTRoleHolder2;
import wt.util.WTException;
import wt.util.WTInvalidParameterException;
import wt.vc.wip.Workable;

public class WorkflowUtil {

	
	private static Logger log=Logger.getLogger(WorkflowUtil.class.getName());
	
	/**
	 * @param args
	 * @throws WTException 
	 */
	public static void main(String[] args) throws WTException {
		// TODO Auto-generated method stub
		System.out.println("test start-------->");
	    String userName = null;
		String passWord = null;
		String partnumber = null;
		String state = null;
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
				}
				 else if ("-n".equals(args[i]))
					{
						partnumber = args[i + 1];
					}
				 else if ("-s".equals(args[i]))
					{
						state = args[i + 1];
					}
			}
		}

		if (userName == null)
			userName = "wcadmin";
		if (passWord == null)
			passWord = "plm";
		RemoteMethodServer server = RemoteMethodServer.getDefault();
		server.setUserName(userName);
		server.setPassword(passWord);
		PromotionNotice pn=getPromotionNotice(partnumber);
		changObjectState(pn, state);
	}
	public static PromotionNotice getPromotionNotice(String number)
	{
		PromotionNotice pn=null;
		if(number==null||number.length()==0)
		{
			return null;
		}
		try {
			QuerySpec spec=new QuerySpec(PromotionNotice.class);
			SearchCondition pnNOCondition=new SearchCondition(PromotionNotice.class,PromotionNotice.NUMBER,SearchCondition.EQUAL,number.toUpperCase());
			spec.appendWhere(pnNOCondition);
			try {
				QueryResult qr=PersistenceHelper.manager.find(spec);
				while(qr.hasMoreElements())
				{
			     pn=(PromotionNotice)qr.nextElement();
				}
			} catch (WTException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (QueryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return pn;
	}
	
	public static void changObjectState(PromotionNotice promotionnotice,String state)
	{
		try {
			QueryResult result=getPromotionTargets(promotionnotice);
			if(result.size()>0)
			{
				modifyState(result, state);
			}
		} catch (WTException e) {
			// TODO Auto-generated catch block
			log.debug("get the promotionNotice target failed");
			e.printStackTrace();
		}
	}
	
	//get the Promotion targets
	public static QueryResult getPromotionTargets(
			PromotionNotice promotionnotice) throws WTException {
		QueryResult qr = null;
		try {
			qr = MaturityHelper.service.getPromotionTargets(promotionnotice);
		} catch (MaturityException e) {
			e.printStackTrace();
			throw new MaturityException(e.getLocalizedMessage());
		} catch (WTException e) {
			e.printStackTrace();
			throw new WTException(e.getLocalizedMessage());
		}
		return qr;
	}
	public static void SetPartStateByString(String partsString,String state)
	{
		StringTokenizer parttoken = new StringTokenizer(partsString,",");
		while(parttoken.hasMoreElements()){
            String partnumber=parttoken.nextToken();
            log.debug("send ERP failed part number===="+partnumber);
            WTPart part=PartUtil.getLastestWTPartByNumber(partnumber);
            
            if(null!=part){
	            try {
					LifeCycleHelper.service.setLifeCycleState((LifeCycleManaged) part, State.toState(state));
				} catch (WTInvalidParameterException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (LifeCycleException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (WTException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }
		}
	}
	public static void sendPartState(String partsString,String state)
	{
		if (partsString.length()>0&&state.length()>0) {
			SetPartStateByString(partsString,state);		
		}

	}
	


	
	//modify the promotion targets state
	public static void modifyState(QueryResult result,String state)
	{
		if(result.size()==0)
		{
			log.debug("target objec is null--------");
		}else{
			while(result.hasMoreElements())
			{
				Object object =result.nextElement();
					try {
						LifeCycleHelper.service.setLifeCycleState((LifeCycleManaged) object, State.toState(state));
					} catch (WTInvalidParameterException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (LifeCycleException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (WTException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	
	public static String getUserOnRole(wt.lifecycle.LifeCycleManaged lifecyclemanaged, String inputrolename) {
        log.info("****** WorkflowUtils.getUserOnRole begin  - role: " + inputrolename);

        Object obj = null;
        try {
            obj = wt.team.TeamHelper.service.getTeam(lifecyclemanaged);
        } catch (wt.util.WTException wte) {
            wte.printStackTrace();
        }

        String s = "";
        int count = 0;

        Enumeration<?> enumeration = null;

        try {
            enumeration = ((WTRoleHolder2) obj).getRoles().elements();
        } catch (WTException wtexception) {
            log.error("encountered error trying to get role list", wtexception);
        }

        while (enumeration.hasMoreElements()) {
            wt.project.Role role1 = (wt.project.Role) enumeration.nextElement();

            // only if the role is the one given as input we proceed seeking
            // participants
            if (role1.toString().compareTo(inputrolename) != 0)
                continue;
            log.info("****** checking Role = " + inputrolename);

            if (obj instanceof wt.team.WTActorRoleHolder2) {
                Enumeration<?> enumeration3 = null;
                try {
                    enumeration3 = ((WTActorRoleHolder2) obj).getActorRoleTarget(role1);
                } catch (WTException wtexception2) {
                    log.error("encountered error trying to get actor role participants", wtexception2);
                }
                while (enumeration3.hasMoreElements()) {
                    wt.project.ActorRole actorrole = (wt.project.ActorRole) enumeration3.nextElement();

                    if (count == 0)
                        s = s + actorrole.toString();
                    else
                        s = s + "," + actorrole.toString();
                    count++;

                }
            }
            Enumeration<?> enumeration4 = null;
            try {
                enumeration4 = ((WTRoleHolder2) obj).getPrincipalTarget(role1);
            } catch (WTException wtexception3) {
                log.error("encountered error trying to get role list", wtexception3);
            }
            while (enumeration4 != null && enumeration4.hasMoreElements()) {
                Object obj1 = enumeration4.nextElement();
                if (obj1 != null) {
                    wt.org.WTPrincipalReference wtprincipalreference = (wt.org.WTPrincipalReference) obj1;

                    if (count == 0)
                        s = s + ((wt.org.WTPrincipal) wtprincipalreference.getObject()).getName();
                    else
                        s = s + "," + ((wt.org.WTPrincipal) wtprincipalreference.getObject()).getName();
                    count++;
                }
            }
            log.debug("****** participant on Role = >" + s + "<");
            return s;
        }

        log.debug("****** WorkflowUtils.getUserOnRole return - no user ");
        return "";
    }
	
	
	
	
}
