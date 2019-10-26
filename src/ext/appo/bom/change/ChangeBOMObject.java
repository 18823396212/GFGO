package ext.appo.bom.change;

import java.io.Serializable;


public class ChangeBOMObject implements Serializable,Cloneable{
  
  /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
/**
	 * 
	 */

  protected String id;
  protected String parentnumber;
  protected String changetype;
  protected String childnumber;
  protected String quantity;
  protected String oldquantity;
  protected String occurrence;//位号
  protected String unit;//单位
  protected String Occurrencmodify;//单位

  
  public ChangeBOMObject(String id,String parentnumber,String changetype,String childnumber,String quantity,String oldquantity,
		  String occurrence,String unit,String Occurrencmodify)
  {
	  this.id=id;
	 this.parentnumber=parentnumber;
	 this.childnumber=childnumber;
	 this.changetype=changetype;
	 this.quantity=quantity;
	 this.oldquantity=oldquantity;
	 this.occurrence=occurrence;
	 this.unit=unit;
	 this.Occurrencmodify=Occurrencmodify;
	 
  }
  public ChangeBOMObject()
  {
	  this.id="";
	 this.parentnumber="";
	 this.childnumber="";
	 this.changetype="";
	 this.quantity="";
	 this.oldquantity="";
	 this.occurrence="";
	 this.unit="";
	 this.Occurrencmodify="";
	 
  }
  public String toString()
  {
	  return "id=="+id+"parentnumber=="+parentnumber+"childnumber=="+childnumber+"changetype=="+changetype
	  +"quantity=="+quantity+"oldquantity=="+oldquantity+"occurrence=="+occurrence+"unit=="+unit+"Occurrencmodify=="+Occurrencmodify;
  }

public String getChangetype() {
	return changetype;
}
public void setChangetype(String changetype) {
	this.changetype = changetype;
}



public String getParentnumber() {
	return parentnumber;
}
public void setParentnumber(String parentnumber) {
	this.parentnumber = parentnumber;
}
public String getChildnumber() {
	return childnumber;
}
public void setChildnumber(String childnumber) {
	this.childnumber = childnumber;
}
public String getId() {
	return id;
}
public void setId(String id) {
	this.id = id;
}
public String getQuantity() {
	return quantity;
}
public void setQuantity(String quantity) {
	this.quantity = quantity;
}
public String getOldquantity() {
	return oldquantity;
}
public void setOldquantity(String oldquantity) {
	this.oldquantity = oldquantity;
}
public String getOccurrence() {
	return occurrence;
}
public void setOccurrence(String occurrence) {
	this.occurrence = occurrence;
}
public String getUnit() {
	return unit;
}
public void setUnit(String unit) {
	this.unit = unit;
}
public String getOccurrencmodify() {
	return Occurrencmodify;
}
public void setOccurrencmodify(String occurrencmodify) {
	Occurrencmodify = occurrencmodify;
}

}
