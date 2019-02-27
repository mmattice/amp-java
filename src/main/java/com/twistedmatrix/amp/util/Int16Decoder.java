package com.twistedmatrix.amp.util;

public class Int16Decoder {
    private byte[] remainder;

    public byte[] getRemainder() {
        return remainder;
    }

    public void setBuffer(byte[] buffer) {
        this.remainder = buffer;
    }

    public Int16Decoder() {
        remainder = new byte[0];
    }

    public Int16Decoder(byte[] buffer) {
        remainder = new byte[buffer.length];
        System.arraycopy(buffer, 0, remainder, 0, buffer.length);
    }

    public boolean extractable() {
        if (remainder.length < 2) {
            return false;
        }

        /* unpack the 16-bit length */
        int remainderLength = getRemainderLength();

        if (remainder.length < (2 + remainderLength)) {
            return false;
        }

        return true;
    }

    public int getRemainderLength() {
        if (remainder.length < 2) {
            return 0;
        }
        return toInt(remainder[0], remainder[1]);
    }

    static void cpy(byte[] a, byte[] b, int offt) {
        System.arraycopy(a, 0, b, offt, a.length);
    }

    static void cpy(byte[] a, byte[] b) {
        cpy(a, b, 0);
    }

    public void addData(byte[] data) {
        byte[] old = remainder;
        remainder = new byte[old.length + data.length];

        cpy(old, remainder);
        cpy(data, remainder, old.length);
    }

    public byte[] extractHunk() {
        int reqlen = getRemainderLength();
        byte[] hunk = new byte[reqlen];
        System.arraycopy(remainder, 2, hunk, 0, reqlen);
        byte[] oldbuf = remainder;
        int newlen = remainder.length - reqlen - 2;
        remainder = new byte[newlen];
        System.arraycopy(oldbuf, reqlen + 2, remainder, 0, newlen);
        return hunk;
    }

    public static int toInt(byte b) {
        // why doesn't java have unsigned bytes again?
        int i;
        if (b < 0) {
            i = 256 + (int) b;
        } else {
            i = (int) b;
        }
        return i;
    }

    private static int toInt(byte a, byte b) {
        // why doesn't java have unsigned bytes again?
        int i;
        return toInt(a) * 256 + toInt(b);
    }

}
