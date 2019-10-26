package ext.appo.folder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import wt.admin.DomainAdministered;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.folder.Folder;
import wt.folder.FolderException;
import wt.folder.FolderHelper;
import wt.folder.SubFolder;
import wt.log4j.LogR;
import wt.pom.UniquenessException;
import wt.session.SessionServerHelper;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;

import com.ptc.core.components.beans.ObjectBean;
import com.ptc.core.components.forms.DefaultObjectFormProcessor;
import com.ptc.core.components.forms.FormProcessingStatus;
import com.ptc.core.components.forms.FormResult;
import com.ptc.netmarkets.folder.folderResource;
import com.ptc.netmarkets.model.NmOid;
import com.ptc.netmarkets.util.beans.NmCommandBean;
 
 public class APPOCreateWizardFolderProcess extends DefaultObjectFormProcessor {
 
     private static final String RESOURCE = "com.ptc.netmarkets.folder.folderResource";
     private static final Logger log;

     static {
         try {
             log = LogR.getLogger (APPOCreateWizardFolderProcess.class.getName());
         } catch (Exception e) {
             throw new ExceptionInInitializerError(e);
         }
     }

     public APPOCreateWizardFolderProcess () {
     }
 
     @Override
     public FormResult doOperation (NmCommandBean clientData, List<ObjectBean> objectBeans)
         throws WTException
     {
    	 log.debug("doOperation----------start-->");
    	 // Trap exception indicating that proposed name for new folder
         // is not unique within its parent folder and issue appropriate
         // error message.
    	 String newFolderName="";
    	 HashMap map = clientData.getText();
    	 Object[] keys = map.keySet().toArray();
    	 for( Object oneSet : keys) {
    	 if (((String)oneSet).contains("foldername")) {
    		 newFolderName=(String)map.get(oneSet);
    	 } 
    	 }
    	 log.debug("newfolder name=="+newFolderName);
          if(newFolderName.length()==0)
          {
        	  throw new WTException("请输入新文件夹的名字！");  
          }
         try {
        	 FormResult phaseResult = new FormResult();
             phaseResult.setStatus(FormProcessingStatus.SUCCESS);
             
             if (getSelectedOidForPopup(clientData) == null)
            	 throw new WTException("请选择一个文件夹进行拷贝！");
             
             SubFolder sourceFolder = (SubFolder)getSelectedOidForPopup(clientData).getRef();
             log.debug("subfolder name=="+sourceFolder.getName());
             Folder parentFolder = null;
             if (sourceFolder.getParentFolder() != null)
            	 parentFolder = (Folder)sourceFolder.getParentFolder().getObject();
             if (parentFolder == null)
            	 parentFolder = sourceFolder.getContainer().getDefaultCabinet();
             
            // String newFolderName = ((SubFolder)objectBeans.get(0).getObject()).getName();
             
             log.debug("new foldername=="+newFolderName);
             copyFolderStructure(newFolderName, sourceFolder, parentFolder);

             return phaseResult;
         } catch (WTException e) {
             Throwable t = e.getNestedThrowable();
             if (t instanceof UniquenessException) {
                 SubFolder sf = (SubFolder) objectBeans.get(0).getObject();
                 String [] params = { sf.getName() };
                 throw new FolderException (RESOURCE, folderResource.UNIQUENESS, params);
             } else {
                 throw e;
             }
         }
     }
     
     protected static void copyFolderStructure(String newFolderName, SubFolder sourceFolder, Folder parentFolder) 
        throws WTException 
     {
      	 log.debug("copyFolderStructure----------start-->");
    	log.debug("parentFolder name=="+parentFolder.getName()); 
		SubFolder newFolder = createFolder(newFolderName, sourceFolder,	parentFolder);
		log.debug("newFolder =="+newFolder.getName());
		QueryResult qr = FolderHelper.service.findSubFolders(sourceFolder);
		log.debug("qr.size()=="+qr.size());
		while (qr != null && qr.hasMoreElements()) {
			SubFolder childFolder = (SubFolder) qr.nextElement();
			copyFolderStructure(childFolder.getName(), childFolder, newFolder);
		}
	}

	protected static SubFolder createFolder(String newFolderName, SubFolder sourceFolder, Folder parentFolder) 
	   throws WTException 
	{
		if (newFolderName == null)
			newFolderName = sourceFolder.getName();
		SubFolder newFolder = SubFolder.newSubFolder(newFolderName);

		try {
			newFolder.setContainerReference(sourceFolder.getContainerReference());
		} catch (WTPropertyVetoException e) {
			throw new WTException(e.getLocalizedMessage());
		}

		boolean origenforce = SessionServerHelper.manager.isAccessEnforced();
		try {
			SessionServerHelper.manager.setAccessEnforced(false);
			// Assign the new folder to its selected parent.
			FolderHelper.assignLocation(newFolder, parentFolder);
			((DomainAdministered) newFolder).setDomainRef(sourceFolder.getDomainRef());
			((DomainAdministered) newFolder).setInheritedDomain(sourceFolder.isInheritedDomain());
		} catch (WTPropertyVetoException e) {
			throw new WTException(e);
		} finally {
			SessionServerHelper.manager.setAccessEnforced(origenforce);
		}

		newFolder = (SubFolder) PersistenceHelper.manager.save(newFolder);

		// Return newly created folder
		return newFolder;
	}


     
     private NmOid getSelectedOidForPopup(NmCommandBean clientData) 
        throws WTException 
     {
		NmOid parentOid = null;

		// Is there a selected folder on the form? If this is a tool
		// bar action and if there is a selected folder there should
		// be a value.
		ArrayList oidList = clientData.getSelectedOidForPopup();
		if (oidList != null && oidList.size() > 0)
			parentOid = (NmOid) oidList.get(0);

		return parentOid;
	}


 }
