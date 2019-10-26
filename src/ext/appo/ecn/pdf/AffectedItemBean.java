package ext.appo.ecn.pdf;

import java.util.Collection;
import java.util.Map;

import ext.appo.ecn.common.util.ChangePartQueryUtils;
import ext.appo.ecn.common.util.ChangeUtils;
import ext.appo.ecn.constants.ChangeConstants;
import ext.customer.common.MBAUtil;
import wt.change2.AffectedActivityData;
import wt.change2.ChangeActivityIfc;
import wt.change2.Changeable2;
import wt.change2.WTChangeOrder2;
import wt.fc.Persistable;
import wt.lifecycle.State;
import wt.part.WTPart;
import wt.session.SessionHelper;
import wt.util.WTException;

public class AffectedItemBean {

	private String name;          //名称
	private String number;      //料号
    private String version;      //版本
    private String state;    //状态
    private String type;     //类型
	private String des;          //说明
    private String inQuantity;   //在制数量
    private String inTreatment;  //在制处理措施
    private String wayQuantity;  //在途数量
    private String wayTreatment;  //在途处理措施
    private String stockQuantity; //库存数量
    private String stockTreatment; //库存处理措施
    private String shipmentsTreatment; //已出货处理措施
    private String expectDate;         //期望完成时间
    private String personLiable;       //责任人
    private String collectObj;       //收集对象
	
	public AffectedItemBean() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the number
	 */
	public String getNumber() {
		return number;
	}

	/**
	 * @param number the number to set
	 */
	public void setNumber(String number) {
		this.number = number;
	}

	/**
	 * @return the version
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * @param version the version to set
	 */
	public void setVersion(String version) {
		this.version = version;
	}

	/**
	 * @return the state
	 */
	public String getState() {
		return state;
	}

