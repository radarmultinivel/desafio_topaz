/*
Desafio Tecnico Topaz
Desenvolvido por L.A.Leandro
São José dos Campos - SP
18-04-2026
*/
package com.topaz.urlshortener.util;

public class Base62 {
    
    private static final String BASE62_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final int BASE = BASE62_CHARS.length();
    
    private Base62() {
    }
    
    public static String encode(long number) {
        if (number == 0) {
            return String.valueOf(BASE62_CHARS.charAt(0));
        }
        
        StringBuilder sb = new StringBuilder();
        while (number > 0) {
            int remainder = (int) (number % BASE);
            sb.append(BASE62_CHARS.charAt(remainder));
            number = number / BASE;
        }
        
        return sb.reverse().toString();
    }
    
    public static long decode(String str) {
        long result = 0;
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            int power = str.length() - (i + 1);
            int index = BASE62_CHARS.indexOf(c);
            if (index == -1) {
                throw new IllegalArgumentException("Caractere invalido '" + c + "' na string Base62");
            }
            result += index * Math.pow(BASE, power);
        }
        return result;
    }
    
    public static String generateRandomCode(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("O comprimento deve ser positivo");
        }
        
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int index = (int) (Math.random() * BASE);
            sb.append(BASE62_CHARS.charAt(index));
        }
        
        return sb.toString();
    }
    
    public static boolean isValidBase62(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (BASE62_CHARS.indexOf(c) == -1) {
                return false;
            }
        }
        
        return true;
    }
    
    public static int getLength(long number) {
        if (number == 0) {
            return 1;
        }
        
        int length = 0;
        while (number > 0) {
            number = number / BASE;
            length++;
        }
        return length;
    }
}