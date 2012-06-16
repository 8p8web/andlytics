package de.betaapps.andlytics.admob;


public class AdmobGenericException extends Exception {

    private static final long serialVersionUID = 1L;

    public AdmobGenericException(String string) {
        super(string);
    }

    public AdmobGenericException(Exception e) {
        super(e);
    }

    public AdmobGenericException(Exception e, String sb) {
        super(sb,e);
    }

}
