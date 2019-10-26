package ext.appo.part.beans;

import wt.folder.Folder;
import wt.org.WTUser;
import wt.type.TypeDefinitionReference;
import ext.lang.bean.annotations.ExcelField;
import ext.lang.bean.persistable.ExcelBeanReadable;

public class ProductStructureTemplateExcelBean implements ExcelBeanReadable{

	private static final long serialVersionUID = 1L;

	// 层次
	@ExcelField(columnIndex = 0)
	private String level = null ;
	
	// 编码
	@ExcelField(columnIndex = 1)
	private String number = null ;
	
	// 名称
	@ExcelField(columnIndex = 2)
	private String name = null ;
	
	// 部件分类
	@ExcelField(columnIndex = 3)
	private String partClassification = null ;
	
	// 所属产品类别
	@ExcelField(columnIndex = 4)
	private String productCategory = null ;
	
	// 所属产品类型内部名称
	private String productCategoryInterior = null ;
	
	// 文档大类
	@ExcelField(columnIndex = 5)
	private String docType = null ;
	
	// 文档类型对象
	private TypeDefinitionReference typeReference = null ;
	
	// 文档小类
	@ExcelField(columnIndex = 6)
	private String docSubclass = null ;
	
	// 是否收集部件
	@ExcelField(columnIndex = 7)
	private String collectPart = null ;
	
	// 拟制人
	@ExcelField(columnIndex = 8)
	private String fictitiousPerson = null ;
	
	// 流程启动者
	private WTUser user = null ;
	
	// 部件备注
	@ExcelField(columnIndex = 9)
	private String partRemarks = null ;
	
	// 文件夹路径
	@ExcelField(columnIndex = 10)
	private String folderPath = null ;
	
	// 文件夹对象
	private Folder folder = null ;

	public String getLevel() {
		return level;
	}

	public void setLevel(String level) {
		this.level = level;
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPartClassification() {
		return partClassification;
	}

	public void setPartClassification(String partClassification) {
		this.partClassification = partClassification;
	}

	public String getProductCategory() {
		return productCategory;
	}

	public void setProductCategory(String productCategory) {
		this.productCategory = productCategory;
	}

	public String getProductCategoryInterior() {
		return productCategoryInterior;
	}

	public void setProductCategoryInterior(String productCategoryInterior) {
		this.productCategoryInterior = productCategoryInterior;
	}

	public String getDocType() {
		return docType;
	}

	public void setDocType(String docType) {
		this.docType = docType;
	}

	public String getDocSubclass() {
		return docSubclass;
	}

	public void setDocSubclass(String docSubclass) {
		this.docSubclass = docSubclass;
	}

	public String getCollectPart() {
		return collectPart;
	}

	public void setCollectPart(String collectPart) {
		this.collectPart = collectPart;
	}

	public String getFictitiousPerson() {
		return fictitiousPerson;
	}

	public void setFictitiousPerson(String fictitiousPerson) {
		this.fictitiousPerson = fictitiousPerson;
	}

	public String getPartRemarks() {
		return partRemarks;
	}

	public void setPartRemarks(String partRemarks) {
		this.partRemarks = partRemarks;
	}

	public String getFolderPath() {
		return folderPath;
	}

	public void setFolderPath(String folderPath) {
		this.folderPath = folderPath;
	}

	public TypeDefinitionReference getTypeReference() {
		return typeReference;
	}

	public void setTypeReference(TypeDefinitionReference typeReference) {
		this.typeReference = typeReference;
	}

	public WTUser getUser() {
		return user;
	}

	public void setUser(WTUser user) {
		this.user = user;
	}

	public Folder getFolder() {
		return folder;
	}

	public void setFolder(Folder folder) {
		this.folder = folder;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ProductStructureTemplateExcelBean [level=");
		builder.append(level);
		builder.append(", number=");
		builder.append(number);
		builder.append(", name=");
		builder.append(name);
		builder.append(", partClassification=");
		builder.append(partClassification);
		builder.append(", productCategory=");
		builder.append(productCategory);
		builder.append(", productCategoryInterior=");
		builder.append(productCategoryInterior);
		builder.append(", docType=");
		builder.append(docType);
		builder.append(", typeReference=");
		builder.append(typeReference);
		builder.append(", docSubclass=");
		builder.append(docSubclass);
		builder.append(", collectPart=");
		builder.append(collectPart);
		builder.append(", fictitiousPerson=");
		builder.append(fictitiousPerson);
		builder.append(", user=");
		builder.append(user == null ? "" : user.getDisplayIdentity());
		builder.append(", partRemarks=");
		builder.append(partRemarks);
		builder.append(", folderPath=");
		builder.append(folderPath);
		builder.append(", folder=");
		builder.append(folder);
		builder.append("]\n");
		return builder.toString();
	}
}
