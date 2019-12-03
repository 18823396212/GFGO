package ext.appo.change.report;

import ext.appo.change.beans.BOMChangeInfoBean;
import ext.appo.change.constants.BomChangeConstants;
import ext.pi.core.PIAttributeHelper;
import org.apache.commons.lang3.StringUtils;
import wt.fc.*;
import wt.fc.collections.WTCollection;
import wt.occurrence.Occurrence;
import wt.occurrence.OccurrenceHelper;
import wt.part.*;
import wt.query.QueryException;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.util.WTException;
import wt.vc.VersionControlHelper;

import java.math.BigDecimal;
import java.util.*;


public class CompareBom {
    /*****
     * ecn流程受影响对象part和产生对象part的BOM差异对比
     * @param affectedPart 受影响的物料
     * @param producePart 产生的物料
     * @return
     * @throws WTException
     */
    public static List<BOMChangeInfoBean> getBomChangeInfo(WTPart affectedPart, WTPart producePart) throws Exception {
        List<BOMChangeInfoBean> bomChangeInfoBeans = new ArrayList<>();

        //受影响物料BOM
        Map<WTPartMaster, WTPartUsageLink> affected = getAllWTPartUsageLink(affectedPart);
        //产生物料BOM
        Map<WTPartMaster, WTPartUsageLink> produce = getAllWTPartUsageLink(producePart);
//        System.out.println("affected=="+affected);
//        System.out.println("produce=="+produce);
        for (WTPartMaster wm : affected.keySet()) {
            if (!produce.keySet().contains(wm)) {
                System.out.println("changeType==删除子料");
                //删除子料
                BOMChangeInfoBean bomChangeInfoBean = new BOMChangeInfoBean();
                Set changeType = new HashSet();
                changeType.add(BomChangeConstants.TYPE_3);
                WTPart childPart = getLatestPart(wm);
                String number = childPart.getNumber();
                String name = childPart.getName();
                String ggms = PIAttributeHelper.service.getValue(childPart, "ggms") == null ? "" : (String) PIAttributeHelper.service.getValue(childPart, "ggms");
                bomChangeInfoBean.setChangeType(changeType);
                bomChangeInfoBean.setNumber(number);
                bomChangeInfoBean.setName(name);
                bomChangeInfoBean.setSpecification(ggms);

//                //获取删除子项的所有特定替代料
//                WTPartUsageLink oldLink = getUsageLink(childPart, wm);
//                if (oldLink != null) {
//                    List<String> replacePartNumber=new ArrayList<>();
//                    Map<String, List<String>> replacePartNumbers = new HashMap<>();
//                    //当前子项特定替代
//                    List<WTPartSubstituteLink> substituteLinks = getSubstituteLinks(oldLink);
//                    if (substituteLinks != null && substituteLinks.size() > 0) {
//                        for (WTPartSubstituteLink substitutelink : substituteLinks) {
//                            String substituteNumber = substitutelink.getSubstitutes().getNumber();//特定替代料编码
//                            replacePartNumber.add(substituteNumber);
//                        }
//                        replacePartNumbers.put("delete",replacePartNumber);
//                    }
//                    bomChangeInfoBean.setReplacePartNumbers(replacePartNumbers);
//                }


                bomChangeInfoBeans.add(bomChangeInfoBean);
            }

        }


        for (WTPartMaster wm : produce.keySet()) {
            if (!affected.keySet().contains(wm)) {
                System.out.println("changeType==新增子料");
                //新增子料
                BOMChangeInfoBean bomChangeInfoBean = new BOMChangeInfoBean();
                List<WTPartSubstituteLink> partSubstituteLinks = new ArrayList<WTPartSubstituteLink>();
//                String replacePartNumber="";
//                List<String> replacePartNumbers=new ArrayList<String>();
                Set changeType = new HashSet();
                changeType.add(BomChangeConstants.TYPE_1);
                WTPart childPart = getLatestPart(wm);
                String number = childPart.getNumber();
                String name = childPart.getName();
                String ggms = PIAttributeHelper.service.getValue(childPart, "ggms") == null ? "" : (String) PIAttributeHelper.service.getValue(childPart, "ggms");
                Map<String, String> placeNumber = new HashMap<String, String>();//位号
                Map<String, String> quantity = new HashMap<String, String>();//数量
                WTPartUsageLink link = produce.get(wm);//产生对象link
                if (link != null) {
                    //获取位号
                    placeNumber.put("after", getPartReferenceDesignators(link));
                    //获取数量
                    quantity.put("after", getLinkSum(link));
//                    //子项特定替代
//                    List<WTPartSubstituteLink> substituteLinks = getSubstituteLinks(link);
//                    if (substituteLinks!=null){
//                        for (WTPartSubstituteLink substitutelink:substituteLinks) {
//                            partSubstituteLinks.add(substitutelink);
//                        }
//                        if (partSubstituteLinks!=null&&partSubstituteLinks.size()>0){
//                            //存在特定替代
//                            for (int i = 0; i < partSubstituteLinks.size(); i++) {
//                                replacePartNumber=partSubstituteLinks.get(i).getSubstitutes().getNumber();
//                                replacePartNumbers.add(replacePartNumber);
//                            }
//                            changeType.add(BomChangeConstants.TYPE_2);
//
//                        }
//                    }
                }

                bomChangeInfoBean.setChangeType(changeType);
                bomChangeInfoBean.setNumber(number);
                bomChangeInfoBean.setName(name);
                bomChangeInfoBean.setSpecification(ggms);
                bomChangeInfoBean.setPlaceNumber(placeNumber);
                bomChangeInfoBean.setQuantit(quantity);
//                bomChangeInfoBean.setReplacePartNumbers(replacePartNumbers);

                bomChangeInfoBeans.add(bomChangeInfoBean);
            } else {
                System.out.println("changeType==修改子料");
                //修改子料
                BOMChangeInfoBean bomChangeInfoBean = new BOMChangeInfoBean();
                WTPartUsageLink oldlink = affected.get(wm);//受影响对象link
                WTPartUsageLink newLink = produce.get(wm);//产生对象link
                Set changeType = new HashSet();
                WTPart childPart = getLatestPart(wm);
                String number = childPart.getNumber();
                String name = childPart.getName();
                String ggms = PIAttributeHelper.service.getValue(childPart, "ggms") == null ? "" : (String) PIAttributeHelper.service.getValue(childPart, "ggms");
                Map<String, String> placeNumber = new HashMap<String, String>();//位号
                Map<String, String> quantity = new HashMap<String, String>();//数量

                String oldPlaceNumber = getPartReferenceDesignators(oldlink);//受影响对象位号
                String newPlaceNumber = getPartReferenceDesignators(newLink);//产生对象位号
                String oldQuantity = getLinkSum(oldlink);//受影响对象数量
                String newQuantity = getLinkSum(newLink);//产生对象数量

                if (!compare(oldPlaceNumber, newPlaceNumber)) {
                    //位号变更
                    changeType.add(BomChangeConstants.TYPE_5);
                    placeNumber.put("before", oldPlaceNumber);
                    placeNumber.put("after", newPlaceNumber);
                }
                if (!compare(oldQuantity, newQuantity)) {
                    //数量变更
                    changeType.add(BomChangeConstants.TYPE_6);
                    quantity.put("before", oldQuantity);
                    quantity.put("after", newQuantity);
                }

                bomChangeInfoBean.setChangeType(changeType);
                bomChangeInfoBean.setNumber(number);
                bomChangeInfoBean.setName(name);
                bomChangeInfoBean.setSpecification(ggms);
                bomChangeInfoBean.setPlaceNumber(placeNumber);
                bomChangeInfoBean.setQuantit(quantity);
                System.out.println("修改子项=="+changeType);
                if (changeType != null && changeType.size() > 0) {
                    //子料有修改
                    if (bomChangeInfoBeans != null && bomChangeInfoBeans.size() > 0) {
                        Boolean isNumber=false;
                        for (int i = 0; i < bomChangeInfoBeans.size(); i++) {
                            if (bomChangeInfoBeans.get(i).getNumber().equals(bomChangeInfoBean.getNumber())) {
                                //存在同一物料编码
                                Set changeTypes = new HashSet();
                                changeTypes = bomChangeInfoBeans.get(i).getChangeType();
                                changeTypes.addAll(bomChangeInfoBean.getChangeType());
                                System.out.println("修改子项changeTypes=="+changeTypes);
                                bomChangeInfoBeans.get(i).setChangeType(changeTypes);
                                bomChangeInfoBeans.get(i).setPlaceNumber(placeNumber);
                                bomChangeInfoBeans.get(i).setQuantit(quantity);
                                isNumber=true;
                            }

                        }
                        if (!isNumber){
                            bomChangeInfoBeans.add(bomChangeInfoBean);
                        }

                    }else {
                        bomChangeInfoBeans.add(bomChangeInfoBean);
                    }

                }
            }

        }

        //最后比较替代料
        Map<WTPartMaster, List<WTPartSubstituteLink>> oldSubstitutes = new HashMap<>();
        Map<WTPartMaster, List<WTPartSubstituteLink>> newSubstitutes = new HashMap<>();
        //获取受影响对象所有替代料
        for (WTPartMaster wm : affected.keySet()) {
            List<WTPartSubstituteLink> oldSubstitute = new ArrayList<>();
            //获取受影响对象子项的所有特定替代料
            WTPartUsageLink oldLink = getUsageLink(affectedPart, wm);
            if (oldLink != null) {
                //当前子项特定替代
                List<WTPartSubstituteLink> substituteLinks = getSubstituteLinks(oldLink);
                if (substituteLinks != null&&substituteLinks.size()>0) {
                    for (WTPartSubstituteLink substitutelink : substituteLinks) {
                        oldSubstitute.add(substitutelink);
                    }
                    oldSubstitutes.put(wm, oldSubstitute);
                }
            }
        }
        //获取产生对象所有替代料
        for (WTPartMaster wm : produce.keySet()) {
            List<WTPartSubstituteLink> newSubstitute = new ArrayList<>();
            //获取产生对象子项的所有特定替代料
            WTPartUsageLink newLink = getUsageLink(producePart, wm);
            if (newLink != null) {
                //当前子项特定替代
                List<WTPartSubstituteLink> substituteLinks = getSubstituteLinks(newLink);
                if (substituteLinks != null&&substituteLinks.size()>0) {
                    for (WTPartSubstituteLink substitutelink : substituteLinks) {
                        newSubstitute.add(substitutelink);
                    }
                    newSubstitutes.put(wm, newSubstitute);
                }
            }
        }
        System.out.println("oldSubstitutes=="+oldSubstitutes);
        System.out.println("newSubstitutes=="+newSubstitutes);

        for (WTPartMaster wm : oldSubstitutes.keySet()) {
            if (!newSubstitutes.keySet().contains(wm)) {
                //删除替代料
                BOMChangeInfoBean bomChangeInfoBean = new BOMChangeInfoBean();
                Set changeType = new HashSet();
                List<String> replacePartNumber = new ArrayList<>();
                Map<String, List<String>> replacePartNumbers = new HashMap<>();

                List<WTPartSubstituteLink> oldSubstituteList = oldSubstitutes.get(wm);
                if (oldSubstituteList != null && oldSubstituteList.size() > 0) {
                    for (int i = 0; i < oldSubstituteList.size(); i++) {
                        String substituteNumber = oldSubstituteList.get(i).getSubstitutes().getNumber();//特定替代料编码
                        replacePartNumber.add(substituteNumber);
                    }
                    replacePartNumbers.put("delete", replacePartNumber);
                    changeType.add(BomChangeConstants.TYPE_4);
                    WTPart childPart = getLatestPart(wm);
                    String number = childPart.getNumber();
                    String name = childPart.getName();
                    String ggms = PIAttributeHelper.service.getValue(childPart, "ggms") == null ? "" : (String) PIAttributeHelper.service.getValue(childPart, "ggms");
                    bomChangeInfoBean.setChangeType(changeType);
                    bomChangeInfoBean.setNumber(number);
                    bomChangeInfoBean.setName(name);
                    bomChangeInfoBean.setSpecification(ggms);
                    bomChangeInfoBean.setReplacePartNumbers(replacePartNumbers);
                }
                if (bomChangeInfoBeans != null && bomChangeInfoBeans.size() > 0) {
                    Boolean isNumber=false;
                    for (int i = 0; i < bomChangeInfoBeans.size(); i++) {
                        if (bomChangeInfoBeans.get(i).getNumber().equals(bomChangeInfoBean.getNumber())) {
                            //存在同一物料编码
                            Set changeTypes = new HashSet();
                            changeTypes = bomChangeInfoBeans.get(i).getChangeType();
                            changeTypes.addAll(bomChangeInfoBean.getChangeType());
                            Map<String, List<String>> beanReplacePartNumbers =bomChangeInfoBeans.get(i).getReplacePartNumbers();
                            List<String> beanList=new ArrayList<>();
                            beanList=beanReplacePartNumbers.get("delete");
                            if (beanList!=null&&!replacePartNumber.isEmpty()){
                                beanList.addAll(replacePartNumber);
                                beanReplacePartNumbers.put("delete",beanList);
                            }else {
                                beanReplacePartNumbers.put("delete",replacePartNumber);
                            }

                            bomChangeInfoBeans.get(i).setChangeType(changeTypes);
                            bomChangeInfoBeans.get(i).setReplacePartNumbers(beanReplacePartNumbers);
                            isNumber=true;
                        }

                    }
                    if (!isNumber){
                        bomChangeInfoBeans.add(bomChangeInfoBean);
                    }
                } else {
                    bomChangeInfoBeans.add(bomChangeInfoBean);
                }

            } else {
                //判断替代料是否一致
                Map<String, WTPartSubstituteLink> oldReplacePartNumber = new HashMap<>();
                Map<String, WTPartSubstituteLink> newReplacePartNumber = new HashMap<>();
                List<WTPartSubstituteLink> oldSubstituteList = oldSubstitutes.get(wm);
                List<WTPartSubstituteLink> newSubstituteList = newSubstitutes.get(wm);
                if (oldSubstituteList != null && oldSubstituteList.size() > 0) {
                    for (int i = 0; i < oldSubstituteList.size(); i++) {
                        String substituteNumber = oldSubstituteList.get(i).getSubstitutes().getNumber();//特定替代料编码
                        oldReplacePartNumber.put(substituteNumber, oldSubstituteList.get(i));
                    }
                }
                if (newSubstituteList != null && newSubstituteList.size() > 0) {
                    for (int i = 0; i < newSubstituteList.size(); i++) {
                        String substituteNumber = newSubstituteList.get(i).getSubstitutes().getNumber();//特定替代料编码
                        newReplacePartNumber.put(substituteNumber, newSubstituteList.get(i));
                    }
                }
                System.out.println("oldReplacePartNumber=="+oldReplacePartNumber);
                System.out.println("newReplacePartNumber=="+newReplacePartNumber);
                for (String str : newReplacePartNumber.keySet()) {
                    if (!oldReplacePartNumber.keySet().contains(str)) {
                        //新增替代料
                        BOMChangeInfoBean bomChangeInfoBean = new BOMChangeInfoBean();
                        Set changeType = new HashSet();
                        List<String> replacePartNumber = new ArrayList<>();
                        Map<String, List<String>> replacePartNumbers = new HashMap<>();

                        List<WTPartSubstituteLink> newSubstituteList2 = newSubstitutes.get(wm);
//                        if (newSubstituteList2 != null && newSubstituteList2.size() > 0) {
//                            for (int i = 0; i < newSubstituteList2.size(); i++) {
//                                String substituteNumber = newSubstituteList2.get(i).getSubstitutes().getNumber();//特定替代料编码
//                                replacePartNumber.add(substituteNumber);
//                            }
                            replacePartNumber.add(str);
                            replacePartNumbers.put("add", replacePartNumber);
                            changeType.add(BomChangeConstants.TYPE_2);
                            WTPart childPart = getLatestPart(wm);
                            String number = childPart.getNumber();
                            String name = childPart.getName();
                            String ggms = PIAttributeHelper.service.getValue(childPart, "ggms") == null ? "" : (String) PIAttributeHelper.service.getValue(childPart, "ggms");
                            bomChangeInfoBean.setChangeType(changeType);
                            bomChangeInfoBean.setNumber(number);
                            bomChangeInfoBean.setName(name);
                            bomChangeInfoBean.setSpecification(ggms);
                            bomChangeInfoBean.setReplacePartNumbers(replacePartNumbers);
//                        }
                        if (bomChangeInfoBeans != null && bomChangeInfoBeans.size() > 0) {
                            Boolean isNumber=false;
                            for (int i = 0; i < bomChangeInfoBeans.size(); i++) {
                                if (bomChangeInfoBeans.get(i).getNumber().equals(bomChangeInfoBean.getNumber())) {
                                    //存在同一物料编码
                                    Set changeTypes = new HashSet();
                                    changeTypes = bomChangeInfoBeans.get(i).getChangeType();
                                    changeTypes.addAll(bomChangeInfoBean.getChangeType());
                                    Map<String, List<String>> beanReplacePartNumbers =bomChangeInfoBeans.get(i).getReplacePartNumbers();
                                    List<String> beanList=new ArrayList<>();
                                    beanList=beanReplacePartNumbers.get("add");
                                    if (beanList!=null&&!replacePartNumber.isEmpty()){
                                        beanList.addAll(replacePartNumber);
                                        beanReplacePartNumbers.put("add",beanList);
                                    }else {
                                        beanReplacePartNumbers.put("add",replacePartNumber);
                                    }
                                    bomChangeInfoBeans.get(i).setChangeType(changeTypes);
                                    bomChangeInfoBeans.get(i).setReplacePartNumbers(beanReplacePartNumbers);
                                    isNumber=true;
                                }

                            }
                            if (!isNumber){
                                bomChangeInfoBeans.add(bomChangeInfoBean);
                            }
                        }else {
                            bomChangeInfoBeans.add(bomChangeInfoBean);
                        }
                    }
                }
                for (String str : oldReplacePartNumber.keySet()) {
                    if (!newReplacePartNumber.keySet().contains(str)) {
                        //删除替代料
                        BOMChangeInfoBean bomChangeInfoBean = new BOMChangeInfoBean();
                        Set changeType = new HashSet();
                        List<String> replacePartNumber = new ArrayList<>();
                        Map<String, List<String>> replacePartNumbers = new HashMap<>();

                        List<WTPartSubstituteLink> oldSubstituteList2 = oldSubstitutes.get(wm);
//                        if (oldSubstituteList2 != null && oldSubstituteList2.size() > 0) {
//                            for (int i = 0; i < oldSubstituteList2.size(); i++) {
//                                String substituteNumber = oldSubstituteList2.get(i).getSubstitutes().getNumber();//特定替代料编码
//                                replacePartNumber.add(substituteNumber);
//                            }
                            replacePartNumber.add(str);
                            replacePartNumbers.put("delete", replacePartNumber);
                            changeType.add(BomChangeConstants.TYPE_4);
                            WTPart childPart = getLatestPart(wm);
                            String number = childPart.getNumber();
                            String name = childPart.getName();
                            String ggms = PIAttributeHelper.service.getValue(childPart, "ggms") == null ? "" : (String) PIAttributeHelper.service.getValue(childPart, "ggms");
                            bomChangeInfoBean.setChangeType(changeType);
                            bomChangeInfoBean.setNumber(number);
                            bomChangeInfoBean.setName(name);
                            bomChangeInfoBean.setSpecification(ggms);
                            bomChangeInfoBean.setReplacePartNumbers(replacePartNumbers);
//                        }
                        if (bomChangeInfoBeans != null && bomChangeInfoBeans.size() > 0) {
                            Boolean isNumber=false;
                            for (int i = 0; i < bomChangeInfoBeans.size(); i++) {
                                if (bomChangeInfoBeans.get(i).getNumber().equals(bomChangeInfoBean.getNumber())) {
                                    //存在同一物料编码
                                    Set changeTypes = new HashSet();
                                    changeTypes = bomChangeInfoBeans.get(i).getChangeType();
                                    changeTypes.addAll(bomChangeInfoBean.getChangeType());
                                    Map<String, List<String>> beanReplacePartNumbers =bomChangeInfoBeans.get(i).getReplacePartNumbers();
                                    List<String> beanList=new ArrayList<>();
                                    beanList=beanReplacePartNumbers.get("delete");
                                    if (beanList!=null&&!replacePartNumber.isEmpty()){
                                        beanList.addAll(replacePartNumber);
                                        beanReplacePartNumbers.put("delete",beanList);
                                    }else {
                                        beanReplacePartNumbers.put("delete",replacePartNumber);
                                    }
                                    bomChangeInfoBeans.get(i).setChangeType(changeTypes);
                                    bomChangeInfoBeans.get(i).setReplacePartNumbers(beanReplacePartNumbers);
                                    isNumber=true;
                                }

                            }
                            if (!isNumber){
                                bomChangeInfoBeans.add(bomChangeInfoBean);
                            }
                        }else {
                            bomChangeInfoBeans.add(bomChangeInfoBean);
                        }

                    }
                }

            }
        }

        for (WTPartMaster wm : newSubstitutes.keySet()) {
            if (!oldSubstitutes.keySet().contains(wm)) {
                //新增替代料
                BOMChangeInfoBean bomChangeInfoBean = new BOMChangeInfoBean();
                Set changeType = new HashSet();
                List<String> replacePartNumber = new ArrayList<>();
                Map<String, List<String>> replacePartNumbers = new HashMap<>();

                List<WTPartSubstituteLink> newSubstituteList = newSubstitutes.get(wm);
                if (newSubstituteList != null && newSubstituteList.size() > 0) {
                    for (int i = 0; i < newSubstituteList.size(); i++) {
                        String substituteNumber = newSubstituteList.get(i).getSubstitutes().getNumber();//特定替代料编码
                        replacePartNumber.add(substituteNumber);
                    }
                    replacePartNumbers.put("add", replacePartNumber);
                    changeType.add(BomChangeConstants.TYPE_2);
                    WTPart childPart = getLatestPart(wm);
                    String number = childPart.getNumber();
                    String name = childPart.getName();
                    String ggms = PIAttributeHelper.service.getValue(childPart, "ggms") == null ? "" : (String) PIAttributeHelper.service.getValue(childPart, "ggms");
                    bomChangeInfoBean.setChangeType(changeType);
                    bomChangeInfoBean.setNumber(number);
                    bomChangeInfoBean.setName(name);
                    bomChangeInfoBean.setSpecification(ggms);
                    bomChangeInfoBean.setReplacePartNumbers(replacePartNumbers);
                }
                if (bomChangeInfoBeans != null && bomChangeInfoBeans.size() > 0) {
                    Boolean isNumber=false;
                    for (int i = 0; i < bomChangeInfoBeans.size(); i++) {
                        if (bomChangeInfoBeans.get(i).getNumber().equals(bomChangeInfoBean.getNumber())) {
                            //存在同一物料编码
                            Set changeTypes = new HashSet();
                            changeTypes = bomChangeInfoBeans.get(i).getChangeType();
                            changeTypes.addAll(bomChangeInfoBean.getChangeType());
                            Map<String, List<String>> beanReplacePartNumbers =bomChangeInfoBeans.get(i).getReplacePartNumbers();
                            List<String> beanList=new ArrayList<>();
                            beanList=beanReplacePartNumbers.get("add");
                            if (beanList!=null&&!replacePartNumber.isEmpty()){
                                beanList.addAll(replacePartNumber);
                                beanReplacePartNumbers.put("add",beanList);
                            }else {
                                beanReplacePartNumbers.put("add",replacePartNumber);
                            }
                            bomChangeInfoBeans.get(i).setChangeType(changeTypes);
                            bomChangeInfoBeans.get(i).setReplacePartNumbers(beanReplacePartNumbers);
                            isNumber=true;
                        }

                    }
                    if (!isNumber){
                        bomChangeInfoBeans.add(bomChangeInfoBean);
                    }
                }else {
                    bomChangeInfoBeans.add(bomChangeInfoBean);
                }

            }
        }

        System.out.println("=====================BOM报表差异结果：" + bomChangeInfoBeans);
        return bomChangeInfoBeans;
    }

