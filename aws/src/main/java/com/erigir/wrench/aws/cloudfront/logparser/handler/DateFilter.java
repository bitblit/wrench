package com.erigir.wrench.aws.cloudfront.logparser.handler;

import com.erigir.wrench.QuietUtils;
import com.erigir.wrench.aws.cloudfront.logparser.CloudFrontAccessLogEntry;
import com.erigir.wrench.aws.cloudfront.logparser.CloudFrontAccessLogHandler;

import java.util.Date;

/**
 * Created by chrweiss on 3/16/15.
 */
public class DateFilter implements CloudFrontAccessLogHandler {

    private Date start;
    private Date end;

    public DateFilter() {
    }

    public DateFilter(Date start, Date end) {
        this.end = end;
        this.start = start;
    }

    public DateFilter(String start, String end) {

        this.end = QuietUtils.quietParse(end, CloudFrontAccessLogEntry.DATE_FORMAT);
        this.start = QuietUtils.quietParse(start, CloudFrontAccessLogEntry.DATE_FORMAT);
    }

    @Override
    public boolean handleCloudFrontAccessLogEntry(CloudFrontAccessLogEntry entry) {
        Date test = entry.getDate();
        return (start == null || start.before(test)) && (end == null || end.after(test));
    }


    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public Date getEnd() {
        return end;
    }

    public void setEnd(Date end) {
        this.end = end;
    }
}
