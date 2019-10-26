package ext.appo.ecn.pdf;

import wt.lifecycle.State;
import wt.part.WTPart;
import wt.session.SessionHelper;
import wt.util.WTException;

public class AffectedProductBean {

    private WTPart part;

    public AffectedProductBean(WTPart part){
        this.part = part;
    }

    public String getNumber(){
        return part.getNumber();
    }

    public String getName(){
        return part.getName();
    }

    public String getState(){

        State s = State.toState(part.getState().toString());
        String rtn = part.getState().toString();
        try {
            rtn = s.getDisplay(SessionHelper.getLocale());
        } catch (WTException e) {
            e.printStackTrace();
        }
        return rtn;
    }

    public String getVersion(){
        String version = part.getVersionInfo().getIdentifier().getValue();
        String iteration = part.getIterationInfo().getIdentifier().getValue();
        return version+"."+iteration;
    }

    public String getGGSM(){
    	String ggms = (String)PdfUtil.getIBAObjectValue(part, "ggms");
        return ggms == null ? "" :  ggms;
    }
}
