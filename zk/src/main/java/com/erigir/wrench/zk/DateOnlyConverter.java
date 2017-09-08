package com.erigir.wrench.zk;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.bind.BindContext;
import org.zkoss.bind.Converter;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.UiException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * cweiss : 10/9/12 4:14 PM
 */
public class DateOnlyConverter implements Converter {
  private static final Logger LOG = LoggerFactory.getLogger(DateOnlyConverter.class);
  String format = "MM/dd/yyyy";

  /**
   * Convert Date to String.
   *
   * @param val  date to be converted
   * @param comp associated component
   * @param ctx  bind context for associate Binding and extra parameter (e.g. format)
   * @return the converted String
   */
  public Object coerceToUi(Object val, Component comp, BindContext ctx) {
    //user sets format in annotation of binding or args when calling binder.addPropertyBinding()
    //final String format = (String) ctx.getConverterArg("format");
    if (format == null) {
      throw new NullPointerException("format attribute not found");
    }
    final Date date = (Date) val;
    return date == null ? null : new SimpleDateFormat(format).format(date);
  }

  /**
   * Convert String to Date.
   *
   * @param val  date in string form
   * @param comp associated component
   * @param ctx  bind context for associate Binding and extra parameter (e.g. format)
   * @return the converted Date
   */
  public Object coerceToBean(Object val, Component comp, BindContext ctx) {
    //final String format = (String) ctx.getConverterArg("format");
    if (format == null) {
      throw new NullPointerException("format attribute not found");
    }
    final String date = (String) val;
    try {
      return date == null ? null : new SimpleDateFormat(format).parse(date);
    } catch (ParseException e) {
      throw UiException.Aide.wrap(e);
    }
  }
}
