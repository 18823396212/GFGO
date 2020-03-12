package ext.appo.change.util;

import ext.lang.PIStringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import wt.doc.WTDocument;
import wt.fc.*;
import wt.part.PartDocHelper;
import wt.part.WTPart;
import wt.util.WTException;

import java.util.Collection;
import java.util.HashSet;

public class CollectDrawingUtil {

    /***
     * 获取部件关联图纸
     * 收集图纸只收集【正在工作】状态下的【关联的说明文档】
     * @param itemOids
     *            指定部件oid集合
     * @return
     */
    public static String collectDrawing(String itemOids) {
        if (PIStringUtils.isNull(itemOids)) {
            return createJSONObject(null, "对象id集合为空!");
        }

        try {

            Collection<WTPart> parts = new HashSet<WTPart>(); //需要收集说明文档的部件集合
            Collection<Persistable> associatedItems = new HashSet<>();//收集图文档
            JSONArray collectDrawingArray = new JSONArray();
            JSONArray jsonArray = new JSONArray(itemOids);
            for (int i = 0; i < jsonArray.length(); i++) {
                String oid = jsonArray.getString(i);
                if (PIStringUtils.isNull(oid)) {
                    continue;
                }
                if (oid.contains(WTPart.class.getName())) {
                    parts.add((WTPart) ((new ReferenceFactory()).getReference(oid).getObject()));
                }
            }
            if (parts!=null&&parts.size()>0){
                //获取部件所有关联的图纸
                associatedItems=getPartDrawing(parts);
            }

            for (Persistable persistable:associatedItems){
                if (persistable instanceof WTDocument){
                    WTDocument document=(WTDocument) persistable;
                    //是否为说明文档,必须是正在工作的说明文档
                    if (!PartDocHelper.isReferenceDocument(document)){
                        String state = document.getState().toString();
                        if (state.equals("INWORK")) {
                            collectDrawingArray.put(PersistenceHelper.getObjectIdentifier(document).toString());
                        }
                    }
                }
            }

           return createJSONObject(collectDrawingArray.toJSONString(),null);
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
            return createJSONObject(null, e.getLocalizedMessage());
        }
    }


    private static String createJSONObject(String resultDatas, String errorMsg) {
        JSONObject returnJSON = new JSONObject();
        try {
            returnJSON.put("resultDatas", resultDatas == null ? "" : resultDatas);
            returnJSON.put("message", errorMsg == null ? "" : errorMsg);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return returnJSON.toJSONString();
    }

    //获取部件关联的图纸
    private static Collection<Persistable> getPartDrawing(Collection<WTPart> parts) throws WTException {
        Collection<Persistable> drawings=new HashSet<>();
        for (WTPart part:parts) {
            QueryResult result = PartDocHelper.service.getAssociatedDocuments(part);//获取部件关联的图文档
            while (result.hasMoreElements()) {
                Object object = result.nextElement();
                if (object instanceof ObjectReference) {
                    object = ((ObjectReference) object).getObject();
                }
                drawings.add((Persistable) object);
            }
        }
        return  drawings;
    }

}
