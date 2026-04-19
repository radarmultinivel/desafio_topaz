/*
 * Desafio Tecnico Topaz
 * Testes Unitarios - Base62
 */
package com.topaz.urlshortener.util;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import java.util.HashSet;
import java.util.Set;

public class Base62Test {

    @Test
    public void testEncode() {
        assertEquals("0", Base62.encode(0));
        assertEquals("1", Base62.encode(1));
        assertEquals("A", Base62.encode(10));
        assertEquals("a", Base62.encode(36));
        assertEquals("10", Base62.encode(62));
    }

    @Test
    public void testDecode() {
        assertEquals(0, Base62.decode("0"));
        assertEquals(1, Base62.decode("1"));
        assertEquals(10, Base62.decode("A"));
        assertEquals(36, Base62.decode("a"));
        assertEquals(62, Base62.decode("10"));
    }

    @Test
    public void testEncodeDecode() {
        for (long i = 0; i < 1000; i++) {
            assertEquals(i, Base62.decode(Base62.encode(i)));
        }
    }

    @Test
    public void testUniqueCodes() {
        Set<String> codes = new HashSet<>();
        for (long i = 0; i < 10000; i++) {
            codes.add(Base62.encode(i));
        }
        assertEquals(10000, codes.size());
    }
}