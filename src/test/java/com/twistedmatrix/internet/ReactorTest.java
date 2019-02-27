package com.twistedmatrix.internet;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;

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
    public void callLater() throws Throwable {
        AtomicBoolean ranLater = new AtomicBoolean(false);

        reactor.callLater(0.01, () -> {
            ranLater.set(true);
            reactor.stop();
        });

        reactor.run();

        assertTrue(ranLater.get());
    }

}