    /**
     * 获取Part所有WTPartUsageLink
     * 即Part的下层子料
     *
     * @param part
     * @return
     * @throws WTException
     */
    public static Map<WTPartMaster, WTPartUsageLink> getAllWTPartUsageLink(WTPart part) throws WTException {
        Map<WTPartMaster, WTPartUsageLink> result = new HashMap<>();
        QueryResult queryresult = PersistenceHelper.manager.navigate(part, WTPartUsageLink.USES_ROLE, WTPartUsageLink.class, false);
        while (queryresult.hasMoreElements()) {
            WTPartUsageLink usageLink = (WTPartUsageLink) queryresult.nextElement();
            WTPartMaster master = usageLink.getUses();
            result.put(master, usageLink);
        }
        return result;
    }

    //获取最新物料
    public static WTPart getLatestPart(WTPartMaster partMaster) {
        try {
            if (partMaster == null)
                return null;
            if (partMaster != null) {
                QueryResult qrVersions = VersionControlHelper.service.allVersionsOf(partMaster);
                if (qrVersions.hasMoreElements()) {
                    WTPart part = (WTPart) qrVersions.nextElement();
                    return part;
                }
            }
        } catch (QueryException e) {
            e.printStackTrace();
        } catch (WTException e) {
            e.printStackTrace();
        }
        return null;
    }


