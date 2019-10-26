package ext.tools;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import kingdee.bos.json.JSONArray;
import kingdee.bos.json.JSONObject;

public class InvokeK3Tools {
	// Cookie ֵ
	public static String CookieVal = null;

	private static String connUrl = "";// 连接字符串
	private static String dbId = "";// 帐套Id
	private static String user = "";// 用户名
	private static String pwd = "";// 密码
	private static String sUrl = "BGJ.K3.CRM.WebApi.Plugin.Service.ERPGetCurrencyJson,BGJ.K3.CRM.WebApi.Plugin.common.kdsvc";

	public static void main(String[] args) throws Exception {

		if (LoginK3()) {
			// 库存
			// String sql111 = "select top 1 * from V_APPO_INVALLQRY_INV ";
			// 在途
			// String sql222 = "select top 1 * from V_APPO_INVALLQRY_POQTY ";
			// 在制
			// String sql333 = "select top 1 * from V_APPO_WIPQTY";
			System.out.println("login success");
			HashMap<String, List<List<String>>> result = queryAllBOM("B90000580020_C");
			// System.out.println(result);
		}
	}

	public static HashMap<String, List<List<String>>> queryAllBOM(String plmbom) throws Exception {
		HashMap<String, List<List<String>>> bomMap = new HashMap<String, List<List<String>>>();
		String sql = "";
		System.out.println("plmbom===" + plmbom);
		if (plmbom != null && plmbom.length() > 0) {
			sql = "SELECT BOM,SUBMATERIAL,ITEMVER,FNUMERATOR,FDENOMINATOR,'' AS FPOSITIONNO FROM V_APPO_BOMCHECK WHERE BOM =  '"
					+ plmbom + "'";
		} else {
			sql = "SELECT TOP 100 BOM,SUBMATERIAL,ITEMVER,FNUMERATOR,FDENOMINATOR,'' AS FPOSITIONNO FROM V_APPO_BOMCHECK   ";
		}
		// String sql = "SELECT
		// BOM,SUBMATERIAL,ITEMVER,FNUMERATOR,FDENOMINATOR,FPOSITIONNO " + "
		// FROM ( "
		// + " SELECT *,ROW_NUMBER() OVER(ORDER BY BOM,SUBMATERIAL) AS rowid
		// FROM V_APPO_BOMCHECK " + " ) T "
		// + " WHERE T.rowid BETWEEN 98000 AND 98500 ";
		// String sql = "SELECT count(*) FROM V_APPO_BOMCHECK ";
		queryAllBOMFenci(bomMap, sql);
		System.out.println("size====" + bomMap.size());
		/**
		 * System.out.println("size====" + bomMap.size());
		 * 
		 * sql =
		 * "SELECT BOM,SUBMATERIAL,ITEMVER,FNUMERATOR,FDENOMINATOR,FPOSITIONNO "
		 * + " FROM		 ( " +
		 * " SELECT *,ROW_NUMBER() OVER(ORDER BY BOM,SUBMATERIAL) AS rowid FROM		 V_APPO_BOMCHECK "
		 * + " ) T " + " WHERE T.rowid BETWEEN 50001 AND 100000 ";
		 * queryAllBOMFenci(bomMap, sql); System.out.println("size====" +
		 * bomMap.size());
		 * 
		 * sql =
		 * "SELECT BOM,SUBMATERIAL,ITEMVER,FNUMERATOR,FDENOMINATOR,FPOSITIONNO "
		 * + " FROM		 ( " +
		 * " SELECT *,ROW_NUMBER() OVER(ORDER BY BOM,SUBMATERIAL) AS rowid FROM		 V_APPO_BOMCHECK "
		 * + " ) T " + " WHERE T.rowid BETWEEN 100001 AND 150000 ";
		 * queryAllBOMFenci(bomMap, sql); System.out.println("size====" +
		 * bomMap.size());
		 * 
		 * sql =
		 * "SELECT BOM,SUBMATERIAL,ITEMVER,FNUMERATOR,FDENOMINATOR,FPOSITIONNO "
		 * + " FROM		 ( " +
		 * " SELECT *,ROW_NUMBER() OVER(ORDER BY BOM,SUBMATERIAL) AS rowid FROM		 V_APPO_BOMCHECK "
		 * + " ) T " + " WHERE T.rowid BETWEEN 150001 AND 200000 ";
		 * queryAllBOMFenci(bomMap, sql); System.out.println("size====" +
		 * bomMap.size());
		 * 
		 * sql =
		 * "SELECT BOM,SUBMATERIAL,ITEMVER,FNUMERATOR,FDENOMINATOR,FPOSITIONNO "
		 * + " FROM		 ( " +
		 * " SELECT *,ROW_NUMBER() OVER(ORDER BY BOM,SUBMATERIAL) AS rowid FROM		 V_APPO_BOMCHECK "
		 * + " ) T " + " WHERE T.rowid BETWEEN 200001 AND 300000 ";
		 * queryAllBOMFenci(bomMap, sql);
		 * 
		 * System.out.println("size====" + bomMap.size());
		 **/
		return bomMap;
	}

