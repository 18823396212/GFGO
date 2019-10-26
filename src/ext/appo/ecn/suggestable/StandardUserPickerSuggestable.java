package ext.appo.ecn.suggestable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;

import wt.log4j.LogR;
import wt.org.WTUser;
import wt.util.WTException;

import com.ptc.core.components.suggest.SuggestParms;
import com.ptc.core.components.suggest.SuggestResult;
import com.ptc.netmarkets.model.NmOid;
import com.ptc.netmarkets.search.suggest.UserPickerSuggestable;

import ext.appo.ecn.config.UserPickerConfig;

public class StandardUserPickerSuggestable extends UserPickerSuggestable{

	private static final String CLASSNAME = StandardUserPickerSuggestable.class.getName() ;
	private static final Logger LOG = LogR.getLogger(CLASSNAME) ;
	
	@Override
	public Collection<SuggestResult> getSuggestions(SuggestParms paramSuggestParms) {
		
		paramSuggestParms.addParm("configClassName", UserPickerConfig.class.getName());
		
		if(LOG.isDebugEnabled()){
			LOG.debug(paramSuggestParms) ;
		}
		
		Collection<SuggestResult> suggestResultCollection = super.getSuggestions(paramSuggestParms);
		
		if(LOG.isDebugEnabled()){
			LOG.debug("Start Print Super Class Result ") ;
			LOG.debug(suggestResultCollection) ;
			LOG.debug("End Print Super Class Result ") ;
		}
		
		suggestResultCollection = additionTextHandler(suggestResultCollection);

		if(LOG.isDebugEnabled()){
			LOG.debug("Start Print Current Class Result ") ;
			LOG.debug(suggestResultCollection) ;
			LOG.debug("End Print Current Class Result ") ;
		}
		
	    return suggestResultCollection;
	}
	
	public List<SuggestResult> additionTextHandler(Collection<SuggestResult> suggestionCollection) {
		LOG.debug("Enter In additionTextHandler...") ;
		
		List<SuggestResult> suggestResultList = new ArrayList<SuggestResult>(suggestionCollection.size());
		
		for (SuggestResult suggestResult : suggestionCollection) {
			if( suggestResult != null ){
				String additionalText = getAdditionalText(suggestResult);
				
				suggestResult = SuggestResult.valueOf(suggestResult.getDisplayText() , additionalText , suggestResult.getDisplayText()) ;
				
				suggestResultList.add(suggestResult);
			}
		}

		return suggestResultList;
	}
	
	private String getAdditionalText(SuggestResult suggestResult) {
		LOG.debug("Enter In getAdditionalText...") ;
		
		String additionalText = suggestResult.getAdditionalText();
		
		if( additionalText == null || additionalText.trim().equals("")){
			
			additionalText = getDefaultAdditionalText(suggestResult) ;
		}else if (additionalText.endsWith(" : ")) {
			
			additionalText = additionalText.substring(0, additionalText.length() - 3);
		}
		
		return additionalText ;
	}
	
	private String getDefaultAdditionalText(SuggestResult suggestResult) {
		LOG.debug("Enter In getDefaultAdditionalText...") ;
		
		String additionalText = null ;
		try {
			NmOid nmoid = NmOid.newNmOid(suggestResult.getValueText());
			Object refObject = nmoid.getRefObject() ;
			if( refObject != null && refObject instanceof WTUser ){
				WTUser user = ( WTUser ) refObject ;
				additionalText = user.getFullName() + " : " + user.getEMail();
			}else{
				if(refObject == null){
					LOG.error("refObject == null") ;
				}else{
					LOG.error("refObject class type is " + refObject.getClass()) ;
				}
			}
		} catch (WTException e) {
			e.printStackTrace();
		}
		
		if(additionalText == null){
			additionalText = "" ;
		}
		
		return additionalText ;
	}
}
