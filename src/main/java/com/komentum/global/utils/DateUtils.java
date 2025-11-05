package com.komentum.global.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateUtils {

  public static String convertToDateString(LocalDateTime date) {
    return DateTimeFormatter.ISO_LOCAL_DATE.format(date);
  }
}
