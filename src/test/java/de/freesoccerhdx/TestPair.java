package de.freesoccerhdx;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;

import de.freesoccerhdx.lib.Pair;

public class TestPair {

    @Test
    public void testPair() {
        Pair<String, String> pair = new Pair<>("a1", "b2");
        assertEquals(pair.getLeft(), pair.getValue1());
        assertEquals(pair.getRight(), pair.getValue2());
        assertNotEquals(pair.getLeft(), pair.getRight());
        assertNotEquals(pair.getValue1(), pair.getValue2());

        assertEquals(pair.getLeft(), "a1");
        assertEquals(pair.getRight(), "b2");
    }
    
}
