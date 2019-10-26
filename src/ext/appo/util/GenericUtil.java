package ext.appo.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import wt.adapter.BasicWebjectDelegate;
import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.fc.ReferenceFactory;
import wt.folder.Folder;
import wt.part.WTPart;
import wt.pom.PersistenceException;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;
import wt.util.WTRuntimeException;
import wt.vc.wip.CheckoutLink;
import wt.vc.wip.NonLatestCheckoutException;
import wt.vc.wip.WorkInProgressException;
import wt.vc.wip.WorkInProgressHelper;
import wt.vc.wip.Workable;






//import com.ptc.core.lwc.server.LWCNormalizedObject;
//10.2 will use PersistableAdapter replace LWCNormalizedObject
import com.ptc.core.lwc.server.PersistableAdapter;
import com.ptc.core.meta.common.TypeInstanceIdentifier;
import com.ptc.core.meta.common.UpdateOperationIdentifier;

/**
 * 
    Some usage examples:
    1)Create a softtype of a WTPart
    HashMap<String,Object> partParams = new HashMap<String, Object>();
    partParams.put("name", "someName");
    partParams.put("number", "someNumber");
    WTPart somePart = GenericUtil.createObject("com.ptc.MyCustomWTPart", partParams);
    
    2)Create a standard WTPartUsageLink object
    //WTPart roleAObject  = some part that was previously checked-out
    //WTPartMaster roleBObjectMaster = some part master
    HashMap<String,Object> linkParams = new HashMap<String, Object>();
    linkParams.put(ObjectToObjectLink.ROLE_AOBJECT_ROLE, roleAObject);
    linkParams.put(ObjectToObjectLink.ROLE_BOBJECT_ROLE, roleBObjectMaster);    
    WTPart somePart = GenericUtil.createObject("wt.part.WTPartUsageLink", linkParams);
    
    
    3)Update a Requirement
    //Requirement myExistingRequirement = some existing Requirement
    HashMap<String,Object> requirementParams = new HashMap<String, Object>();
    requirementParams.put("name", "someNewName");
    requirementParams.put("description", "somenewDescription");
    Requirement myUpdatedRequirement = GenericUtil.updateObject(myExistingRequirement, requirementParams);
    
    
    4)Update only attributes values that changed.
      This is more smart update, that does the update if at least 1 attribute value changed (e.g. no need to iterated if nothing changed)
    //EPMDocument myExistingCADDocument = some existing CADDocument with name equals "myCADName"
    HashMap<String,Object> cadDocumentParams = new HashMap<String, Object>();
    cadDocumentParams.put("name", "myCADName");
    cadDocumentParams.put("comment", "someComment");
    cadDocumentParams.put("description", "someDescription");
    
    ///get a list of the attributes that changed
    String[] attributes_that_changed = GenericUtil.getAttributesValueThatChanged(myExistingCADDocument,cadDocumentParams);
    
    //update the object
    EPMDocument myUpdatedCADDocument = GenericUtil.updateObject(myExistingRequirement, attributes_that_changed, cadDocumentParams);
    
    
    
    4)Get an object attribute
    //WTDocument myDocument = some document tooked previously
    Object theDescription = GenericUtil.getObjectAttributeValue(myDocument, "description");
 *
 */
public class GenericUtil {

    public static ReferenceFactory rf = new ReferenceFactory();
    
