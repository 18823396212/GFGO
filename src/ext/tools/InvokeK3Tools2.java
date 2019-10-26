package ext.tools;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import kingdee.bos.json.JSONArray;
import kingdee.bos.json.JSONObject;

public class InvokeK3Tools2 {
	// Cookie ֵ
	public static String CookieVal = null;

	private static String connUrl = "";// 连接字符串
	private static String dbId = "";// 帐套Id
	private static String user = "";// 用户名
	private static String pwd = "";// 密码
	private static String sUrl = "BGJ.K3.CRM.WebApi.Plugin.Service.ERPGetCurrencyJson,BGJ.K3.CRM.WebApi.Plugin.common.kdsvc";

	public static void main(String[] args) throws Exception {

		if (LoginK3()) {
			System.out.println("login success");
			List<List<String>> result = queryAllPart();
		}
	}

	public static List<List<String>> queryAllPart() throws Exception {
		List<List<String>> k3PartList = new ArrayList<List<String>>();
		String sql = "select     a.FNumber as '料号',isnull(zz.FAUXPTYNUMBER,'') as '最高版本' "
				+ " from T_BD_MATERIAL a  left join T_BD_MATERIAL_L b on a.FMATERIALID = b.FMATERIALID AND b.FLOCALEID ='2052' "
				+ " left join t_BD_MaterialBase c on a.FMATERIALID = c.FMATERIALID   left join T_BD_UNIT_L e on c.FBASEUNITID = e.funitid and e.FLOCALEID = '2052' "
				+ " left join T_BD_UNIT ff on c.FBASEUNITID = ff.funitid   "
				+ " left join   dbo.T_BD_AUXPTYVALUE yy on a.FMASTERID =yy.FMATERIALID and yy.FMATERIALAUXPROPERTYID ='100001'  "
				+ " left join dbo.T_BD_AUXPTYVALUEENTITY zz on  zz.FAUXPTYVALUEID = yy.FAUXPTYVALUEID   "
				+ " left join   dbo.T_BD_AUXPTYVALUE ww on a.FMASTERID = ww.FMATERIALID and ww.FMATERIALAUXPROPERTYID = '100002' "
				+ " left join dbo.T_BD_AUXPTYVALUEENTITY ee on  ww.FAUXPTYVALUEID = ee.FAUXPTYVALUEID and   ee.FISDEFAULT=1   "
				+ " left join   dbo.T_BD_AUXPTYVALUE dy on a.FMASTERID =dy.FMATERIALID and dy.FMATERIALAUXPROPERTYID ='100001'  "
				+ " left join dbo.T_BD_AUXPTYVALUEENTITY dz on  dz.FAUXPTYVALUEID = dy.FAUXPTYVALUEID  and   dz.FISDEFAULT=1   "
				+ " where  a.FDOCUMENTSTATUS='C'   " + " and   a.FUSEORGID=1  " + " AND exists (  "
				+ " select  1 from  T_BD_MATERIAL ax  "
				+ " left join   dbo.T_BD_AUXPTYVALUE xy on ax.FMASTERID =xy.FMATERIALID and xy.FMATERIALAUXPROPERTYID ='100001' "
				+ " left join dbo.T_BD_AUXPTYVALUEENTITY xz on  xz.FAUXPTYVALUEID = xy.FAUXPTYVALUEID   "
				+ " where  ( isnull(xz.FAUXPTYNUMBER,'') not like 'V%' AND LEN( isnull(xz.FAUXPTYNUMBER,''))=1)  "
				+ " and  " + " ax.FNumber=a.FNumber  "
				+ " GROUP BY ax.FNumber HAVING MAX(isnull(xz.FAUXPTYNUMBER,''))=isnull(zz.FAUXPTYNUMBER,'')) ";

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
					String FNumber = jsonObject1.getString("料号");
					String FAUXPTYNUMBER = jsonObject1.getString("最高版本");
					// String ITEMVER = jsonObject1.getString("ITEMVER");
					// String FNUMERATOR = jsonObject1.getString("FNUMERATOR");
					// String FDENOMINATOR =
					// jsonObject1.getString("FDENOMINATOR");
					// String FPOSITIONNO =
					// jsonObject1.getString("FPOSITIONNO");

					list.add(FNumber);
					list.add(FAUXPTYNUMBER);

					k3PartList.add(list);

				}
			}
		}
		reader.close();

		connectionExec.disconnect();

		System.out.println("size====" + k3PartList.size());
		return k3PartList;
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
