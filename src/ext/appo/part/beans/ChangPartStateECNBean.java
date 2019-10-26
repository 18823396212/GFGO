package ext.appo.part.beans;

public class ChangPartStateECNBean {
	
		private String uuid;
		private String partoid;
	    private String partvid;
	    private String partName;
	    private String partNumber;
	    private String ecnoid;
	    private String ecnName;
	    private String ecnNumber;
	    
	    public ChangPartStateECNBean() {
			super();
		}

	    public String getUuid() {
			return uuid;
		}
		public void setUuid(String uuid) {
			this.uuid = uuid;
		}
		public String getPartoid() {
			return partoid;
		}
		public void setPartoid(String partoid) {
			this.partoid = partoid;
		}
		public String getPartvid() {
			return partvid;
		}
		public void setPartvid(String partvid) {
			this.partvid = partvid;
		}
		public String getPartName() {
			return partName;
		}
		public void setPartName(String partName) {
			this.partName = partName;
		}
		public String getPartNumber() {
			return partNumber;
		}
		public void setPartNumber(String partNumber) {
			this.partNumber = partNumber;
		}
		public String getEcnoid() {
			return ecnoid;
		}
		public void setEcnoid(String ecnoid) {
			this.ecnoid = ecnoid;
		}
		public String getEcnName() {
			return ecnName;
		}
		public void setEcnName(String ecnName) {
			this.ecnName = ecnName;
		}
		public String getEcnNumber() {
			return ecnNumber;
		}
		public void setEcnNumber(String ecnNumber) {
			this.ecnNumber = ecnNumber;
		}
		
		public ChangPartStateECNBean(String uuid, String partoid, String partvid, String partName, String partNumber,
				String ecnoid, String ecnName, String ecnNumber) {
			super();
			this.uuid = uuid;
			this.partoid = partoid;
			this.partvid = partvid;
			this.partName = partName;
			this.partNumber = partNumber;
			this.ecnoid = ecnoid;
			this.ecnName = ecnName;
			this.ecnNumber = ecnNumber;
		}
	    
}
