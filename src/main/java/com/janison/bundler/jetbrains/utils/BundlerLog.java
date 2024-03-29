package com.janison.bundler.jetbrains.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class BundlerLog {

  private final BundlerSeverity severity;
  private final String message;
  private final Date date;

  public BundlerLog(String message, String timestamp, String level) {
    this.message = message;
    this.date = getDefaultDate(timestamp);
    this.severity = getSeverityLevelFromString(level);
  }

  public BundlerSeverity getSeverity() {
    return severity;
  }

  public String getMessage() {
    return message;
  }

  public Date getDate() {
    return date;
  }

  /**
   * This method returns severity tag corresponding to the severity.
   *
   * @param severity the severity value
   * @return a string corresponding to the severity value
   */
  public static String getSeverityTagFromSeverity(BundlerSeverity severity) {
    switch (severity) {
      case INFO:
        return "[INFO]";
      case WARNING:
        return "[WARNING]";
      case ERROR:
        return "[ERROR]";
      case DEBUG:
        return "[DEBUG]";
      default:
        return null;
    }
  }

  /**
   * This methods returns the log time stamp in bundler friendly format that can be directly
   * printed to the console.
   *
   * @return time stamp string in the required format
   */
  public String getPrintableTimeStamp() {
    final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("[HH:mm:ss]");
    simpleDateFormat.setTimeZone(TimeZone.getDefault());
    return simpleDateFormat.format(date);
  }

  private Date getDefaultDate(String timestamp) {
    final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

    try {
      return simpleDateFormat.parse(timestamp);
    } catch (ParseException e) {
      e.printStackTrace();
    }

    return null;
  }

  public static BundlerSeverity getSeverityLevelFromString(String severity) {
    switch (severity) {
      case "ERROR":
        return BundlerSeverity.ERROR;
      case "WARNING":
        return BundlerSeverity.WARNING;
      case "DEBUG":
        return BundlerSeverity.DEBUG;
      default:
        return null;
    }
  }
}
