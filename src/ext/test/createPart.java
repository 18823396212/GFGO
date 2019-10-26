package ext.test;

import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.folder.Folder;
import wt.folder.FolderEntry;
import wt.folder.FolderHelper;
import wt.inf.container.WTContainer;
import wt.inf.container.WTContainerRef;
import wt.inf.library.WTLibrary;
import wt.method.RemoteMethodServer;
import wt.part.WTPart;
import wt.query.QueryException;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.type.TypeDefinitionReference;
import wt.type.TypedUtility;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;
import wt.vc.views.ViewHelper;
import wt.vc.views.ViewReference;
import com.ibm.icu.util.StringTokenizer;

import ext.appo.util.PartUtil;

public class createPart implements wt.method.RemoteAccess {

	private static String lenovoPart_type = "wt.part.WTPart";

	private static String PartPath = "Default/NewPart";

	/**
	 * ����һ���µĲ���
	 * 
	 * @param args
	 * @throws WTException
	 * @throws WTPropertyVetoException
	 * @throws InvocationTargetException 
	 * @throws RemoteException 
	 */
	public static void main(String[] args) throws WTPropertyVetoException,
			WTException, RemoteException, InvocationTargetException {
		// TODO Auto-generated method stub
		
		WTPart part = RemoteMethod("D99000000211", lenovoPart_type);
		if (part != null) {
			System.out.println("part 部件名称:" + part.getName());
			System.out.println("part 部件版本:" + part.getVersionDisplayIdentity().getDisplayIdentifier().toString());
		}
		newPart("D99000000211");
	}
	
public static WTPart RemoteMethod(String partNumber, String partType)
			throws RemoteException, InvocationTargetException {
		String CLASSNAME = (createPart.class).getName();
		Class argTypes[];
		Object svrArgs[];
		argTypes = (new Class[] { String.class, String.class });
		svrArgs = (new Object[] { partNumber, partType });
		return (WTPart) RemoteMethodServer.getDefault().invoke("newPart",
				CLASSNAME, null, argTypes, svrArgs);
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
	public static WTPart newPart(String number)
			throws WTException, WTPropertyVetoException {
		WTPart newPart = null;
		newPart=PartUtil.getLastestWTPartByNumber(number);
//		String partType="wt.part.WTPart|cn.com.invt.000test";
//		// ��ȡ����
//		WTContainer container = getContainer("testproduct");
//		System.out.println(1);
//		// ��ȡ����������
//		WTContainerRef ref = ref = WTContainerRef.newWTContainerRef(container);
//
//		Folder folder = wt.folder.FolderHelper.service.getFolder(PartPath, ref);
//		System.out.println(2);
//		String strPartNumber = number;
//		String strPartName = number;
//		wt.vc.views.View view = ViewHelper.service.getView("Design");
//		ViewReference viewRef = ViewReference.newViewReference(view);
//		System.out.println(3);
//		TypeDefinitionReference typeDefinitionRef = TypedUtility
//				.getTypeDefinitionReference(partType);
//		System.out.println(4);
//		// ����һ�����������������
//		newPart = WTPart.newWTPart(strPartNumber, "shangxuetang.cn");
//		// Ϊ������������
//		newPart.setContainer(container);
//		// Ϊ������������
//		newPart.setTypeDefinitionReference(typeDefinitionRef);
//		// Ϊ��������view
//		newPart.setView(viewRef);
//		// Ϊ�˲������ô���λ��
//		FolderHelper.assignLocation((FolderEntry) newPart, folder);
//		newPart = (WTPart) PersistenceHelper.manager.save(newPart);
//		// ����ݿ�����ȡpart
		newPart = (WTPart) PersistenceHelper.manager.delete(newPart);
		return newPart;
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
			SearchCondition sc = new SearchCondition(WTContainer.class,
					WTContainer.NAME, "=", containerName);
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
