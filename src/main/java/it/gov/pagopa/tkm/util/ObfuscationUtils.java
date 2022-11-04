package it.gov.pagopa.tkm.util;

public class ObfuscationUtils {

    private ObfuscationUtils(){}

    public static String obfuscateHpan(String hpan) {
        if (hpan != null && hpan.trim().length() > 6) {
            hpan = hpan.trim();
            hpan = hpan.substring(0, 6) + "************";
        }
        return hpan;
    }

    public static String obfuscateHtoken(String htoken) {
        return obfuscateHpan(htoken);
    }

    public static String obfuscatePar(String par) {
        return obfuscateHpan(par);
    }

}
