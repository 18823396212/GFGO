package ext.appo.change.mvc.builder;

import com.ptc.core.components.beans.CreateAndEditWizBean;
import com.ptc.jca.mvc.components.JcaComponentParams;
import com.ptc.jca.mvc.components.JcaTableConfig;
import com.ptc.mvc.components.*;
import com.ptc.mvc.util.ClientMessageSource;
import com.ptc.netmarkets.util.beans.NmCommandBean;
import com.ptc.netmarkets.util.beans.NmHelperBean;
import ext.appo.change.ModifyHelper;
import ext.appo.change.constants.ModifyConstants;
import ext.appo.ecn.constants.ChangeConstants;
import ext.lang.PIStringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import wt.change2.WTChangeOrder2;
import wt.configurablelink.ConfigurableDescribeLink;
import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.fc.ReferenceFactory;
import wt.log4j.LogR;
import wt.part.WTPart;
import wt.session.SessionContext;
import wt.session.SessionHelper;
import wt.util.WTException;

import java.util.Collection;
import java.util.HashSet;

@ComponentBuilder("ext.appo.change.mvc.builder.AffectedEndItemsTableBuilder")
public class AffectedEndItemsTableBuilder extends AbstractComponentBuilder implements ChangeConstants, ModifyConstants {

    private static final String TABLE_ID = "ext.appo.change.mvc.builder.AffectedEndItemsTableBuilder";
    ClientMessageSource messageChange2ClientResource = getMessageSource("ext.appo.ecn.resource.changeNoticeActionsRB");
    private static final Logger LOGGER = LogR.getLogger(AffectedEndItemsTableBuilder.class.getName());

    @Override
    public Object buildComponentData(ComponentConfig paramComponentConfig, ComponentParams paramComponentParams) throws Exception {
        Collection<Persistable> collection = new HashSet<>();

        SessionContext previous = SessionContext.newContext();
        try {
            SessionHelper.manager.setAdministrator();

            NmHelperBean nmHelperBean = ((JcaComponentParams) paramComponentParams).getHelperBean();
            Object object = nmHelperBean.getNmCommandBean().getActionOid().getRefObject();
            LOGGER.info("=====buildComponentData.object: " + object);

            //ECN关联的受影响产品
            if (object instanceof WTChangeOrder2) {
                WTChangeOrder2 changeOrder2 = (WTChangeOrder2) object;
                collection.addAll(ModifyHelper.service.queryPersistable(changeOrder2, LINKTYPE_2));
            }

            //新添加数据
            addDatasArray(paramComponentParams, collection);
        } finally {
            SessionContext.setContext(previous);
        }
        return collection;
    }

