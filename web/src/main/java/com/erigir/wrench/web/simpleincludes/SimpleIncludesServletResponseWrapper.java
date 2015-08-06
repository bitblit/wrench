package com.erigir.wrench.web.simpleincludes;

import com.erigir.wrench.simpleincludes.SimpleIncludesProcessor;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

/**
 * Created by cweiss on 7/29/15.
 */
class SimpleIncludesServletResponseWrapper extends HttpServletResponseWrapper {

    private SimpleIncludesServletOutputStream simpleIncludesServletOutputStream = null;
    private PrintWriter printWriter      = null;
    private SimpleIncludesProcessor simpleIncludesProcessor;

    public SimpleIncludesServletResponseWrapper(HttpServletResponse response, SimpleIncludesProcessor processor)
            throws IOException {
        super(response);
        this.simpleIncludesProcessor = processor;
    }

    public void close() throws IOException {

        //PrintWriter.close does not throw exceptions.
        //Hence no try-catch block.
        if (this.printWriter != null) {
            this.printWriter.close();
        }

        if (this.simpleIncludesServletOutputStream != null) {
            this.simpleIncludesServletOutputStream.close();
        }
    }


    /**
     * Flush OutputStream or PrintWriter
     *
     * @throws IOException
     */

    @Override
    public void flushBuffer() throws IOException {

        //PrintWriter.flush() does not throw exception
        if(this.printWriter != null) {
            this.printWriter.flush();
        }

        IOException exception1 = null;
        try{
            if(this.simpleIncludesServletOutputStream != null) {
                this.simpleIncludesServletOutputStream.flush();
            }
        } catch(IOException e) {
            exception1 = e;
        }

        IOException exception2 = null;
        try {
            super.flushBuffer();
        } catch(IOException e){
            exception2 = e;
        }

        if(exception1 != null) throw exception1;
        if(exception2 != null) throw exception2;
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        if (this.printWriter != null) {
            throw new IllegalStateException(
                    "PrintWriter obtained already - cannot get OutputStream");
        }
        if (this.simpleIncludesServletOutputStream == null) {
            this.simpleIncludesServletOutputStream = new SimpleIncludesServletOutputStream(
                    getResponse().getOutputStream(),simpleIncludesProcessor);
        }
        return this.simpleIncludesServletOutputStream;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        if (this.printWriter == null && this.simpleIncludesServletOutputStream != null) {
            throw new IllegalStateException(
                    "OutputStream obtained already - cannot get PrintWriter");
        }
        if (this.printWriter == null) {
            this.simpleIncludesServletOutputStream = new SimpleIncludesServletOutputStream(
                    getResponse().getOutputStream(),simpleIncludesProcessor);
            this.printWriter      = new PrintWriter(new OutputStreamWriter(
                    this.simpleIncludesServletOutputStream, getResponse().getCharacterEncoding()));
        }
        return this.printWriter;
    }


    @Override
    public void setContentLength(int len) {
        // Ignore, since this filter changes the length of the content
    }

}