    //通过父件和子件获得link
    public static WTPartUsageLink getUsageLink(WTPart parentWTPart, WTPartMaster childWTPartMaster) throws WTException{
        QuerySpec qs = new QuerySpec(WTPartUsageLink.class);
        int[] fromIndicies = {0, wt.query.FromClause.NULL_INDEX};
        qs.appendWhere(new SearchCondition(WTPartUsageLink.class,
                ObjectToObjectLink.ROLE_AOBJECT_REF + "." + ObjectReference.KEY + "." + ObjectIdentifier.ID,
                SearchCondition.EQUAL, PersistenceHelper.getObjectIdentifier(parentWTPart).getId()), fromIndicies);
        qs.appendAnd();
        qs.appendWhere(new SearchCondition(WTPartUsageLink.class,
                ObjectToObjectLink.ROLE_BOBJECT_REF + "." + ObjectReference.KEY + "." + ObjectIdentifier.ID,
                SearchCondition.EQUAL, PersistenceHelper.getObjectIdentifier(childWTPartMaster).getId()), fromIndicies);

        QueryResult qr = PersistenceHelper.manager.find((wt.pds.StatementSpec) qs);
        while (qr.hasMoreElements()) {
            WTPartUsageLink link = (WTPartUsageLink) qr.nextElement();
            return link;
        }
        return null;
    }

