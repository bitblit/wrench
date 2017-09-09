package com.erigir.wrench.aws.sns;

import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import com.erigir.wrench.QuietUtils;
import com.erigir.wrench.web.HitMeasuringEntry;
import com.erigir.wrench.web.HitMeasuringFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

/**
 * This class monitors a hit measuring filter and sends notifications if the hits aren't coming
 * fast enough (or are coming too fast) - uses AmazonSNS to send the notifications (details of
 * configuration of who recieves, etc, are left to the topic creation)
 * <p>
 * Obviously your AWS user will need the sns:PublishRequest priv if you want to use this class.
 * <p>
 * You can set the maximum amount of time between requests (to detect dead clients) or minimum
 * (to detect DDOS).  DDOS detection is probably better done elsewhere though, since this runs
 * in a single thread and is really meant to execute about once a minute at most.
 * <p>
 * Defaults to a 5 minute startup delay (so that the requests have a chance to come in) and
 * a 1 minute check interval.  Yes, this means by default you'll get a notification per minute
 * if something is out of whack.
 * <p>
 * Created by chrweiss on 3/13/15.
 */
public class HitMeasuringFilterNotifier implements Runnable {
  private static final Logger LOG = LoggerFactory.getLogger(HitMeasuringFilterNotifier.class);
  private HitMeasuringFilter hitMeasuringFilter;
  private Map<HitMeasuringEntry, Long> maxHitAge = new TreeMap<>();
  private Map<HitMeasuringEntry, Long> minHitAge = new TreeMap<>();
  private Long checkPeriodInMs = 60_000L; // Defaults to minute long checks
  private Long startupDelay = 300_000L; // 5 minute startup delay to give the system time to start
  private boolean aborted = false;
  private AmazonSNSClient sns;
  private String snsTopicARN;

  public HitMeasuringFilterNotifier() {
  }

  @Override
  public void run() {
    LOG.info("Starting up HitMeasuringFilterNotifier (Startup delay is {})", startupDelay);

    if (startupDelay != null) {
      QuietUtils.quietSleep(startupDelay);
    }

    while (!aborted) {
      LOG.debug("Running HitMeasuringFilterNotifier check");
      checkConfiguration();

      long now = System.currentTimeMillis();

      Map<HitMeasuringEntry, String> issues = new TreeMap<>();
      for (Map.Entry<HitMeasuringEntry, Long> e : maxHitAge.entrySet()) {
        Date last = hitMeasuringFilter.getLastHit().get(e.getKey());
        Long age = (last == null) ? null : now - last.getTime();
        if (age == null || age > e.getValue()) {
          issues.put(e.getKey(), "Max age is " + e.getValue() + "ms but it has been " + age + "ms");
        }
      }

      for (Map.Entry<HitMeasuringEntry, Long> e : minHitAge.entrySet()) {
        Date last = hitMeasuringFilter.getLastHit().get(e.getKey());
        Long age = (last == null) ? null : now - last.getTime();
        if (age == null || age < e.getValue()) {
          issues.put(e.getKey(), "Min age is " + e.getValue() + "ms but it has been " + age + "ms");
        }
      }

      processIssues(issues);

      QuietUtils.quietSleep(checkPeriodInMs);
    }
    LOG.info("Stopping HitMeasuringFilterNotifier thread (Aborted)");
  }

  public void processIssues(Map<HitMeasuringEntry, String> issues) {
    if (issues != null && issues.size() > 0) {
      LOG.info("Sending notification with {} issues", issues.size());
      // Build the email
      StringBuffer sb = new StringBuffer();
      sb.append("The following issues have occurred: \n\n");
      for (Map.Entry<HitMeasuringEntry, String> e : issues.entrySet()) {
        sb.append(e.getKey().toString()).append(" : ").append(e.getValue()).append("\n\n");
      }

      try {
        PublishRequest publishRequest = new PublishRequest(snsTopicARN, sb.toString());
        PublishResult publishResult = sns.publish(publishRequest);
      } catch (Exception e) {
        LOG.warn("An error occurred trying to send notification email", e);
      }
    } else {
      LOG.info("No issues found");
    }
  }

  private void checkConfiguration() {
    if (hitMeasuringFilter == null) {
      throw new IllegalStateException("Cant continue - hitMeasuringFilter not set");
    }
    if (sns == null) {
      throw new IllegalStateException("Cant continue - sns not set");
    }
    if (snsTopicARN == null) {
      throw new IllegalStateException("Cant continue - snsTopicARN not set");
    }
  }

  public void setHitMeasuringFilter(HitMeasuringFilter hitMeasuringFilter) {
    this.hitMeasuringFilter = hitMeasuringFilter;
  }

  public void setMaxHitAge(Map<HitMeasuringEntry, Long> maxHitAge) {
    this.maxHitAge = maxHitAge;
  }

  public void setMinHitAge(Map<HitMeasuringEntry, Long> minHitAge) {
    this.minHitAge = minHitAge;
  }

  public void setCheckPeriodInMs(Long checkPeriodInMs) {
    this.checkPeriodInMs = checkPeriodInMs;
    if (checkPeriodInMs == null || checkPeriodInMs < 1000) {
      throw new IllegalArgumentException("Cannot set check period to less than 1000 (once per second)");
    }
  }

  public void setAborted(boolean aborted) {
    this.aborted = aborted;
  }

  public void setStartupDelay(Long startupDelay) {
    this.startupDelay = startupDelay;
  }

  public void setSns(AmazonSNSClient sns) {
    this.sns = sns;
  }

  public void setSnsTopicARN(String snsTopicARN) {
    this.snsTopicARN = snsTopicARN;
  }

}
