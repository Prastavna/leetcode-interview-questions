package com.prastavna.leetcode.utils;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

public final class Date {
  private Date() {}

  public static String toDate(String isoDateTime) {
    if (isoDateTime == null || isoDateTime.isBlank()) return isoDateTime;
    try {
      OffsetDateTime odt = OffsetDateTime.parse(isoDateTime);
      return odt.toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE);
    } catch (Exception e) {
      int t = isoDateTime.indexOf('T');
      return t > 0 ? isoDateTime.substring(0, t) : isoDateTime;
    }
  }
}

