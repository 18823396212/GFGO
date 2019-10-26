package ext.appo.erp.util;

import static ext.generic.integration.erp.util.SqlHelperUtil.queryBOMFid;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import ext.appo.erp.bean.BomCompareBean;
import ext.appo.erp.bean.BomInfo;
import ext.appo.erp.bean.ECNInfoEntity;
import ext.appo.erp.bean.MaterialInfo;
import ext.appo.erp.bean.MaterialJson;
import ext.generic.integration.erp.util.InvokeHelper;
import kingdee.bos.json.JSONArray;
import kingdee.bos.json.JSONObject;
import kingdee.bos.webapi.client.K3CloudApiClient;
import wt.part.WTPart;

public class KingDeeK3Helper {

	static String K3CloudURL = "";
	static String dbId = "";
	static String uid = "";
	static String pwd = "";
	static int lang;

	static {
		try {
			Map<String, String> map = ReadProperty.ReadProperty();
			K3CloudURL = map.get("K3CloudURL");
			dbId = map.get("dbId");
			uid = map.get("uid");
			pwd = map.get("pwd");
			lang = Integer.parseInt(map.get("lang"));
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	// 通过物料编码查询物料表辅助属性是否开启
	// FAuxPropertyId 100001为版本id,100002为等级
	// FIsEnable1 true为开启，false为未开启
	// FUseOrgId 使用组织 1为光峰
	public static List<List<Object>> queryMaterielFAuxProperty(String partNumber) throws Exception {
		K3CloudApiClient client = new K3CloudApiClient(K3CloudURL);
		Boolean result = client.login(dbId, uid, pwd, lang);
		if (result) {
			String sContent = "{\"FormId\":\"BD_MATERIAL\"," + // 物料formid
					"\"FilterString\":\"FNumber=\'" + partNumber + "\'\"," + // 过滤条件
					"\"FieldKeys\":\"FAuxPropertyId,FIsEnable1,FUseOrgId,FNumber,FMATERIALID\"}";// FAuxPropertyId
																									// 辅助属性，FIsEnable1
																									// 启用，FNumber
																									// 物料编码,FMATERIALID
																									// 物料内码

			List<List<Object>> sResult = client.executeBillQuery(sContent);
			// System.out.println(sContent);
			return sResult;
		} else {
			throw new RuntimeException("ERP连接失败，请联系管理员！");
		}
	}

	/***
	 * 保存物料数据到ERP（标准接口）
	 * 
	 * @param data
	 * @return
	 * @throws Exception
	 */
	public static String saveMaterial(String data) throws Exception {
		K3CloudApiClient client = new K3CloudApiClient(K3CloudURL);
		Boolean result = client.login(dbId, uid, pwd, lang);
		if (result) {

			String sContent = client.save("BD_MATERIAL", data);

			return sContent;
		} else {
			throw new RuntimeException("ERP连接失败，请联系管理员！");
		}
	}

	/*****
	 * 保存BOM到ERP
	 * 
	 * @param data
	 * @return
	 * @throws Exception
	 */
	public static String saveMaterialBOM(String data) throws Exception {
		K3CloudApiClient client = new K3CloudApiClient(K3CloudURL);
		Boolean result = client.login(dbId, uid, pwd, lang);
		if (result) {

			String sContent = client.save("ENG_BOM", data);
			System.out.println("发送BOM返回结果：" + sContent);
			return sContent;
		} else {
			throw new RuntimeException("ERP连接失败，请联系管理员！");
		}
	}

	/*****
	 * 保存ECN到ERP
	 * 
	 * @param data
	 * @return
	 * @throws Exception
	 */
	public static String saveECN(String data) throws Exception {
		K3CloudApiClient client = new K3CloudApiClient(K3CloudURL);
		Boolean result = client.login(dbId, uid, pwd, lang);
		if (result) {

			String sContent = client.save("ENG_ECNOrder", data);

			return sContent;
		} else {
			throw new RuntimeException("ERP连接失败，请联系管理员！");
		}
	}

	// 拼接发送物料的json（标准接口）
	public static String materialInfoToJson(MaterialInfo materialInfo) {

		String partNumber = materialInfo.getNumber();
		String sbu = partNumber.substring(0, 1);
		// 是否可为主产品和启用ecn
		Boolean is = false;
		if ("A".equals(sbu) || "B".equals(sbu)) {
			is = true;
		}
		if ("已发布".equals(materialInfo.getLifecycle())) {

		}

		String json = "{\n" + "    \"IsDeleteEntry\": \"true\",\n" + "    \"IsVerifyBaseDataField\": \"false\",\n"
				+ "    \"IsEntryBatchFill\": \"true\",\n" + "    \"ValidateFlag\": \"true\",\n"
				+ "    \"NumberSearch\": \"true\",\n" + "    \"IsAutoSubmitAndAudit\": \"true\",\n" + // 是否自动提交与审核，默认为否
				"    \"Model\": {\n" + "        \"FMATERIALID\": 0,\n" + // id
				"        \"FCreateOrgId\": {\n" + "            \"FNumber\": \"02\"\n" + // 创建组织
				"        },\n" + "        \"FUseOrgId\": {\n" + "            \"FNumber\": \"02\"\n" + // 使用组织
				"        },\n" + "        \"FNumber\": \"" + materialInfo.getNumber() + "\",\n" + // 物料编码
				"        \"FName\": \"" + materialInfo.getName() + "\",\n" + // 物料名称
				"        \"FSpecification\": \"" + materialInfo.getGgms() + "\",\n" + // 规格型号
				"        \"FDescription\":\"" + materialInfo.getGgms() + "\",\n" + // 描述
				"        \"FMaterialGroup\": {\n" + "           \"FNumber\": \"" + materialInfo.getClassification()
				+ "\"" + // 物料分组
				"        },\n" + "       \"F_APPO_NBXH\": \"" + materialInfo.getNbxh() + "\",\n" + // 内部型号
				"        \"F_APPO_xsxh\": \"" + materialInfo.getXsxh() + "\",\n" + // 销售型号
				"        \"F_APPO_pp\": {\n" + "            \"FNumber\": \"ZY\"\n" + // 品牌
				"        },\n" + "        \"F_APPO_ZT\": \"" + materialInfo.getLifecycle() + "\",\n" + // 状态
				"        \"F_APPO_ZXLCBB\": \"" + materialInfo.getMajorVersion() + "\",\n" + // 版本
				"        \"F_APPO_CPZT\": \"" + materialInfo.getCpzt() + "\",\n" + // 产品状态
				"        \"F_APPO_SSXM\": \"" + materialInfo.getSsxm() + "\",\n" + // 所属项目
				"        \"F_APPO_PLMCJZ\": \"" + materialInfo.getCreateUser() + "\",\n" + // PLM创建者
				"        \"F_APPO_GYLX\": \"" + materialInfo.getGylx() + "\",\n" + // 光源类型
				"        \"F_APPO_HBSX\": \"" + materialInfo.getHbsx() + "\",\n" + // 环保属性
				"        \"F_APPO_XSJS\": \"" + materialInfo.getXsjs() + "\",\n" + // 显示技术
				"        \"F_APPO_PLMCPXL\": \"" + materialInfo.getCpxl() + "\",\n" + // 产品系列
				"        \"SubHeadEntity\": {\n" + "        \"FGROSSWEIGHT\": " + materialInfo.getZl() + ",\n" + // 毛重
				"        \"FNETWEIGHT\": " + materialInfo.getZl() + ",\n" + // 净重
				"        \"FVOLUME\": " + materialInfo.getBzcc() + ",\n" + // 体积
				"            \"FBaseUnitId\": {\n" + "                \"FNumber\": \"" + materialInfo.getDefaultUnit()
				+ "\"\n" + // 单位
				"            },\n" + "            \"FWEIGHTUNITID\": {\n" + // 重量单位
				"                \"FNUMBER\": \"" + materialInfo.getZldw() + "\"\n" + "            },\n"
				+ "        },\n" + "        \"SubHeadEntity5\": {\n" + // 生产
				"            \"FIsMainPrd\":" + is + ",\n" + // 是否可为主产品
				"            \"FIsECN\":" + is + ",\n" + // 是否启用ECN
				"        }\n" + "    }\n" + "}";

		System.out.println("========物料========" + json);
		return json;
	}

	/****
	 * 定制物料发送接口
	 * 
	 * @param json
	 * @return
	 * @throws Exception
	 */
	public static String AddMaterials(String json) throws Exception {

		// System.out.println("发送物料的lang:"+lang);
		K3CloudApiClient client = new K3CloudApiClient(K3CloudURL);
		// System.out.println("连接K3:"+client);
		Boolean login = client.login(dbId, uid, pwd, lang);

		// System.out.println("进入物料发送接口");

		// System.out.println("地址："+(String)
		// map.get("K3CloudURL")+"；帐套id："+(String)
		// map.get("dbId")+"用户名:"+(String)map.get("uid")+"密码："+(String)map.get("pwd")+";"+Integer.parseInt(map.get("lang")));
		// System.out.println("连接K3 login："+login);

		String json2 = "{DBId:\"" + dbId + "\",User:\"" + uid + "\",Psw:\"" + pwd + "\",Url:\"" + K3CloudURL + "\"}";

		if (login) {
			String[] data = { json, json2 };
			// System.out.println("发物料JSON2:"+json2);
			String execute = client.execute(
					"GFBasicInformationAdded.GFBasicInformationAdd.AddMmaterials,GFBasicInformationAdded", data,
					String.class);
			System.out.println("AddMaterials信息:" + execute);
			return execute;
		} else {
			throw new RuntimeException("ERP连接失败，请联系管理员！");
		}
	}

	// 通过父项BOM版本查询结果下FTreeEntity的Id(发ecn变更前，变更后需要)
	public static String viewBOMFBomEntryId(String version) throws Exception {
		String result = "0";
		// System.out.println("发送物料的lang:"+lang);
		System.out.println("查询的BOM版本:" + version);
		K3CloudApiClient client = new K3CloudApiClient(K3CloudURL);
		Boolean login = client.login(dbId, uid, pwd, lang);
		if (login) {
			String formId = "ENG_BOM";
			String data = "{\"Number\":\"" + version + "\"}";
			result = client.view(formId, data);
			// System.out.println("结果："+result);
			if (result.contains("Id")) {// ID
				result = result.substring(10, result.length() - 1);
				System.out.println("截取" + result);
				JSONObject jsonObject = new JSONObject(result);
				JSONObject result1 = jsonObject.getJSONObject("Result");
				System.out.println("result1:" + result1);
				JSONArray table = result1.getJSONArray("TreeEntity");
				System.out.println("table:" + table);
				System.out.println("table.get(0):" + table.getJSONObject(0));
				JSONObject jsonObject1 = table.getJSONObject(0);

				Object viewBOMFBomEntryId = jsonObject1.get("Id");
				System.out.println("viewBOMFBomEntryId:" + viewBOMFBomEntryId);
				result = String.valueOf(viewBOMFBomEntryId);
			} else {
				System.out.println("查询FTreeEntity的Id：" + result);
				result = "0";
			}
		} else {
			throw new RuntimeException("ERP连接失败，请联系管理员！");
		}
		System.out.println("返回的BOM内码结果:" + result);
		return result;
	}

	// 通过物料BOM版本查询对应BOM内码（PkIds）
	public static String viewBOM(String version) throws Exception {
		String result = "0";
		// System.out.println("发送物料的lang:"+lang);
		System.out.println("查询的BOM版本:" + version);
		K3CloudApiClient client = new K3CloudApiClient(K3CloudURL);
		Boolean login = client.login(dbId, uid, pwd, lang);
		if (login) {
			String formId = "ENG_BOM";
			String data = "{\n" + "    \"Number\": \"" + version + "\",\n" + "}";
			result = client.view(formId, data);
			// System.out.println("结果："+result);
			if (result.contains("Id")) {// ID
				result = result.substring(10, result.length() - 1);
				// System.out.println("截取"+result);
				JSONObject jsonObject = new JSONObject(result);
				JSONObject s = jsonObject.getJSONObject("Result");
				Object Id = s.get("Id");
				System.out.println("Id:" + Id);
				// result="查询BOM内码成功";
				result = String.valueOf(Id);
			} else {
				System.out.println("查询BOM内码：" + result);
				result = "0";
			}
		} else {
			throw new RuntimeException("ERP连接失败，请联系管理员！");
		}
		System.out.println("返回的BOM内码结果:" + result);
		return result;
	}

	// 分配（将BOM发送给峰米） PkIds BOM内码,TOrgIds 组织内码 （02光峰，20峰米）
	public static String fp(String PkIds, String TOrgIds) throws Exception {
		String result = "";
		Boolean login = InvokeHelper.Login(dbId, uid, pwd, lang);
		if (login) {
			String formId = "ENG_BOM";

			String json = "{\n" + "    \"PkIds\": \"" + PkIds + "\",\n" + "    \"TOrgIds\": \"" + TOrgIds + "\",\n"
					+ "    \"IsAutoSubmitAndAudit\": \"true\"\n" + "}";
			System.out.println("发送格式：" + json);
			// System.out.println("formId:"+formId);
			// System.out.println("json:"+json);
			result = InvokeHelper.Allocate(formId, json);
		} else {
			throw new RuntimeException("ERP连接失败，请联系管理员！");
		}
		System.out.println("分配峰米结果：" + result);
		return result;
	}

	/****
	 * ECN生效接口
	 * 
	 * @param number
	 *            ECN编号
	 * @return
	 * @throws Exception
	 */
	public static String ECNEffective(String number) throws Exception {
		K3CloudApiClient client = new K3CloudApiClient(K3CloudURL);
		Boolean login = client.login(dbId, uid, pwd, lang);
		if (login) {
			String data = "{\"Numbers\":[\"" + number + "\"]}";

			String s = client.excuteOperation("ENG_ECNOrder", "Effect", data);
			return s;
		} else {
			throw new RuntimeException("ERP连接失败，请联系管理员！");
		}
	}

	/*****
	 * 获取定制接口发送物料的Json
	 * 
	 * @param materialInfo
	 * @return
	 */
	public static String getMaterialJsonData(MaterialInfo materialInfo) {

		MaterialJson material = new MaterialJson();

		material.setNumber(materialInfo.getNumber());
		material.setName(materialInfo.getName());
		material.setClassification(materialInfo.getClassification());
		// if (!materialInfo.getBzcc().equals("")) {
		// material.setBzcc(materialInfo.getBzcc());
		// }
		material.setBzcc(materialInfo.getBzcc());
		material.setCpxl(materialInfo.getCpxl());
		material.setCpzt(materialInfo.getCpzt());
		material.setMajorVersion(materialInfo.getMajorVersion());
		material.setBrand(materialInfo.getBrand());
		material.setCreateUser(materialInfo.getCreateUser());
		material.setGgms(materialInfo.getGgms());
		material.setDefaultUnit(materialInfo.getDefaultUnit());
		material.setLifecycle(materialInfo.getLifecycle());
		material.setHbsx(materialInfo.getHbsx());
		material.setNbxh(materialInfo.getNbxh());
		material.setGylx(materialInfo.getGylx());
		if (!materialInfo.getZl().equals("")) {
			material.setZl(Double.parseDouble(materialInfo.getZl()));
		}
		material.setZldw(materialInfo.getZldw());
		material.setXsxh(materialInfo.getXsxh());
		material.setXsjs(materialInfo.getXsjs());
		material.setSsxm(materialInfo.getSsxm());
		material.setSscpx(materialInfo.getSscpx());
		material.setSfxnj(materialInfo.getSfxnj());
		// material.setSscpx("CPX1001");//测试使用
		// System.out.println("产品线："+materialInfo.getSscpx());
		material.setZzlcbb(materialInfo.getZxlcbb());

		material.setF_APPO_CLYJ(materialInfo.getF_APPO_CLYJ());//

		// System.out.println("获取到的最低等级："+materialInfo.getZdchdj());
		// material.setZdchdj(materialInfo.getZdchdj());//最低存货等级

		// material.setF_APPO_BZCC(materialInfo.getF_APPO_BZCC());//包装尺寸

		// material.setFSpecification(materialInfo.getFSpecification());//规格型号

		material.setSource(materialInfo.getSource());// 源
		// mao
		material.setOldVersion(materialInfo.getOldVersion());

		String json = material.toString();

		System.out.println("==========materialJson===========" + json);

		return json;

	}

	// 拼接发BOM送的json
	public static String bomInfoToJson(List<BomInfo> bomInfoList, String company) throws Exception {

		if (bomInfoList == null || bomInfoList.size() <= 0) {
			return null;
		}
		BomInfo info = bomInfoList.get(0);

		String parentNumber = info.getParentNumber();
		String zt = info.getState();
		String version = info.getMajorVersion();
		String bomVersion = parentNumber + "_" + version;
		String parentwlfl = info.getParentwlfl();

		String fTreeEntityJson = BomUtil.createFTreeEntityJson(bomInfoList, company);

		String json = "";
		// String mVersion=info.getMajorVersion();
		// List zdchdjList=new ArrayList();
		// String zdchdj="";
		// for (int i = 0; i <bomInfoList.size() ; i++) {
		// zdchdjList.add(bomInfoList.get(i).getZdchdj());
		// }
		// if(zdchdjList!=null&&zdchdjList.size()>0){
		// zdchdj= (String) Collections.min(zdchdjList); //最低存货等级
		// }

		String OrgId = "1";
		String FCreateOrgId = "02";
		String FUseOrgId = "02";
		if (company.equals("FM")) {
			// FCreateOrgId="20";
			FUseOrgId = "20";
			OrgId = "100146";
		} else if (company.equals("CINEAPPO")) {
			FUseOrgId = "50";
			OrgId = "100148";
		}

		// 判断K3父件是否开启版本
		// FAuxPropertyId,FIsEnable1,FUseOrgId
		// FAuxPropertyId 100001为版本id,100002为等级
		// FIsEnable1 true为开启，false为未开启
		// FUseOrgId 使用组织 1为光峰，100146为峰米，100148(与BOM的使用组织FUseOrgId不同，BOM的
		// 02为光峰，20为峰米,50为中影光峰)，
		List<List<Object>> list = queryMaterielFAuxProperty(parentNumber);
		boolean isFIsEnable1 = false;
		if (list != null && list.size() > 0) {
			// System.out.println("list.size():"+list.size());
			for (int i = 0; i < list.size(); i++) {
				String FAuxPropertyId = String.valueOf(list.get(i).get(0));// 辅助属性id
				String FIsEnable1 = String.valueOf(list.get(i).get(1));// 是否开启
				String FUseOrgId2 = String.valueOf(list.get(i).get(2));// 组织id
				// System.out.println("BOM版本-父件：" + FAuxPropertyId + ";" +
				// FIsEnable1 + ";" + FUseOrgId2);
				if (company.equals("FM")) {
					if (FUseOrgId2.trim().equals("100146") && FAuxPropertyId.trim().equals("100001")
							&& FIsEnable1.trim().equals("true")) {
						System.out.println("true");
						isFIsEnable1 = true;
						break;
					}
				}
				if (company.equals("CINEAPPO")) {
					if (FUseOrgId2.trim().equals("100148") && FAuxPropertyId.trim().equals("100001")
							&& FIsEnable1.trim().equals("true")) {
						System.out.println("true");
						isFIsEnable1 = true;
						break;
					}
				} else {
					if (FUseOrgId2.trim().equals("1") && FAuxPropertyId.trim().equals("100001")
							&& FIsEnable1.trim().equals("true")) {
						System.out.println("true");
						isFIsEnable1 = true;
						break;
					}

				}
			}
			// System.out.println("父件物料"+parentNumber+"是否开启版本：" +isFIsEnable1);
		}

		String FParentAuxPropIdStr = "";
		if (isFIsEnable1) {
			FParentAuxPropIdStr = " \"FParentAuxPropId\":{\"FPARENTAUXPROPID__FF100001\":{ \n"
					+ "                           \"FNumber\":\"" + version + "\" \n" + "                       }},\n";
		}

		// 查询父件BOM版本是否存在，存在则IsAutoSubmitAndAudit自动提与审核改为false，Model下传FID()
		// String result=viewBOM(bomVersion);
		//
		// System.out.println("发BOM时是否已存在BOM："+!result.trim().equals("0"));
		String fid = "";// FID
		String IsAutoSubmitAndAudit = "true";// 自动提与审核

		List<List<Object>> fidList = queryBOMFid(OrgId, bomVersion);
		if (fidList != null && fidList.size() > 0) {
			// 已存在BOM
			IsAutoSubmitAndAudit = "false";
			fid = "\"FID\":\"" + fidList.get(0).get(0) + "\",\n";
		}

		// //已存在BOM
		// if (!result.trim().equals("0")){
		// IsAutoSubmitAndAudit="false";
		// fid="\"FID\":\""+result+"\",\n";
		// }
		json = "{" + "  \"IsDeleteEntry\": \"true\",\n" + "  \"IsVerifyBaseDataField\": \"false\",\n"
				+ "  \"IsEntryBatchFill\": \"true\",\n" + "  \"ValidateFlag\": \"true\",\n"
				+ "  \"NumberSearch\": \"true\",\n" + "  \"InterationFlags\": \"\",\n"
				+ "  \"IsAutoSubmitAndAudit\": \"" + IsAutoSubmitAndAudit + "\",\n" + "  \"Model\": {\n" + fid
				+ "  \"FCreateOrgId\": {\n" + "      \"FNumber\":\"" + FCreateOrgId + "\"" + "    },\n"
				+ "  \"FUseOrgId\": {\n" + "      \"FNumber\":\"" + FUseOrgId + "\"" + "    },\n"
				+ "    \"FGroup\": {\n" + "      \"FNumber\":\"" + parentwlfl + "\"" + "    },\n" + "   \"FNumber\":\""
				+ bomVersion + "\",\n" + "    \"FBOMCATEGORY\": \"1\",\n" + "    \"FBOMUSE\": \"99\",\n"
				+ "    \"FYIELDRATE\": 100.0,\n" + "    \"F_APPO_BOMZT\": \"" + zt + "\",\n"
				+ "    \"FMATERIALID\": {\n" + "      \"FNumber\":\"" + parentNumber + "\"" + "    },\n"
				+ FParentAuxPropIdStr + "\"FTreeEntity\":" + fTreeEntityJson + "  }\n" + "}";

		System.out.println("=====BOM=======" + json);
		return json;
	}

	// 拼接发送的json
	public static String ecnInfoToJson(Map<WTPart, List<BomCompareBean>> map, Map<String, String> headInfo,
			String companyStr) throws Exception {

		System.out.println("========拼接ECN发送的JSON==========");

		if (map == null || map.size() <= 0) {
			return null;
		}

		// 变更说明
		String description = headInfo.get("description");
		// 变更原因
		String changeComment = headInfo.get("changeComment");
		// ECN编号
		String ECNNumber = headInfo.get("ECNNumber");
		// 变更类型（库存处理措施）
		String changeType = headInfo.get("changeType");// 0用完废料 1立即变更
		// 变更单名称
		String ecnName = headInfo.get("ecnName");

		String clyj = headInfo.get("F_APPO_CLYJ");

		// System.out.println("====changeType====ECNNumber====companyStr===="+ECNNumber+"======"+changeType+"======"+companyStr);

		if ("客户申请变更".equals(changeComment)) {
			changeComment = "GCBGYY01_SYS";
		} else if ("厂内申请变更".equals(changeComment)) {
			changeComment = "GCBGYY02_SYS";
		} else if ("供应商申请变更".equals(changeComment)) {
			changeComment = "GCBGYY03_SYS";
		} else if ("设计改进".equals(changeComment)) {
			changeComment = "GCBGYY04_SYS";
		} else {
			changeComment = "GCBGYY99_SYS";
		}

		String company = "02";
		// companyStr 0光峰，1峰米,峰米变更单后面加_FM,光峰不变
		if (companyStr.equals("1")) {
			company = "20";
			ECNNumber = ECNNumber + "_FM";
		}
		List<ECNInfoEntity> ecnInfoEntityToJson = BomUtil.getECNInfoEntityToJson(map, company);

		String FTreeEntity = ecnInfoEntityToJson.toString();
		String json = "{" + "  \"IsDeleteEntry\": \"true\",\n" + "  \"IsVerifyBaseDataField\": \"false\",\n"
				+ "  \"IsEntryBatchFill\": \"true\",\n" + "  \"ValidateFlag\": \"true\",\n"
				+ "  \"NumberSearch\": \"true\",\n" + "  \"InterationFlags\": \"\",\n"
				+ "  \"IsAutoSubmitAndAudit\": \"true\",\n" +
				// " \"IsAutoSubmitAndAudit\": \"false\",\n" +
				"  \"Model\": {\n" + "    \"FBillNo\": \"" + ECNNumber + "\",\n" + // 单据编号
				"    \"FDescription\": \"" + description + "\",\n" + // 变更说明（备注）
				"    \"FChangeOrgId\":{\n" + // 变更组织
				"    \"FNumber\":\"" + company + "\"},\n" + "    \"FChangeReason\":{\n" + // 变更原因
				"    \"FNumber\":\"" + changeComment + "\"},\n" + "    \"F_APPO_ECNMC\":\" " + ecnName + "\",\n" + // 变更单名称F_APPO_CLYJ
		// " \"FChangeType\": "+changeType+",\n" +//变更类型
		"    \"F_APPO_PLMChangeType\": " + changeType + ",\n" + // 变更类型
		// " \"F_APPO_CLYJ\":\" "+clyj+"\",\n" +//处理意见
		"    \"FIsUpdateVersion\": false,\n" + // 版本升级
				"    \"FIsUpdatePPBom\": true,\n" + // 更新BOM清单
				"    \"FYIELDRATE\": 100.0,\n" +
				// " \"FMATERIALID\": {\n" +
				// " },\n" +
		"\"FTreeEntity\":" + FTreeEntity + "  }\n" + "}";

		System.out.println("=====ECN======" + json);
		return json;
	}

}
