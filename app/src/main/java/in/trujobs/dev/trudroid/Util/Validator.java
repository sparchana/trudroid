package in.trujobs.dev.trudroid.Util;


import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by zero on 7/5/16.
 */
public class Validator {
    public static boolean isValidLocalityName(String localityName){
        String expression = "^[\\.a-zA-Z\\s]+";
        return localityName.matches(expression);
    }
    public static boolean isPhoneNumberValid(String phoneNo){
        if (phoneNo.matches("[7-9]{1}[0-9]{9}")) return true;
        return false;
    }

    public static boolean isNameValid(String name){
        if (name.matches("[a-zA-Z][a-zA-Z ]*") && name.length()>2) {
            return true;
        }
        return false;
    }

    public static boolean validateDL(String dlNumber) {
        if (dlNumber == null) {
            return false;
        }
        dlNumber = dlNumber.replaceAll("[^a-zA-Z0-9]", "");
        if(dlNumber.length() != 15){
            Tlog.i(dlNumber+"dlNumber length is not 15, its: "+dlNumber.length());
            return false;
        }
        int validCount = 0;
        dlNumber = dlNumber.toLowerCase();
        String stateCode = dlNumber.substring(0,2);
        String cityCode = dlNumber.substring(2,4);
        String issueYear = dlNumber.substring(4,8);
        String uid = dlNumber.substring(8,15);
        if(StringUtils.isAlpha(stateCode)) {
            Tlog.i("statecode found");
            validCount++;
        }

        if(NumberUtils.isDigits(cityCode)) {
            Tlog.i("citycode found");
            validCount++;
        }

        if(issueYear.length() == 4 && NumberUtils.isDigits(issueYear)){
            Tlog.i("issueYear found");
            validCount++;
        }
        if(NumberUtils.isDigits(uid)) {
            Tlog.i("uid found");
            validCount++;
        }

        if(validCount == 4){
            return true;
        } else {
            return false;
        }
    }

    public static boolean validatePASSPORT(String passPort) {
        if (passPort == null) {
            return false;
        }
        passPort = passPort.replaceAll("[^a-zA-Z0-9]", "");
        passPort = passPort.replaceAll("\\s", "");

        int validCount = 0;
        passPort = passPort.toLowerCase();
        String code = passPort.substring(1, passPort.length());

        if(StringUtils.isAlpha(passPort.substring(0, 1))) {
            Tlog.i("passPort first char found");
            validCount++;
        }
        if(NumberUtils.isDigits(code)) {
            Tlog.i("passPort number found");
            validCount++;
        }

        if(validCount == 2){
            return true;
        } else {
            return false;
        }
    }

    public static boolean validatePAN(String panNumber) {
        if(panNumber == null) {
            return false;
        }

        Pattern pattern = Pattern.compile("[A-Z]{5}[0-9]{4}[A-Z]{1}");

        Matcher matcher = pattern.matcher(panNumber);
        // Check if pattern matches
        if (matcher.matches()) {
            return true;
        }
        return false;
    }

    public static boolean validateAadhaar(String aadharNumber) {
        if(aadharNumber == null) {
            return false;
        }
        Pattern aadharPattern = Pattern.compile("\\d{12}");
        boolean isValidAadhar = aadharPattern.matcher(aadharNumber).matches();
        if(isValidAadhar){
            isValidAadhar = VerhoeffAlgorithm.validateVerhoeff(aadharNumber);
        }
        return isValidAadhar;
    }
}
