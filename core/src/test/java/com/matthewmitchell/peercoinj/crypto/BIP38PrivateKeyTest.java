/*
 * Copyright 2014 Andreas Schildbach
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

package com.matthewmitchell.peercoinj.crypto;

import com.matthewmitchell.peercoinj.core.ECKey;
import com.matthewmitchell.peercoinj.crypto.BIP38PrivateKey.BadPassphraseException;
import com.matthewmitchell.peercoinj.params.MainNetParams;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import static org.junit.Assert.assertEquals;

public class BIP38PrivateKeyTest {

    private static final MainNetParams MAINNET = MainNetParams.get();

    @Test
    public void bip38testvector_test1() throws Exception {
        BIP38PrivateKey encryptedKey = new BIP38PrivateKey(MAINNET,
                "6PfRBdq72gua9HRkZS1X8DJLd8vRi7hbNjUkiauLkThWvd52eMyGmeS7vc");
        ECKey key = encryptedKey.decrypt("TestingOneTwoThree");
        assertEquals("79mY6oZViLXkGjgD2BDsdP6WVzqEJU9aAhDevZzRFZNJ4My5qcB", key.getPrivateKeyEncoded(MAINNET)
                .toString());
    }

    @Test
    public void bip38testvector_test2() throws Exception {
        BIP38PrivateKey encryptedKey = new BIP38PrivateKey(MAINNET,
                "6PfSCRxC8cvNk83vJV7FDSS3HWcyez2bVzc4wqTFFHGMwnaowAjQrDCeA3");
        ECKey key = encryptedKey.decrypt("Satoshi");
        assertEquals("7AKD3wi1cMKpha7UGGT1LroBZhw55QciLedyqqjUrHhEyfaAqiK", key.getPrivateKeyEncoded(MAINNET)
                .toString());
    }

    @Test(expected = BadPassphraseException.class)
    public void badPassphrase() throws Exception {
        BIP38PrivateKey encryptedKey = new BIP38PrivateKey(MAINNET,
                "6PfRBdq72gua9HRkZS1X8DJLd8vRi7hbNjUkiauLkThWvd52eMyGmeS7vc");
        encryptedKey.decrypt("BAD");
    }

}

