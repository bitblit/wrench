package com.erigir.wrench.ape.http;

import com.erigir.wrench.ape.exception.ApeExceptionWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by chrweiss on 7/6/14.
 */
@ControllerAdvice
public class ApeExceptionHandler {
    private static final Logger LOG = LoggerFactory.getLogger(ApeExceptionHandler.class);

    @Resource(name = "scribeExceptionWriter")
    private ApeExceptionWriter scribeExceptionWriter;

    @ExceptionHandler(value = Exception.class)
    public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse resp, Exception ex) {
        scribeExceptionWriter.writeExceptionToResponse(request, resp, ex);
        return null;
    }

}
