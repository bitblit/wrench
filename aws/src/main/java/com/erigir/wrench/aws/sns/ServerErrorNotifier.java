package com.erigir.wrench.aws.sns;

import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServerErrorNotifier {
  private static final Logger LOG = LoggerFactory.getLogger(ServerErrorNotifier.class);

  private AmazonSNSClient sns;
  private String snsTopicARN;
  private boolean devMode;

  /**
   * Generates an error report from the provided request and sends it to the defined ARN for this bean
   *
   * @param errorRequest
   */
  public void reportError(HttpServletRequest errorRequest, Map<String, Object> otherData) {
    String errorReport = buildErrorReport(errorRequest, otherData);
    LOG.error("Error occurred:" + errorReport);
    if (devMode) {
      LOG.error("Not sending since we are running dev mode");
    } else {
      try {
        PublishRequest publishRequest = new PublishRequest(snsTopicARN, errorReport);
        PublishResult publishResult = sns.publish(publishRequest);
      } catch (Exception e) {
        LOG.warn("An error occurred trying to send notification email", e);
      }
    }

  }

  public void reportError(HttpServletRequest errorRequest) {
    reportError(errorRequest, new HashMap<String, Object>());
  }

  private String buildErrorReport(HttpServletRequest req, Map<String, Object> otherData) {
    StringBuilder sb = new StringBuilder();

    sb.append("Error report generated :").append(new Date()).append("\n");
    sb.append("URI         :").append(req.getRequestURI()).append("\n");
    sb.append("Query String:").append(req.getQueryString()).append("\n");
    Throwable main = (Throwable) req.getAttribute("javax.servlet.error.exception");
    if (main != null) {
      sb.append("\n\n------------------- BEGIN MAIN ERROR-------------------------\n\n");
      sb.append(ExceptionUtils.getStackTrace(main));
      sb.append("\n\n------------------- END MAIN ERROR---------------------------\n\n");
    }

    sb.append("\n\nOther Provided Data:\n").append(String.valueOf(otherData)).append("\n\n");

    sb.append("Contents of request attributes: \n\n");
    List<String> attrNames = Collections.list(req.getAttributeNames());
    for (String s : attrNames) {
      sb.append(s).append(" = ");
      Object o = req.getAttribute(s);
      if (o != null && Throwable.class.isAssignableFrom(o.getClass())) {
        sb.append("\n");
        sb.append(ExceptionUtils.getStackTrace((Throwable) o));
      } else {
        sb.append(String.valueOf(o));
      }
      sb.append("\n");
    }

    sb.append("\n-----\n\nContents of session attributes: \n\n");
    attrNames = Collections.list(req.getSession().getAttributeNames());
    for (String s : attrNames) {
      sb.append(s).append(" = ");
      Object o = req.getSession().getAttribute(s);
      if (o != null && Throwable.class.isAssignableFrom(o.getClass())) {
        sb.append("\n");
        sb.append(ExceptionUtils.getStackTrace((Throwable) o));
      } else {
        sb.append(String.valueOf(o));
      }
      sb.append("\n");
    }

    sb.append("\n-----\n\nServer State: \n\n");
    Runtime r = Runtime.getRuntime();
    sb.append("\nTotal Memory : ").append(r.totalMemory()).append("\n");
    sb.append("\nFree Memory : ").append(r.freeMemory()).append("\n");
    sb.append("\nMax Memory : ").append(r.maxMemory()).append("\n");
    sb.append("\nProcessors : ").append(r.availableProcessors()).append("\n");
    return sb.toString();
  }

  public void setSns(AmazonSNSClient sns) {
    this.sns = sns;
  }

  public void setSnsTopicARN(String snsTopicARN) {
    this.snsTopicARN = snsTopicARN;
  }

  public void setDevMode(boolean devMode) {
    this.devMode = devMode;
  }
}
