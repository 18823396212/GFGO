package ext.appo.ecn.mvc.builder;

import com.ptc.jca.mvc.components.JcaColumnConfig;
import com.ptc.jca.mvc.components.JcaComponentParams;
import com.ptc.jca.mvc.components.JcaTableConfig;
import com.ptc.mvc.components.*;
import com.ptc.mvc.util.ClientMessageSource;
import com.ptc.netmarkets.util.beans.NmCommandBean;
import com.ptc.netmarkets.util.beans.NmHelperBean;
import ext.appo.ecn.common.util.ChangePartQueryUtils;
import ext.appo.ecn.constants.ChangeConstants;
import ext.lang.PIStringUtils;
import ext.pi.core.PIAccessHelper;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import wt.access.AccessControlHelper;
import wt.access.AccessPermission;
import wt.fc.ObjectReference;
import wt.fc.Persistable;
import wt.fc.QueryResult;
import wt.fc.ReferenceFactory;
import wt.log4j.LogR;
import wt.org.WTPrincipalReference;
import wt.part.PartDocHelper;
import wt.part.WTPart;
import wt.part.WTPartUsageLink;
import wt.session.SessionContext;
import wt.session.SessionHelper;
import wt.session.SessionServerHelper;
import wt.util.WTException;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

@ComponentBuilder(value = "ext.appo.ecn.mvc.builder.CollectItemsTableBuilder")
public class CollectItemsTableBuilder extends AbstractComponentBuilder {

    private static final String CLASSNAME = CollectItemsTableBuilder.class.getName();
    private static final Logger LOG = LogR.getLogger(CLASSNAME);

    ClientMessageSource messageChange2ClientResource = getMessageSource("ext.appo.ecn.resource.changeNoticeActionsRB");

    @Override
    public Object buildComponentData(ComponentConfig paramComponentConfig, ComponentParams paramComponentParams) throws Exception {
        SessionContext previous = SessionContext.newContext();
        try {
            // 当前用户设置为管理员，用于忽略权限
            SessionHelper.manager.setAdministrator();

            NmHelperBean nmhelperbean = ((JcaComponentParams) paramComponentParams).getHelperBean();
            NmCommandBean commandbean = nmhelperbean.getNmCommandBean();
            HttpServletRequest request = commandbean.getRequest();
            //获取选择的部件Oid
            String selectOid = request.getParameter("selectOid");
            if (LOG.isDebugEnabled()) {
                LOG.debug("selectOid = " + selectOid);
            }
            return collectItems(selectOid);
        } finally {
            SessionContext.setContext(previous);
        }
    }

    @Override
    public ComponentConfig buildComponentConfig(ComponentParams componentparams) throws WTException {
        ComponentConfigFactory componentconfigfactory = getComponentConfigFactory();
        JcaTableConfig tableConfig = (JcaTableConfig) componentconfigfactory.newTableConfig();
        tableConfig.setId("ext.appo.ecn.mvc.builder.CollectItemsTableBuilder");
        tableConfig.setLabel(this.messageChange2ClientResource.getMessage("COLLECTITEMS_TABLE_NAME"));
        tableConfig.setSelectable(true);
        tableConfig.setShowCount(true);

        ColumnConfig columnconfig = componentconfigfactory.newColumnConfig("name", true);
        columnconfig.setAutoSize(true);
        columnconfig.setDataUtilityId("customizationDataUtility");
        tableConfig.addComponent(columnconfig);

        columnconfig =  componentconfigfactory.newColumnConfig("number", true);
        columnconfig.setAutoSize(true);
        columnconfig.setDataUtilityId("customizationDataUtility");
        tableConfig.addComponent(columnconfig);

        columnconfig = componentconfigfactory.newColumnConfig("version", true);
        columnconfig.setAutoSize(true);
        columnconfig.setDataUtilityId("customizationDataUtility");
        tableConfig.addComponent(columnconfig);

        columnconfig = componentconfigfactory.newColumnConfig("state", true);
        columnconfig.setAutoSize(true);
        columnconfig.setDataUtilityId("customizationDataUtility");
        tableConfig.addComponent(columnconfig);

        columnconfig = componentconfigfactory.newColumnConfig("ggms", true);
        columnconfig.setLabel(this.messageChange2ClientResource.getMessage("COLUMNNAME_GGMS"));
        columnconfig.setAutoSize(true);
        columnconfig.setDataUtilityId("customizationDataUtility");
        tableConfig.addComponent(columnconfig);

        return tableConfig;
    }

