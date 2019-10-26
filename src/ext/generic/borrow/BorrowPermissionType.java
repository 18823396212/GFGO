package ext.generic.borrow;

import com.ptc.windchill.annotations.metadata.GenAsEnumeratedType;
import ext.generic.borrow._BorrowPermissionType;

@GenAsEnumeratedType
public class BorrowPermissionType extends _BorrowPermissionType {
	static final long serialVersionUID = 1L;
	public static final BorrowPermissionType READ = toBorrowPermissionType("Read");
	public static final BorrowPermissionType READ_ADN_DOWNLOAD = toBorrowPermissionType("ReadAndDownload");
}