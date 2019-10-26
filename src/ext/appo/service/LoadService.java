package ext.appo.service;

import java.io.IOException;

import wt.content.ApplicationData;
import wt.content.ContentHolder;
import wt.doc.WTDocument;
import wt.util.WTException;

public interface LoadService
{


	public abstract ApplicationData getPrimaryContent(WTDocument wtdocument) throws WTException, IOException;
	public abstract  Boolean ChangePrimaryContentName(ContentHolder object,String newname) throws WTException, Exception ;

}
