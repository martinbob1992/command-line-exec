package com.marsh.exec.stream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 行输出对象
 * @author Marsh
 * @date 2022-04-07日 13:43
 */
public class LineOutputStream extends OutputStream {

    /** Initial buffer size. */
    private static final int INTIAL_SIZE = 132;

    /** Carriage return */
    private static final int CR = 0x0d;

    /** Linefeed */
    private static final int LF = 0x0a;

    /** the internal buffer */
    protected final ByteArrayOutputStream buffer = new ByteArrayOutputStream(
            INTIAL_SIZE);
    /** 数据按照行存储在这个对象中 */
    protected final List<String> lines = new ArrayList<>();
    private LineOutputListener lineOutputListener = new DefaultLineOutputListener();

    private boolean skip = false;

    protected static final String SYSTEM_CHARSET = System.getProperty("sun.jnu.encoding");

    protected final String charset;


    /**
     * Creates a new instance of this class.
     * Uses the default system charset.
     */
    public LineOutputStream() {
        this(SYSTEM_CHARSET);
    }

    public LineOutputStream(LineOutputListener lineOutputListener) {
        this(SYSTEM_CHARSET,lineOutputListener);
    }

    /**
     * Creates a new instance of this class.
     *
     */
    public LineOutputStream(final String charset) {
        this.charset = charset;
    }

    public LineOutputStream(final String charset,LineOutputListener lineOutputListener) {
        this.charset = charset;
        this.lineOutputListener = lineOutputListener;
    }
    /**
     * Write the data to the buffer and flush the buffer, if a line separator is
     * detected.
     *
     * @param cc data to log (byte).
     * @see OutputStream#write(int)
     */
    @Override
    public void write(final int cc) throws IOException {
        final byte c = (byte) cc;
        if (c == '\n' || c == '\r') {
            if (!skip) {
                processBuffer();
            }
        } else {
            buffer.write(cc);
        }
        skip = c == '\r';
    }

    /**
     * Flush this log stream.
     *
     * @see OutputStream#flush()
     */
    @Override
    public void flush() throws UnsupportedEncodingException {
        if (buffer.size() > 0) {
            processBuffer();
        }
    }

    /**
     * Writes all remaining data from the buffer.
     *
     * @see OutputStream#close()
     */
    @Override
    public void close() throws IOException {
        if (buffer.size() > 0) {
            processBuffer();
        }
        super.close();
    }

    /**
     * Write a block of characters to the output stream
     *
     * @param b the array containing the data
     * @param off the offset into the array where data starts
     * @param len the length of block
     * @throws IOException if the data cannot be written into the stream.
     * @see OutputStream#write(byte[], int, int)
     */
    @Override
    public void write(final byte[] b, final int off, final int len)
            throws IOException {
        // find the line breaks and pass other chars through in blocks
        int offset = off;
        int blockStartOffset = offset;
        int remaining = len;
        while (remaining > 0) {
            while (remaining > 0 && b[offset] != LF && b[offset] != CR) {
                offset++;
                remaining--;
            }
            // either end of buffer or a line separator char
            final int blockLength = offset - blockStartOffset;
            if (blockLength > 0) {
                buffer.write(b, blockStartOffset, blockLength);
            }
            while (remaining > 0 && (b[offset] == LF || b[offset] == CR)) {
                write(b[offset]);
                offset++;
                remaining--;
            }
            blockStartOffset = offset;
        }
    }

    /**
     * Converts the buffer to a string and sends it to {@code processLine}.
     */
    protected void processBuffer() throws UnsupportedEncodingException {
        String content = charset == null || "".equals(charset) ? buffer.toString() : buffer.toString(charset);
        processLine(content);
        lines.add(content);
        buffer.reset();
    }

    /**
     * Logs a line to the log system of the user.
     *
     * @param line
     *            the line to log.
     */
    protected void processLine(final String line){
        lineOutputListener.processLine(line);
    }

    /**
     * 复制一个新的list对象
     * @return
     */
    public List<String> getLines() {
        return lines.stream().collect(Collectors.toList());
    }

    public LineOutputListener getLineOutputListener() {
        return lineOutputListener;
    }

    public void setLineOutputListener(LineOutputListener lineOutputListener) {
        this.lineOutputListener = lineOutputListener;
    }
}
