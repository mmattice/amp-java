package com.twistedmatrix.amp;

import com.twistedmatrix.amp.util.Int16Decoder;
import com.twistedmatrix.internet.Protocol;

/**
 * This class buffers incoming data and does some initial processing.
 */
public abstract class Int16StringReceiver extends Protocol {

    Int16Decoder buffs;

    public Int16StringReceiver() {
        buffs = new Int16Decoder();
    }

    /**
     * Deliver the data.
     */
    public abstract void stringReceived(byte[] hunk);

    /**
     * Handle incoming data.
     */
    public void dataReceived(byte[] data) {
        buffs.addData(data);
        while (tryToDeliverData()) {
            /* nothing to do */
        }
    }

    /**
     * Convert a byte to an unsigned integer.
     */
    public static int toInt(byte b) {
        return Int16Decoder.toInt(b);
    }

    /**
     * Attempt to drain some data from our buffer into somewhere else.
     */
    private boolean tryToDeliverData() {

        if (buffs.extractable()) {

            byte[] hunk = buffs.extractHunk();
            try {
                stringReceived(hunk);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        } else {
            return false;
        }
    }

}
