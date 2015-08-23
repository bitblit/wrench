package com.erigir.wrench.web.simpleincludes;

import com.erigir.wrench.UTF8Encoder;
import com.erigir.wrench.simpleincludes.SimpleIncludesProcessor;

import javax.servlet.ServletOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by cweiss on 7/29/15.
 */
class SimpleIncludesServletOutputStream extends ServletOutputStream {
    private SimpleIncludesProcessor simpleIncludesProcessor;
    private ByteArrayOutputStream buffer = null;
    private OutputStream wrapped;

    public SimpleIncludesServletOutputStream(OutputStream output, SimpleIncludesProcessor processor)
            throws IOException {
        super();
        this.buffer = new ByteArrayOutputStream();
        this.wrapped = output;
        this.simpleIncludesProcessor = processor;
    }

    @Override
    public void close() throws IOException {
        // On close, do the replace and write to the main output stream,
        // then close the main output stream
        String cache = new String(buffer.toByteArray());
        String output = simpleIncludesProcessor.processIncludes(cache);
        this.wrapped.write(output.getBytes(UTF8Encoder.UTF8));
        this.wrapped.flush();
        this.wrapped.close();
    }

    @Override
    public void flush() throws IOException {
        // DO nothing?this.gzipOutputStream.flush();
    }

    @Override
    public void write(byte b[]) throws IOException {
        this.buffer.write(b);
    }

    @Override
    public void write(byte b[], int off, int len) throws IOException {
        this.buffer.write(b, off, len);
    }

    @Override
    public void write(int b) throws IOException {
        this.buffer.write(b);
    }

    public void setSimpleIncludesProcessor(SimpleIncludesProcessor simpleIncludesProcessor) {
        this.simpleIncludesProcessor = simpleIncludesProcessor;
    }
}