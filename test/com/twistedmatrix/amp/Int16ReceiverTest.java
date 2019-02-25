package com.twistedmatrix.amp;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class Int16ReceiverTest {
    byte[] parsedString = null;

    @Test
    public void testParsing() throws Throwable {
        Int16StringReceiver ir = new Int16StringReceiver() {
            public void stringReceived(byte[] hunk) {
                parsedString = hunk;
            }
        };
        String hw = "hello world";
        byte[] hwbytes = hw.getBytes("utf-8");
        byte[] lengthprefix = {0, (byte) hwbytes.length};
        byte[] trailingGarbage = {(byte) 251};
        ir.dataReceived(lengthprefix);
        ir.dataReceived(hwbytes);
        ir.dataReceived(trailingGarbage);
        assertEquals(new String(parsedString, "ISO-8859-1"),
                new String(hwbytes, "ISO-8859-1"));
    }
}
