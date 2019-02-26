package com.twistedmatrix.amp;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

import com.twistedmatrix.internet.ITransport;
import com.twistedmatrix.internet.Deferred;

import org.junit.Test;

import static org.junit.Assert.*;

public class TestAMP {
    AMPBox received;

    @Test
    public void testParsing() throws Throwable {
        AMPParser ap = new AMPParser() {
            public void ampBoxReceived(AMPBox hm) {
                received = hm;
            }
        };

        byte[] data =
                "\u0000\u0005hello\u0000\u0005world\u0000\u0000"
                        .getBytes("ISO-8859-1");

        ap.dataReceived(data);
        assertEquals(AMPBox.asString(received.get("hello")), "world");
    }

    @Test
    public void testParseData() throws Throwable {
        assertEquals(
                AMPBox.asString(
                        AMPParser.parseData(
                                "\u0000\u0005hello\u0000\u0005world\u0000\u0000"
                                        .getBytes("ISO-8859-1")).get(0).get("hello")
                ),
                "world");
    }

    @Test
    public void testByteIntegerConversion() {
        for (int i = 0; i < 256; i++) {
            assertEquals(Int16StringReceiver.toInt((byte) i),
                    i);
        }
    }

    @Test
    public void testParseLongString() throws Throwable {
        String veryLong = (
                "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx" +
                        "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx" +
                        "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx" +
                        "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");

        String toParse = ("\u0000\u00ff" + veryLong + "\u0000\u00ff" + veryLong + "\u0000\u0000");
        byte[] dataToParse = toParse.getBytes("ISO-8859-1");

        List<AMPBox> alhm = AMPParser.parseData(dataToParse);
        assertEquals(
                AMPBox.asString(alhm.get(0).get(veryLong)),
                veryLong);
        assertEquals(alhm.size(), 1);
    }

    @Test
    public void testParseEvenLongerString() throws Throwable {
        String veryLong = (
                "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx" +
                        "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx" +
                        "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx" +
                        "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");

        String toParse = ("\u0001\u0004" + veryLong + "\u0001\u0004" + veryLong + "\u0000\u0000");
        byte[] dataToParse = toParse.getBytes("ISO-8859-1");

        List<AMPBox> alhm = AMPParser.parseData(dataToParse);
        assertEquals(
                AMPBox.asString(alhm.get(0).get(veryLong)),
                veryLong);
        assertEquals(alhm.size(), 1);
    }

    @Test
    public void testParseDoubleInAmpList() throws Throwable {
        String toParse = (
                "\u0000\u0007numeric" +
                "\u0000\u0008123456.0" +

                "\u0000\u0003NaN" +
                "\u0000\u0003nan" +

                "\u0000\u0006posinf" +
                "\u0000\u0003Inf" +

                "\u0000\u0006neginf" +
                "\u0000\u0004-Inf" +

                "\u0000\u0000");
        byte[] dataToParse = toParse.getBytes("ISO-8859-1");

        List<AMPBox> alhm = AMPParser.parseData(dataToParse);
        assertEquals(123456.0, alhm.get(0).getAndDecode("numeric", Double.class));
        assertEquals(Double.NaN, alhm.get(0).getAndDecode("NaN", Double.class));
        assertEquals(Double.POSITIVE_INFINITY, alhm.get(0).getAndDecode("posinf", Double.class));
        assertEquals(Double.NEGATIVE_INFINITY, alhm.get(0).getAndDecode("neginf", Double.class));
    }

    public class ListAttribute {
        public List<Double> a;

        @Override
        public boolean equals(Object o) {
            if (o instanceof ListAttribute) {
                ListAttribute other = (ListAttribute) o;
                return ((other.a == this.a));
            }
            return false;
        }
    }

    @Test
    public void testParseDoubleInListOf() throws Throwable {
        AMPBox ab = new AMPBox();
        ListAttribute la = new ListAttribute();

        String toParse = (
                "\u0000\u0008123456.0" +
                "\u0000\u0003nan" +
                "\u0000\u0003Inf" +
                "\u0000\u0004-Inf"
                );
        ab.put("a", toParse);
        ab.fillOut(la);

        assertEquals(123456.0, (double) la.a.get(0), 0.001);
        assertEquals(Double.NaN, (double) la.a.get(1), 0);
        assertEquals(Double.POSITIVE_INFINITY, (double) la.a.get(2), 0);
        assertEquals(Double.NEGATIVE_INFINITY, (double) la.a.get(3), 0);

    }

    public class SomeAttributes {
        public int a;
        public String b;
        public boolean c;
        public byte[] d;
        // float?

        @Override
        public boolean equals(Object o) {
            if (o instanceof SomeAttributes) {
                SomeAttributes other = (SomeAttributes) o;
                return ((other.a == this.a) &&
                        (other.b.equals(this.b)) &&
                        (other.c == this.c) &&
                        (Arrays.equals(other.d, this.d)));
            }
            return false;
        }
    }

    @Test
    public void testFillingOutStruct() {
        AMPBox ab = new AMPBox();
        SomeAttributes sa = new SomeAttributes();

        ab.put("a", "1");
        ab.put("b", "hello world");
        ab.put("c", "True");
        ab.put("d", "bytes");

        ab.fillOut(sa);
        assertEquals(sa.a, 1);
        assertEquals(sa.b, "hello world");
        assertEquals(sa.c, true);
        assertEquals(AMPBox.asString(sa.d), "bytes");
    }