	public static HashMap<String, List<List<String>>> queryAllBOMFenci(HashMap<String, List<List<String>>> bomMap,
			String sql) throws Exception {

		JSONArray jParas = new JSONArray();
		jParas.put(sql);
		HttpURLConnection connectionExec = initUrlConn(connUrl, sUrl, jParas.toString());
		BufferedReader reader = new BufferedReader(new InputStreamReader(connectionExec.getInputStream()));
		String line;

		while ((line = reader.readLine()) != null) {
			String sResult = new String(line.getBytes(), "utf-8");
			// System.out.println("sResult===" + sResult);
			if (sResult.contains("true")) {
				String result = sResult.substring(17, sResult.length() - 14).replaceAll("[\b\r\n\t]*", "");
				System.out.println("查询结果：" + result);
				JSONObject jsonObject = new JSONObject(result);
				JSONArray table = jsonObject.getJSONArray("Table");

				for (int i = 0; i < table.length(); i++) {
					List<String> list = new ArrayList<String>();
					JSONObject jsonObject1 = table.getJSONObject(i);
					String BOM = jsonObject1.getString("BOM");
					String SUBMATERIAL = jsonObject1.getString("SUBMATERIAL");
					String ITEMVER = jsonObject1.getString("ITEMVER");
					String FNUMERATOR = jsonObject1.getString("FNUMERATOR");
					String FDENOMINATOR = jsonObject1.getString("FDENOMINATOR");
					// String FPOSITIONNO =
					// jsonObject1.getString("FPOSITIONNO");
					String FPOSITIONNO = "";

					list.add(BOM);
					list.add(SUBMATERIAL);
					list.add(ITEMVER);
					list.add(FNUMERATOR);
					list.add(FDENOMINATOR);
					list.add(FPOSITIONNO);

					List<List<String>> bomList = bomMap.get(BOM);
					if (bomList == null) {
						bomList = new ArrayList<List<String>>();

					}

					bomList.add(list);
					bomMap.put(BOM, bomList);

				}
			}
		}
		reader.close();

		connectionExec.disconnect();

		return bomMap;
	}

	private static List<List<String>> queryAllPartKC() throws Exception {
		List<List<String>> kcList = new ArrayList<List<String>>();

		// String sql = "select top 1 * sum(IQTY) iqty,ItemNum,ITEMVER from
		// V_APPO_INVALLQRY_INV Group By ItemNum,ITEMVER ";

		String sql = "select top 1 IQTY iqty,ItemNum,ITEMVER from V_APPO_INVALLQRY_INV ";
		JSONArray jParas = new JSONArray();
		jParas.put(sql);

		HttpURLConnection connectionExec = initUrlConn(connUrl, sUrl, jParas.toString());

		BufferedReader reader = new BufferedReader(new InputStreamReader(connectionExec.getInputStream()));

		String line;
		while ((line = reader.readLine()) != null) {
			String sResult = new String(line.getBytes(), "utf-8");
			System.out.println("sResult===" + sResult);
			if (sResult.contains("true")) {
				String result = sResult.substring(17, sResult.length() - 14);
				System.out.println("查询结果：" + result);
				JSONObject jsonObject = new JSONObject(result);
				JSONArray table = jsonObject.getJSONArray("Table");

				for (int i = 0; i < table.length(); i++) {
					List list = new ArrayList();
					JSONObject jsonObject1 = table.getJSONObject(i);

					String iqty = jsonObject1.getString("iqty");
					String ItemNum = jsonObject1.getString("ItemNum");
					String ITEMVER = jsonObject1.getString("ITEMVER");

					list.add(iqty);
					list.add(ItemNum);
					list.add(ITEMVER);

					kcList.add(list);
				}
			}
		}
		reader.close();

		connectionExec.disconnect();

		return kcList;
	}