	/**
	 * @param state the state to set
	 */
	public void setState(String state) {
		this.state = state;
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * @return the des
	 */
	public String getDes() {
		return des;
	}

	/**
	 * @param des the des to set
	 */
	public void setDes(String des) {
		this.des = des;
	}

	/**
	 * @return the inQuantity
	 */
	public String getInQuantity() {
		return inQuantity;
	}

	/**
	 * @param inQuantity the inQuantity to set
	 */
	public void setInQuantity(String inQuantity) {
		this.inQuantity = inQuantity;
	}

	/**
	 * @return the inTreatment
	 */
	public String getInTreatment() {
		return inTreatment;
	}

	/**
	 * @param inTreatment the inTreatment to set
	 */
	public void setInTreatment(String inTreatment) {
		this.inTreatment = inTreatment;
	}

	/**
	 * @return the wayQuantity
	 */
	public String getWayQuantity() {
		return wayQuantity;
	}

	/**
	 * @param wayQuantity the wayQuantity to set
	 */
	public void setWayQuantity(String wayQuantity) {
		this.wayQuantity = wayQuantity;
	}

	/**
	 * @return the wayTreatment
	 */
	public String getWayTreatment() {
		return wayTreatment;
	}

	/**
	 * @param wayTreatment the wayTreatment to set
	 */
	public void setWayTreatment(String wayTreatment) {
		this.wayTreatment = wayTreatment;
	}

	/**
	 * @return the stockQuantity
	 */
	public String getStockQuantity() {
		return stockQuantity;
	}

	/**
	 * @param stockQuantity the stockQuantity to set
	 */
	public void setStockQuantity(String stockQuantity) {
		this.stockQuantity = stockQuantity;
	}

	/**
	 * @return the stockTreatment
	 */
	public String getStockTreatment() {
		return stockTreatment;
	}

	/**
	 * @param stockTreatment the stockTreatment to set
	 */
	public void setStockTreatment(String stockTreatment) {
		this.stockTreatment = stockTreatment;
	}

	/**
	 * @return the shipmentsTreatment
	 */
	public String getShipmentsTreatment() {
		return shipmentsTreatment;
	}

	/**
	 * @param shipmentsTreatment the shipmentsTreatment to set
	 */
	public void setShipmentsTreatment(String shipmentsTreatment) {
		this.shipmentsTreatment = shipmentsTreatment;
	}

	/**
	 * @return the expectDate
	 */
	public String getExpectDate() {
		return expectDate;
	}

	/**
	 * @param expectDate the expectDate to set
	 */
	public void setExpectDate(String expectDate) {
		this.expectDate = expectDate;
	}

	/**
	 * @return the personLiable
	 */
	public String getPersonLiable() {
		return personLiable;
	}

	/**
	 * @param personLiable the personLiable to set
	 */
	public void setPersonLiable(String personLiable) {
		this.personLiable = personLiable;
	}

	/**
	 * @return the collectObj
	 */
	public String getCollectObj() {
		return collectObj;
	}

	/**
	 * @param collectObj the collectObj to set
	 */
	public void setCollectObj(String collectObj) {
		this.collectObj = collectObj;
	}
	
	public AffectedItemBean(String name, String number, String version, String state, String type, String des,
			String inQuantity, String inTreatment, String wayQuantity, String wayTreatment, String stockQuantity,
			String stockTreatment, String shipmentsTreatment, String expectDate, String personLiable,
			String collectObj) {
		super();
		this.name = name;
		this.number = number;
		this.version = version;
		this.state = state;
		this.type = type;
		this.des = des;
		this.inQuantity = inQuantity;
		this.inTreatment = inTreatment;
		this.wayQuantity = wayQuantity;
		this.wayTreatment = wayTreatment;
		this.stockQuantity = stockQuantity;
		this.stockTreatment = stockTreatment;
		this.shipmentsTreatment = shipmentsTreatment;
		this.expectDate = expectDate;
		this.personLiable = personLiable;
		this.collectObj = collectObj;
	}


//    public String getNumber(){
//        return part.getNumber();
//    }
//
//    public String getName(){
//        return part.getName();
//    }
//
//    public String getState(){
//
//        State s = State.toState(part.getState().toString());
//        String rtn = part.getState().toString();
//        try {
//            rtn = s.getDisplay(SessionHelper.getLocale());
//        } catch (WTException e) {
//            e.printStackTrace();
//        }
//        return rtn;
//    }
//
//    public String getVersion(){
//        String version = part.getVersionInfo().getIdentifier().getValue();
//        String iteration = part.getIterationInfo().getIdentifier().getValue();
//        return version+"."+iteration;
//    }
//    
//    //说明
//    public String getDes() throws WTException{
//    	  String des = "";
//	        Collection<ChangeActivityIfc> changeActivities = ChangeUtils.getChangeActivities(eco) ;
//			for(ChangeActivityIfc changeActivityIfc : changeActivities){
//				AffectedActivityData affectedActivityData = ChangeUtils.getAffectedActivity(changeActivityIfc, part) ;
//				if(affectedActivityData != null){
//					des = affectedActivityData.getDescription();
//				}
//			}
//        return des;
//    }
//    
//
//    //责任人
//    public String getResponsiblePerson(){
//    	String responsiblePerson = (String)PdfUtil.getIBAObjectValue(part, "ResponsiblePerson");
//    	String count = responsiblePerson.replaceAll("\\d+","");
//        String[] arry = count.split("\\|");
//        return arry[0];
//    }
//
//
//    //在制数量
//    public String getZzsl(){
//    	String articleInventory = (String)PdfUtil.getIBAObjectValue(part, "ArticleInventory");
//        return articleInventory;
//    }
//
//    //在制处理措施
//    public String getZzclcs(){
//    	String articleDispose = (String)PdfUtil.getIBAObjectValue(part, "ArticleDispose");
//    	String[] articleArr = articleDispose.split(";");	
//        return articleArr[1];
//    }
//
//    //在途数量
//    public String getZtsl(){
//    	String passageInventory = (String)PdfUtil.getIBAObjectValue(part, "PassageInventory");
//    	
//        return passageInventory;
//    }
//
//    //在途处理措施
//    public String getZtclcs(){
//    	String passageDispose = (String)PdfUtil.getIBAObjectValue(part, "PassageDispose");
//    	String[] passageArr = passageDispose.split(";");	
//        return passageArr[1];
//    }
//
//    //库存数量
//    public String getKcsl(){
//    	String centralWarehouseInventory = (String)PdfUtil.getIBAObjectValue(part, "CentralWarehouseInventory");
//        return centralWarehouseInventory;
//    }
//
//    //库存处理措施
//    public String getKcclcs(){
//    	String inventoryDispose = (String)PdfUtil.getIBAObjectValue(part, "InventoryDispose");
//    	String[] inventoryArr = inventoryDispose.split(";");	
//        return inventoryArr[1];
//    }
//
//    //已出货处理措施
//    public String getYcfclcs(){
//    	String productDispose = (String)PdfUtil.getIBAObjectValue(part, "ProductDispose");
//    	String[] productArr = productDispose.split(";");	
//        return productArr[1];
//    }
//
//    //期望完成时间
//    public String getQwwcsj(){
//    	String CompletionTime = (String)PdfUtil.getIBAObjectValue(part, "CompletionTime");
//        return CompletionTime;
//    }
//    
//    //收集对象
//    public String getCollectObj() throws WTException{
//    	String collecObj = getChildsNumber(part, eco);
//        return collecObj;
//    }
//    
//  //类型
//    public String getTypeName(){
//    	String partTypeName = (String)PdfUtil.getIBAObjectValue(part, "ChangeType");
//    	String[] typeArr = partTypeName.split(";");
//        return typeArr[1];
//    }
//    
//    /***
//	 * 获取存在受影响对象列表的部件编码
//	 * 
//	 * @param paramModelContext
//	 * @param parentPart
//	 * @return
//	 * @throws WTException
//	 */
//	public String getChildsNumber(WTPart parentPart,WTChangeOrder2 eco) throws WTException{
//		StringBuilder returnStr = new StringBuilder() ;
//		Collection<WTPart> childArray = ChangePartQueryUtils.getPartMultiwallStructure(parentPart) ;
//		if(childArray == null || childArray.size() == 0){
//			return returnStr.toString() ;
//		}
//		
//		Collection<Changeable2> changeablesBefore = ChangeUtils.getChangeablesBefore(eco) ;
//		for(Changeable2 changeable2 : changeablesBefore){
//			if(changeable2 instanceof WTPart){
//				for(WTPart childPart : childArray){
//					if(ChangeUtils.getNumber(changeable2).equals(childPart.getNumber())){
//						if(returnStr.length() > 0){
//							returnStr.append(ChangeConstants.USER_KEYWORD) ; 
//						}
//						returnStr.append(childPart.getNumber()) ;
//						break ;
//					}
//				}
//			}
//		}
//    	return returnStr.toString() ;
//	}
    			
    
}
