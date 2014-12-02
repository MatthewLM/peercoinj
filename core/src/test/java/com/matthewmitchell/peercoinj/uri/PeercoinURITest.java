/*
 * Copyright 2012, 2014 the original author or authors.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package com.matthewmitchell.peercoinj.uri;

import com.matthewmitchell.peercoinj.core.Address;
import com.matthewmitchell.peercoinj.params.MainNetParams;
import com.matthewmitchell.peercoinj.uri.PeercoinURI;
import com.google.common.collect.ImmutableList;
import org.junit.Test;

import java.io.UnsupportedEncodingException;

import static com.matthewmitchell.peercoinj.core.Coin.*;
import static org.junit.Assert.*;

public class PeercoinURITest {
    private PeercoinURI testObject = null;

    private static final String MAINNET_GOOD_ADDRESS = "PKf1PvTHnNncWTigggeXPnt5GH6LuDsnM4";
    private static final String MAINNET_BAD_ADDRESS = "mranY19RYUjgJjXY4BJNYp88WXXAg7Pr9T";

    @Test
    public void testConvertToPeercoinURI() throws Exception {
        Address goodAddress = new Address(MainNetParams.get(), MAINNET_GOOD_ADDRESS);
        
        // simple example
        assertEquals("ppcoin:" + MAINNET_GOOD_ADDRESS + "?amount=12.34&label=Hello&message=AMessage", PeercoinURI.convertToPeercoinURI(goodAddress, parseCoin("12.34"), "Hello", "AMessage"));
        
        // example with spaces, ampersand and plus
        assertEquals("ppcoin:" + MAINNET_GOOD_ADDRESS + "?amount=12.34&label=Hello%20World&message=Mess%20%26%20age%20%2B%20hope", PeercoinURI.convertToPeercoinURI(goodAddress, parseCoin("12.34"), "Hello World", "Mess & age + hope"));

        // no amount, label present, message present
        assertEquals("ppcoin:" + MAINNET_GOOD_ADDRESS + "?label=Hello&message=glory", PeercoinURI.convertToPeercoinURI(goodAddress, null, "Hello", "glory"));
        
        // amount present, no label, message present
        assertEquals("ppcoin:" + MAINNET_GOOD_ADDRESS + "?amount=0.1&message=glory", PeercoinURI.convertToPeercoinURI(goodAddress, parseCoin("0.1"), null, "glory"));
        assertEquals("ppcoin:" + MAINNET_GOOD_ADDRESS + "?amount=0.1&message=glory", PeercoinURI.convertToPeercoinURI(goodAddress, parseCoin("0.1"), "", "glory"));

        // amount present, label present, no message
        assertEquals("ppcoin:" + MAINNET_GOOD_ADDRESS + "?amount=12.34&label=Hello", PeercoinURI.convertToPeercoinURI(goodAddress, parseCoin("12.34"), "Hello", null));
        assertEquals("ppcoin:" + MAINNET_GOOD_ADDRESS + "?amount=12.34&label=Hello", PeercoinURI.convertToPeercoinURI(goodAddress, parseCoin("12.34"), "Hello", ""));
              
        // amount present, no label, no message
        assertEquals("ppcoin:" + MAINNET_GOOD_ADDRESS + "?amount=1000", PeercoinURI.convertToPeercoinURI(goodAddress, parseCoin("1000"), null, null));
        assertEquals("ppcoin:" + MAINNET_GOOD_ADDRESS + "?amount=1000", PeercoinURI.convertToPeercoinURI(goodAddress, parseCoin("1000"), "", ""));
        
        // no amount, label present, no message
        assertEquals("ppcoin:" + MAINNET_GOOD_ADDRESS + "?label=Hello", PeercoinURI.convertToPeercoinURI(goodAddress, null, "Hello", null));
        
        // no amount, no label, message present
        assertEquals("ppcoin:" + MAINNET_GOOD_ADDRESS + "?message=Agatha", PeercoinURI.convertToPeercoinURI(goodAddress, null, null, "Agatha"));
        assertEquals("ppcoin:" + MAINNET_GOOD_ADDRESS + "?message=Agatha", PeercoinURI.convertToPeercoinURI(goodAddress, null, "", "Agatha"));
      
        // no amount, no label, no message
        assertEquals("ppcoin:" + MAINNET_GOOD_ADDRESS, PeercoinURI.convertToPeercoinURI(goodAddress, null, null, null));
        assertEquals("ppcoin:" + MAINNET_GOOD_ADDRESS, PeercoinURI.convertToPeercoinURI(goodAddress, null, "", ""));
    }

    @Test
    public void testGood_Simple() throws PeercoinURIParseException {
        testObject = new PeercoinURI(MainNetParams.get(), PeercoinURI.PEERCOIN_SCHEME + ":" + MAINNET_GOOD_ADDRESS);
        assertNotNull(testObject);
        assertNull("Unexpected amount", testObject.getAmount());
        assertNull("Unexpected label", testObject.getLabel());
        assertEquals("Unexpected label", 20, testObject.getAddress().getHash160().length);
    }

    /**
     * Test a broken URI (bad scheme)
     */
    @Test
    public void testBad_Scheme() {
        try {
            testObject = new PeercoinURI(MainNetParams.get(), "blimpcoin:" + MAINNET_GOOD_ADDRESS);
            fail("Expecting PeercoinURIParseException");
        } catch (PeercoinURIParseException e) {
        }
    }

    /**
     * Test a broken URI (bad syntax)
     */
    @Test
    public void testBad_BadSyntax() {
        // Various illegal characters
        try {
            testObject = new PeercoinURI(MainNetParams.get(), PeercoinURI.PEERCOIN_SCHEME + "|" + MAINNET_GOOD_ADDRESS);
            fail("Expecting PeercoinURIParseException");
        } catch (PeercoinURIParseException e) {
            assertTrue(e.getMessage().contains("Bad URI syntax"));
        }

        try {
            testObject = new PeercoinURI(MainNetParams.get(), PeercoinURI.PEERCOIN_SCHEME + ":" + MAINNET_GOOD_ADDRESS + "\\");
            fail("Expecting PeercoinURIParseException");
        } catch (PeercoinURIParseException e) {
            assertTrue(e.getMessage().contains("Bad URI syntax"));
        }

        // Separator without field
        try {
            testObject = new PeercoinURI(MainNetParams.get(), PeercoinURI.PEERCOIN_SCHEME + ":");
            fail("Expecting PeercoinURIParseException");
        } catch (PeercoinURIParseException e) {
            assertTrue(e.getMessage().contains("Bad URI syntax"));
        }
    }

    /**
     * Test a broken URI (missing address)
     */
    @Test
    public void testBad_Address() {
        try {
            testObject = new PeercoinURI(MainNetParams.get(), PeercoinURI.PEERCOIN_SCHEME);
            fail("Expecting PeercoinURIParseException");
        } catch (PeercoinURIParseException e) {
        }
    }

    /**
     * Test a broken URI (bad address type)
     */
    @Test
    public void testBad_IncorrectAddressType() {
        try {
            testObject = new PeercoinURI(MainNetParams.get(), PeercoinURI.PEERCOIN_SCHEME + ":" + MAINNET_BAD_ADDRESS);
            fail("Expecting PeercoinURIParseException");
        } catch (PeercoinURIParseException e) {
            assertTrue(e.getMessage().contains("Bad address"));
        }
    }

    /**
     * Handles a simple amount
     * 
     * @throws PeercoinURIParseException
     *             If something goes wrong
     */
    @Test
    public void testGood_Amount() throws PeercoinURIParseException {
        // Test the decimal parsing
        testObject = new PeercoinURI(MainNetParams.get(), PeercoinURI.PEERCOIN_SCHEME + ":" + MAINNET_GOOD_ADDRESS
                + "?amount=6543210.123456");
        assertEquals("6543210123456", testObject.getAmount().toString());

        // Test the decimal parsing
        testObject = new PeercoinURI(MainNetParams.get(), PeercoinURI.PEERCOIN_SCHEME + ":" + MAINNET_GOOD_ADDRESS
                + "?amount=.123456");
        assertEquals("123456", testObject.getAmount().toString());

        // Test the integer parsing
        testObject = new PeercoinURI(MainNetParams.get(), PeercoinURI.PEERCOIN_SCHEME + ":" + MAINNET_GOOD_ADDRESS
                + "?amount=6543210");
        assertEquals("6543210000000", testObject.getAmount().toString());
    }

    /**
     * Handles a simple label
     * 
     * @throws PeercoinURIParseException
     *             If something goes wrong
     */
    @Test
    public void testGood_Label() throws PeercoinURIParseException {
        testObject = new PeercoinURI(MainNetParams.get(), PeercoinURI.PEERCOIN_SCHEME + ":" + MAINNET_GOOD_ADDRESS
                + "?label=Hello%20World");
        assertEquals("Hello World", testObject.getLabel());
    }

    /**
     * Handles a simple label with an embedded ampersand and plus
     * 
     * @throws PeercoinURIParseException
     *             If something goes wrong
     * @throws UnsupportedEncodingException 
     */
    @Test
    public void testGood_LabelWithAmpersandAndPlus() throws Exception {
        String testString = "Hello Earth & Mars + Venus";
        String encodedLabel = PeercoinURI.encodeURLString(testString);
        testObject = new PeercoinURI(MainNetParams.get(), PeercoinURI.PEERCOIN_SCHEME + ":" + MAINNET_GOOD_ADDRESS + "?label="
                + encodedLabel);
        assertEquals(testString, testObject.getLabel());
    }

    /**
     * Handles a Russian label (Unicode test)
     * 
     * @throws PeercoinURIParseException
     *             If something goes wrong
     * @throws UnsupportedEncodingException 
     */
    @Test
    public void testGood_LabelWithRussian() throws Exception {
        // Moscow in Russian in Cyrillic
        String moscowString = "\u041c\u043e\u0441\u043a\u0432\u0430";
        String encodedLabel = PeercoinURI.encodeURLString(moscowString); 
        testObject = new PeercoinURI(MainNetParams.get(), PeercoinURI.PEERCOIN_SCHEME + ":" + MAINNET_GOOD_ADDRESS + "?label="
                + encodedLabel);
        assertEquals(moscowString, testObject.getLabel());
    }

    /**
     * Handles a simple message
     * 
     * @throws PeercoinURIParseException
     *             If something goes wrong
     */
    @Test
    public void testGood_Message() throws PeercoinURIParseException {
        testObject = new PeercoinURI(MainNetParams.get(), PeercoinURI.PEERCOIN_SCHEME + ":" + MAINNET_GOOD_ADDRESS
                + "?message=Hello%20World");
        assertEquals("Hello World", testObject.getMessage());
    }

    /**
     * Handles various well-formed combinations
     * 
     * @throws PeercoinURIParseException
     *             If something goes wrong
     */
    @Test
    public void testGood_Combinations() throws PeercoinURIParseException {
        testObject = new PeercoinURI(MainNetParams.get(), PeercoinURI.PEERCOIN_SCHEME + ":" + MAINNET_GOOD_ADDRESS
                + "?amount=6543210&label=Hello%20World&message=Be%20well");
        assertEquals(
                "PeercoinURI['amount'='6543210000000','label'='Hello World','message'='Be well','address'='PKf1PvTHnNncWTigggeXPnt5GH6LuDsnM4']",
                testObject.toString());
    }

    /**
     * Handles a badly formatted amount field
     * 
     * @throws PeercoinURIParseException
     *             If something goes wrong
     */
    @Test
    public void testBad_Amount() throws PeercoinURIParseException {
        // Missing
        try {
            testObject = new PeercoinURI(MainNetParams.get(), PeercoinURI.PEERCOIN_SCHEME + ":" + MAINNET_GOOD_ADDRESS
                    + "?amount=");
            fail("Expecting PeercoinURIParseException");
        } catch (PeercoinURIParseException e) {
            assertTrue(e.getMessage().contains("amount"));
        }

        // Non-decimal (BIP 21)
        try {
            testObject = new PeercoinURI(MainNetParams.get(), PeercoinURI.PEERCOIN_SCHEME + ":" + MAINNET_GOOD_ADDRESS
                    + "?amount=12X4");
            fail("Expecting PeercoinURIParseException");
        } catch (PeercoinURIParseException e) {
            assertTrue(e.getMessage().contains("amount"));
        }
    }

    @Test
    public void testEmpty_Label() throws PeercoinURIParseException {
        assertNull(new PeercoinURI(MainNetParams.get(), PeercoinURI.PEERCOIN_SCHEME + ":" + MAINNET_GOOD_ADDRESS
                + "?label=").getLabel());
    }

    @Test
    public void testEmpty_Message() throws PeercoinURIParseException {
        assertNull(new PeercoinURI(MainNetParams.get(), PeercoinURI.PEERCOIN_SCHEME + ":" + MAINNET_GOOD_ADDRESS
                + "?message=").getMessage());
    }

    /**
     * Handles duplicated fields (sneaky address overwrite attack)
     * 
     * @throws PeercoinURIParseException
     *             If something goes wrong
     */
    @Test
    public void testBad_Duplicated() throws PeercoinURIParseException {
        try {
            testObject = new PeercoinURI(MainNetParams.get(), PeercoinURI.PEERCOIN_SCHEME + ":" + MAINNET_GOOD_ADDRESS
                    + "?address=aardvark");
            fail("Expecting PeercoinURIParseException");
        } catch (PeercoinURIParseException e) {
            assertTrue(e.getMessage().contains("address"));
        }
    }

    @Test
    public void testGood_ManyEquals() throws PeercoinURIParseException {
        assertEquals("aardvark=zebra", new PeercoinURI(MainNetParams.get(), PeercoinURI.PEERCOIN_SCHEME + ":"
                + MAINNET_GOOD_ADDRESS + "?label=aardvark=zebra").getLabel());
    }
    
    /**
     * Handles unknown fields (required and not required)
     * 
     * @throws PeercoinURIParseException
     *             If something goes wrong
     */
    @Test
    public void testUnknown() throws PeercoinURIParseException {
        // Unknown not required field
        testObject = new PeercoinURI(MainNetParams.get(), PeercoinURI.PEERCOIN_SCHEME + ":" + MAINNET_GOOD_ADDRESS
                + "?aardvark=true");
        assertEquals("PeercoinURI['aardvark'='true','address'='PKf1PvTHnNncWTigggeXPnt5GH6LuDsnM4']", testObject.toString());

        assertEquals("true", (String) testObject.getParameterByName("aardvark"));

        // Unknown not required field (isolated)
        try {
            testObject = new PeercoinURI(MainNetParams.get(), PeercoinURI.PEERCOIN_SCHEME + ":" + MAINNET_GOOD_ADDRESS
                    + "?aardvark");
            fail("Expecting PeercoinURIParseException");
        } catch (PeercoinURIParseException e) {
            assertTrue(e.getMessage().contains("no separator"));
        }

        // Unknown and required field
        try {
            testObject = new PeercoinURI(MainNetParams.get(), PeercoinURI.PEERCOIN_SCHEME + ":" + MAINNET_GOOD_ADDRESS
                    + "?req-aardvark=true");
            fail("Expecting PeercoinURIParseException");
        } catch (PeercoinURIParseException e) {
            assertTrue(e.getMessage().contains("req-aardvark"));
        }
    }

    @Test
    public void brokenURIs() throws PeercoinURIParseException {
        // Check we can parse the incorrectly formatted URIs produced by blockchain.info and its iPhone app.
        String str = "ppcoin://PKf1PvTHnNncWTigggeXPnt5GH6LuDsnM4?amount=0.01000000";
        PeercoinURI uri = new PeercoinURI(str);
        assertEquals("PKf1PvTHnNncWTigggeXPnt5GH6LuDsnM4", uri.getAddress().toString());
        assertEquals(CENT, uri.getAmount());
    }

    @Test(expected = PeercoinURIParseException.class)
    public void testBad_AmountTooPrecise() throws PeercoinURIParseException {
        new PeercoinURI(MainNetParams.get(), PeercoinURI.PEERCOIN_SCHEME + ":" + MAINNET_GOOD_ADDRESS
                + "?amount=0.123456789");
    }

    @Test(expected = PeercoinURIParseException.class)
    public void testBad_NegativeAmount() throws PeercoinURIParseException {
        new PeercoinURI(MainNetParams.get(), PeercoinURI.PEERCOIN_SCHEME + ":" + MAINNET_GOOD_ADDRESS
                + "?amount=-1");
    }

    @Test(expected = PeercoinURIParseException.class)
    public void testBad_TooLargeAmount() throws PeercoinURIParseException {
        new PeercoinURI(MainNetParams.get(), PeercoinURI.PEERCOIN_SCHEME + ":" + MAINNET_GOOD_ADDRESS
                + "?amount=2000000001");
    }

    @Test
    public void testPaymentProtocolReq() throws Exception {
        // Non-backwards compatible form ...
        PeercoinURI uri = new PeercoinURI(MainNetParams.get(), "ppcoin:?r=https%3A%2F%2Fppcoincore.org%2F%7Egavin%2Ff.php%3Fh%3Db0f02e7cea67f168e25ec9b9f9d584f9");
        assertEquals("https://ppcoincore.org/~gavin/f.php?h=b0f02e7cea67f168e25ec9b9f9d584f9", uri.getPaymentRequestUrl());
        assertEquals(ImmutableList.of("https://ppcoincore.org/~gavin/f.php?h=b0f02e7cea67f168e25ec9b9f9d584f9"),
                uri.getPaymentRequestUrls());
        assertNull(uri.getAddress());
    }

    @Test
    public void testMultiplePaymentProtocolReq() throws Exception {
        PeercoinURI uri = new PeercoinURI(MainNetParams.get(),
                "ppcoin:?r=https%3A%2F%2Fppcoincore.org%2F%7Egavin&r1=bt:112233445566");
        assertEquals(ImmutableList.of("bt:112233445566", "https://ppcoincore.org/~gavin"), uri.getPaymentRequestUrls());
        assertEquals("https://ppcoincore.org/~gavin", uri.getPaymentRequestUrl());
    }

    @Test
    public void testNoPaymentProtocolReq() throws Exception {
        PeercoinURI uri = new PeercoinURI(MainNetParams.get(), "ppcoin:" + MAINNET_GOOD_ADDRESS);
        assertNull(uri.getPaymentRequestUrl());
        assertEquals(ImmutableList.of(), uri.getPaymentRequestUrls());
        assertNotNull(uri.getAddress());
    }

    @Test
    public void testUnescapedPaymentProtocolReq() throws Exception {
        PeercoinURI uri = new PeercoinURI(MainNetParams.get(),
                "ppcoin:?r=https://merchant.com/pay.php?h%3D2a8628fc2fbe");
        assertEquals("https://merchant.com/pay.php?h=2a8628fc2fbe", uri.getPaymentRequestUrl());
        assertEquals(ImmutableList.of("https://merchant.com/pay.php?h=2a8628fc2fbe"), uri.getPaymentRequestUrls());
        assertNull(uri.getAddress());
    }
}

