/** *********************************************************************** */
/*                                                                          */
/* Copyright (c) 2008-2012 YULONG Company                                   */
/* 宇龙计算机通信科技（深圳）有限公司版权所有 2008-2012                             */
/*                                                                          */
/* PROPRIETARY RIGHTS of YULONG Company are involved in the                 */
/* subject matter of this material. All manufacturing, reproduction, use,   */
/* and sales rights pertaining to this subject matter are governed by the   */
/* license agreement. The recipient of this software implicitly accepts     */
/* the terms of the license.                                                */
/* 本软件文档资料是宇龙公司的资产，任何人士阅读和使用本资料必须获得                        */
/* 相应的书面授权，承担保密责任和接受相应的法律约束。                                     */

/**
 * <pre>
 * 修改记录：01 
 * 修改日期：2019-08-27
 * 修  改   人：mao
 * 关联活动：IT-DB00040091 C_变更管理_变更过程看板_代码开发_maxueyou
 * 修改内容：初始化
 * </pre>
 */

package ext.appo.statReport.statChangeInfo.bean;

import java.io.Serializable;

/**
 * 此类用于封装变更过程看板需要统计的信息
 */
public class ChangeInfoBean implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	// ECN创建者
	private String ecnCreator = "";
	// ECN编号
	private String ecnNumber = "";
	// ECN名称
	private String ecnName = "";
	// 变更原因
	private String changeReason = "";
	// 变更原因说明
	private String changeReasonDes = "";
	// 所属产品类别
	private String productType = "";
	// 所属项目
	private String projectName = "";
	// 受影响对象状态
	private String effectObjectState = "";
	// 受影响对象编号
	private String effectObjectNo = "";
	// 受影响对象名称
	private String effectObjectName = "";
	// 受影响对象版本
	private String effectObjectVesion = "";
	// 在制数量
	private String inProcessQuantities = "";

	// 在制处理措施
	private String processingMeasures = "";
	// 在途数量
	private String onthewayQuantity = "";
	// 在途处理措施
	private String onthewayTreatmentMeasure = "";
	// 库存数量
	private String stockQuantity = "";
	// 库存处理措施
	private String stockTreatmentMeasure = "";

	private int line = 1;

	public String getEcnCreator() {
		return ecnCreator;
	}

	public void setEcnCreator(String ecnCreator) {
		this.ecnCreator = ecnCreator;
	}

	public String getEcnNumber() {
		return ecnNumber;
	}

	public void setEcnNumber(String ecnNumber) {
		this.ecnNumber = ecnNumber;
	}

	public String getEcnName() {
		return ecnName;
	}

	public void setEcnName(String ecnName) {
		this.ecnName = ecnName;
	}

	public String getChangeReason() {
		return changeReason;
	}

	public void setChangeReason(String changeReason) {
		this.changeReason = changeReason;
	}

	public String getChangeReasonDes() {
		return changeReasonDes;
	}

	public void setChangeReasonDes(String changeReasonDes) {
		this.changeReasonDes = changeReasonDes;
	}

	public String getProductType() {
		return productType;
	}

	public void setProductType(String productType) {
		this.productType = productType;
	}

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public String getEffectObjectState() {
		return effectObjectState;
	}

	public void setEffectObjectState(String effectObjectState) {
		this.effectObjectState = effectObjectState;
	}

	public String getEffectObjectNo() {
		return effectObjectNo;
	}

	public void setEffectObjectNo(String effectObjectNo) {
		this.effectObjectNo = effectObjectNo;
	}

	public String getEffectObjectName() {
		return effectObjectName;
	}

	public void setEffectObjectName(String effectObjectName) {
		this.effectObjectName = effectObjectName;
	}

	public String getEffectObjectVesion() {
		return effectObjectVesion;
	}

	public void setEffectObjectVesion(String effectObjectVesion) {
		this.effectObjectVesion = effectObjectVesion;
	}

	public String getInProcessQuantities() {
		return inProcessQuantities;
	}

	public void setInProcessQuantities(String inProcessQuantities) {
		this.inProcessQuantities = inProcessQuantities;
	}

	public String getProcessingMeasures() {
		return processingMeasures;
	}

	public void setProcessingMeasures(String processingMeasures) {
		this.processingMeasures = processingMeasures;
	}

	public String getOnthewayQuantity() {
		return onthewayQuantity;
	}

	public void setOnthewayQuantity(String onthewayQuantity) {
		this.onthewayQuantity = onthewayQuantity;
	}

	public String getOnthewayTreatmentMeasure() {
		return onthewayTreatmentMeasure;
	}

	public void setOnthewayTreatmentMeasure(String onthewayTreatmentMeasure) {
		this.onthewayTreatmentMeasure = onthewayTreatmentMeasure;
	}

	public String getStockQuantity() {
		return stockQuantity;
	}

	public void setStockQuantity(String stockQuantity) {
		this.stockQuantity = stockQuantity;
	}

	public String getStockTreatmentMeasure() {
		return stockTreatmentMeasure;
	}

	public void setStockTreatmentMeasure(String stockTreatmentMeasure) {
		this.stockTreatmentMeasure = stockTreatmentMeasure;
	}

	public int getLine() {
		return line;
	}

	public void setLine(int line) {
		this.line = line;
	}

}
