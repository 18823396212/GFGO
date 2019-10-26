package ext.appo.part.util;

import com.ptc.core.foundation.container.common.FdnWTContainerHelper;
import com.ptc.core.meta.common.TypeIdentifier;
import com.ptc.core.meta.type.mgmt.server.impl.WTTypeDefinition;
import com.ptc.windchill.enterprise.copy.server.CoreMetaUtility;
import com.ptc.wpcfg.generic.GenericUtil;

import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.method.RemoteMethodServer;
import wt.org.WTUser;
import wt.part.WTPart;
import wt.query.KeywordExpression;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.session.SessionHelper;
import wt.util.WTException;
import wt.vc.views.View;
import wt.vc.views.ViewHelper;

/**
 * Services和Package part的工具包
 * 
 * @author jiafengzhou
 * 
 */
public class PartUtil implements wt.method.RemoteAccess {

	/**
	 * 根据来源视图新建目标视图（目标视图必须是来源视图的子类）
	 * 
	 * @param num
	 * @param sourceView
	 * @param targetView
	 * @return
	 * @throws Exception
	 * 
	 */
	public static WTPart createTargetViewPartFromSourceViewPart(String num,
			String sourceView, String targetView) throws Exception {


		WTPart sourcePart =getPart(num, "", sourceView);

		if (sourcePart == null) {
			throw new WTException("系统不存在视图为" + sourceView + "编号为" + num + "的部件");
		}

		WTPart targetPart = getPart(num, "", targetView);
		if (targetPart != null) {
			throw new WTException("系统已存在视图为" + targetView + "编号为" + num
					+ "的部件，不能再转视图");
		} else {
			targetPart = (WTPart) ViewHelper.service.newBranchForView(
					sourcePart, targetView);
			System.out.println(" now  create  product-" + num + " view is "
					+ targetView);
		}
		PersistenceHelper.manager.store(targetPart);
		return targetPart;
	}

	
	public static WTPart getPart(String num, String ver, String viewName)
			throws Exception {
		if (viewName.equals(""))
			viewName = "Design";
		View view = ViewHelper.service.getView(viewName);
		WTPart part = null;
		QuerySpec qs = new QuerySpec(WTPart.class);
		SearchCondition sc = new SearchCondition(WTPart.class, WTPart.NUMBER,
				SearchCondition.EQUAL, num);
		qs.appendWhere(sc);
		sc = new SearchCondition(WTPart.class, "view.key.id",
				SearchCondition.EQUAL, view.getPersistInfo()
						.getObjectIdentifier().getId());
		qs.appendAnd();
		qs.appendWhere(sc);
		if (!ver.equals("")) {
			qs.appendAnd();
			qs.appendWhere(new SearchCondition(new KeywordExpression(
					"A0.versionIdA2versionInfo"), SearchCondition.EQUAL,
					new KeywordExpression("'" + ver + "'")));
		}
		qs.appendAnd();
		qs.appendWhere(new SearchCondition(new KeywordExpression(
				"A0.latestiterationInfo"), SearchCondition.EQUAL,
				new KeywordExpression("1")));
		qs.appendOrderBy(WTPart.class, "thePersistInfo.createStamp", true);
		QueryResult qr = PersistenceHelper.manager.find(qs);
		if (qr.hasMoreElements())
			part = (WTPart) qr.nextElement();

		return part;
	}
	/**
	 * @param part
	 * @param softType
	 *            "wt.part.WTPart|com.weichai.ServicePart" 或者
	 *            "wt.part.WTPart|com.weichai.SparePart"
	 * @return WTPart
	 * @throws WTException
	 *             Time 2016-6-21 下午3:19:12 Author jiafengzhou
	 * 
	 */
	protected static WTPart changeWTPartSoftType(WTPart part, String softType)
			throws WTException {
		WTUser user = (WTUser) SessionHelper.manager.getPrincipal();
		SessionHelper.manager.setAdministrator();
		WTTypeDefinition definition = null;

		if (!RemoteMethodServer.ServerFlag) {
			try {
				RemoteMethodServer.getDefault().invoke("changeWTPartSoftType",
						PartUtil.class.getName(), null,
						new Class[] { WTPart.class, String.class },
						new Object[] { part, softType });
			} catch (Exception e) {

			}
		} else {
			TypeIdentifier typeidentifier = FdnWTContainerHelper
					.toTypeIdentifier(softType);
			part = (WTPart) CoreMetaUtility.setType(part, typeidentifier);

		}
		SessionHelper.manager.setPrincipal(user.getName());
		return part;
	}


}
