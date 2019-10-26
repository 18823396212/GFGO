package ext.appo.email;

/** *********************************************************************** */
/*                                                                          */
/* Copyright (c) 2008-2012 YULONG Company */
/* 宇龙计算机通信科技（深圳）有限公司版权所有 2008-2012 */
/*                                                                          */
/* PROPRIETARY RIGHTS of YULONG Company are involved in the */
/* subject matter of this material. All manufacturing, reproduction, use, */
/* and sales rights pertaining to this subject matter are governed by the */
/* license agreement. The recipient of this software implicitly accepts */
/* the terms of the license. */
/* 本软件文档资料是宇龙公司的资产，任何人士阅读和使用本资料必须获得 */
/* 相应的书面授权，承担保密责任和接受相应的法律约束。 */
/*                                                                          */
/** *********************************************************************** */

/**
 * <pre>
 * 系统缩写：PLM 
 * 系统名称：产品生命周期管理系统 
 * 组件编号：C_系统管理
 * 组件名称：系统管理
 * 文件名称：WorkflowConfigFactory.java 
 * 作         者: 裴均宇
 * 生成日期：2011-09-23
 * </pre>
 */

/**
 * <pre>
 * 修改记录：01 
 * 修改日期：2011-09-23
 * 修  改   人：裴均宇
 * 关联活动：IT-DB00040918 C_系统管理_邮件跟催_代码开发_peijunyu
 * 修改内容：初始化
 * </pre>
 */
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.dom4j.tree.DefaultDocument;
import org.dom4j.tree.DefaultElement;

import com.ptc.core.logging.Log;
import com.ptc.core.logging.LogFactory;

import ext.appo.email.bean.ProcessBean;

/**
 * 此类用来解析发送邮件的配置文件,并把解析的配置文件放到一个Map 中
 * 
 */
public class WorkflowConfigFactory {

	static Log log = LogFactory.getLog(WorkflowConfigFactory.class);
	/**
	 * 邮件配置文件的位置
	 */
	private static final String MAIL_CONFIG_FILE = "workflow_config.xml";

	/**
	 * 保存邮件配置信息的Map, 当监听程序监听到任务完成时，会使用此Map 查找是否需要发送邮件(根据流程模板名称，活动名称，路由即对象
	 * MailMapKey)
	 */
	// public final List<ProcessBean> WORKFLOW_CONFIG_LIST = new
	// ArrayList<ProcessBean>();
	public static String DEFAULT_ENDTIME = null;
	public static String DEFAULT_MANAGERENDTIME = null;

	@SuppressWarnings("rawtypes")
	public List<ProcessBean> parseXML() {

		List<ProcessBean> beans = new ArrayList<ProcessBean>();
		log.debug("开始解析邮件配置文件...");
		SAXReader reader = new SAXReader();
		InputStream is = WorkflowConfigFactory.class.getResourceAsStream(MAIL_CONFIG_FILE);
		DefaultDocument document;
		try {
			document = (DefaultDocument) reader.read(is);
			is.close();
			List list = document.selectNodes("processlist/process");

			for (int i = 0; i < list.size(); i++) {
				DefaultElement element = (DefaultElement) list.get(i);
				ProcessBean bean = new ProcessBean();
				DefaultElement element1 = (DefaultElement) element.selectSingleNode("processName");
				if (element1 != null) {
					bean.setProcessName(StringUtils.trimToEmpty(element1.getText()));
				}
				element1 = (DefaultElement) element.selectSingleNode("activityName");
				if (element1 != null) {
					bean.setActivityName(StringUtils.trimToEmpty(element1.getText()));
				}
				element1 = (DefaultElement) element.selectSingleNode("endTimeSpace");
				if (element1 != null) {
					bean.setEndTimeSpace(StringUtils.trimToEmpty(element1.getText()));
				}
				element1 = (DefaultElement) element.selectSingleNode("managerEndTimeSpace");
				if (element1 != null) {
					bean.setManagerEndTimeSpace(StringUtils.trimToEmpty(element1.getText()));
				}
				element1 = (DefaultElement) element.selectSingleNode("type");
				if (element1 != null) {
					bean.setType(StringUtils.trimToEmpty(element1.getText()));
				}
				log.debug(bean);

				if (bean.getProcessName().equals("DEFAULT") && bean.getActivityName().equals("DEFAULT")) {
					DEFAULT_ENDTIME = bean.getEndTimeSpace();
					DEFAULT_MANAGERENDTIME = bean.getManagerEndTimeSpace();
				}
				beans.add(bean);
			}
		} catch (DocumentException e) {
			e.printStackTrace();
		}

		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return beans;
	}
}
