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
    public void bip38testvector_noCompression_noEcMultiply_test1() throws Exception {
        BIP38PrivateKey encryptedKey = new BIP38PrivateKey(MAINNET,
                "6PRVWUbkzzsbcVac2qwfssoUJAN1Xhrg6bNk8J7Nzm5H7kxEbn2Nh2ZoGg");
        ECKey key = encryptedKey.decrypt("TestingOneTwoThree");
        assertEquals("5KN7MzqK5wt2TP1fQCYyHBtDrXdJuXbUzm4A9rKAteGu3Qi5CVR", key.getPrivateKeyEncoded(MAINNET)
                .toString());
    }

    @Test
    public void bip38testvector_noCompression_noEcMultiply_test2() throws Exception {
        BIP38PrivateKey encryptedKey = new BIP38PrivateKey(MAINNET,
                "6PRNFFkZc2NZ6dJqFfhRoFNMR9Lnyj7dYGrzdgXXVMXcxoKTePPX1dWByq");
        ECKey key = encryptedKey.decrypt("Satoshi");
        assertEquals("5HtasZ6ofTHP6HCwTqTkLDuLQisYPah7aUnSKfC7h4hMUVw2gi5", key.getPrivateKeyEncoded(MAINNET)
                .toString());
    }

    @Test
    public void bip38testvector_noCompression_noEcMultiply_test3() throws Exception {
        BIP38PrivateKey encryptedKey = new BIP38PrivateKey(MAINNET,
                "6PRW5o9FLp4gJDDVqJQKJFTpMvdsSGJxMYHtHaQBF3ooa8mwD69bapcDQn");
        StringBuilder passphrase = new StringBuilder();
        passphrase.appendCodePoint(0x03d2); // GREEK UPSILON WITH HOOK
        passphrase.appendCodePoint(0x0301); // COMBINING ACUTE ACCENT
        passphrase.appendCodePoint(0x0000); // NULL
        passphrase.appendCodePoint(0x010400); // DESERET CAPITAL LETTER LONG I
        passphrase.appendCodePoint(0x01f4a9); // PILE OF POO
        ECKey key = encryptedKey.decrypt(passphrase.toString());
        assertEquals("5Jajm8eQ22H3pGWLEVCXyvND8dQZhiQhoLJNKjYXk9roUFTMSZ4", key.getPrivateKeyEncoded(MAINNET)
                .toString());
    }

    @Test
    public void bip38testvector_compression_noEcMultiply_test1() throws Exception {
        BIP38PrivateKey encryptedKey = new BIP38PrivateKey(MainNetParams.get(),
                "6PYNKZ1EAgYgmQfmNVamxyXVWHzK5s6DGhwP4J5o44cvXdoY7sRzhtpUeo");
        ECKey key = encryptedKey.decrypt("TestingOneTwoThree");
        assertEquals("L44B5gGEpqEDRS9vVPz7QT35jcBG2r3CZwSwQ4fCewXAhAhqGVpP", key.getPrivateKeyEncoded(MAINNET)
                .toString());
    }

    @Test
    public void bip38testvector_compression_noEcMultiply_test2() throws Exception {
        BIP38PrivateKey encryptedKey = new BIP38PrivateKey(MainNetParams.get(),
                "6PYLtMnXvfG3oJde97zRyLYFZCYizPU5T3LwgdYJz1fRhh16bU7u6PPmY7");
        ECKey key = encryptedKey.decrypt("Satoshi");
        assertEquals("KwYgW8gcxj1JWJXhPSu4Fqwzfhp5Yfi42mdYmMa4XqK7NJxXUSK7", key.getPrivateKeyEncoded(MAINNET)
                .toString());
    }

    @Test
    public void bip38testvector_ecMultiply_noCompression_noLotAndSequence_test1() throws Exception {
        BIP38PrivateKey encryptedKey = new BIP38PrivateKey(MainNetParams.get(),
                "6PfQu77ygVyJLZjfvMLyhLMQbYnu5uguoJJ4kMCLqWwPEdfpwANVS76gTX");
        ECKey key = encryptedKey.decrypt("TestingOneTwoThree");
        assertEquals("5K4caxezwjGCGfnoPTZ8tMcJBLB7Jvyjv4xxeacadhq8nLisLR2", key.getPrivateKeyEncoded(MAINNET)
                .toString());
    }

    @Test
    public void bip38testvector_ecMultiply_noCompression_noLotAndSequence_test2() throws Exception {
        BIP38PrivateKey encryptedKey = new BIP38PrivateKey(MainNetParams.get(),
                "6PfLGnQs6VZnrNpmVKfjotbnQuaJK4KZoPFrAjx1JMJUa1Ft8gnf5WxfKd");
        ECKey key = encryptedKey.decrypt("Satoshi");
        assertEquals("5KJ51SgxWaAYR13zd9ReMhJpwrcX47xTJh2D3fGPG9CM8vkv5sH", key.getPrivateKeyEncoded(MAINNET)
                .toString());
    }

    @Test
    public void bip38testvector_ecMultiply_noCompression_lotAndSequence_test1() throws Exception {
        BIP38PrivateKey encryptedKey = new BIP38PrivateKey(MainNetParams.get(),
                "6PgNBNNzDkKdhkT6uJntUXwwzQV8Rr2tZcbkDcuC9DZRsS6AtHts4Ypo1j");
        ECKey key = encryptedKey.decrypt("MOLON LABE");
        assertEquals("5JLdxTtcTHcfYcmJsNVy1v2PMDx432JPoYcBTVVRHpPaxUrdtf8", key.getPrivateKeyEncoded(MAINNET)
                .toString());
    }

    @Test
    public void bip38testvector_ecMultiply_noCompression_lotAndSequence_test2() throws Exception {
        BIP38PrivateKey encryptedKey = new BIP38PrivateKey(MainNetParams.get(),
                "6PgGWtx25kUg8QWvwuJAgorN6k9FbE25rv5dMRwu5SKMnfpfVe5mar2ngH");
        ECKey key = encryptedKey.decrypt("ΜΟΛΩΝ ΛΑΒΕ");
        assertEquals("5KMKKuUmAkiNbA3DazMQiLfDq47qs8MAEThm4yL8R2PhV1ov33D", key.getPrivateKeyEncoded(MAINNET)
                .toString());
    }

    @Test(expected = BadPassphraseException.class)
    public void badPassphrase() throws Exception {
        BIP38PrivateKey encryptedKey = new BIP38PrivateKey(MAINNET,
                "6PRVWUbkzzsbcVac2qwfssoUJAN1Xhrg6bNk8J7Nzm5H7kxEbn2Nh2ZoGg");
        encryptedKey.decrypt("BAD");
    }

}

