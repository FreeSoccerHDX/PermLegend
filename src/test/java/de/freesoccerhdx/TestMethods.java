package de.freesoccerhdx;



import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;

import de.freesoccerhdx.lib.Methods;

public class TestMethods {

    @Test
    public void testCalculateTimeFromInput() {
        assertEquals(Methods.calculateTimeFromInput("10s"), 10);
        assertEquals(Methods.calculateTimeFromInput("33"), 33);
        assertEquals(Methods.calculateTimeFromInput("3"), 3);
        assertEquals(Methods.calculateTimeFromInput("0"), 0);
        assertEquals(Methods.calculateTimeFromInput("1h"), 1*60*60);
        assertEquals(Methods.calculateTimeFromInput("1d 10s"), 1*60*60*24 + 10);
        assertEquals(Methods.calculateTimeFromInput("1d 7h 10m 30s"), 30+(10+(7+1*24)*60)*60);
    }

    @Test
    public void testSecondsToCountdown() {
        assertEquals(Methods.secondsToCountdown(0), "00s");
        assertEquals(Methods.secondsToCountdown(9), "09s");
        assertEquals(Methods.secondsToCountdown(15), "15s");
        assertEquals(Methods.secondsToCountdown(1*60*60), "01h 00m 00s");
        assertEquals(Methods.secondsToCountdown(1*60*60 + 60), "01h 01m 00s");
        assertEquals(Methods.secondsToCountdown(1*60*60 + 60 + 1), "01h 01m 01s");
        assertEquals(Methods.secondsToCountdown(24*1*60*60 + 1*60*60), "1d 01h 00m 00s");
    }

    @Test
    public void testReplaceColorCodes() {
        assertEquals(Methods.replaceColorCodes("&4Hallo"), "§4Hallo");
        assertEquals(Methods.replaceColorCodes("&4&10&22Hallo"), "§4§10§22Hallo");
        assertEquals(Methods.replaceColorCodes("&lHallo"), "§lHallo");
        assertNotEquals(Methods.replaceColorCodes("&iHallo"), "§iHallo");
    }

}
