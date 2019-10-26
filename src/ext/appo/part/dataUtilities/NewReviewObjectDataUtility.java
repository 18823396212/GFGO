//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package ext.appo.part.dataUtilities;

import com.ptc.core.components.descriptor.ModelContext;
import com.ptc.core.components.factory.dataUtilities.AttributeDataUtilityHelper;
import com.ptc.core.components.rendering.guicomponents.*;
import com.ptc.netmarkets.model.NmOid;
import com.ptc.netmarkets.util.beans.NmCommandBean;
import com.ptc.windchill.enterprise.change2.dataUtilities.ChangeLinkAttributeDataUtility;

import java.rmi.RemoteException;

import ext.pi.core.PIWorkflowHelper;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import wt.log4j.LogR;
import wt.part.WTPart;
import wt.session.SessionContext;
import wt.session.SessionHelper;
import wt.util.WTException;
import wt.workflow.engine.WfProcess;
import wt.workflow.work.WfAssignedActivity;
import wt.workflow.work.WorkItem;

public class NewReviewObjectDataUtility extends ChangeLinkAttributeDataUtility {
    private static String CLASSNAME = NewReviewObjectDataUtility.class.getName();
    private static final Logger LOGGER;

    static {
        LOGGER = LogR.getLogger(CLASSNAME);
    }

    public NewReviewObjectDataUtility() {
    }