    @Override
    public ComponentConfig buildComponentConfig(ComponentParams paramComponentParams) throws WTException {
        ComponentConfigFactory componentconfigfactory = getComponentConfigFactory();
        JcaTableConfig tableConfig = (JcaTableConfig) componentconfigfactory.newTableConfig();

        tableConfig.setLabel(this.messageChange2ClientResource.getMessage("changeNotice.affectedEndItemsTableBuilder.description"));
        tableConfig.setId(TABLE_ID);
        tableConfig.setSelectable(true);
        tableConfig.setShowCount(true);

        NmHelperBean localNmHelperBean = ((JcaComponentParams) paramComponentParams).getHelperBean();
        NmCommandBean localNmCommandBean = localNmHelperBean.getNmCommandBean();
        boolean bool = CreateAndEditWizBean.isCreateEditWizard(localNmCommandBean);
        if (bool) {
            tableConfig.setActionModel("affectedEndItems.table.create_remove");
        }

        ColumnConfig columnconfig = componentconfigfactory.newColumnConfig("type_icon", true);
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

        columnconfig = componentconfigfactory.newColumnConfig("infoPageAction", false);
        columnconfig.setAutoSize(true);
        tableConfig.addComponent(columnconfig);

        columnconfig = componentconfigfactory.newColumnConfig("state", true);
        columnconfig.setAutoSize(true);
        tableConfig.addComponent(columnconfig);

        columnconfig = componentconfigfactory.newColumnConfig("ggms", true);
        columnconfig.setLabel(this.messageChange2ClientResource.getMessage("COLUMNNAME_GGMS"));
        columnconfig.setAutoSize(true);
        columnconfig.setDataUtilityId("customizationDataUtility");
        tableConfig.addComponent(columnconfig);

        columnconfig = componentconfigfactory.newColumnConfig("sscpx", true);
        columnconfig.setLabel(this.messageChange2ClientResource.getMessage("COLUMNNAME_SSCPX"));
        columnconfig.setAutoSize(true);
        columnconfig.setDataUtilityId("customizationDataUtility");
        tableConfig.addComponent(columnconfig);

        columnconfig = componentconfigfactory.newColumnConfig("ssxm", true);
        columnconfig.setLabel(this.messageChange2ClientResource.getMessage("COLUMNNAME_SSXM"));
        columnconfig.setAutoSize(true);
        columnconfig.setDataUtilityId("customizationDataUtility");
        tableConfig.addComponent(columnconfig);

        columnconfig = componentconfigfactory.newColumnConfig("xsxh", true);
        columnconfig.setLabel(this.messageChange2ClientResource.getMessage("COLUMNNAME_XSXH"));
        columnconfig.setAutoSize(true);
        columnconfig.setDataUtilityId("customizationDataUtility");
        tableConfig.addComponent(columnconfig);

        columnconfig = componentconfigfactory.newColumnConfig("cpzt", true);
        columnconfig.setLabel(this.messageChange2ClientResource.getMessage("COLUMNNAME_CPZT"));
        columnconfig.setAutoSize(true);
        columnconfig.setDataUtilityId("customizationDataUtility");
        tableConfig.addComponent(columnconfig);

        columnconfig = componentconfigfactory.newColumnConfig("nbxh", true);
        columnconfig.setLabel(this.messageChange2ClientResource.getMessage("COLUMNNAME_NBXH"));
        columnconfig.setAutoSize(true);
        columnconfig.setDataUtilityId("customizationDataUtility");
        tableConfig.addComponent(columnconfig);

        columnconfig = componentconfigfactory.newColumnConfig("childNumber", true);
        columnconfig.setLabel(this.messageChange2ClientResource.getMessage("COLUMNNAME_CHILDNUMBER"));
        columnconfig.setDataUtilityId("customizationDataUtility");
        columnconfig.setAutoSize(true);
        tableConfig.addComponent(columnconfig);

        return tableConfig;
    }

    /***
     * 根据ChangeOrder2对象查询关联的成品对象
     * @param paramComponentParams
     * @return
     */
    public Collection<WTPart> getEndItemsByChangeOrder2(ComponentParams paramComponentParams) throws WTException {
        Collection<WTPart> collection = new HashSet<>();
        if (paramComponentParams == null) return collection;

        NmHelperBean helper = ((JcaComponentParams) paramComponentParams).getHelperBean();
        Object pbo = helper.getNmCommandBean().getActionOid().getRefObject();
        if (pbo instanceof WTChangeOrder2) {
            QueryResult qr = PersistenceHelper.manager.navigate((WTChangeOrder2) pbo, ConfigurableDescribeLink.DESCRIBED_BY_ROLE, ConfigurableDescribeLink.class, true);
            while (qr.hasMoreElements()) {
                Object value = qr.nextElement();
                if (value instanceof WTPart) {
                    collection.add((WTPart) value);
                }
            }
        }

        return collection;
    }

    /***
     * 新增数据
     * @param paramComponentParams
     * @param collection
     *            新增产品OID集合
     * @throws WTException
     */
    public void addDatasArray(ComponentParams paramComponentParams, Collection<Persistable> collection) throws Exception {
        NmHelperBean helper = ((JcaComponentParams) paramComponentParams).getHelperBean();
        String datasJSON = helper.getNmCommandBean().getRequest().getParameter(AFFECTED_PRODUCT_ID);
        if (PIStringUtils.isNotNull(datasJSON)) {
            JSONArray jsonArray = new JSONArray(datasJSON);
            for (int i = 0; i < jsonArray.length(); i++) {
                String partOID = jsonArray.getString(i);
                if (PIStringUtils.isNull(partOID)) continue;

                WTPart part = (WTPart) ((new ReferenceFactory()).getReference(partOID).getObject());
                if (!collection.contains(part)) collection.add(part);
            }
        }
    }

}
