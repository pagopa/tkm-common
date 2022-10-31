package it.gov.pagopa.tkm.util;

import org.apache.commons.lang3.*;

public class ObfuscationUtils {

    private ObfuscationUtils(){}

    public static String obfuscateHpan(String hpan) {
        if (StringUtils.isBlank(hpan)) {
            return hpan;
        }
        return hpan.substring(0,3) + "***" + hpan.substring(hpan.length() - 3);
    }

    public static String obfuscateTaxCode(String taxCode) {
        if (StringUtils.isBlank(taxCode)) {
            return taxCode;
        }
        return taxCode.substring(0,3) + "***" + taxCode.substring(taxCode.length() - 3);
    }

    public static String obfuscatePar(String par) {
        if (StringUtils.isBlank(par)) {
            return par;
        }
        return par.substring(0,3) + "***" + par.substring(par.length() - 3);
    }

    public static String obfuscateHtoken(String htoken) {
        if (StringUtils.isBlank(htoken)) {
            return htoken;
        }
        return htoken.substring(0,3) + "***" + htoken.substring(htoken.length() - 3);
    }

}