    public Object getDataValue(String paramString, Object paramObject, ModelContext paramModelContext) throws WTException {
        SessionContext previous = SessionContext.newContext();
        try {
            // 当前用户设置为管理员，用于忽略权限
            SessionHelper.manager.setAdministrator();

            if(paramObject instanceof WTPart) {
                NmCommandBean nmCommandBean = paramModelContext.getNmCommandBean();
                NmOid pageOid = nmCommandBean.getPageOid();
                Object refObject = pageOid.getRefObject();
                WTPart part = (WTPart) paramObject;
                if (refObject != null && refObject instanceof WorkItem) {
                    WorkItem item = (WorkItem) refObject;
                    WfAssignedActivity wfassignedactivity = (WfAssignedActivity) item.getSource().getObject();
                    String wfactivityname = wfassignedactivity.getName();
                    if (wfactivityname.equals("编制") || wfactivityname.equals("修改")) {
                        if (paramString.equals("ChangeReason")) {
                            GUIComponentArray gui_array = new GUIComponentArray();
                            gui_array.addGUIComponent(generateTextArea(paramModelContext, paramObject, paramString,
                                    getValue(paramModelContext, paramObject, paramString)));
                            return gui_array;
                        } else if (paramString.equals("PreChangeContent")) {
                            GUIComponentArray gui_array = new GUIComponentArray();
                            gui_array.addGUIComponent(generateTextArea(paramModelContext, paramObject, paramString,
                                    getValue(paramModelContext, paramObject, paramString)));
                            return gui_array;
                        }else if (paramString.equals("PostChangeContent")) {
                            GUIComponentArray gui_array = new GUIComponentArray();
                            gui_array.addGUIComponent(generateTextArea(paramModelContext, paramObject, paramString,
                                    getValue(paramModelContext, paramObject, paramString)));
                            return gui_array;
                        }
//                        else if (paramString.equals("SpecNo")) {
//                            GUIComponentArray gui_array = new GUIComponentArray();
//                            gui_array.addGUIComponent(generateTextBox(paramModelContext, paramObject, paramString,
//                                    getValue(paramModelContext, paramObject, paramString)));
//                            return gui_array;
//                        }
                        GUIComponentArray gui_array = new GUIComponentArray();
                        gui_array.addGUIComponent(generateTextDisplayComponent(paramModelContext,
                                paramObject, paramString, ""));
                        return gui_array;
                    } else {
                        WfProcess process = PIWorkflowHelper.service.getParentProcess(item);
                        if (paramString.equals("ChangeReason")) {
                            return getValue(paramModelContext, paramObject, paramString);
                        } else if (paramString.equals("PreChangeContent")) {
                            return getValue(paramModelContext, paramObject, paramString);
                        } else if (paramString.equals("PostChangeContent")) {
                            return getValue(paramModelContext, paramObject, paramString);
                        }
//                        else if (paramString.equals("SpecNo")) {
//                            String changeContent = (String) process.getContext().getValue("specNo");
//                            return changeContent;
//                        }

                    }

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new WTException(e.getLocalizedMessage()) ;
        } finally{
            SessionContext.setContext(previous);
        }
        return  null;
    }



    /***
     * 获取每一列的属性值
     *
     * @param paramModelContext
     *
     * @param paramObject
     *            表单行对象
     * @param compidPartAttrChangeReviewObjectTableBuilder
     *            表单列名称
     * @return
     * @throws WTException
     */
    public Object getValue(ModelContext paramModelContext, Object paramObject, String compid) throws WTException, RemoteException {
        String value="";
        if(paramObject instanceof WTPart){
            NmCommandBean nmCommandBean = paramModelContext.getNmCommandBean();
            NmOid pageOid = nmCommandBean.getPageOid();
            Object refObject = pageOid.getRefObject();
            WorkItem item = (WorkItem) refObject;
            WfProcess process = PIWorkflowHelper.service.getParentProcess(item);
            WTPart part = (WTPart)paramObject ;

            if(compid.equals("ChangeReason")){
                String crValues ="";
                String changeReason = (String) process.getContext().getValue("changeReason");
                try {
                    JSONObject jsonObject = new JSONObject(changeReason);
                    crValues = (String) jsonObject.get(String.valueOf(part.getBranchIdentifier())==null?"":String.valueOf(part.getBranchIdentifier()));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return crValues;
            }else if(compid.equals("PreChangeContent")){
                String preValues ="";
                String preChangeContent = (String) process.getContext().getValue("preChangeContent");
                try {
                    JSONObject jsonObject = new JSONObject(preChangeContent);
                    preValues = (String) jsonObject.get(String.valueOf(part.getBranchIdentifier())==null?"":String.valueOf(part.getBranchIdentifier()));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return preValues;

            }else if(compid.equals("PostChangeContent")){
                String postValues ="";
                String changeReason = (String) process.getContext().getValue("postChangeContent");
                try {
                    JSONObject jsonObject = new JSONObject(changeReason);
                    postValues = (String) jsonObject.get(String.valueOf(part.getBranchIdentifier())==null?"":String.valueOf(part.getBranchIdentifier()));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return postValues;

            }
//            else if(compid.equals("SpecNo")){
//                String changeContent = (String) process.getContext().getValue("specNo");
////                System.out.println("物料"+part+"SpecNo返回值=="+changeContent);
//                return changeContent;
//            }
        }

//        System.out.println("属性修改返回值=="+compid+"==="+value);

        return  value;
    }

    /***
     * 构建文本输入框
     *
     * @param paramModelContext
     *
     * @param paramObject
     *            表单行对象
     * @param keyStr
     *            表单列名称
     * @param value
     *            输入框填充值
     * @return
     * @throws WTException
     */
    public TextArea generateTextArea(ModelContext paramModelContext, Object paramObject, String keyStr, Object value) throws WTException{
        TextArea textArea = new TextArea();
        textArea.setWidth(78);
        textArea.setHeight(5);
        textArea.addJsAction("onChange", "delaySaveChangeTaskArray() ;") ;
        textArea.setEnabled(true);
        textArea.setRequired(true);
        textArea.setId(keyStr);
        textArea.setValue(value == null ? "" : (String)value);
        textArea.setColumnName(AttributeDataUtilityHelper.getColumnName(keyStr, paramObject, paramModelContext));
        return textArea ;
    }


    /***
     * 构建文档显示框
     *
     * @param paramModelContext
     *
     * @param paramObject
     *            表单行对象
     * @param keyStr
     *            表单列名称
     * @param value
     *            填充值
     * @return
     * @throws WTException
     */
    public TextDisplayComponent generateTextDisplayComponent(ModelContext paramModelContext, Object paramObject, String keyStr, Object value) throws WTException{
        TextDisplayComponent gui = new TextDisplayComponent(keyStr);
        gui.setId(keyStr);
        gui.setValue(value == null ? "" : (String)value);
        gui.setName(AttributeDataUtilityHelper.getColumnName(keyStr, paramObject, paramModelContext));
        return gui ;
    }

    /***
     * 构建文本输入框
     *
     * @param paramModelContext
     *
     * @param paramObject
     *            表单行对象
     * @param keyStr
     *            表单列名称
     * @param value
     *            输入框填充值
     * @return
     * @throws WTException
     */
    public TextBox generateTextBox(ModelContext paramModelContext, Object paramObject, String keyStr, Object value) throws WTException{
        TextBox textBox = new TextBox();
        textBox.setWidth(100);
        textBox.addJsAction("onChange", "delaySaveChangeTaskArray() ;") ;
        textBox.setEnabled(true);
        textBox.setRequired(true);
        textBox.setId(keyStr);
        textBox.setValue(value == null ? "" : (String)value);
        textBox.setColumnName(AttributeDataUtilityHelper.getColumnName(keyStr, paramObject, paramModelContext));
        return textBox ;
    }
}
