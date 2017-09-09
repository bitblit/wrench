package com.erigir.wrench;

import java.util.regex.Pattern;

/**
 * Utils for data validation tasks
 */
public class ValidationUtils {
  public static final Pattern PHONE = Pattern.compile("\\d\\d\\d-\\d\\d\\d-\\d\\d\\d\\d");
  public static final Pattern EMAIL = Pattern.compile(".+@.+\\.[a-z]+");

  public static boolean validPhone(String number) {
    return number != null && number.length() == 12 && PHONE.matcher(number).matches();
  }

  public static boolean validEmail(String email) {
    return email != null && EMAIL.matcher(email).matches();
  }

}
