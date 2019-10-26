package ext.tools;

import java.util.List;

public class InvokeTest {

	public static void main(String[] args) throws Exception {
		// String K3CloudURL = "https://appo.test.ik3cloud.com/K3cloud/";
		String dbId = "20190329100129";
		String uid = "plm1";
		String pwd = "abcd1234!@#$";
		int lang = 2052;
		if (InvokeHelper.Login(dbId, uid, pwd, lang)) {
			// ���
			String sql111 = "select top 1 * from V_APPO_INVALLQRY_INV ";
			// ��;
			String sql222 = "select top 1 *  from V_APPO_INVALLQRY_POQTY ";
			// ����
			String sql333 = "select top 1 * from V_APPO_WIPQTY";

			String s = "select * from \n"
					+ "(select iqty,fqty,poqty,isnull(c.ItemNum,d.ItemNum) as ItemNum,isnull(c.ITEMVER,d.ITEMVER) as ITEMVER from\n"
					+ " (select iqty,fqty,isnull(a.ItemNum,b.FMATNUMBER) as ItemNum,isnull(a.ITEMVER,b.FVERNUMBER) as ITEMVER\n"
					+ " from (select sum(IQTY) iqty,ItemNum,ITEMVER from V_APPO_INVALLQRY_INV  Group By ItemNum,ITEMVER) a\n"
					+ " full join (select sum(FQTY) fqty,FMATNUMBER,FVERNUMBER from V_APPO_WIPQTY Group By FMATNUMBER,FVERNUMBER) b on a.ItemNum=b.FMATNUMBER and a.ITEMVER=b.FVERNUMBER) c \n"
					+ " full join (select sum(POQTY) poqty,ItemNum,ITEMVER from V_APPO_INVALLQRY_POQTY Group By ItemNum,ITEMVER) d on c.ItemNum=d.ItemNum and c.ITEMVER=d.ITEMVER) e";
			List<List<Object>> sResultsResult = InvokeHelper.QueryAll(s);
		}
	}
}
