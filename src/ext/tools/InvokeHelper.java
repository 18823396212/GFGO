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
import java.util.Map;
import java.util.UUID;

import kingdee.bos.json.JSONArray;
import kingdee.bos.json.JSONObject;

public class InvokeHelper {

	public static String POST_K3CloudURL = "https://appo.test.ik3cloud.com/K3cloud/";

	// Cookie ֵ
	public static String CookieVal = null;
	private static Map map = new HashMap();

	static {
		map.put("Query", "BGJ.K3.CRM.WebApi.Plugin.Service.ERPGetCurrencyJson,BGJ.K3.CRM.WebApi.Plugin.common.kdsvc");
		map.put("Query2", "BGJ.K3.CRM.WebApi.Plugin.Service.ERPGetCurrencyJson,BGJ.K3.CRM.WebApi.Plugin.common.kdsvc");
		map.put("QueryAll",
				"BGJ.K3.CRM.WebApi.Plugin.Service.ERPGetCurrencyJson,BGJ.K3.CRM.WebApi.Plugin.common.kdsvc");
		map.put("Test", "GFBasicInformationAdded.GFBasicInformationAdd.AddMmaterials,GFBasicInformationAdded");
		// 分配
		map.put("Allocate", "Kingdee.BOS.WebApi.ServicesStub.DynamicFormService.Allocate.common.kdsvc");
	}

	// HttpURLConnection
	private static HttpURLConnection initUrlConn(String url, String paras) throws Exception {
		URL postUrl = new URL(POST_K3CloudURL.concat(url));
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
		System.out.println("-----------------------------------");
		System.out.println(jObj.toString());
		System.out.println("-----------------------------------");
		out.flush();
		out.close();

		return connection;
	}

	// Query
	public static List<List<Object>> Query(String sql) throws Exception {
		return Invoke("Query", sql);
	}

	// Query2
	public static List<List<Object>> Query2(String sql) throws Exception {
		return Invoke("Query2", sql);
	}

	// QueryAll
	public static List<List<Object>> QueryAll(String sql) throws Exception {
		return Invoke("QueryAll", sql);
	}

	// Test
	public static List<List<Object>> Test(String sql) throws Exception {
		return Invoke("Test", sql);
	}

	// Allocate
	public static void Allocate(String formId, String content) throws Exception {
		Invoke("Allocate", formId, content);
	}

	private static void Invoke(String deal, String formId, String content) throws Exception {

		String sUrl = map.get(deal).toString();
		JSONArray jParas = new JSONArray();
		HttpURLConnection connectionInvoke = null;
		if (null != formId && content != null) {
			jParas.put(formId);
			jParas.put(content);
			connectionInvoke = initUrlConn(sUrl, jParas.toString());
		} else {
			connectionInvoke = initUrlConn(sUrl, content);
		}

		BufferedReader reader = new BufferedReader(new InputStreamReader(connectionInvoke.getInputStream()));

		String line;
		System.out.println(" ============================= ");
		System.out.println(" Contents of post request ");
		System.out.println(" ============================= ");
		while ((line = reader.readLine()) != null) {
			String sResult = new String(line.getBytes(), "utf-8");
			System.out.println("我的" + sResult);
		}
		System.out.println(" ============================= ");
		System.out.println(" Contents of post request ends ");
		System.out.println(" ============================= ");
		reader.close();

		connectionInvoke.disconnect();
	}

	private static List<List<Object>> Invoke(String deal, String sql) throws Exception {

		List resultList = new ArrayList();

		String sUrl = map.get(deal).toString();
		JSONArray jParas = new JSONArray();
		HttpURLConnection connectionInvoke = null;
		if (null != sql) {
			jParas.put(sql);
			connectionInvoke = initUrlConn(sUrl, jParas.toString());
		} else {
			connectionInvoke = initUrlConn(sUrl, sql);
		}

		BufferedReader reader = new BufferedReader(new InputStreamReader(connectionInvoke.getInputStream()));

		String line;
		System.out.println(" ============================= ");
		System.out.println(" Contents of post request ");
		System.out.println(" ============================= ");
		while ((line = reader.readLine()) != null) {
			String sResult = new String(line.getBytes(), "utf-8");
			// System.out.println("sResult==="+sResult);
			if (sResult.contains("true")) {
				String result = sResult.substring(17, sResult.length() - 14);
				System.out.println("查询结果：" + result);
				System.out.println("deal:" + deal);
				JSONObject jsonObject = new JSONObject(result);
				JSONArray table = jsonObject.getJSONArray("Table");

				if (deal.equals("Query")) {
					for (int i = 0; i < table.length(); i++) {
						List list = new ArrayList();
						JSONObject jsonObject1 = table.getJSONObject(i);

						String FMATERIALID = jsonObject1.getString("FMATERIALID");// 物料内码

						list.add(FMATERIALID);

						resultList.add(list);
						System.out.println("Query:" + resultList);
					}
				}
			} else {
				System.out.println("查询失败");
				System.out.println("查询失败结果" + sResult);
				return null;
			}
		}
		System.out.println(" ============================= ");
		System.out.println(" Contents of post request ends ");
		System.out.println(" ============================= ");
		reader.close();

		connectionInvoke.disconnect();

		return resultList;
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

	// Login
	public static boolean Login(String dbId, String user, String pwd, int lang) throws Exception {

		boolean bResult = false;

		String sUrl = "Kingdee.BOS.WebApi.ServicesStub.AuthService.ValidateUser.common.kdsvc";

		JSONArray jParas = new JSONArray();
		jParas.put(dbId);// 帐套Id
		jParas.put(user);// 用户名
		jParas.put(pwd);// 密码
		jParas.put(lang);// 语言

		HttpURLConnection connection = initUrlConn(sUrl, jParas.toString());
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
		System.out.println(" ============================= ");
		System.out.println(" Contents of post request ");
		System.out.println(" ============================= ");
		while ((line = reader.readLine()) != null) {
			String sResult = new String(line.getBytes(), "utf-8");
			System.out.println(sResult);
			bResult = line.contains("\"LoginResultType\":1");
		}
		System.out.println(" ============================= ");
		System.out.println(" Contents of post request ends ");
		System.out.println(" ============================= ");
		reader.close();

		connection.disconnect();

		return bResult;
	}

}
