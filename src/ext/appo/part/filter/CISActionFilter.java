package ext.appo.part.filter;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import com.ptc.core.ui.validation.DefaultSimpleValidationFilter;
import com.ptc.core.ui.validation.UIValidationCriteria;
import com.ptc.core.ui.validation.UIValidationKey;
import com.ptc.core.ui.validation.UIValidationStatus;

import ext.lang.PIExcelUtils;
import ext.pi.PIException;
import ext.pi.core.PIClassificationHelper;
import ext.pi.core.PICoreHelper;
import ext.tool.util.PrinicipalUtil;
import wt.fc.Persistable;
import wt.fc.WTReference;
import wt.inf.container.WTContainer;
import wt.inf.team.ContainerTeam;
import wt.inf.team.ContainerTeamHelper;
import wt.inf.team.ContainerTeamManaged;
import wt.org.WTGroup;
import wt.org.WTPrincipal;
import wt.org.WTPrincipalReference;
import wt.org.WTUser;
import wt.part.WTPart;
import wt.project.Role;
import wt.session.SessionHelper;
import wt.util.WTException;

public class CISActionFilter extends DefaultSimpleValidationFilter {

	public static String filePath;

	static {
		try {
			String codebase = PICoreHelper.service.getCodebase();
			filePath = codebase + File.separator + "ext" + File.separator + "appo" + File.separator + "part"
					+ File.separator + "cisConfigure.xlsx";
		} catch (PIException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public UIValidationStatus preValidateAction(UIValidationKey uiValidationKey,
			UIValidationCriteria uivalidationcriteria) {

		List<String> classList = new ArrayList<String>();
		List<String> roleList = new ArrayList<String>();
		UIValidationStatus uivalidationstatus = UIValidationStatus.HIDDEN;
		try {
			Workbook workbook = PIExcelUtils.getWorkbook(new File(filePath));
			Sheet sheet = workbook.getSheetAt(0);
			int lastRowNum = sheet.getLastRowNum();
			for (int i = 1; i < lastRowNum; i++) {
				Row row = sheet.getRow(i);
				Cell cell = row.getCell(0);
				String classVlaue = PIExcelUtils.getStringCellValue(cell);
				classList.add(classVlaue);
			}
			
			Sheet sheetAt = workbook.getSheetAt(1);
			int rowlast = sheetAt.getLastRowNum();
			for (int i = 1; i < rowlast; i++) {
				Row row = sheetAt.getRow(i);
				Cell cell = row.getCell(0);
				String classVlaue = PIExcelUtils.getStringCellValue(cell);
				roleList.add(classVlaue);
			}
			
		} catch (PIException e2) {
			e2.printStackTrace();
		}
		
		
		
		WTReference wtreference = uivalidationcriteria.getContextObject();
		Persistable per = wtreference.getObject();
		if (per != null && per instanceof WTPart) {
			WTPart part = (WTPart) per;
			WTContainer container = part.getContainer();
			boolean memberOfContainerRole = false;
				try {
					WTPrincipal pri = SessionHelper.manager.getPrincipal();
					for (int i = 0; i < roleList.size(); i++) {
						Role role = Role.toRole(roleList.get(i));
					    memberOfContainerRole = isMemberOfContainerRole(container,role,pri);
						if (memberOfContainerRole) {
							break;
						}
					}
					
				} catch (WTException e1) {
					e1.printStackTrace();
				}
			
			String state = part.getState().toString();
			try {
				Collection<String> classifyNodes = PIClassificationHelper.service.getClassifyNodes(part);
				if (classifyNodes != null) {
					Iterator<String> iterator = classifyNodes.iterator();
					while (iterator.hasNext()) {
						String next = iterator.next();
						String nodeHierarchy = PIClassificationHelper.service.getNodeHierarchy(next);
						int lastIndexOf = nodeHierarchy.lastIndexOf("/");
						String node = nodeHierarchy.substring(lastIndexOf + 1, nodeHierarchy.length());
						// 电子料、光学件和光电子   状态是已归档
						if (state.equals("ARCHIVED") && classList.contains(node) && memberOfContainerRole) {
							uivalidationstatus = UIValidationStatus.ENABLED;
						}
					}
				}
			} catch (PIException e) {
				e.printStackTrace();
			}

		}

		return uivalidationstatus;
	}
	
	public static boolean isMemberOfContainerRole(WTContainer container,
			Role role, WTPrincipal prin) {
		boolean flag = false;
		if (container == null) {
			return flag;
		}

		if (role == null) {
			return flag;
		}

		if (prin == null) {
			return flag;
		}

		try {
			if (container instanceof ContainerTeamManaged) {
				ContainerTeamManaged tm = (ContainerTeamManaged) container;
				List<WTUser> allWtps = getRoleAndUserByProject(tm, role);
				if (prin instanceof WTUser) {
					WTUser cuser = (WTUser) prin;
					if (allWtps != null) {
						if (allWtps.contains(cuser)) {
							flag = true;
						}
					}
				}
			}
		} catch (WTException e) {
			e.printStackTrace();
		}
		return flag;
	}
	
	public static List<WTUser> getRoleAndUserByProject(ContainerTeamManaged tm,
			Role role) throws WTException {
		List<WTUser> userList = new ArrayList<WTUser>();
		if (tm != null) {
			boolean enforce = wt.session.SessionServerHelper.manager
					.setAccessEnforced(false);
			try {
				ContainerTeam containerteam = ContainerTeamHelper.service
						.getContainerTeam(tm);

				List tempUserList = containerteam
						.getAllPrincipalsForTarget(role);

				for (int j = 0; tempUserList != null && j < tempUserList.size(); j++) {
					WTPrincipalReference wtprincipalreference = (WTPrincipalReference) tempUserList
							.get(j);
					Persistable persistable = wtprincipalreference.getObject();
					if (persistable instanceof WTUser) {
						userList.add((WTUser) persistable);
					} else if (persistable instanceof WTGroup) {
						List<WTUser> tempList = PrinicipalUtil
								.getGroupMembersOfUser((WTGroup) persistable);
						for (int k = 0; k < tempList.size(); k++) {
							userList.add(tempList.get(k));
						}
					}
				}
			} catch (WTException e) {
				throw new WTException("WTException" + e);
			} finally {
				wt.session.SessionServerHelper.manager
						.setAccessEnforced(enforce);
			}
		}
		return userList;
	}

}
