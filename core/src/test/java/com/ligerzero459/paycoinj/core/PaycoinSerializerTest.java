/**
 * Copyright 2011 Noa Resare
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

package com.ligerzero459.paycoinj.core;


import com.ligerzero459.paycoinj.core.AddressMessage;
import com.ligerzero459.paycoinj.core.Block;
import com.ligerzero459.paycoinj.core.HeadersMessage;
import com.ligerzero459.paycoinj.core.PeerAddress;
import com.ligerzero459.paycoinj.core.PaycoinSerializer;
import com.ligerzero459.paycoinj.core.Transaction;
import com.ligerzero459.paycoinj.params.MainNetParams;
import com.ligerzero459.paycoinj.core.AddressMessage;
import com.ligerzero459.paycoinj.core.Block;
import com.ligerzero459.paycoinj.core.HeadersMessage;
import com.ligerzero459.paycoinj.core.Message;
import com.ligerzero459.paycoinj.core.PeerAddress;
import com.ligerzero459.paycoinj.core.PaycoinSerializer;
import com.ligerzero459.paycoinj.core.ProtocolException;
import com.ligerzero459.paycoinj.core.Transaction;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import static com.ligerzero459.paycoinj.core.Utils.HEX;
import static org.junit.Assert.*;

public class PaycoinSerializerTest {
    private final byte[] addrMessage = HEX.decode("e6e8e9e56164647200000000000000001f000000" +
            "ed52399b01e215104d010000000000000000000000000000000000ffff0a000001208d");

    private final byte[] txMessage = HEX.withSeparator(" ", 2).decode(
            "e6 e8 e9 e5 74 78 00 00  00 00 00 00 00 00 00 00" +
            "79 01 00 00 60 b3 19 26" +
            "01 00 00 00 aa 3d 0b 54  02 a0 46 88 b5 c7 bf 97" +
            "a7 2b 6c 3d 28 b8 4a 6c  5e a0 fd cd 3b f1 7a 19" +
            "c0 4e d3 55 92 60 44 51  d1 01 00 00 00 6a 47 30" +
            "44 02 20 5f 96 12 2a 91  ce 63 00 81 12 c5 d2 8e" +
            "03 7d 25 a5 20 53 72 81  8c 82 6c 76 a0 4b ae 6a" +
            "7b 1c 64 02 20 30 fb 26  59 83 5c 84 6c 2f fc c1" +
            "84 8f aa 07 9f 67 2a ca  39 6f bb 18 07 0e 0e 54" +
            "c7 fd b4 59 a3 01 21 02  d6 bb 69 ba 37 bf 00 53" +
            "81 5c f0 2b 8b 09 df 01  81 17 8a 2f 64 3a 9b 43" +
            "91 87 fb e5 72 50 ab 2a  ff ff ff ff db bf 69 88" +
            "38 13 d8 8f ca d8 61 10  58 c1 ee ce b5 19 53 66" +
            "1c 4c 02 f6 9c d3 f2 96  35 26 53 5a 01 00 00 00" +
            "6b 48 30 45 02 21 00 e2  76 9a db 5c d4 07 04 49" +
            "6d cd f4 ac 21 a3 c7 d9  de 0a 67 dc 2f 01 d3 8a" +
            "3d de 3e e6 40 dd 37 02  20 6d cb bb 70 1a e3 28" +
            "1f dc 5b fe a7 75 ba 46  2d 34 6f d1 a7 c5 9b 38" +
            "bf 77 c4 12 64 07 a5 11  40 01 21 02 d6 bb 69 ba" +
            "37 bf 00 53 81 5c f0 2b  8b 09 df 01 81 17 8a 2f" +
            "64 3a 9b 43 91 87 fb e5  72 50 ab 2a ff ff ff ff" +
            "02 80 8d 5b 00 00 00 00  00 19 76 a9 14 98 fe 16" +
            "32 33 99 f6 61 1f a4 22  51 f8 3a ac 83 0d e0 02" +
            "b3 88 ac 10 7a 07 00 00  00 00 00 19 76 a9 14 94" +
            "df 70 c3 6d 3a 90 ea c8  b5 1a e6 ab 16 af 5d 22" +
            "69 fc 74 88 ac 00 00 00  00");

    @Test
    public void testAddr() throws Exception {
        PaycoinSerializer bs = new PaycoinSerializer(MainNetParams.get());
        AddressMessage a = (AddressMessage)bs.deserialize(ByteBuffer.wrap(addrMessage));
        assertEquals(1, a.getAddresses().size());
        PeerAddress pa = a.getAddresses().get(0);
        assertEquals(8333, pa.getPort());
        assertEquals("10.0.0.1", pa.getAddr().getHostAddress());
        ByteArrayOutputStream bos = new ByteArrayOutputStream(addrMessage.length);
        bs.serialize(a, bos);

        //this wont be true due to dynamic timestamps.
        //assertTrue(LazyParseByteCacheTest.arrayContains(bos.toByteArray(), addrMessage));
    }

    @Test
    public void testLazyParsing()  throws Exception {
        PaycoinSerializer bs = new PaycoinSerializer(MainNetParams.get(), true, false);

    	Transaction tx = (Transaction)bs.deserialize(ByteBuffer.wrap(txMessage));
        assertNotNull(tx);
        assertEquals(false, tx.isParsed());
        assertEquals(true, tx.isCached());
        tx.getInputs();
        assertEquals(true, tx.isParsed());

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bs.serialize(tx, bos);
        assertEquals(true, Arrays.equals(txMessage, bos.toByteArray()));
    }

    @Test
    public void testCachedParsing()  throws Exception {
        testCachedParsing(true);
        testCachedParsing(false);
    }

    private void testCachedParsing(boolean lazy)  throws Exception {
        PaycoinSerializer bs = new PaycoinSerializer(MainNetParams.get(), lazy, true);
        
        //first try writing to a fields to ensure uncaching and children are not affected
        Transaction tx = (Transaction)bs.deserialize(ByteBuffer.wrap(txMessage));
        assertNotNull(tx);
        assertEquals(!lazy, tx.isParsed());
        assertEquals(true, tx.isCached());

        tx.setLockTime(1);
        //parent should have been uncached
        assertEquals(false, tx.isCached());
        //child should remain cached.
        assertEquals(true, tx.getInputs().get(0).isCached());

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bs.serialize(tx, bos);
        assertEquals(true, !Arrays.equals(txMessage, bos.toByteArray()));

      //now try writing to a child to ensure uncaching is propagated up to parent but not to siblings
        tx = (Transaction)bs.deserialize(ByteBuffer.wrap(txMessage));
        assertNotNull(tx);
        assertEquals(!lazy, tx.isParsed());
        assertEquals(true, tx.isCached());

        tx.getInputs().get(0).setSequenceNumber(1);
        //parent should have been uncached
        assertEquals(false, tx.isCached());
        //so should child
        assertEquals(false, tx.getInputs().get(0).isCached());

        bos = new ByteArrayOutputStream();
        bs.serialize(tx, bos);
        assertEquals(true, !Arrays.equals(txMessage, bos.toByteArray()));

      //deserialize/reserialize to check for equals.
        tx = (Transaction)bs.deserialize(ByteBuffer.wrap(txMessage));
        assertNotNull(tx);
        assertEquals(!lazy, tx.isParsed());
        assertEquals(true, tx.isCached());
        bos = new ByteArrayOutputStream();
        bs.serialize(tx, bos);
        assertEquals(true, Arrays.equals(txMessage, bos.toByteArray()));

      //deserialize/reserialize to check for equals.  Set a field to it's existing value to trigger uncache
        tx = (Transaction)bs.deserialize(ByteBuffer.wrap(txMessage));
        assertNotNull(tx);
        assertEquals(!lazy, tx.isParsed());
        assertEquals(true, tx.isCached());

        tx.getInputs().get(0).setSequenceNumber(tx.getInputs().get(0).getSequenceNumber());

        bos = new ByteArrayOutputStream();
        bs.serialize(tx, bos);
        assertEquals(true, Arrays.equals(txMessage, bos.toByteArray()));

    }


    /**
     * Get 1 header of the block number 1 (the first one is 0) in the chain
     */
    @Test
    public void testHeaders1() throws Exception {
        PaycoinSerializer bs = new PaycoinSerializer(MainNetParams.get());

        HeadersMessage hm = (HeadersMessage) bs.deserialize(ByteBuffer.wrap(HEX.decode("e6e8e9e5686561" +
                "64657273000000000053000000a25e0bfc01010000006fe28c0ab6f1b372c1a6a246ae6" +
                "3f74f931e8365e15a089c68d6190000000000982051fd1e4ba744bbbe680e1fee14677b" +
                "a1a3c3540bf7b1cdb606e857233e0e61bc6649ffff001d01e362990000")));

        // The first block after the genesis
        // http://blockexplorer.com/b/1
        Block block = hm.getBlockHeaders().get(0);
        String hash = block.getHashAsString();
        assertEquals(hash, "00000000839a8e6886ab5951d76f411475428afc90947ee320161bbf18eb6048");

        assertNull(block.transactions);

        assertEquals(HEX.encode(block.getMerkleRoot().getBytes()),
                "0e3e2357e806b6cdb1f70b54c3a3a17b6714ee1f0e68bebb44a74b1efd512098");
    }


    @Test
    /**
     * Get 6 headers of blocks 1-6 in the chain
     */
    public void testHeaders2() throws Exception {
        PaycoinSerializer bs = new PaycoinSerializer(MainNetParams.get());

        HeadersMessage hm = (HeadersMessage) bs.deserialize(ByteBuffer.wrap(HEX.decode("e6e8e9e56865616465" +
                "72730000000000ed010000ef3fcbd706010000006fe28c0ab6f1b372c1a6a246ae63f74f931e" +
                "8365e15a089c68d6190000000000982051fd1e4ba744bbbe680e1fee14677ba1a3c3540bf7b1c" +
                "db606e857233e0e61bc6649ffff001d01e362990000010000004860eb18bf1b1620e37e9490fc8a" +
                "427514416fd75159ab86688e9a8300000000d5fdcc541e25de1c7a5addedf24858b8bb665c9f36" +
                "ef744ee42c316022c90f9bb0bc6649ffff001d08d2bd61000001000000bddd99ccfda39da1b108ce1" +
                "a5d70038d0a967bacb68b6b63065f626a0000000044f672226090d85db9a9f2fbfe5f0f9609b387" +
                "af7be5b7fbb7a1767c831c9e995dbe6649ffff001d05e0ed6d0000010000004944469562ae1c2c74" +
                "d9a535e00b6f3e40ffbad4f2fda3895501b582000000007a06ea98cd40ba2e3288262b28638cec" +
                "5337c1456aaf5eedc8e9e5a20f062bdf8cc16649ffff001d2bfee0a900000100000085144a84488e" +
                "a88d221c8bd6c059da090e88f8a2c99690ee55dbba4e00000000e11c48fecdd9e72510ca84f023" +
                "370c9a38bf91ac5cae88019bee94d24528526344c36649ffff001d1d03e477000001000000fc33f5" +
                "96f822a0a1951ffdbf2a897b095636ad871707bf5d3162729b00000000379dfb96a5ea8c81700ea4" +
                "ac6b97ae9a9312b2d4301a29580e924ee6761a2520adc46649ffff001d189c4c970000")));

        int nBlocks = hm.getBlockHeaders().size();
        assertEquals(nBlocks, 6);

        // index 0 block is the number 1 block in the block chain
        // http://blockexplorer.com/b/1
        Block zeroBlock = hm.getBlockHeaders().get(0);
        String zeroBlockHash = zeroBlock.getHashAsString();

        assertEquals("00000000839a8e6886ab5951d76f411475428afc90947ee320161bbf18eb6048",
                zeroBlockHash);
        assertEquals(zeroBlock.getNonce(), 2573394689L);


        Block thirdBlock = hm.getBlockHeaders().get(3);
        String thirdBlockHash = thirdBlock.getHashAsString();

        // index 3 block is the number 4 block in the block chain
        // http://blockexplorer.com/b/4
        assertEquals("000000004ebadb55ee9096c9a2f8880e09da59c0d68b1c228da88e48844a1485",
                thirdBlockHash);
        assertEquals(thirdBlock.getNonce(), 2850094635L);
    }

    @Test
    public void testPaycoinPacketHeader() {
        try {
            new PaycoinSerializer.PaycoinPacketHeader(ByteBuffer.wrap(new byte[]{0}));
            fail();
        } catch (BufferUnderflowException e) {
        }

        // Message with a Message size which is 1 too big, in little endian format.
        byte[] wrongMessageLength = HEX.decode("000000000000000000000000010000020000000000");
        try {
            new PaycoinSerializer.PaycoinPacketHeader(ByteBuffer.wrap(wrongMessageLength));
            fail();
        } catch (ProtocolException e) {
            // expected
        }
    }

    @Test
    public void testSeekPastMagicBytes() {
        // Fail in another way, there is data in the stream but no magic bytes.
        byte[] brokenMessage = HEX.decode("000000");
        try {
            new PaycoinSerializer(MainNetParams.get()).seekPastMagicBytes(ByteBuffer.wrap(brokenMessage));
            fail();
        } catch (BufferUnderflowException e) {
            // expected
        }
    }

    @Test
    /**
     * Tests serialization of an unknown message.
     */
    public void testSerializeUnknownMessage() {
        PaycoinSerializer bs = new PaycoinSerializer(MainNetParams.get());

        UnknownMessage a = new UnknownMessage();
        ByteArrayOutputStream bos = new ByteArrayOutputStream(addrMessage.length);
        try {
            bs.serialize(a, bos);
            fail();
        } catch (Throwable e) {
        }
    }

    /**
     * Unknown message for testSerializeUnknownMessage.
     */
    class UnknownMessage extends Message {
        @Override
        void parse() throws ProtocolException {
        }

        @Override
        protected void parseLite() throws ProtocolException {
        }
    }

}
