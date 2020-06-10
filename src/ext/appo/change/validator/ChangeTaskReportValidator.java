package ext.appo.change.validator;

import com.ptc.core.ui.validation.DefaultSimpleValidationFilter;
import com.ptc.core.ui.validation.UIValidationCriteria;
import com.ptc.core.ui.validation.UIValidationKey;
import com.ptc.core.ui.validation.UIValidationStatus;
import ext.appo.change.util.ModifyUtils;
import ext.com.org.OrgUtil;
import ext.pi.core.PIPrincipalHelper;
import org.apache.log4j.Logger;
import wt.change2.WTChangeActivity2;
import wt.change2.WTChangeOrder2;
import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.fc.WTReference;
import wt.log4j.LogR;
import wt.org.*;
import wt.session.SessionHelper;
import wt.util.WTException;
import wt.workflow.engine.WfEngineHelper;
import wt.workflow.engine.WfProcess;
import wt.workflow.engine.WfState;

import java.util.Collection;
import java.util.Enumeration;

/**
 * 事务性任务跟踪报表过滤
 * 用户必须是管理员 或 事务性任务管理群组 成员
 */
public class ChangeTaskReportValidator extends DefaultSimpleValidationFilter {

    private static final Logger LOGGER = LogR.getLogger(ChangeTaskReportValidator.class.getName());

    @Override
    public UIValidationStatus preValidateAction(UIValidationKey key, UIValidationCriteria criteria) {
        UIValidationStatus status = UIValidationStatus.HIDDEN;
        try {
            WTPrincipal principal = SessionHelper.manager.getPrincipal();
            boolean flag = PIPrincipalHelper.service.isOrganizationAdministrator((WTUser) principal);
            WTOrganization wtorganization = OrganizationServicesHelper.manager.getOrganization(principal);
            WTGroup group = OrgUtil.getGroupByName( wtorganization , "事务性任务管理群组" );
            boolean isGroupUser=false;
            if(group != null){
                isGroupUser = isGroupUser(principal , group);
            }
            if (flag||isGroupUser) {
                status = UIValidationStatus.ENABLED;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return status;
    }

    //判断用户是否是特殊组内成员
    public static boolean isGroupUser(WTPrincipal currentPrincipal , WTGroup group) throws WTException {
        boolean isGroupUser = false ;
        if( group != null ){
            //获取组成员
            Enumeration members= group.members();
            while( members.hasMoreElements()) {
                Object userObj =  members.nextElement();
                if( userObj != null && userObj instanceof WTPrincipal ){
                    WTPrincipal groupPrincipal = ( WTPrincipal ) userObj ;
                    isGroupUser = PersistenceHelper.isEquivalent( groupPrincipal , currentPrincipal ) ;
                    if( isGroupUser ){
                        return isGroupUser ;
                    }
                }
            }
        }


        return isGroupUser ;
    }
}