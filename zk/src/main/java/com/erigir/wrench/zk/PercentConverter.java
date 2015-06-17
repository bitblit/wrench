package com.erigir.wrench.zk;

import org.zkoss.bind.BindContext;
import org.zkoss.bind.Converter;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.UiException;

import java.text.NumberFormat;
import java.text.ParseException;

/**
 * cweiss : 10/9/12 4:14 PM
 */
public class PercentConverter implements Converter {
    /**
     * Convert Number to String.
     *
     * @param val  number to be converted
     * @param comp associated component
     * @param ctx  bind context for associate Binding and extra parameter (e.g. format)
     * @return the converted String
     */
    public Object coerceToUi(Object val, Component comp, BindContext ctx) {
        final Number number = (Number) val;
        return (number == null) ? null : nf().format(val);
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
            return number == null ? null : nf().parse(number);
        } catch (ParseException e) {
            throw UiException.Aide.wrap(e);
        }
    }

    private NumberFormat nf() {
        NumberFormat rval = NumberFormat.getNumberInstance();
        rval.setMinimumFractionDigits(2);
        rval.setMaximumFractionDigits(2);
        return rval;
    }
}
