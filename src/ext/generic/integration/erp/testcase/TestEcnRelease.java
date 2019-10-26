/**
 * 测试ECN发布
 * 
 */
package ext.generic.integration.erp.testcase;

import java.rmi.RemoteException;

import wt.change2.WTChangeOrder2;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.method.RemoteAccess;
import wt.method.RemoteMethodServer;
import wt.query.QueryException;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.util.WTException;
import ext.generic.integration.erp.service.ECNReleaseService;

public class TestEcnRelease implements RemoteAccess {
	public static final String CLASSNAME = TestEcnRelease.class.getName();

	public static void testEcnRelease(String number) throws RemoteException {
		WTChangeOrder2 ecn = null;
		
		QuerySpec qs = null;
		QueryResult qr = null;
		try {
			qs = new QuerySpec(WTChangeOrder2.class);
			
			//定义搜索条件，以零部件编号方式在master中搜索
			SearchCondition sc = new SearchCondition(WTChangeOrder2.class, WTChangeOrder2.NUMBER, SearchCondition.EQUAL, number);
			qs.appendSearchCondition(sc);
			qr = PersistenceHelper.manager.find(qs);
			
			if( qr != null ){
				System.out.println("qr size : " + qr.size() ) ;
				
				if ( qr.hasMoreElements() ) {
					ecn = (WTChangeOrder2) qr.nextElement();
					
					if( ecn != null ){
						ECNReleaseService.release(ecn, null, "") ;
					}else{
						System.out.println( "ecn == null") ;
					}
				}
			}else{
				System.out.println( "qr == null") ;
			}
		} catch (QueryException e) {
			e.printStackTrace();
		} catch (WTException e) {
			e.printStackTrace();
		}	
	}

	/**
	 * 参数为：ECN编号
	 * 
	 * @param args
	 * @throws RemoteException 
	 */
	public static void main(String[] args) throws RemoteException {
		if (args == null || args.length != 1 ) {
			System.out.println("请输入ECN编号");

			return;
		}

		// 如果是客户端执行该方法,则通过远程调用接口条用Windchill method server
		if (!RemoteMethodServer.ServerFlag) {
			String methodName = "testEcnRelease";

			try {
				RemoteMethodServer.getDefault().invoke(methodName,CLASSNAME,null,new Class[] { String.class },new Object[] { args[0] });
			} catch (Exception exp) {
				exp.printStackTrace();
			}
		} else {
			testEcnRelease(args[0]);
		}
	}
}
