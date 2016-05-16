package com.erigir.wrench.ape.http;

import com.erigir.wrench.ape.exception.ApeExceptionWriter;
import com.erigir.wrench.ape.exception.DataValidationException;
import com.erigir.wrench.ape.exception.NoSuchResourceException;
import com.erigir.wrench.aws.sns.ServerErrorNotifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.Principal;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by cweiss on 8/22/15.
 */
@Controller
public class ApeErrorHandlerControl {
    private static final Logger LOG = LoggerFactory.getLogger(ApeErrorHandlerControl.class);
    @Resource(name = "apeExceptionWriter")
    private ApeExceptionWriter apeExceptionWriter;
    // Autowired here since there might not be one
    @Autowired(required = false)
    private ServerErrorNotifier serverErrorNotifier;

    @RequestMapping(value = "/ErrorHandler")
    public void
    errorHandler(HttpServletRequest req, HttpServletResponse resp) {
        Exception main = (Exception) req.getAttribute("javax.servlet.error.exception");
        int currentStatus = resp.getStatus();
        if (main == null) {
            if (currentStatus == 400) {
                // This is typically caused by Spring not being able to parse the supplied
                // body into the needed object.  Fake that instead
                LOG.warn("No exception and status 400 - treating as spring parse error");
                main = new DataValidationException(Collections.singletonMap("request-body", "bad or mismatched json"));
            } else {
                LOG.warn("Someone requested the error handler directly.  Faking a 404");
                main = new NoSuchResourceException();
            }
        }

        try {
            if (serverErrorNotifier != null) {
                buildAndSendErrorReport(req);
            }
            // Now output json
            apeExceptionWriter.writeExceptionToResponse(req, resp, main);

            //req.removeAttribute("javax.servlet.error.exception");
        } catch (Exception e) {
            LOG.error("Well this is really bad, got an exception trying to handle the error page!:" + e, e);
        }
    }

    private void buildAndSendErrorReport(HttpServletRequest req) {
        LOG.info("Building error report");
        try {
            Map<String, Object> other = new TreeMap<String, Object>();
            Principal principal = req.getUserPrincipal();
            if (principal != null) {
                other.put("Logged In Principal", principal);
            }
            serverErrorNotifier.reportError(req, other);
        } catch (Throwable t) {
            LOG.error("Didn't send report due to error", t);
        }
    }

    public void setServerErrorNotifier(ServerErrorNotifier serverErrorNotifier) {
        this.serverErrorNotifier = serverErrorNotifier;
    }

    public void setApeExceptionWriter(ApeExceptionWriter apeExceptionWriter) {
        this.apeExceptionWriter = apeExceptionWriter;
    }
}
