package ext.appo.access;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import wt.method.RemoteAccess;
import wt.method.RemoteMethodServer;
import wt.pom.Transaction;
import wt.util.WTException;

public class ACLHelper implements RemoteAccess, Serializable {

	private static final long serialVersionUID = 4435309049565969187L;

	private static void printUsage() {
		System.out.println("Windchill ACL Toolbox");
		System.out.println();
		System.out.println("- To clean all ACL in a domian:");
		System.out.println("  windchill com.catl.common.access.ACLHelper clean [domain_path] [username] [password]");
		System.out.println();
		System.out.println("- To export all ACL in a domian as CSV format file:");
		System.out.println("  windchill com.catl.common.access.ACLHelper export [domain_path] [csv_file_path_and_name] [username] [password]");
		System.out.println();
		System.out.println("- To sync all ACL from a domian to other domains");
		System.out.println("  windchill com.catl.common.access.ACLHelper sync [source_domain_Path] [target_domain_path1,target_domain_path2...] [username] [password]");
		System.out.println();
		System.out.println("- To import ACL using CSV format file to multi domains without clean");
		System.out.println("  windchill com.catl.common.access.ACLHelper importcsv [csv_file_path] [target_domain_path1,target_domain_path2...] [username] [password]");
		System.out.println();
		System.out.println("- To import ACL using CSV format file to multi domains after clean");
		System.out.println("  windchill com.catl.common.access.ACLHelper fillwith [csv_file_path] [target_domain_path1,target_domain_path2...] [username] [password]");		
		System.out.println();
		System.out.println();
		System.out.println("domain path example:");
		System.out.println("[/wt.inf.container.OrgContainer=orgName]/Default");
		System.out.println("[/wt.inf.container.OrgContainer=CATL/wt.pdmlink.PDMLinkProduct=ZZT]/Default");
	}

	public void cleanDomainACL(String domainPath) throws RemoteException, InvocationTargetException {
		Class argTypes[] = { String.class };
		Object args[] = { domainPath };
		RemoteMethodServer.getDefault().invoke("_cleanDomainACL", null, this, argTypes, args);
	}

	public void _cleanDomainACL(String domainPath) throws WTException {
		Transaction tx = null;
		try {
			tx = new Transaction();
			tx.start();
			ACLExporter.deleteAllACL(domainPath);
			tx.commit();
		} catch (WTException e) {
			if (tx != null) {
				tx.rollback();
				throw e;
			}
		}
	}

	public void exportDomainACL(String domainPath, String csvFilePath) throws RemoteException, InvocationTargetException {
		Class argTypes[] = { String.class, String.class };
		Object args[] = { domainPath, csvFilePath };
		RemoteMethodServer.getDefault().invoke("_exportDomainACL", null, this, argTypes, args);
	}

	public void _exportDomainACL(String domainPath, String csvFilePath) throws IOException, WTException {
		ACLExporter.exportDomainACLtoCSVFile(domainPath, csvFilePath);
	}
	
	public void synchronizeDomainACL(String sourceDomainPath, String targetDomainPath) throws RemoteException, InvocationTargetException {
		Class argTypes[] = { String.class, String.class };
		Object args[] = { sourceDomainPath, targetDomainPath };
		RemoteMethodServer.getDefault().invoke("_synchronizeDomainACL", null, this, argTypes, args);
	}
	
	public void _synchronizeDomainACL(String sourceDomainPath, String targetDomainPath) throws WTException {
		if (null == targetDomainPath || targetDomainPath.length() == 0 ) {
			System.out.println("Invalid target domain path");
			return;
		}
		String[] targetDomainPaths = targetDomainPath.split(",");
		List<String> targetDomainList = new ArrayList<String>();
		for (String s : targetDomainPaths) {
			if (StringUtils.isNotEmpty(s)) {
				targetDomainList.add(s);
			}
		}
		Transaction tx = null;
		try {
			tx = new Transaction();
			tx.start();
			ACLUtil.synchronizeDomainACL(sourceDomainPath, targetDomainList);
			tx.commit();
		} catch (WTException e) {
			if (tx != null) {
				tx.rollback();
				throw e;
			}
		}
	}
	
