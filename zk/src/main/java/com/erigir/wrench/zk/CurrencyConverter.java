package com.erigir.wrench.zk;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.bind.BindContext;
import org.zkoss.bind.Converter;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.UiException;

import java.text.NumberFormat;
import java.text.ParseException;


/**
 * cweiss : 10/9/12 4:14 PM
 */
public class CurrencyConverter implements Converter {
  private static final Logger LOG = LoggerFactory.getLogger(CurrencyConverter.class);

  /**
   * Convert Number to String.
   *
   * @param val  number to be converted
   * @param comp associated component
   * @param ctx  bind context for associate Binding and extra parameter (e.g. format)
   * @return the converted String
   */
  public Object coerceToUi(Object val, Component comp, BindContext ctx) {
    Number number = null;
    if (val != null && !"-".equals(val)) {
      if (Number.class.isAssignableFrom(val.getClass())) {
        number = (Number) val;
      } else {
        try {
          number = NumberFormat.getNumberInstance().parse(String.valueOf(val));
        } catch (ParseException e) {
          LOG.warn("Error parsing {} as a number", val);
          throw UiException.Aide.wrap(e);
        } catch (IllegalArgumentException ie) {
          LOG.warn("Error parsing {} as a number", val);
          throw UiException.Aide.wrap(ie);
        }
      }
    }
    return (number == null) ? null : NumberFormat.getCurrencyInstance().format(number);

  }

  /**
   * Convert String to Number.
   *
   * @param val  date in string form
   * @param comp associated component
   * @param ctx  bind context for associate Binding and extra parameter (e.g. format)
   * @return the converted Date
   */
  public Object coerceToBean(Object val, Component comp, BindContext ctx) {
        /*final String format = (String) ctx.getConverterArg("format");
        if(format==null) throw new NullPointerException("format attribute not found"); */
    final String number = (String) val;
    try {
      return number == null ? null : NumberFormat.getCurrencyInstance().parse(number);
    } catch (ParseException e) {
      throw UiException.Aide.wrap(e);
    }
  }
}
