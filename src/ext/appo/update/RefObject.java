package ext.appo.update;

public class RefObject 
{

	
	protected String id;  //序号
	protected String partNumber;  //部件编号
	protected String partname; //部件名称;
	protected String partstatedes; //部件状态;
	protected String partstateref; //部件状态;
	protected String refdocname;  //参考文档名称
	protected String desdocname;  //说明文档名称
	protected String refdocnumber;  //参考文档编号
	protected String desdocnumber;  //说明文档编号
	protected String refdocstate;  //参考文档状态
	protected String desdocstate;  //说明文档状态
	protected String refdoctype;  //说明文档类型
	protected String desdoctype;  //说明文档类型
	
	
	public RefObject(String id,String partNumber,String partname,String partstatedes,String partstateref,String refdocname,String refdoctype,String desdoctype,
			String desdocname,String refdocnumber,String desdocnumber,String refdocstate,String desdocstate)
	{
		
		this.id = id;
		this.partNumber = partNumber;
		this.partname = partname;
		this.partstatedes = partstatedes;
		this.partstateref = partstateref;
		this.refdocname = refdocname;
		this.refdoctype = refdoctype;
		this.desdoctype = desdoctype;
		this.desdocname = desdocname;
		this.refdocnumber = refdocnumber;
		this.desdocnumber = desdocnumber;
		this.refdocstate = refdocstate;
		this.desdocstate = desdocstate;
	}
	
	public RefObject()
	{
		
		this.id = "";
		this.partNumber = "";
		this.partname = "";
		this.partstatedes = "";
		this.partstateref = "";
		this.refdocname = "";
		this.refdoctype = "";
		this.desdoctype = "";
		this.desdocname = "";
		this.refdocnumber = "";
		this.desdocnumber = "";
		this.refdocstate = "";
		this.desdocstate = "";
	}
	
	public String toString()
    {
        return "[partNumber="+ this.partNumber + ",partstate="+ partstatedes +",partstate="+ partstateref +",partname="+ partname +",refdoctype="+ refdoctype +",desdoctype="+ desdoctype + ",refdocname="+ refdocname  +
        ",desdocname="+ desdocname + ",refdocnumber="+ refdocnumber 
        + ",desdocnumber="+ desdocnumber + ",refdocstate="+ refdocstate+ ",desdocstate="+ desdocstate+"]";
    }

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getPartNumber() {
		return partNumber;
	}

	public void setPartNumber(String partNumber) {
		this.partNumber = partNumber;
	}

	public String getPartname() {
		return partname;
	}

	public void setPartname(String partname) {
		this.partname = partname;
	}

	public String getRefdocname() {
		return refdocname;
	}

	public void setRefdocname(String refdocname) {
		this.refdocname = refdocname;
	}

	public String getDesdocname() {
		return desdocname;
	}

	public void setDesdocname(String desdocname) {
		this.desdocname = desdocname;
	}

	public String getRefdocnumber() {
		return refdocnumber;
	}

	public void setRefdocnumber(String refdocnumber) {
		this.refdocnumber = refdocnumber;
	}

	public String getDesdocnumber() {
		return desdocnumber;
	}

	public void setDesdocnumber(String desdocnumber) {
		this.desdocnumber = desdocnumber;
	}

	public String getRefdocstate() {
		return refdocstate;
	}

	public void setRefdocstate(String refdocstate) {
		this.refdocstate = refdocstate;
	}

	public String getDesdocstate() {
		return desdocstate;
	}

	public void setDesdocstate(String desdocstate) {
		this.desdocstate = desdocstate;
	}



	public String getRefdoctype() {
		return refdoctype;
	}

	public void setRefdoctype(String refdoctype) {
		this.refdoctype = refdoctype;
	}

	public String getDesdoctype() {
		return desdoctype;
	}

	public void setDesdoctype(String desdoctype) {
		this.desdoctype = desdoctype;
	}



	public String getPartstatedes() {
		return partstatedes;
	}

	public void setPartstatedes(String partstatedes) {
		this.partstatedes = partstatedes;
	}

	public String getPartstateref() {
		return partstateref;
	}

	public void setPartstateref(String partstateref) {
		this.partstateref = partstateref;
	}

	
}

