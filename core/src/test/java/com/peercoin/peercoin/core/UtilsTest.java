/**
 * Copyright 2011 Thilo Planz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.matthewmitchell.peercoinj.core;

import org.junit.Assert;
import org.junit.Test;

import java.math.BigInteger;

import static com.matthewmitchell.peercoinj.core.Utils.*;
import static org.junit.Assert.*;

public class UtilsTest {

    @Test
    public void testToNanoCoins() {
        // String version
        assertEquals(CENT, toNanoCoins("0.01"));
        assertEquals(CENT, toNanoCoins("1E-2"));
        assertEquals(COIN.add(Utils.CENT), toNanoCoins("1.01"));
        try {
            toNanoCoins("2E-20");
            org.junit.Assert.fail("should not have accepted fractional nanocoins");
        } catch (ArithmeticException e) {
        }

        // int version
        assertEquals(CENT, toNanoCoins(0, 1));

        try {
            toNanoCoins(1, -1);
            fail();
        } catch (IllegalArgumentException e) {}
        try {
            toNanoCoins(-1, 0);
            fail();
        } catch (IllegalArgumentException e) {}
        try {
            toNanoCoins("-1");
            fail();
        } catch (ArithmeticException e) {}
    }

    @Test
    public void testFormatting() {
        assertEquals("1.00", peercoinValueToFriendlyString(toNanoCoins(1, 0)));
        assertEquals("1.23", peercoinValueToFriendlyString(toNanoCoins(1, 23)));
        assertEquals("0.001", peercoinValueToFriendlyString(BigInteger.valueOf(COIN.longValue() / 1000)));
        assertEquals("-1.23", peercoinValueToFriendlyString(toNanoCoins(1, 23).negate()));
    }
    
    /**
     * Test the peercoinValueToPlainString amount formatter
     */
    @Test
    public void testPeercoinValueToPlainString() {
        // null argument check
        try {
            peercoinValueToPlainString(null);
            org.junit.Assert.fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Value cannot be null"));
        }

        assertEquals("0.0015", peercoinValueToPlainString(BigInteger.valueOf(150000)));
        assertEquals("1.23", peercoinValueToPlainString(toNanoCoins("1.23")));

        assertEquals("0.1", peercoinValueToPlainString(toNanoCoins("0.1")));
        assertEquals("1.1", peercoinValueToPlainString(toNanoCoins("1.1")));
        assertEquals("21.12", peercoinValueToPlainString(toNanoCoins("21.12")));
        assertEquals("321.123", peercoinValueToPlainString(toNanoCoins("321.123")));
        assertEquals("4321.1234", peercoinValueToPlainString(toNanoCoins("4321.1234")));
        assertEquals("54321.12345", peercoinValueToPlainString(toNanoCoins("54321.12345")));
        assertEquals("654321.123456", peercoinValueToPlainString(toNanoCoins("654321.123456")));
        assertEquals("7654321.1234567", peercoinValueToPlainString(toNanoCoins("7654321.1234567")));
        try {
            assertEquals("87654321.12345678", peercoinValueToPlainString(toNanoCoins("87654321.12345678")));
            Assert.fail();  // More than MAX_MONEY
        } catch (Exception e) {}

        // check there are no trailing zeros
        assertEquals("1", peercoinValueToPlainString(toNanoCoins("1.0")));
        assertEquals("2", peercoinValueToPlainString(toNanoCoins("2.00")));
        assertEquals("3", peercoinValueToPlainString(toNanoCoins("3.000")));
        assertEquals("4", peercoinValueToPlainString(toNanoCoins("4.0000")));
        assertEquals("5", peercoinValueToPlainString(toNanoCoins("5.00000")));
        assertEquals("6", peercoinValueToPlainString(toNanoCoins("6.000000")));
        assertEquals("7", peercoinValueToPlainString(toNanoCoins("7.0000000")));
        assertEquals("8", peercoinValueToPlainString(toNanoCoins("8.00000000")));
    }    
    
    @Test
    public void testReverseBytes() {
        assertArrayEquals(new byte[]{1, 2, 3, 4, 5}, Utils.reverseBytes(new byte[]{5, 4, 3, 2, 1}));
    }

    @Test
    public void testReverseDwordBytes() {
        assertArrayEquals(new byte[]{1, 2, 3, 4, 5, 6, 7, 8}, Utils.reverseDwordBytes(new byte[]{4, 3, 2, 1, 8, 7, 6, 5}, -1));
        assertArrayEquals(new byte[]{1, 2, 3, 4}, Utils.reverseDwordBytes(new byte[]{4, 3, 2, 1, 8, 7, 6, 5}, 4));
        assertArrayEquals(new byte[0], Utils.reverseDwordBytes(new byte[]{4, 3, 2, 1, 8, 7, 6, 5}, 0));
        assertArrayEquals(new byte[0], Utils.reverseDwordBytes(new byte[0], 0));
    }

    @Test
    public void testMaxOfMostFreq() throws Exception {
        assertEquals(0, Utils.maxOfMostFreq());
        assertEquals(0, Utils.maxOfMostFreq(0, 0, 1));
        assertEquals(2, Utils.maxOfMostFreq(1, 1, 2, 2));
        assertEquals(1, Utils.maxOfMostFreq(1, 1, 2, 2, 1));
        assertEquals(-1, Utils.maxOfMostFreq(-1, -1, 2, 2, -1));
    }

    @Test
    public void compactEncoding() throws Exception {
        assertEquals(new BigInteger("1234560000", 16), Utils.decodeCompactBits(0x05123456L));
        assertEquals(new BigInteger("c0de000000", 16), Utils.decodeCompactBits(0x0600c0de));
        assertEquals(0x05123456L, Utils.encodeCompactBits(new BigInteger("1234560000", 16)));
        assertEquals(0x0600c0deL, Utils.encodeCompactBits(new BigInteger("c0de000000", 16)));
    }
}
