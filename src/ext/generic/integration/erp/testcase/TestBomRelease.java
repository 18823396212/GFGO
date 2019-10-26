/**
 * 测试BOM发布
 * 
 */
package ext.generic.integration.erp.testcase;

import java.rmi.RemoteException;

import wt.identity.IdentityFactory;
import wt.method.RemoteAccess;
import wt.method.RemoteMethodServer;
import wt.part.WTPart;
import wt.util.WTException;
import ext.com.core.CoreUtil;
import ext.generic.integration.erp.service.BOMReleaseService;

public class TestBomRelease implements RemoteAccess {
	public static final String CLASSNAME = TestBomRelease.class.getName();

	public static void testBomRelease(String number, String view, String type) throws RemoteException {
		StringBuilder erpErrorMessageBuilder = new StringBuilder();
		erpErrorMessageBuilder.append("");
		try {
			WTPart part = CoreUtil.getWTPartByNumberAndView(number, view);

			if (part == null) {
				System.out.println("不能获取零部件,编号为：" + number + "，视图为：" + view);

				return;
			}

			String partViewName = part.getViewName();
			if (partViewName != null && (!partViewName.equals(view))) {
				System.out.println("不能获取对应视图的零部件,编号为：" + number + "，视图为："
						+ view);
				System.out.println("不能获取的零部件为："
						+ IdentityFactory.getDisplayIdentifier(part));

				return;
			}

			if( type != null && type.equalsIgnoreCase("a")){
				
				BOMReleaseService.releaseAllLevel(part, null, null) ;
				
			}else if( type != null && type.equalsIgnoreCase("s")){
				
				BOMReleaseService.releaseSingleLevel(part, "", null,null);
				
			}
		} catch (WTException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 参数为：零部件编号，零部件视图,发布类型：S表示单层，A表示整个BOM
	 * 
	 * @param args
	 * @throws RemoteException 
	 */
	public static void main(String[] args) throws RemoteException {
		if (args == null || args.length != 3) {
			System.out.println("请输入零部件编号和视图，以及发布类型：S表示单层，A表示整个BOM");

			return;
		}

		// 如果是客户端执行该方法,则通过远程调用接口条用Windchill method server
		if (!RemoteMethodServer.ServerFlag) {
			String methodName = "testBomRelease";

			try {
				RemoteMethodServer.getDefault()
						.invoke(methodName,
								CLASSNAME,
								null,
								new Class[] { String.class, String.class,
										String.class },
								new Object[] { args[0], args[1], args[2] });
			} catch (Exception exp) {
				exp.printStackTrace();
			}
		} else {
			testBomRelease(args[0], args[1] , args[2] );
		}
	}
}
