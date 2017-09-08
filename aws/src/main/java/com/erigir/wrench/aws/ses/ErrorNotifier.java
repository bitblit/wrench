package com.erigir.wrench.aws.ses;

import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.model.Body;
import com.amazonaws.services.simpleemail.model.Content;
import com.amazonaws.services.simpleemail.model.Destination;
import com.amazonaws.services.simpleemail.model.Message;
import com.amazonaws.services.simpleemail.model.SendEmailRequest;
import com.amazonaws.services.simpleemail.model.SendEmailResult;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;

/**
 * Simple way to email any error to a specific email address
 */
public class ErrorNotifier {
  private static final Logger LOG = LoggerFactory.getLogger(ErrorNotifier.class);

  /**
   * Can set enabled to false when running in DEV mode, which will prevent actual error mailing
   */
  private boolean enabled = true;
  private AmazonSimpleEmailService simpleEmailService;
  private ExecutorService executor;
  private String reportToEmailAddress;
  private String reportFromEmailAddress;

  public static Map<String, Object> servletRequestToDescriptiveMap(HttpServletRequest req, String inPrefix) {
    String pre = (inPrefix == null) ? "" : inPrefix;
    Map<String, Object> rval = new TreeMap<>();
    rval.put(pre + "URI", req.getRequestURI());
    rval.put(pre + "Query-String", req.getQueryString());

    Map<String, Object> reqAttr = new TreeMap<>();
    Map<String, Object> sesAttr = new TreeMap<>();

    rval.put(pre + "ReqAttr", reqAttr);
    rval.put(pre + "SesAttr", reqAttr);

    for (String key : Collections.list(req.getAttributeNames())) {
      Object val = req.getAttribute(key);
      if (val != null) {
        Object outVal = (Throwable.class.isAssignableFrom(val.getClass())) ? ExceptionUtils.getStackTrace((Throwable) val) : val;
        reqAttr.put(key, outVal);
      }
    }

    HttpSession sess = req.getSession();
    for (String key : Collections.list(sess.getAttributeNames())) {
      Object val = sess.getAttribute(key);
      if (val != null) {
        Object outVal = (Throwable.class.isAssignableFrom(val.getClass())) ? ExceptionUtils.getStackTrace((Throwable) val) : val;
        sesAttr.put(key, outVal);
      }
    }

    return rval;
  }

  /**
   * Generates an error report from the provided throwable and emails it to the defined address for this bean
   *
   * @param t
   */
  public void reportError(Throwable t, Map<String, Object> otherData) {
    String errorReport = buildErrorReport(t, otherData);
    executor.execute(new SendMailCommand("ErrReport: " + new Date(), errorReport));
  }

  /**
   * Generates an error report from the provided request and throwable and emails it to the defined address for this bean
   *
   * @param errorRequest
   */
  public void reportError(Throwable t, HttpServletRequest errorRequest, Map<String, Object> otherData) {
    Map<String, Object> holder = new TreeMap<>();
    holder.putAll(servletRequestToDescriptiveMap(errorRequest, "REQ:"));
    holder.putAll(otherData);
    reportError(t, holder);
  }

  private String buildErrorReport(Throwable main, Map<String, Object> otherData) {
    StringBuilder sb = new StringBuilder();

    sb.append("Error report generated :").append(new Date()).append("\n");
    if (main != null) {
      sb.append("\n\n------------------- BEGIN MAIN ERROR-------------------------\n\n");
      sb.append(ExceptionUtils.getStackTrace(main));
      sb.append("\n\n------------------- END MAIN ERROR---------------------------\n\n");
    }

    sb.append("\n\nOther Provided Data:\n").append(String.valueOf(otherData)).append("\n\n");

    sb.append("\n-----\n\nServer State: \n\n");
    Runtime r = Runtime.getRuntime();
    sb.append("\nTotal Memory : ").append(FileUtils.byteCountToDisplaySize(r.totalMemory())).append("\n");
    sb.append("\nFree Memory : ").append(FileUtils.byteCountToDisplaySize(r.freeMemory())).append("\n");
    sb.append("\nMax Memory : ").append(FileUtils.byteCountToDisplaySize(r.maxMemory())).append("\n");
    sb.append("\nProcessors : ").append(FileUtils.byteCountToDisplaySize(r.availableProcessors())).append("\n");
    return sb.toString();
  }

  public void setSimpleEmailService(AmazonSimpleEmailService simpleEmailService) {
    this.simpleEmailService = simpleEmailService;
  }

  public void setExecutor(ExecutorService executor) {
    this.executor = executor;
  }

  public void setReportToEmailAddress(String reportToEmailAddress) {
    this.reportToEmailAddress = reportToEmailAddress;
  }

  public void setReportFromEmailAddress(String reportFromEmailAddress) {
    this.reportFromEmailAddress = reportFromEmailAddress;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  class SendMailCommand implements Runnable {
    private String subject;
    private String body;

    public SendMailCommand(String subject, String body) {
      super();
      this.subject = subject;
      this.body = body;
    }

    @Override
    public void run() {
      if (enabled) {
        try {
          SendEmailRequest ser = new SendEmailRequest(reportFromEmailAddress, new Destination(Arrays.asList(reportToEmailAddress)),
              new Message(new Content(subject), new Body(new Content(body))));
          SendEmailResult res = simpleEmailService.sendEmail(ser);

          LOG.debug("Sent {} Got {}", ser, res);
        } catch (Exception e) {
          LOG.error("Error while processing the error email thread:" + e, e);
        }
      } else {
        LOG.warn("Not sending email since bean is disabled");
      }
    }

  }
}