    /***
     * 获取部件上层父件及相关联的文档对象
     *
     * @param selectOid
     *            部件OID数组字符串
     * @return
     */
    public Collection<Persistable> collectItems(String selectOid) {
        Collection<Persistable> itemsArray = new HashSet<>();
        if (PIStringUtils.isNull(selectOid)) {
            return itemsArray;
        }

        try {
            // 部件对象集合
            Collection<WTPart> partArray = new HashSet<>();
            if (selectOid.contains(ChangeConstants.USER_KEYWORD)) {
                String[] oidStr = selectOid.split(ChangeConstants.USER_KEYWORD);
                for (String s : oidStr) {
                    Persistable persistable = ((new ReferenceFactory()).getReference(s).getObject());
                    if (persistable instanceof WTPart) {
                        partArray.add((WTPart) persistable);
                    }
                }
            } else {
                Persistable persistable = ((new ReferenceFactory()).getReference(selectOid).getObject());
                if (persistable instanceof WTPart) {
                    partArray.add((WTPart) persistable);
                }
            }
            for (WTPart part : partArray) {
                // 获取部件关联文档
                QueryResult qr = PartDocHelper.service.getAssociatedDocuments(part);
                while (qr.hasMoreElements()) {
                    Object object = qr.nextElement();
                    if (object instanceof ObjectReference) {
                        object = ((ObjectReference) object).getObject();
                    }
                    itemsArray.add((Persistable) object);
                }
            }
            // 批量查询上层父件
            Map<WTPart, WTPartUsageLink> parentMap = ChangePartQueryUtils.batchQueryParentParts(partArray);
            if (parentMap != null && parentMap.size() > 0) {
                Collection<WTPart> parentArray = parentMap.keySet();
                // 过滤非最新部件
                itemsArray.addAll(ChangePartQueryUtils.excludeNonLatestVersionsPart(parentArray));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return itemsArray;
    }

    /***
     * 动态授予用户读取对象权限
     *
     * @param selectOids
     *            对象OID集合
     */
    public void grantReadPersistablePermissions(String selectOids) {
        if (PIStringUtils.isNull(selectOids)) {
            return;
        }

        // 收集需要赋予权限的对象
        Collection<Persistable> ptArray = new HashSet<Persistable>();
        // 忽略权限
        boolean flage = SessionServerHelper.manager.setAccessEnforced(false);
        try {
            if (selectOids.contains(ChangeConstants.USER_KEYWORD)) {
                String[] oidStr = selectOids.split(ChangeConstants.USER_KEYWORD);
                for (int i = 0; i < oidStr.length; i++) {
                    ptArray.add(((new ReferenceFactory()).getReference(oidStr[i]).getObject()));
                }
            } else if (selectOids.contains("[") && selectOids.contains("]")) {
                JSONArray jsonArray = new JSONArray(selectOids);
                for (int i = 0; i < jsonArray.length(); i++) {
                    ptArray.add(((new ReferenceFactory()).getReference(jsonArray.getString(i)).getObject()));
                }
            } else {
                ptArray.add(((new ReferenceFactory()).getReference(selectOids).getObject()));
            }
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        } finally {
            SessionServerHelper.manager.setAccessEnforced(flage);
        }

        // 赋予读取权限
        try {
            for (Persistable persistable : ptArray) {
                if (!AccessControlHelper.manager.hasAccess(SessionHelper.manager.getPrincipal(), persistable, AccessPermission.READ)) {
                    PIAccessHelper.service.grantPersistablePermissions(persistable, WTPrincipalReference.newWTPrincipalReference(SessionHelper.manager.getPrincipal()), AccessPermission.READ, true);
                }
            }
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }
}