    @Test
    public void testFillingOutRoundTrip() {
        AMPBox ab = new AMPBox();
        SomeAttributes sa = new SomeAttributes();
        SomeAttributes sb = new SomeAttributes();
        sa.a = 7;
        sa.b = "more stufp";
        sa.c = true;
        sa.d = new byte[]{1, 2, 3, 4, 5, 6, 7, 8};
        assertNotEquals(sa, sb);

        ab.extractFrom(sa);
        ab.fillOut(sb);
        assertEquals(sa, sb);
    }

    @Test
    public void testEncodeDecodeRoundTrip() {
        AMPBox ab = new AMPBox();
        AMPBox ab2;
        String veryLong = (
                "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx" +
                        "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx" +
                        "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx" +
                        "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
        ab.put("a", "1");
        ab.put("asdfasdfasdfasdfasdfasdf", "1");
        ab.put("ninjas", "ashdufa879ghawghawunfauwdvn");
        ab.put(veryLong, "haha");
        ab.put("asdfqwer", veryLong);
        ab2 = AMPParser.parseData(ab.encode()).get(0);
        assertEquals(ab, ab2);
        ab2 = AMPParser.parseData(ab2.encode()).get(0);
        assertEquals(ab, ab2);
    }

    private int rancommandcount = 0;

    @Test
    public void testCommandDispatch() {
        AMP a = new AMP() {
            public void thingy() {
                rancommandcount++;
            }
        };
        AMPBox ab = new AMPBox();
        ab.put("_command", "ninjas");
        ab.put("_ask", "ninjas");
        a.localCommand("ninjas", new LocalCommand("thingy", new String[]{}));
        a.ampBoxReceived(ab);
        assertEquals(1, rancommandcount);
    }

    class FakeTransport implements ITransport {
        ArrayList<byte[]> alb;

        public FakeTransport() {
            alb = new ArrayList<byte[]>();
        }

        public void write(byte[] data) {
            alb.add(data);
        }

        public void connectionLost(Throwable reason) {
        }

        public void loseConnection(Throwable reason) {
        }
    }


    /**
     * Verify that AMP commands which return a Deferred are answered
     * only when the Deferred fires.
     */
    @Test
    public void testCommandDeferredReturn() {

        class DeferredReturner extends AMP {
            public Deferred d;

            public Deferred thingy() {
                d = new Deferred();
                return d;
            }
        }

        FakeTransport ft = new FakeTransport();
        DeferredReturner dr = new DeferredReturner();
        dr.localCommand("ninjas",
                new LocalCommand("thingy", new String[]{}));
        dr.makeConnection(ft);

        AMPBox ab = new AMPBox();
        ab.put("_command", "ninjas");
        ab.put("_ask", "> pirates");
        dr.ampBoxReceived(ab);
        assertEquals(0, ft.alb.size());

        dr.d.callback(new Object());
        assertEquals(1, ft.alb.size());
        List<AMPBox> lab = AMPParser.parseData(ft.alb.get(0));
        assertEquals(1, lab.size());
        assertEquals("> pirates", new String(lab.get(0).get("_answer")));
    }

    /**
     * Verify that AMP commands that return a synchronous value
     * do not get Deferred.
     */
    @Test
    public void testSynchronousValue() {

        class SynchronousValue {
            // Make some default values
            public String strValue = "samurai";
            public int intValue = 0;
        }

        class SynchronousReturner extends AMP {
            public SynchronousValue returnSynch() {
                SynchronousValue thisSV = new SynchronousValue();
                // Change those values arbitrarily
                thisSV.strValue = "ninjas";
                thisSV.intValue = 1;
                return thisSV;
            }
        }

        FakeTransport ft = new FakeTransport();

        SynchronousReturner sr = new SynchronousReturner();
        sr.localCommand("returnSynch",
                new LocalCommand("returnSynch", new String[]{}));
        sr.makeConnection(ft);

        assertEquals(0, ft.alb.size());

        AMPBox ab = new AMPBox();
        ab.put("_command", "returnSynch");
        ab.put("_ask", "> pirates");
        sr.ampBoxReceived(ab);

        assertEquals(1, ft.alb.size());
        List<AMPBox> lab = AMPParser.parseData(ft.alb.get(0));
        assertEquals(1, lab.size());
        assertEquals("> pirates", new String(lab.get(0).get("_answer")));
        assertEquals("ninjas", new String(lab.get(0).get("strValue")));
        assertEquals("1", new String(lab.get(0).get("intValue")));
    }

    private ArrayList<Integer> ali;

    public class WhatTheHell extends AMP {
        public class Command extends LocalCommand {
            public int java;
            public int doesnt;
            public int know;
            public int argnames;

            public Command() {
                super("whatthe", new String[]{"java", "doesnt", "know", "argnames"});
            }
        }

        public WhatTheHell() {
            localCommand("addstuff", new Command());
        }

        public void whatthe(int java, int doesnt, int know, int argnames) {
            rancommandcount++;
            ali.add(java);
            ali.add(doesnt);
            ali.add(know);
            ali.add(argnames);
        }
    }

    @Test
    public void testCommandArgumentParsing() {
        this.ali = new ArrayList<Integer>();
        AMP a = new WhatTheHell();

        AMPBox ab = new AMPBox();
        ab.put("_command", "addstuff");
        ab.put("_ask", "0x847");
        ab.putAndEncode("java", new Integer(1234));
        ab.putAndEncode("doesnt", new Integer(5678));
        ab.putAndEncode("know", new Integer(9101112));
        ab.putAndEncode("argnames", new Integer(13141516));
        a.ampBoxReceived(ab);
        assertEquals(1, rancommandcount);
        ArrayList<Integer> alicheck = new ArrayList<Integer>();
        alicheck.add(1234);
        alicheck.add(5678);
        alicheck.add(9101112);
        alicheck.add(13141516);
        assertEquals(ali, alicheck);
    }
}
