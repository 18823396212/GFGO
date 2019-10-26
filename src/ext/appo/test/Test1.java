package ext.appo.test;

import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.util.Enumeration;

import ext.appo.util.PartUtil;
import wt.fc.ObjectReference;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.fc.collections.WTCollection;
import wt.folder.Folder;
import wt.folder.FolderEntry;
import wt.folder.FolderHelper;
import wt.httpgw.GatewayAuthenticator;
import wt.inf.container.WTContainer;
import wt.inf.container.WTContainerRef;
import wt.method.RemoteMethodServer;
import wt.org.OrganizationServicesHelper;
import wt.org.WTUser;
import wt.part.Source;
import wt.part.WTPart;
import wt.part.WTPartHelper;
import wt.part.WTPartMaster;
import wt.part.WTPartSubstituteLink;
import wt.query.CompositeWhereExpression;
import wt.query.ConstantExpression;
import wt.query.LogicalOperator;
import wt.query.QueryException;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.query.TableColumn;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;
import wt.vc.views.ViewHelper;
import wt.vc.views.ViewReference;

public class Test1 implements wt.method.RemoteAccess {

	private static String lenovoPart_type = "wt.part.WTPart";

	private static String PartPath = "Default/02结构件/零部件";

	/**
	 * ����һ���µĲ���
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		RemoteMethodServer rms = RemoteMethodServer.getDefault();
		GatewayAuthenticator auth = new GatewayAuthenticator();
		auth.setRemoteUser("wcadmin");
		rms.setAuthenticator(auth);

		String Number = null;

		for (int i = 0; i < args.length; i += 2) {
			if ("-num1".equals(args[i])) {
				Number = args[i + 1];
			}
		}
		// WTPart part = RemoteMethod("test20190227", lenovoPart_type);

		// newPart("test20190227");
		// PartUtil.createTargetViewPartFromSourceViewPart("C55030000100",
		// "Design", "Manufacturing");
		// WTContainer container= getContainer("原材料库");
		// WTPrincipal wtprincipal = getUserFromName("00823");
		// System.out.println("name=="+wtprincipal.getName());
		// Vector<WTPrincipal> principalVector=new Vector();
		// principalVector.addElement(wtprincipal);
		// AppoContainerTeamExecutor teamExecutor =new
		// AppoContainerTeamExecutor((ContainerTeamManaged) container,
		// "project_manager", principalVector, "ADD");
		// teamExecutor.doExecute();
		System.out.println("Number===" + Number);
		WTPart wtPart = PartUtil.getLastestWTPartByNumber(Number);
		System.out.println("wtPart===" + wtPart);
		WTCollection collection = WTPartHelper.service.getSubstituteLinksAnyAssembly(wtPart.getMaster());
		for (Object object : collection) {
			if (object instanceof ObjectReference) {
				object = ((ObjectReference) object).getObject();
			}
			if (object instanceof WTPartSubstituteLink) {
				WTPartSubstituteLink usage = (WTPartSubstituteLink) object;
				WTPartMaster partmasterObj = (WTPartMaster) usage.getRoleBObject();
				// PersistenceHelper.manager.delete(usage);
				System.out.println("usage===" + usage);
				System.out.println("partmasterObj===" + partmasterObj.getNumber());

			}
		}

	}

	public static WTPart RemoteMethod(String partNumber, String partType)
			throws RemoteException, InvocationTargetException {
		String CLASSNAME = (Test1.class).getName();
		Class argTypes[];
		Object svrArgs[];
		argTypes = (new Class[] { String.class, String.class });
		svrArgs = (new Object[] { partNumber, partType });
		return (WTPart) RemoteMethodServer.getDefault().invoke("newPart", CLASSNAME, null, argTypes, svrArgs);
	}

	public static WTUser getUserFromName(String name) throws WTException {
		WTUser user = null;
		if ("".equals(name) || null == name) {
			throw new WTException("用户名不能为null");
		}
		Enumeration enumUser = OrganizationServicesHelper.manager.findUser(WTUser.NAME, name);

		if (enumUser.hasMoreElements())
			user = (WTUser) enumUser.nextElement();
		if (user == null) {
			enumUser = OrganizationServicesHelper.manager.findUser(WTUser.FULL_NAME, name);
			if (enumUser.hasMoreElements())
				user = (WTUser) enumUser.nextElement();
		}
		if (user == null) {
			throw new WTException("系统中不存在用户名为'" + name + "'的用户！");
		}
		return user;
	}

	/**
	 * ����һ���µĲ��������ƵĴ������ʵ���޸�part�Ļ�����
	 * 
	 * @param number
	 *            �����ı���
	 * @param PartType
	 *            ���������ͣ�������windchill�п����Զ���ܶ�������
	 * @return
	 * @throws WTException
	 * @throws WTPropertyVetoException
	 */
	public static WTPart newPart(String number) throws WTException, WTPropertyVetoException {
		WTPart newPart = null;
		String partType = "wt.part.WTPart";
		// ��ȡ����
		WTContainer container = getContainer("原材料库");
		System.out.println(1);
		// ��ȡ����������
		WTContainerRef ref = WTContainerRef.newWTContainerRef(container);

		Folder folder = wt.folder.FolderHelper.service.getFolder(PartPath, ref);
		System.out.println(2);
		String strPartNumber = number;
		String strPartName = number;
		wt.vc.views.View view = ViewHelper.service.getView("Design");
		ViewReference viewRef = ViewReference.newViewReference(view);
		System.out.println(3);

		// ����һ�����������������
		newPart = WTPart.newWTPart(strPartNumber, "shangxuetang.cn");
		// Ϊ������������
		newPart.setContainer(container);
		// 设置‘源’
		Source source = Source.toSource("make");
		newPart.setSource(source);
		// Ϊ��������view
		newPart.setView(viewRef);
		// Ϊ�˲������ô���λ��
		FolderHelper.assignLocation((FolderEntry) newPart, folder);
		newPart = (WTPart) PersistenceHelper.manager.save(newPart);
		// ����ݿ�����ȡpart
		newPart = (WTPart) PersistenceHelper.manager.refresh(newPart);
		WTPart mpPart = (WTPart) ViewHelper.service.newBranchForView(newPart, "Manufacturing");
		// TeamTemplate teamtemplate = (TeamTemplate)
		// TeamHelper.service.getTeamTemplate(newPart.getContainerReference(),
		// "Default");
		// System.out.println("Name:"+teamtemplate.getName());
		// TeamHelper.setTeamTemplate(mpPart, teamtemplate);
		// PersistenceHelper.manager.store(mpPart);
		System.out.println("mpart==" + mpPart.getViewName());
		System.out.println("mpart==" + mpPart.getNumber());
		PersistenceHelper.manager.store(mpPart);

		return newPart;
	}

