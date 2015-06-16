package com.erigir.wrench.web;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

/**
 * Created by chrweiss on 6/13/15.
 */
public class TestHitMeasuringFilter {
    private static final Logger LOG = LoggerFactory.getLogger(TestHitMeasuringFilter.class);

    @Test
    public void testHitIncrementing()
            throws Exception
    {
        HitMeasuringFilter filter = new HitMeasuringFilter();
        HttpServletRequest mockReq = createMock(HttpServletRequest.class);
        expect(mockReq.getRequestURI()).andReturn("/t1").anyTimes();
        replay(mockReq);


        HttpServletResponse mockResp = createMock(HttpServletResponse.class);
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        mockResp.setContentType("application/json");
        mockResp.setContentLength(230);
        expect(mockResp.getWriter()).andReturn(pw);
        replay(mockResp);


        FilterChain mockFilterChain = createMock(FilterChain.class);

        filter.setReportingPattern(Pattern.compile("/report"));

        HitMeasuringEntry hme1 = new HitMeasuringEntry(Pattern.compile("/t1"), Collections.EMPTY_MAP, Collections.EMPTY_MAP);
        HitMeasuringEntry hme2 =  new HitMeasuringEntry(Pattern.compile("/t2"), Collections.EMPTY_MAP, Collections.EMPTY_MAP);

        filter.setTrackingList(Arrays.asList(hme1, hme2));

        for (int i=0;i<3;i++)
        {
            filter.doFilter(mockReq, mockResp, mockFilterChain);
        }

        List<Map<String,Object>> report = filter.generateReport();

        //LOG.info("Report : \n{}", report);
        Map<String,Object> vals = report.get(0);
        assertNotNull(vals);
        assertEquals(vals.get(HitMeasuringFilter.HIT_COUNT_REPORT_KEY), 3);

        filter.doReport(mockResp);

        sw.flush();
        String s = sw.getBuffer().toString();
        //LOG.info(s);


    }
}
