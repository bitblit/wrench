package com.erigir.wrench.ape.http;

import com.erigir.wrench.ape.exception.TimestampSkewTooLargeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Verify that the timestamp, if submitted, is within acceptable bounds
 *
 * @author cweiss
 */
@Component(value = "checkTimestampSkewFilter")
public class CheckTimestampSkewFilter extends AbstractApeFilter {
    private static Logger LOG = LoggerFactory.getLogger(CheckTimestampSkewFilter.class);
    private long maxTimestampSkew = 1000 * 60 * 5; // Default 5 minutes

    @Override
    public void innerFilter(HttpServletRequest req, HttpServletResponse resp, FilterChain chain) throws IOException, ServletException {
        Long skew = getTimestampSkew(req);
        if (skew != null && skew > maxTimestampSkew) {
            throw new TimestampSkewTooLargeException(skew, maxTimestampSkew);
        }

        chain.doFilter(req, resp); // Matched a no-key regex, handle publicly
    }

    public void setMaxTimestampSkew(long maxTimestampSkew) {
        this.maxTimestampSkew = maxTimestampSkew;
    }

}
