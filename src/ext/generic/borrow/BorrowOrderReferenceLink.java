package ext.generic.borrow;

import com.ptc.windchill.annotations.metadata.GenAsBinaryLink;
import com.ptc.windchill.annotations.metadata.GeneratedProperty;
import com.ptc.windchill.annotations.metadata.GeneratedRole;
import com.ptc.windchill.annotations.metadata.PropertyConstraints;
import ext.generic.borrow.BorrowOrder;
import ext.generic.borrow.BorrowPermissionType;
import ext.generic.borrow._BorrowOrderReferenceLink;
import java.io.Externalizable;
import wt.fc.IdentificationObject;
import wt.fc.ObjectToObjectLink;
import wt.fc.WTObject;
import wt.iba.value.IBAHolder;
import wt.type.TypeDefinitionInfo;
import wt.type.Typed;
import wt.util.WTException;

@GenAsBinaryLink(superClass = ObjectToObjectLink.class, interfaces = {Externalizable.class, Typed.class,
		IBAHolder.class}, properties = {
				@GeneratedProperty(name = "borrowPermissionType", type = BorrowPermissionType.class, initialValue = "BorrowPermissionType.getBorrowPermissionTypeDefault()", constraints = @PropertyConstraints(required = true) )}, roleA = @GeneratedRole(name = "borrowOrder", type = BorrowOrder.class) , roleB = @GeneratedRole(name = "borrowObject", type = WTObject.class) )
public class BorrowOrderReferenceLink extends _BorrowOrderReferenceLink {
	static final long serialVersionUID = 1L;

	public static BorrowOrderReferenceLink newBorrowOrderReferenceLink(BorrowOrder borrowOrder, WTObject wtobject)
			throws WTException {
		BorrowOrderReferenceLink instance = new BorrowOrderReferenceLink();
		instance.initialize(borrowOrder, wtobject);
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
		return null;
	}
}