    // 获取特定替换件
    public static List<WTPartSubstituteLink> getSubstituteLinks(WTPartUsageLink usageLink) throws WTException {
        List<WTPartSubstituteLink> substituteLinks = new ArrayList<WTPartSubstituteLink>();
        WTCollection collection = WTPartHelper.service.getSubstituteLinks(usageLink);
        for (Object object : collection) {
            if (object instanceof ObjectReference) {
                ObjectReference objref = (ObjectReference) object;
                Object obj = objref.getObject();

                if (obj instanceof WTPartSubstituteLink) {
                    WTPartSubstituteLink link = (WTPartSubstituteLink) obj;
                    substituteLinks.add(link);
                }
            }
        }

        return substituteLinks;
    }

    /**
     * 获得数量
     * @param link
     * @return
     */
    public static String getLinkSum(WTPartUsageLink link) {
        if (link != null) {
            String str = new BigDecimal(link.getQuantity().getAmount() + "").toString();
//            System.out.println("数量中的str"+str);
            if (str.indexOf(".") > 0) {
                str = str.replaceAll("0+$", "").replaceAll("[.]$", "");
            }
//            System.out.println("数量修改的str"+str);
            return str;
        } else {
            return "";
        }
    }

    /**
     * 获得位号
     * @param partUsageLink
     * @return
     * @throws WTException
     */
    public static String getPartReferenceDesignators(WTPartUsageLink partUsageLink) throws WTException {
        String result = "";
        QueryResult qr = OccurrenceHelper.service.getUsesOccurrences(partUsageLink);
        int nOccurences = qr.size();
        ArrayList refDesignatorList = new ArrayList(nOccurences);

        while (qr.hasMoreElements()) {
            Occurrence occurrence = (Occurrence) qr.nextElement();
            String occurrenceName = occurrence.getName();
            if (occurrenceName != null) {
                refDesignatorList.add(occurrenceName);
            }
        }

        Collections.sort(refDesignatorList);
        if (!refDesignatorList.isEmpty()) {
            result = StringUtils.join(refDesignatorList, ",");
        }

        return result;
    }

    public static boolean compare(String oldV, String newV) {
        if (StringUtils.isEmpty(oldV) && StringUtils.isEmpty(newV)) {
            return true;
        } else if (StringUtils.isNotEmpty(oldV) && StringUtils.isEmpty(newV)) {
            return false;
        } else if (StringUtils.isEmpty(oldV) && StringUtils.isNotEmpty(newV)) {
            return false;
        } else {
            return oldV.equals(newV);
        }
    }

}
