package com.komentum.global.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class DateUtils {

  public static String convertToDateString(LocalDateTime date) {
    return DateTimeFormatter.ISO_LOCAL_DATE.format(date);
  }

  public static LocalDate toLocalDate(Date date) {
    return date
        .toInstant()
        .atZone(ZoneId.systemDefault())
        .toLocalDate();
  }
}
