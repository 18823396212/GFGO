package ext.appo.erp.service;

import ext.com.iba.IBAUtil;
import wt.iba.value.IBAHolder;

public class UpdatePDMDataService {
    public static void updatePDMFailed(IBAHolder ibaHolder , String ibaName ){

        updatePDMData( ibaHolder , ibaName , "PDM发布失败"  ) ;

    }



    public static void updatePDMSucessful(IBAHolder ibaHolder , String ibaName ){
        updatePDMData( ibaHolder , ibaName , "PDM发布成功" );
    }


    protected static void updatePDMData(IBAHolder ibaHolder , String ibaName , String ibaValue ){
        if( ibaHolder != null ){
            try {
                Object obj = IBAUtil.getIBAValue(ibaHolder, ibaName);

                if( obj == null || ( (String)obj ).trim().isEmpty() || !( (String)obj ).trim().equals( ibaValue ) ){
                    IBAUtil.forceSetIBAValue(ibaHolder, ibaName, ibaValue ) ;
                }

            } catch ( Exception e ) {
                e.printStackTrace();
            }
        }
    }


}
