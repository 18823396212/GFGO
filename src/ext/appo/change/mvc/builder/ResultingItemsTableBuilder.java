package ext.appo.change.mvc.builder;

import com.ptc.jca.mvc.components.JcaComponentParams;
import com.ptc.jca.mvc.components.JcaTableConfig;
import com.ptc.mvc.components.*;
import com.ptc.mvc.util.ClientMessageSource;
import com.ptc.netmarkets.util.beans.NmCommandBean;
import com.ptc.netmarkets.util.beans.NmHelperBean;
import com.ptc.windchill.enterprise.change2.mvc.builders.tables.AbstractAffectedAndResultingItemsTableBuilder;
import ext.appo.change.util.AffectedObjectUtil;
import ext.appo.change.util.ModifyUtils;
import ext.pi.core.PIWorkflowHelper;
import org.apache.log4j.Logger;
import wt.change2.ChangeHelper2;
import wt.change2.Changeable2;
import wt.change2.WTChangeActivity2;
import wt.change2.WTChangeOrder2;
import wt.fc.Persistable;
import wt.fc.QueryResult;
import wt.log4j.LogR;
import wt.session.SessionContext;
import wt.session.SessionHelper;
import wt.util.WTException;
import wt.workflow.engine.WfProcess;
import wt.workflow.work.WorkItem;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

import static ext.appo.ecn.constants.ChangeConstants.CHANGETASK_ARRAY;

@ComponentBuilder("ext.appo.change.mvc.builder.ResultingItemsTableBuilder")
public class ResultingItemsTableBuilder extends AbstractAffectedAndResultingItemsTableBuilder{

    private static final Logger log = LogR.getLogger(ResultingItemsTableBuilder.class.getName());
    private static final String TABLE_ID = "ext.appo.change.mvc.builder.ResultingItemsTableBuilder";
    ClientMessageSource messageChange2ClientResource = getMessageSource("ext.appo.ecn.resource.changeNoticeActionsRB");

    @Override
    public Object buildComponentData(ComponentConfig paramComponentConfig, ComponentParams paramComponentParams) throws Exception {
        Collection<Persistable> result = new HashSet<>();

        SessionContext previous = SessionContext.newContext();
        try {
            SessionHelper.manager.setAdministrator();
            NmHelperBean nmhelperbean = ((JcaComponentParams) paramComponentParams).getHelperBean();
            NmCommandBean commandbean = nmhelperbean.getNmCommandBean();
            HttpServletRequest request = commandbean.getRequest();
            Map<String, Object> parameterMap = commandbean.getParameterMap();
            Object object = commandbean.getActionOid().getRefObject();// 获取操作对象
            log.info("=====buildComponentData.object: " + object);

            AffectedObjectUtil affectedObjectUtil = new AffectedObjectUtil(commandbean, null);
            Map<Persistable, Map<String, String>> pageDataMap = affectedObjectUtil.PAGEDATAMAP;
            if (pageDataMap.isEmpty()) {
                if (!parameterMap.containsKey(CHANGETASK_ARRAY)) {
                    //获取已暂存或已创建ECA的数据
                    if (object instanceof WTChangeOrder2) {
                        WTChangeOrder2 changeOrder2 = (WTChangeOrder2) object;
                        // 获取ECN中所有产生对象
                        Map<WTChangeActivity2, Collection<Changeable2>> dataMap = ModifyUtils.getChangeablesAfter(changeOrder2);
                        for (Map.Entry<WTChangeActivity2, Collection<Changeable2>> entry : dataMap.entrySet()) {
                            result.addAll(entry.getValue());
                        }

                    }else if (object instanceof WorkItem) {
                        //在流程中
                        WTChangeOrder2 changeOrder2 = new WTChangeOrder2();
                        WorkItem workItem = (WorkItem) object;
                        WfProcess wfprocess = PIWorkflowHelper.service.getParentProcess(workItem);
                        Object[] objects = wfprocess.getContext().getObjects();
                        if (objects.length > 0) {
                            objects:for (int i = 0; i < objects.length; i++) {
                                if (objects[i] instanceof WTChangeActivity2) { //eca
                                    WTChangeActivity2 eca = (WTChangeActivity2) objects[i];
                                    QueryResult ecaqr = ChangeHelper2.service.getChangeOrder(eca);
                                    while (ecaqr.hasMoreElements()) {
                                        Object ecn = ecaqr.nextElement();
                                        if (ecn instanceof WTChangeOrder2) {
                                            changeOrder2 = (WTChangeOrder2) ecn;
                                            break objects;
                                        }
                                    }

                                }
                            }
                        }
                        // 获取ECN中所有产生对象
                        Map<WTChangeActivity2, Collection<Changeable2>> dataMap = ModifyUtils.getChangeablesAfter(changeOrder2);
                        for (Map.Entry<WTChangeActivity2, Collection<Changeable2>> entry : dataMap.entrySet()) {
                            result.addAll(entry.getValue());
                        }

                    }
                }
            } else {
                result.addAll(pageDataMap.keySet());
            }

        } finally {
            SessionContext.setContext(previous);
        }

        return result;
    }

    @Override
    public ComponentConfig buildComponentConfig(ComponentParams paramComponentParams) throws WTException {
        ComponentConfigFactory componentconfigfactory = getComponentConfigFactory();
        JcaTableConfig tableConfig = (JcaTableConfig) componentconfigfactory.newTableConfig();

        tableConfig.setLabel("产生的对象");
        tableConfig.setId(TABLE_ID);
        tableConfig.setSelectable(true);
        tableConfig.setShowCount(true);

        ColumnConfig columnconfig = componentconfigfactory.newColumnConfig("type_icon", true);
        columnconfig.setAutoSize(true);
        tableConfig.addComponent(columnconfig);

        columnconfig = componentconfigfactory.newColumnConfig("name", true);
        columnconfig.setAutoSize(true);
        tableConfig.addComponent(columnconfig);

        columnconfig = componentconfigfactory.newColumnConfig("number", true);
        columnconfig.setAutoSize(true);
        tableConfig.addComponent(columnconfig);

        columnconfig = componentconfigfactory.newColumnConfig("version", true);
        columnconfig.setAutoSize(true);
        tableConfig.addComponent(columnconfig);

        columnconfig = componentconfigfactory.newColumnConfig("state", true);
        columnconfig.setAutoSize(true);
        tableConfig.addComponent(columnconfig);

        columnconfig = componentconfigfactory.newColumnConfig("crDescription", true);
        columnconfig.setAutoSize(true);
        tableConfig.addComponent(columnconfig);

        columnconfig = componentconfigfactory.newColumnConfig("compare", true);
        columnconfig.setAutoSize(true);
        tableConfig.addComponent(columnconfig);

        columnconfig = componentconfigfactory.newColumnConfig("infoPageAction", true);
        columnconfig.setAutoSize(true);
        tableConfig.addComponent(columnconfig);

        return tableConfig;
    }

}
