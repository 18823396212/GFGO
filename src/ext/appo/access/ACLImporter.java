package ext.appo.access;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;

import wt.access.LoadAccessControlRules;
import wt.util.WTException;

public class ACLImporter {
	
	public static List<Hashtable> parseAclCsvFile(String csvPathFile) throws IOException {
		FileReader fr = new FileReader(csvPathFile);
		BufferedReader br = new BufferedReader(fr);
		List<Hashtable> ruleList = new ArrayList<Hashtable>();
		String line = null;
		while ((line = br.readLine()) != null) {
			if (StringUtils.isEmpty(line)) {
				break;
			}
			if (line.startsWith("#")) {
				continue;
			}
			
			String[] params = line.split(",");
			if (params.length < 8 || !line.startsWith("AccessRule")) {
				System.out.println("***WARNNING*** Invalid ACL rule line:" + line);
				continue;
			}
			
			Hashtable ruleInfo = new Hashtable();
			ruleInfo.put("user", params[1]);
			ruleInfo.put("domain", params[2]);
			ruleInfo.put("typeId", params[3]);
			ruleInfo.put("permission", params[4]);
			ruleInfo.put("principal", params[5]);
			ruleInfo.put("permissionList", params[6]);
			ruleInfo.put("state", params[7]);
			
			ruleList.add(ruleInfo);
		}
		return ruleList;
	}
	
	public static void createAclIntoMultiDomain(List<Hashtable> aclInfoList, List<String> targetDomainPath, boolean cleanBeforeImport) throws WTException {
		for (String domainPath : targetDomainPath) {
			createAclIntoDomain(aclInfoList, domainPath, cleanBeforeImport);
		}
	}
	
	public static void createAclIntoDomain(List<Hashtable> aclInfoList, String targetDomainPath, boolean cleanBeforeImport) throws WTException {
		if (cleanBeforeImport) {
			ACLExporter.deleteAllACL(targetDomainPath); //delete all ACL in target domain frist!
		}
		for (Hashtable ruleInfo : aclInfoList) {
			ruleInfo.put("domain", targetDomainPath);
			Hashtable cmdLine = new Hashtable();
			Vector returnObject = new Vector();
			LoadAccessControlRules.createAccessControlRule(ruleInfo, cmdLine, returnObject);
		}
	}
	
	public static void createAclIntoDomain(Hashtable ruleInfo, String targetDomainPath) {
		ruleInfo.put("domain", targetDomainPath);
		Hashtable cmdLine = new Hashtable();
		Vector returnObject = new Vector();
		LoadAccessControlRules.createAccessControlRule(ruleInfo, cmdLine, returnObject);
	}

}