	public void importAclFromCSVToMultiDomain(String csvFilePath, String targetDomainPath, boolean cleanBeforeImport) throws RemoteException, InvocationTargetException {
		Class argTypes[] = { String.class, String.class, boolean.class };
		Object args[] = { csvFilePath, targetDomainPath, cleanBeforeImport };
		RemoteMethodServer.getDefault().invoke("_importAclFromCSVToMultiDomain", null, this, argTypes, args);
	}
	
	public void _importAclFromCSVToMultiDomain(String csvFilePath, String targetDomainPath, boolean cleanBeforeImport) throws IOException {
		if (null == targetDomainPath || targetDomainPath.length() == 0 ) {
			System.out.println("Invalid target domain path");
			return;
		}
		String[] targetDomainPaths = targetDomainPath.split(",");
		List<String> targetDomainList = new ArrayList<String>();
		for (String s : targetDomainPaths) {
			if (StringUtils.isNotEmpty(s)) {
				targetDomainList.add(s);
			}
		}
		
		Transaction tx = null;
		try {
			tx = new Transaction();
			tx.start();
			List<Hashtable> ruleList = ACLImporter.parseAclCsvFile(csvFilePath);
			ACLImporter.createAclIntoMultiDomain(ruleList, targetDomainList, cleanBeforeImport);
			tx.commit();
		} catch (WTException e) {
			if (tx != null) {
				tx.rollback();
			}
		}
	}

	private static void setToken(String username, String password) {
		RemoteMethodServer.getDefault().setUserName(username);
		RemoteMethodServer.getDefault().setPassword(password);
	}
	
	private static void checkArgs(String[] args, int minArgs) {
		if (args == null || args.length == 0) {
			printUsage();
			System.exit(0);
		}
		
		if (args.length < minArgs) {
			printUsage();
			System.exit(0);
		}
	}

	public static void main(String[] args) throws RemoteException, InvocationTargetException {
		if (args == null || args.length == 0) {
			printUsage();
			return;
		}

		ACLHelper helper = new ACLHelper();

		if ("clean".equals(args[0])) {
			// delete domain ACLs
			// java className clean domainPath username password
			checkArgs(args, 4);
			setToken(args[2], args[3]);
			helper.cleanDomainACL(args[1]);
			return;
		}

		if ("export".equals(args[0])) {
			// export ACL to CSV format file
			// java className export domainPath csvFilePath username password
			checkArgs(args, 5);
			setToken(args[3], args[4]);
			helper.exportDomainACL(args[1], args[2]);
			return;
		}

		if ("sync".equals(args[0])) {
			// export ACL to CSV format file
			// java className sync [domainPath] [target_domain_path1,target_domain_path2...] [username] [password]
			checkArgs(args, 5);
			setToken(args[3], args[4]);
			helper.synchronizeDomainACL(args[1], args[2]);
			return;
		}
		
		if ("importcsv".equals(args[0])) {
			// export ACL to CSV format file
			// java className importcsv [csv_file_path] [target_domain_path1,target_domain_path2...] [username] [password]
			checkArgs(args, 5);
			setToken(args[3], args[4]);
			helper.importAclFromCSVToMultiDomain(args[1], args[2], false);
			return;
		}
		
		if ("fillwith".equals(args[0])) {
			// export ACL to CSV format file
			// java className fillwith [csv_file_path] [target_domain_path1,target_domain_path2...] [username] [password]
			checkArgs(args, 5);
			setToken(args[3], args[4]);
			helper.importAclFromCSVToMultiDomain(args[1], args[2], true);
			return;
		}
		
		printUsage();
	}

}