	/**
	 * 生产环境
	 * 
	 * @return
	 * @throws Exception
	 */
	public static boolean LoginK3() throws Exception {
		boolean bResult = false;
		connUrl = "https://appo.ik3cloud.com/K3cloud/";
		dbId = "20190329095706";
		user = "plm1";
		pwd = "abcd1234!@#$";
		bResult = Login(connUrl, dbId, user, pwd, 2052);
		return bResult;
	}

	/**
	 * 测试环境
	 * 
	 * @return
	 */
	public static boolean LoginK3Test() throws Exception {
		boolean bResult = false;
		connUrl = "https://appo.test.ik3cloud.com/K3cloud/";
		dbId = "20190329100129";
		user = "plm1";
		pwd = "abcd1234!@#$";
		bResult = Login(connUrl, dbId, user, pwd, 2052);
		return bResult;
	}

	// Login
	public static boolean Login(String connUrl, String dbId, String user, String pwd, int lang) throws Exception {

		boolean bResult = false;

		String sUrl = "Kingdee.BOS.WebApi.ServicesStub.AuthService.ValidateUser.common.kdsvc";

		JSONArray jParas = new JSONArray();
		jParas.put(dbId);// 帐套Id
		jParas.put(user);// 用户名
		jParas.put(pwd);// 密码
		jParas.put(lang);// 语言

		HttpURLConnection connection = initUrlConn(connUrl, sUrl, jParas.toString());
		// 获取Cookie
		String key = null;
		for (int i = 1; (key = connection.getHeaderFieldKey(i)) != null; i++) {
			if (key.equalsIgnoreCase("Set-Cookie")) {
				String tempCookieVal = connection.getHeaderField(i);
				if (tempCookieVal.startsWith("kdservice-sessionid")) {
					CookieVal = tempCookieVal;
					break;
				}
			}
		}

		BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		String line;

		while ((line = reader.readLine()) != null) {
			String sResult = new String(line.getBytes(), "utf-8");
			bResult = line.contains("\"LoginResultType\":1");
		}
		reader.close();

		connection.disconnect();

		return bResult;
	}

	private static HttpURLConnection initUrlConn(String connUrl, String sUrl, String paras) throws Exception {
		URL postUrl = new URL(connUrl.concat(sUrl));
		HttpURLConnection connection = (HttpURLConnection) postUrl.openConnection();
		if (CookieVal != null) {
			connection.setRequestProperty("Cookie", CookieVal);
		}
		if (!connection.getDoOutput()) {
			connection.setDoOutput(true);
		}
		connection.setRequestMethod("POST");
		connection.setUseCaches(false);
		connection.setInstanceFollowRedirects(true);
		connection.setRequestProperty("Content-Type", "application/json");
		DataOutputStream out = new DataOutputStream(connection.getOutputStream());

		UUID uuid = UUID.randomUUID();
		int hashCode = uuid.toString().hashCode();

		JSONObject jObj = new JSONObject();

		jObj.put("format", 1);
		jObj.put("useragent", "ApiClient");
		jObj.put("rid", hashCode);
		jObj.put("parameters", chinaToUnicode(paras));
		jObj.put("timestamp", new Date().toString());
		jObj.put("v", "1.0");

		out.writeBytes(jObj.toString());

		out.flush();
		out.close();

		return connection;
	}

	/**
	 * 把中文转成Unicode码
	 *
	 * @param str
	 * @return
	 */
	public static String chinaToUnicode(String str) {
		String result = "";
		for (int i = 0; i < str.length(); i++) {
			int chr1 = (char) str.charAt(i);
			if (chr1 >= 19968 && chr1 <= 171941) {// 汉字范围 \u4e00-\u9fa5 (中文)
				result += "\\u" + Integer.toHexString(chr1);
			} else {
				result += str.charAt(i);
			}
		}
		return result;
	}
}
