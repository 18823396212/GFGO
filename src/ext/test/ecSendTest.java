package ext.test;

import ext.com.workflow.WorkflowUtil;
import ext.generic.esignature.delegate.EsignatureDelegate;
import ext.generic.reviewObject.util.ReviewObjectAndWorkFlowLink;
import java.rmi.RemoteException;
import java.util.List;
import wt.fc.ObjectReference;
import wt.fc.WTObject;
import wt.fc.collections.WTArrayList;
import wt.fc.collections.WTKeyedHashMap;
import wt.util.WTException;
import wt.workflow.engine.WfProcess;

public class ecSendTest {

	public static WTKeyedHashMap esignature(ObjectReference self) throws WTException, RemoteException {
		System.out.println("self====================="+self);
        WfProcess process = WorkflowUtil.getProcess(self);
        System.out.println("process==================="+process);
        WTArrayList arrayList = new WTArrayList();
        new WTKeyedHashMap();
        System.out.println("================test===========================");
        List<WTObject> reviewObject = ReviewObjectAndWorkFlowLink.getReviewObjectByProcess(self);
        System.out.println("reviewObject==============================="+reviewObject);
        arrayList.addAll(reviewObject);
        System.out.println("arrayList======================================="+arrayList);
        EsignatureDelegate delegate = new EsignatureDelegate(arrayList, process);
        System.out.println("delegate========================================"+delegate);
        WTKeyedHashMap wtMap = delegate.eSignature();
        System.out.println("wtMap========================================"+wtMap);
        return wtMap;
    }
	
	
}
