package com.erigir.wrench.aws.cloudfront.logparser.handler;

import com.erigir.wrench.aws.cloudfront.logparser.CloudFrontAccessLogEntry;
import com.erigir.wrench.aws.cloudfront.logparser.CloudFrontAccessLogHandler;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by chrweiss on 3/16/15.
 */
public class ChainHandler implements CloudFrontAccessLogHandler {
    private List<CloudFrontAccessLogHandler> handlerChain = new LinkedList<>();

    public ChainHandler() {
    }

    @Override
    public boolean handleCloudFrontAccessLogEntry(CloudFrontAccessLogEntry entry) {
        boolean cont = true;

        for (Iterator<CloudFrontAccessLogHandler> i = handlerChain.iterator();i.hasNext() && cont;)
        {
            cont = i.next().handleCloudFrontAccessLogEntry(entry);
        }

        return cont;
    }

    public ChainHandler addHandler(CloudFrontAccessLogHandler handler)
    {
        if (handler==null || handler==this)
        {
            throw new IllegalArgumentException("Cannot add null or self to the chain");
        }
        handlerChain.add(handler);
        return this;
    }

}
