package ext.generic.borrow;

import com.ptc.windchill.annotations.metadata.ColumnProperties;
import com.ptc.windchill.annotations.metadata.GenAsPersistable;
import com.ptc.windchill.annotations.metadata.GeneratedProperty;
import com.ptc.windchill.annotations.metadata.PropertyConstraints;
import com.ptc.windchill.annotations.metadata.StringCase;
import com.ptc.windchill.annotations.metadata.SupportedAPI;
import ext.generic.borrow.BorrowOrderIdentity;
import ext.generic.borrow._BorrowOrder;
import java.sql.Timestamp;
import wt.access.IdentityAccessControlled;
import wt.content.FormatContentHolder;
import wt.enterprise.Managed;
import wt.fc.IdentificationObject;
import wt.fc.UniquelyIdentified;
import wt.folder.history.Movable;
import wt.iba.value.IBAHolder;
import wt.inf.container.WTContained;
import wt.inf.container.WTContainedIdentified;
import wt.org.WTOrganization;
import wt.type.TypeDefinitionInfo;
import wt.type.Typed;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;

@GenAsPersistable(superClass = Managed.class, interfaces = {IBAHolder.class, FormatContentHolder.class, Typed.class,
		UniquelyIdentified.class, WTContained.class, WTContainedIdentified.class, IdentityAccessControlled.class,
		Movable.class}, extendable = true, properties = {
				@GeneratedProperty(name = "number", type = String.class, supportedAPI = SupportedAPI.PUBLIC, javaDoc = "A string representing the number of a order.", constraints = @PropertyConstraints(stringCase = StringCase.UPPER_CASE, upperLimit = 32, required = true) , columnProperties = @ColumnProperties(index = true, columnName = "BorrowOrderNumber") ),
				@GeneratedProperty(name = "name", type = String.class, supportedAPI = SupportedAPI.PUBLIC, javaDoc = "A string representing the name of a order.", constraints = @PropertyConstraints(upperLimit = 160, required = true) ),
				@GeneratedProperty(name = "borrowEndDate", type = Timestamp.class, supportedAPI = SupportedAPI.PUBLIC, javaDoc = "Attribute specific to the Windchill A&D module borrowDate"),
				@GeneratedProperty(name = "borrowDays", type = Integer.class, supportedAPI = SupportedAPI.PUBLIC, javaDoc = "Attribute specific to the Windchill A&D module borrowDays"),
				@GeneratedProperty(name = "borrowReason", type = String.class, supportedAPI = SupportedAPI.PUBLIC, javaDoc = "Attribute specific to the Windchill A&D module reasion", constraints = @PropertyConstraints(upperLimit = 1000) )})
public class BorrowOrder extends _BorrowOrder {
	static final long serialVersionUID = 1L;

	public static BorrowOrder newBorrowOrder() throws WTException {
		BorrowOrder instance = new BorrowOrder();
		instance.initialize();
		return instance;
	}

	public String getFlexTypeIdPath() {
		return null;
	}

	public void setValue(String s, String s1) {
	}

	public Object getValue() {
		return null;
	}

	public TypeDefinitionInfo getTypeDefinitionInfo() {
		return null;
	}

	public IdentificationObject getIdentificationObject() throws WTException {
		return BorrowOrderIdentity.newBorrowOrderIdentity(this);
	}

	public void setOrganization(WTOrganization org) throws WTPropertyVetoException {
	}

	public WTOrganization getOrganization() {
		return null;
	}
}