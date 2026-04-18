/*
Desafio Tecnico Topaz
Desenvolvido por L.A.Leandro
São José dos Campos - SP
18-04-2026
*/
package com.topaz.urlshortener.util;

import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;

public class Base62Test {
    
    @Test
    public void testEncodeZero() {
        assertEquals("0", Base62.encode(0));
    }
    
    @Test
    public void testEncodeSmallNumbers() {
        assertEquals("1", Base62.encode(1));
        assertEquals("9", Base62.encode(9));
        assertEquals("A", Base62.encode(10));
        assertEquals("Z", Base62.encode(35));
        assertEquals("a", Base62.encode(36));
        assertEquals("z", Base62.encode(61));
    }
    
    @Test
    public void testEncodeLargeNumbers() {
        assertEquals("10", Base62.encode(62));
        assertEquals("11", Base62.encode(63));
        assertEquals("1A", Base62.encode(72));
        assertEquals("20", Base62.encode(124));
        assertEquals("100", Base62.encode(3844));
    }

    @Test
    public void testDecode() {
        assertEquals(0, Base62.decode("0"));
        assertEquals(1, Base62.decode("1"));
        assertEquals(10, Base62.decode("A"));
        assertEquals(35, Base62.decode("Z"));
        assertEquals(36, Base62.decode("a"));
        assertEquals(61, Base62.decode("z"));
        assertEquals(62, Base62.decode("10"));
        assertEquals(124, Base62.decode("20"));
        assertEquals(3844, Base62.decode("100"));
    }
    
    @Test
    public void testEncodeDecodeRoundtrip() {
        long[] testNumbers = {0, 1, 10, 61, 62, 1234, 99999, 123456789L};
        
        for (long number : testNumbers) {
            String encoded = Base62.encode(number);
            long decoded = Base62.decode(encoded);
            assertEquals(number, decoded);
        }
    }
    
    @Test
    public void testGenerateRandomCode() {
        int[] lengths = {1, 5, 10, 20};
        
        for (int length : lengths) {
            String code = Base62.generateRandomCode(length);
            assertEquals(length, code.length());
            
            for (int i = 0; i < code.length(); i++) {
                char c = code.charAt(i);
                assertTrue("Caractere '" + c + "' nao e valido Base62", 
                          Base62.isValidBase62(String.valueOf(c)));
            }
        }
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testGenerateRandomCodeWithInvalidLength() {
        Base62.generateRandomCode(0);
    }
    
    @Test
    public void testIsValidBase62() {
        assertTrue(Base62.isValidBase62("0"));
        assertTrue(Base62.isValidBase62("12345"));
        assertTrue(Base62.isValidBase62("ABCDEF"));
        assertTrue(Base62.isValidBase62("abcdef"));
        assertTrue(Base62.isValidBase62("1aB2c"));
        assertTrue(Base62.isValidBase62("10"));
        assertTrue(Base62.isValidBase62("Zz"));
        
        assertFalse(Base62.isValidBase62(""));
        assertFalse(Base62.isValidBase62(null));
        assertFalse(Base62.isValidBase62("123!"));
        assertFalse(Base62.isValidBase62("ABC@"));
        assertFalse(Base62.isValidBase62("abc#"));
        assertFalse(Base62.isValidBase62("1 2"));
        assertFalse(Base62.isValidBase62("-123"));
    }
    
    @Test
    public void testGetLength() {
        assertEquals(1, Base62.getLength(0));
        assertEquals(1, Base62.getLength(9));
        assertEquals(1, Base62.getLength(61));
        assertEquals(2, Base62.getLength(62));
        assertEquals(2, Base62.getLength(123));
        assertEquals(3, Base62.getLength(3844));
        assertEquals(4, Base62.getLength(238328));
    }
}