	/**
	 * 获取所有的特定替代关系
	 * 
	 * @param partMaster
	 * @return
	 */
	public static QueryResult getSubLink(WTPartMaster partMaster) {
		long id2a2 = PersistenceHelper.getObjectIdentifier(partMaster).getId();
		System.out.println("id2a2===" + id2a2);
		try {
			QuerySpec queryspec = new QuerySpec();
			int b = queryspec.appendClassList(WTPartSubstituteLink.class, true);
			queryspec.setAdvancedQueryEnabled(true);
			String[] aliases = new String[1];
			aliases[0] = queryspec.getFromClause().getAliasAt(b);
			TableColumn tc1 = new TableColumn(aliases[0], "IDA3B5");
			CompositeWhereExpression andExpression = new CompositeWhereExpression(LogicalOperator.AND);
			andExpression.append(new SearchCondition(tc1, "=", new ConstantExpression(new Long(id2a2))));
			queryspec.appendWhere(andExpression, null);
			QueryResult qs = PersistenceHelper.manager.find(queryspec);
			if (qs.hasMoreElements()) {
				return qs;
			}

		} catch (QueryException e) {
			e.printStackTrace();
		} catch (WTException e) {
			e.printStackTrace();
		}
		return null;

	}

	/**
	 * ��Ʒ��һ������������ͨ��һ����Ʒ����ƻ�ȡ��������Ʒ�������windchill�о���Ψһ��
	 * 
	 * @param containerName
	 *            ��Ʒ�����
	 * @return
	 */
	public static WTContainer getContainer(String containerName) {

		try {
			QuerySpec qs = new QuerySpec(WTContainer.class);
			SearchCondition sc = new SearchCondition(WTContainer.class, WTContainer.NAME, "=", containerName);
			qs.appendWhere(sc);
			QueryResult qr = PersistenceHelper.manager.find(qs);
			while (qr.hasMoreElements()) {
				WTContainer container = (WTContainer) qr.nextElement();
				return container;
			}
		} catch (QueryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (WTException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

}
