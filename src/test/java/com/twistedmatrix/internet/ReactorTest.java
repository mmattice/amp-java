package com.twistedmatrix.internet;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ReactorTest {
    private Reactor reactor;

    @Before
    public void setUp(){
        reactor = Reactor.get();
    }

    @After
    public void tearDown(){
        reactor.stop();
        reactor = null;
    }

    @Test
    public void callLaterOneLater() throws Throwable {
        AtomicBoolean ranLater = new AtomicBoolean(false);

        reactor.callLater(0.01, () -> {
            ranLater.set(true);
            reactor.stop();
        });

        reactor.run();

        assertTrue(ranLater.get());
    }

    @Test
    public void callLaterSeveralLater() throws Throwable {
        AtomicInteger i = new AtomicInteger(0);

        reactor.callLater(0.05, () -> {
            int j = i.getAndAdd(1);
            assertEquals(j, 5);
        });

        reactor.callLater(0.01, () -> {
            int j = i.getAndSet(5);
            assertEquals(j, 0);
        });

        reactor.callLater(0.1, () -> { reactor.stop();});

        reactor.run();

        assertEquals(i.get(), 6);
    }
}