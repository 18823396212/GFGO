package ext.test;

public class AffObjBean {

	private String name;          //名称
	private String partNum;      //料号
    private String version;      //版本               
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
	
	    public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPartNum() {
		return partNum;
	}
	public void setPartNum(String partNum) {
		this.partNum = partNum;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public String getDes() {
		return des;
	}
	public void setDes(String des) {
		this.des = des;
	}
	public String getInQuantity() {
		return inQuantity;
	}
	public void setInQuantity(String inQuantity) {
		this.inQuantity = inQuantity;
	}
	public String getInTreatment() {
		return inTreatment;
	}
	public void setInTreatment(String inTreatment) {
		this.inTreatment = inTreatment;
	}
	public String getWayQuantity() {
		return wayQuantity;
	}
	public void setWayQuantity(String wayQuantity) {
		this.wayQuantity = wayQuantity;
	}
	public String getWayTreatment() {
		return wayTreatment;
	}
	public void setWayTreatment(String wayTreatment) {
		this.wayTreatment = wayTreatment;
	}
	public String getStockQuantity() {
		return stockQuantity;
	}
	public void setStockQuantity(String stockQuantity) {
		this.stockQuantity = stockQuantity;
	}
	public String getStockTreatment() {
		return stockTreatment;
	}
	public void setStockTreatment(String stockTreatment) {
		this.stockTreatment = stockTreatment;
	}
	public String getShipmentsTreatment() {
		return shipmentsTreatment;
	}
	public void setShipmentsTreatment(String shipmentsTreatment) {
		this.shipmentsTreatment = shipmentsTreatment;
	}
	public String getExpectDate() {
		return expectDate;
	}
	public void setExpectDate(String expectDate) {
		this.expectDate = expectDate;
	}
	public String getPersonLiable() {
		return personLiable;
	}
	public void setPersonLiable(String personLiable) {
		this.personLiable = personLiable;
	}   
	
	  public AffObjBean(String name, String partNum, String version, String des, String inQuantity, String inTreatment,
				String wayQuantity, String wayTreatment, String stockQuantity, String stockTreatment,
				String shipmentsTreatment, String expectDate, String personLiable) {
			super();
			this.name = name;
			this.partNum = partNum;
			this.version = version;
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
		}
	
}
