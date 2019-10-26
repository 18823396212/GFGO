package ext.appo.util;

import java.util.StringTokenizer;

import wt.folder.Folder;
import wt.folder.FolderHelper;
import wt.inf.container.WTContainer;
import wt.inf.container.WTContainerRef;
import wt.util.WTException;

import com.ptc.windchill.cadx.common.util.FolderUtilities;

public class FolderUtil {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}	
	public static Folder getFolder(String path, WTContainer con) throws WTException
	{
		Folder folder = null;
		StringTokenizer tokenizer = new StringTokenizer(path, "/");
		String subPath = "";
		while (tokenizer.hasMoreTokens())
		{
			String token = tokenizer.nextToken();
			subPath = subPath + "/" + token;
			if (FolderUtilities.doesFolderExist(subPath, con))
			{
				folder = FolderHelper.service.getFolder(subPath, WTContainerRef.newWTContainerRef(con));
			} else
			{
				folder = FolderHelper.service.createSubFolder(subPath, WTContainerRef.newWTContainerRef(con));
			}
		}
		return folder;
	}

}