    /**
     * get Reference String giving an persistable object
     * @param p
     * @return
     * @throws WTException
     */
    public static String getRefStr(Persistable p){
        String s =null;
        try {
            s= rf.getReferenceString(p);
        } catch (WTException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return s;
     }
    
    /**
     * Get an object instance giving a reference String
     *  for example getInstance("wt.part.WTPart:215486");
     * @param refStr
     * @return Persistable 
     */
    public static Persistable getInstance(String refStr) {
        
        Persistable obj=null;
        try {
            obj = rf.getReference(refStr).getObject();
        } catch (WTRuntimeException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (WTException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    
     return obj;
 }
    /**
     * Create a generic Persistable object giving a HashMap of attributes
     * for example  
     *  HashMap<String,Object> partParams = new HashMap<String, Object>();
        partParams.put("name", "someName4");
        partParams.put("number", "someNumber4");
        WTPart somePart = (WTPart)GenericUtil.createObject("com.ptcnet.MyPart", partParams);
     * @param ti
     * @param params
     * @return
     * @throws WTException 
     */
    public static Persistable createObject(String objectType, HashMap<String, Object> params) throws WTException  {
        Persistable createdObject = null;
       
        PersistableAdapter genericObj      = new PersistableAdapter(objectType,null,null);
        
        Iterator<String> attributesIterator = params.keySet().iterator();
        while (attributesIterator.hasNext()){
            String attKey    =  attributesIterator.next();
            Object attValue  =  params.get(attKey);
            genericObj.load(attKey);
            genericObj.set(attKey, attValue);
        }        
        //persist the object        
        TypeInstanceIdentifier tii = genericObj.persist();
        
        //get the Persistable
        createdObject=rf.getReference(tii.getPersistenceIdentifier()).getObject();
        
        //save the object
       // createdObject = PersistenceHelper.manager.store(createdObject);*/
        
     return createdObject;
    }
    
    /**
     * Updates attributes of a generic Persistable from a given HashMap of attributes
     * If object is of a Workable type need to iterate, otherwise, just update
     * It updates only the attributes specified in the attributesToUpdate, the rest are ignored
     * @param theObject
     * @param params
     * @return
     * @throws Exception
     */
    public static Persistable updateObject(Persistable theObject, String[] attributesToUpdate, HashMap<String, Object> params) throws Exception{
        
      if (attributesToUpdate !=null && attributesToUpdate.length>0){    
          Persistable updatedObject = null;
        
            ///check-out the object
          //  if (theObject instanceof Workable)
                //theObject = checkout((Workable)theObject, "Checked-out to update");
            
            PersistableAdapter genericObj      = new PersistableAdapter(theObject,null,null, new UpdateOperationIdentifier());
    
            ///load the attributes to update
            genericObj.load(attributesToUpdate);
            
            //set the values
            for (int i=0;i<attributesToUpdate.length;i++){
                Object attValue  =  params.get(attributesToUpdate[i]);
                //genericObj.load(attKey);
                genericObj.set(attributesToUpdate[i], attValue);
            }
            
            
            //update the object     
            updatedObject = genericObj.apply();
            
            //save
            updatedObject = PersistenceHelper.manager.save(updatedObject);
            
            //check-in
         //   if (theObject instanceof Workable)
           // updatedObject = checkin((Workable)updatedObject, "Checked-in from update");
        return updatedObject;
        
      } else {
          return theObject;
      }
    }
    
    /**
     * Updates attributes of a generic Persistable from a given HashMap of attributes
     * If object is of a Workable type need to iterate, otherwise, just update
     * @param theObject
     * @param params
     * @return
     * @throws Exception
     */ 
    public static Persistable updateObject(Persistable theObject, HashMap<String, Object> params) throws Exception{
        

        if (params!=null) {
            Persistable updatedObject     = null;
            List<String> tempList         = new ArrayList<String>();
            Iterator<String> keysIterator = params.keySet().iterator();
            while (keysIterator.hasNext()){
                tempList.add(keysIterator.next());
            }
            
            String[] attributes = new String[tempList.size()];
            attributes = tempList.toArray(attributes);
            updatedObject = updateObject(theObject, attributes, params);
            
            return updatedObject;
        } else {
            return theObject;
        }
    }
    
    
    
    /**
     * Retrieve a list of object attributes that changed from a predefined list of attributes that want to update
     * @param theObject
     * @param attributesThatWantToUpdate
     * @param attribute_values_map
     * @return
     * @throws Exception
     */
    public static String[] getAttributesValueThatChanged(Persistable theObject, String[] attributesThatWantToUpdate, HashMap<String, Object> attribute_values_map) throws Exception {
        List<String> attributesThatExists = new ArrayList<String>();
        List<String> attributesUpdated    = new ArrayList<String>();
        PersistableAdapter genericObj = new PersistableAdapter(theObject,null,null,null);
        
        //load the attributes to test
        for (int i=0;i<attributesThatWantToUpdate.length; i++){
            if (attribute_values_map.containsKey(attributesThatWantToUpdate[i])){
                attributesThatExists.add(attributesThatWantToUpdate[i]);
            }
        }
        
        //if any attribute that wanna set
        if (attributesThatExists.size()>0){
            
            ///load the attributes
            genericObj.load(attributesThatExists);          
                        
            //verify each attribute value
            for (int i=0;i<attributesThatExists.size(); i++){

                                
                ///verify each attribute value
                if (!genericObj.get(attributesThatExists.get(i)).equals(
                    attribute_values_map.get(attributesThatExists.get(i)))){
                    attributesUpdated.add(attributesThatExists.get(i));
                }
            }//end loop
        }//end if
        
        String[] changedAttributesList = new String[attributesUpdated.size()];
        changedAttributesList = attributesUpdated.toArray(changedAttributesList);
        
     return changedAttributesList;  
    }
    
    /**
     * Retrieve a list of object attributes that changed from a predefined list of attributes that want to update
     * @param theObject
     * @param attributesThatWantToUpdate
     * @param attribute_values_map
     * @return
     * @throws Exception
     */ 
    public static String[] getAttributesValueThatChanged(Persistable theObject, HashMap<String, Object> attribute_values_map) throws Exception {
        
        String[] attributes_to_update = new String[0];
        if (attribute_values_map!=null) {

            List<String> tempList         = new ArrayList<String>();
            Iterator<String> keysIterator = attribute_values_map.keySet().iterator();
            while (keysIterator.hasNext()){
                tempList.add(keysIterator.next());
            }
            
            //get all posibile attributes from the hashmap
            String[] attributes_to_check = new String[tempList.size()];
            attributes_to_check  = tempList.toArray(attributes_to_check);
            attributes_to_update = getAttributesValueThatChanged(theObject, attributes_to_check, attribute_values_map);

        }
     return attributes_to_update;
    }
    
    
    /**
     * Get an object Attribute value
     * @param theObject
     * @param attributeName
     * @return
     * @throws WTException
     */
    public static Object getObjectAttributeValue(Persistable theObject, String attributeName) throws WTException{
        PersistableAdapter genericObj = new PersistableAdapter(theObject,null,null,null);
        genericObj.load(attributeName); 
     return genericObj.get(attributeName);
    }
    
    
    /**
     * Returns a checked out copy 
     * If checked out already, returns it self
     * @param holder
     * @param checkoutMsg
     * @return Workable
     * @throws WTException
     * @throws WTPropertyVetoException 
     * @throws PersistenceException 
     * @throws WorkInProgressException 
     * @throws NonLatestCheckoutException 
     */
    public static Workable checkout (Workable holder, String checkoutMsg) throws
    WTPropertyVetoException, NonLatestCheckoutException, WorkInProgressException, PersistenceException, WTException 
    {
        if(! WorkInProgressHelper.isCheckedOut(holder)){
            Folder folder = WorkInProgressHelper.service.getCheckoutFolder();
            CheckoutLink checkoutLink = WorkInProgressHelper.service.checkout(holder, folder, checkoutMsg);
            holder = checkoutLink.getWorkingCopy();             
           
        }
       return holder;
    }
    
    
    /**
     * Make checkin an object 
     * If not checked out, make checkin
     * @param holder
     * @param checkinMsg
     * @return Workable
     * @throws WTException
     */
    public static Workable checkin (Workable holder, String checkinMsg) throws WTException {
        if(WorkInProgressHelper.isCheckedOut((Workable)holder)){
            try {
                holder = WorkInProgressHelper.service.checkin(holder, checkinMsg);
            }catch (WTPropertyVetoException e1){
                throw new WTException (e1);
            }
        }
     return holder;
    } 
    
    /****get object id ****/
    public static long getId(Persistable obj) {
        return obj.getPersistInfo().getObjectIdentifier().getId();
    }
    public static String getUfid(Persistable obj) throws WTException{
        return BasicWebjectDelegate.getUfid(obj);
    }
    
    /**
     * Method that get preference value
     * 
     * 
     * 
     * example usage is  
     * String value = GenericUtils.getPreferenceValue("/midea/viewName");
     * 
     Example load format is

     <?xml version="1.0"?><!DOCTYPE NmLoader SYSTEM "standardX24.dtd">

    <NmLoader>
        <csvPreferenceCategory handler="wt.preference.LoadPreference.createPreferenceCategory">
            <csvname>MIDEA_CATEGORY</csvname>
            <csvparentName />
            <csvdisplayName>Midea Customization</csvdisplayName>
            <csvdescription />
        </csvPreferenceCategory>

        <csvPreferenceDefinition handler="wt.preference.LoadPreference.createPreferenceDefinition">
            <csvname>/midea/viewName</csvname>
            <csvvisibility>SITE</csvvisibility>
            <csvcategoryName>MIDEA_CATEGORY</csvcategoryName>
            <csvdisplayName>Design View Name</csvdisplayName>
            <csvdescription>Design View Name</csvdescription>
            <csvlongDescription>Design View Name</csvlongDescription>
            <csvdefaultValue>设计</csvdefaultValue>
            <csvhandler>com.ptc.windchill.enterprise.preference.handler.StringPreferenceValueHandler:</csvhandler>
        </csvPreferenceDefinition>

        <csvLinkPreferenceClientDefinition handler="wt.preference.LoadPreference.setClientDefinitionLink">
            <csvname>/midea/viewName</csvname>
            <csvclientName>WINDCHILL</csvclientName>
        </csvLinkPreferenceClientDefinition>
     </NmLoader>
     * @param preference
     *            definition name
     * @return preference value
     * @throws WTException
     */
    public static String getPreferenceValue(String definitionName) throws WTException {
        Object preferenceValue = wt.preference.PreferenceHelper.service.getValue(definitionName, wt.preference.PreferenceClient.WINDCHILL_CLIENT_NAME);
        if (preferenceValue == null) {
            return "";
        } else {
            return preferenceValue.toString();
        }

    }
    
    
    
    
}

