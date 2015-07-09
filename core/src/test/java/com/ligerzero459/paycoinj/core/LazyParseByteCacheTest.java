/**
 * Copyright 2011 Steve Coughlan.
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

package com.ligerzero459.paycoinj.core;

import com.ligerzero459.paycoinj.params.MainNetParams;
import com.ligerzero459.paycoinj.params.UnitTestParams;
import com.ligerzero459.paycoinj.store.BlockStore;
import com.ligerzero459.paycoinj.store.MemoryBlockStore;
import com.ligerzero459.paycoinj.core.Block;
import com.ligerzero459.paycoinj.core.ECKey;
import com.ligerzero459.paycoinj.core.Message;
import com.ligerzero459.paycoinj.core.NetworkParameters;
import com.ligerzero459.paycoinj.core.PaycoinSerializer;
import com.ligerzero459.paycoinj.core.Transaction;
import com.ligerzero459.paycoinj.core.TransactionInput;
import com.ligerzero459.paycoinj.core.TransactionOutput;
import com.ligerzero459.paycoinj.core.Utils;
import com.ligerzero459.paycoinj.core.Wallet;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;

import static com.ligerzero459.paycoinj.core.Coin.*;
import static com.ligerzero459.paycoinj.core.Utils.HEX;
import static com.ligerzero459.paycoinj.testing.FakeTxBuilder.createFakeBlock;
import static com.ligerzero459.paycoinj.testing.FakeTxBuilder.createFakeTx;
import static org.junit.Assert.*;

public class LazyParseByteCacheTest {

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
    
    private final byte[] txMessagePart = HEX.withSeparator(" ", 2).decode(
            "3d de 3e e6 40 dd 37 02  20 6d cb bb 70 1a e3 28" +
            "1f dc 5b fe a7 75 ba 46  2d 34 6f d1 a7 c5 9b 38" +
            "bf 77 c4 12 64 07 a5 11  40 01 21 02 d6 bb 69 ba" +
            "37 bf 00 53 81 5c f0 2b  8b 09 df 01 81 17 8a 2f"); 
    
    private Wallet wallet;
    private BlockStore blockStore;
    private NetworkParameters unitTestParams;
    
    private byte[] b1Bytes;
    private byte[] b1BytesWithHeader;
    
    private byte[] tx1Bytes;
    private byte[] tx1BytesWithHeader;
    
    private byte[] tx2Bytes;
    private byte[] tx2BytesWithHeader;
    
    private void resetBlockStore() {
        blockStore = new MemoryBlockStore(unitTestParams);
    }
    
    @Before
    public void setUp() throws Exception {
        unitTestParams = UnitTestParams.get();
        wallet = new Wallet(unitTestParams);
        wallet.freshReceiveKey();

        resetBlockStore();
        
        Transaction tx1 = createFakeTx(unitTestParams,
                valueOf(2, 0),
                wallet.currentReceiveKey().toAddress(unitTestParams));
        
        //add a second input so can test granularity of byte cache.
        Transaction prevTx = new Transaction(unitTestParams);
        TransactionOutput prevOut = new TransactionOutput(unitTestParams, prevTx, COIN, wallet.currentReceiveKey().toAddress(unitTestParams));
        prevTx.addOutput(prevOut);
        // Connect it.
        tx1.addInput(prevOut);
        
        Transaction tx2 = createFakeTx(unitTestParams, COIN,
                new ECKey().toAddress(unitTestParams));

        Block b1 = createFakeBlock(blockStore, tx1, tx2).block;

        PaycoinSerializer bs = new PaycoinSerializer(unitTestParams);
        
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bs.serialize(tx1, bos);
        tx1BytesWithHeader = bos.toByteArray();
        tx1Bytes = tx1.paycoinSerialize();
        
        bos.reset();
        bs.serialize(tx2, bos);
        tx2BytesWithHeader = bos.toByteArray();
        tx2Bytes = tx2.paycoinSerialize();
        
        bos.reset();
        bs.serialize(b1, bos);
        b1BytesWithHeader = bos.toByteArray();
        b1Bytes = b1.paycoinSerialize();
    }
    
    @Test
    public void validateSetup() {
        byte[] b1 = new byte[] {1, 1, 1, 2, 3, 4, 5, 6, 7};
        byte[] b2 = new byte[] {1, 2, 3};
        assertTrue(arrayContains(b1, b2));
        assertTrue(arrayContains(txMessage, txMessagePart));
        assertTrue(arrayContains(tx1BytesWithHeader, tx1Bytes));
        assertTrue(arrayContains(tx2BytesWithHeader, tx2Bytes));
        assertTrue(arrayContains(b1BytesWithHeader, b1Bytes));
        assertTrue(arrayContains(b1BytesWithHeader, tx1Bytes));
        assertTrue(arrayContains(b1BytesWithHeader, tx2Bytes));
        assertFalse(arrayContains(tx1BytesWithHeader, b1Bytes));
    }
    
    @Test
    public void testTransactionsLazyRetain() throws Exception {
        testTransaction(MainNetParams.get(), txMessage, false, true, true);
        testTransaction(unitTestParams, tx1BytesWithHeader, false, true, true);
        testTransaction(unitTestParams, tx2BytesWithHeader, false, true, true);
    }
    
    @Test
    public void testTransactionsLazyNoRetain() throws Exception {
        testTransaction(MainNetParams.get(), txMessage, false, true, false);
        testTransaction(unitTestParams, tx1BytesWithHeader, false, true, false);
        testTransaction(unitTestParams, tx2BytesWithHeader, false, true, false);
    }
    
    @Test
    public void testTransactionsNoLazyNoRetain() throws Exception {
        testTransaction(MainNetParams.get(), txMessage, false, false, false);
        testTransaction(unitTestParams, tx1BytesWithHeader, false, false, false);
        testTransaction(unitTestParams, tx2BytesWithHeader, false, false, false);
    }
    
    @Test
    public void testTransactionsNoLazyRetain() throws Exception {
        testTransaction(MainNetParams.get(), txMessage, false, false, true);
        testTransaction(unitTestParams, tx1BytesWithHeader, false, false, true);
        testTransaction(unitTestParams, tx2BytesWithHeader, false, false, true);
    }
    
    @Test
    public void testBlockAll() throws Exception {
        testBlock(b1BytesWithHeader, false, false, false);
        testBlock(b1BytesWithHeader, false, true, true);
        testBlock(b1BytesWithHeader, false, true, false);
        testBlock(b1BytesWithHeader, false, false, true);
    }
    
    
    public void testBlock(byte[] blockBytes, boolean isChild, boolean lazy, boolean retain) throws Exception {
        //reference serializer to produce comparison serialization output after changes to
        //message structure.
        PaycoinSerializer bsRef = new PaycoinSerializer(unitTestParams, false, false);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        
        PaycoinSerializer bs = new PaycoinSerializer(unitTestParams, lazy, retain);
        Block b1;
        Block bRef;
        b1 = (Block) bs.deserialize(ByteBuffer.wrap(blockBytes));
        bRef = (Block) bsRef.deserialize(ByteBuffer.wrap(blockBytes));
        
        //verify our reference PaycoinSerializer produces matching byte array.
        bos.reset();
        bsRef.serialize(bRef, bos);
        assertTrue(Arrays.equals(bos.toByteArray(), blockBytes));
        
        //check lazy and retain status survive both before and after a serialization
        assertEquals(!lazy, b1.isParsedTransactions());
        assertEquals(!lazy, b1.isParsedHeader());
        if (b1.isParsedHeader())
            assertEquals(retain, b1.isHeaderBytesValid());
        if (b1.isParsedTransactions())
            assertEquals(retain, b1.isTransactionBytesValid());
        
        serDeser(bs, b1, blockBytes, null, null);
        
        assertEquals(!lazy, b1.isParsedTransactions());
        assertEquals(!lazy, b1.isParsedHeader());
        if (b1.isParsedHeader())
            assertEquals(retain, b1.isHeaderBytesValid());
        if (b1.isParsedTransactions())
            assertEquals(retain, b1.isTransactionBytesValid());
        
        //compare to ref block
        bos.reset();
        bsRef.serialize(bRef, bos);
        serDeser(bs, b1, bos.toByteArray(), null, null);
        
        //retrieve a value from a child
        b1.getTransactions();
        assertTrue(b1.isParsedTransactions());
        if (b1.getTransactions().size() > 0) {
            assertTrue(b1.isParsedTransactions());
            Transaction tx1 = b1.getTransactions().get(0);
            
            //this will always be true for all children of a block once they are retrieved.
            //the tx child inputs/outputs may not be parsed however.
            
            //no longer forced to parse if length not provided.
            //assertEquals(true, tx1.isParsed());
            if (tx1.isParsed())
                assertEquals(retain, tx1.isCached());
            else
                assertTrue(tx1.isCached());
            
            //does it still match ref block?
            serDeser(bs, b1, bos.toByteArray(), null, null);
        }
        
        //refresh block
        b1 = (Block) bs.deserialize(ByteBuffer.wrap(blockBytes));
        bRef = (Block) bsRef.deserialize(ByteBuffer.wrap(blockBytes));
        
        //retrieve a value from header
        b1.getDifficultyTarget();
        assertTrue(b1.isParsedHeader());
        assertEquals(lazy, !b1.isParsedTransactions());
        
        //does it still match ref block?
        serDeser(bs, b1, bos.toByteArray(), null, null);

        
        //refresh block
        b1 = (Block) bs.deserialize(ByteBuffer.wrap(blockBytes));
        bRef = (Block) bsRef.deserialize(ByteBuffer.wrap(blockBytes));
        
        //retrieve a value from a child and header
        b1.getDifficultyTarget();
        assertTrue(b1.isParsedHeader());
        assertEquals(lazy, !b1.isParsedTransactions());
        
        b1.getTransactions();
        assertTrue(b1.isParsedTransactions());
        if (b1.getTransactions().size() > 0) {
            assertTrue(b1.isParsedTransactions());
            Transaction tx1 = b1.getTransactions().get(0);
            
            //no longer forced to parse if length not provided.
            //assertEquals(true, tx1.isParsed());
            
            if (tx1.isParsed())
                assertEquals(retain, tx1.isCached());
            else
                assertTrue(tx1.isCached());    
        }
        //does it still match ref block?
        serDeser(bs, b1, bos.toByteArray(), null, null);
        
        //refresh block
        b1 = (Block) bs.deserialize(ByteBuffer.wrap(blockBytes));
        bRef = (Block) bsRef.deserialize(ByteBuffer.wrap(blockBytes));
        
        //change a value in header
        b1.setNonce(23);
        bRef.setNonce(23);
        assertTrue(b1.isParsedHeader());
        assertEquals(lazy, !b1.isParsedTransactions());
        assertFalse(b1.isHeaderBytesValid());
        if (b1.isParsedTransactions())
            assertEquals(retain , b1.isTransactionBytesValid());
        else
            assertEquals(true, b1.isTransactionBytesValid());
        //does it still match ref block?
        bos.reset();
        bsRef.serialize(bRef, bos);
        serDeser(bs, b1, bos.toByteArray(), null, null);
        
        //refresh block
        b1 = (Block) bs.deserialize(ByteBuffer.wrap(blockBytes));
        bRef = (Block) bsRef.deserialize(ByteBuffer.wrap(blockBytes));
        
        //retrieve a value from a child of a child
        b1.getTransactions();
        if (b1.getTransactions().size() > 0) {
            Transaction tx1 = b1.getTransactions().get(0);
            
            TransactionInput tin = tx1.getInputs().get(0);
            
            assertTrue(tx1.isParsed());
            assertTrue(b1.isParsedTransactions());
            assertEquals(!lazy, b1.isParsedHeader());
            
            assertEquals(!lazy, tin.isParsed());
            assertEquals(tin.isParsed() ? retain : true, tin.isCached());    
            
            //does it still match ref tx?
            bos.reset();
            bsRef.serialize(bRef, bos);
            serDeser(bs, b1, bos.toByteArray(), null, null);
        }
        
        //refresh block
        b1 = (Block) bs.deserialize(ByteBuffer.wrap(blockBytes));
        bRef = (Block) bsRef.deserialize(ByteBuffer.wrap(blockBytes));
        
        //add an input
        b1.getTransactions();
        if (b1.getTransactions().size() > 0) {
            Transaction tx1 = b1.getTransactions().get(0);
            
            if (tx1.getInputs().size() > 0) {
                tx1.addInput(tx1.getInputs().get(0));
                //replicate on reference tx
                bRef.getTransactions().get(0).addInput(bRef.getTransactions().get(0).getInputs().get(0));
                
                assertFalse(tx1.isCached());
                assertTrue(tx1.isParsed());
                assertFalse(b1.isTransactionBytesValid());
                assertTrue(b1.isParsedHeader());
                
                //confirm sibling cache status was unaffected
                if (tx1.getInputs().size() > 1) {
                    boolean parsed = tx1.getInputs().get(1).isParsed();
                    assertEquals(parsed ? retain : true, tx1.getInputs().get(1).isCached());
                    assertEquals(!lazy, parsed);
                }
                
                //this has to be false. Altering a tx invalidates the merkle root.
                //when we have seperate merkle caching then the entire header won't need to be
                //invalidated.
                assertFalse(b1.isHeaderBytesValid());
                
                bos.reset();
                bsRef.serialize(bRef, bos);
                byte[] source = bos.toByteArray();
                //confirm we still match the reference tx.
                serDeser(bs, b1, source, null, null);
            }
            
            //does it still match ref tx?
            bos.reset();
            bsRef.serialize(bRef, bos);
            serDeser(bs, b1, bos.toByteArray(), null, null);
        }
        
        //refresh block
        b1 = (Block) bs.deserialize(ByteBuffer.wrap(blockBytes));
        Block b2 = (Block) bs.deserialize(ByteBuffer.wrap(blockBytes));
        bRef = (Block) bsRef.deserialize(ByteBuffer.wrap(blockBytes));
        Block bRef2 = (Block) bsRef.deserialize(ByteBuffer.wrap(blockBytes));
        
        //reparent an input
        b1.getTransactions();
        if (b1.getTransactions().size() > 0) {
            Transaction tx1 = b1.getTransactions().get(0);
            Transaction tx2 = b2.getTransactions().get(0);
            
            if (tx1.getInputs().size() > 0) {
                TransactionInput fromTx1 = tx1.getInputs().get(0);
                tx2.addInput(fromTx1);
                
                //replicate on reference tx
                TransactionInput fromTxRef = bRef.getTransactions().get(0).getInputs().get(0);
                bRef2.getTransactions().get(0).addInput(fromTxRef);
                
                //b1 hasn't changed but it's no longer in the parent
                //chain of fromTx1 so has to have been uncached since it won't be
                //notified of changes throught the parent chain anymore.
                assertFalse(b1.isTransactionBytesValid());
                
                //b2 should have it's cache invalidated because it has changed.
                assertFalse(b2.isTransactionBytesValid());
                
                bos.reset();
                bsRef.serialize(bRef2, bos);
                byte[] source = bos.toByteArray();
                //confirm altered block matches altered ref block.
                serDeser(bs, b2, source, null, null);
            }
            
            //does unaltered block still match ref block?
            bos.reset();
            bsRef.serialize(bRef, bos);
            serDeser(bs, b1, bos.toByteArray(), null, null);
            
            //how about if we refresh it?
            bRef = (Block) bsRef.deserialize(ByteBuffer.wrap(blockBytes));
            bos.reset();
            bsRef.serialize(bRef, bos);
            serDeser(bs, b1, bos.toByteArray(), null, null);
        }
        
    }
    
    public void testTransaction(NetworkParameters params, byte[] txBytes, boolean isChild, boolean lazy, boolean retain) throws Exception {

        //reference serializer to produce comparison serialization output after changes to
        //message structure.
        PaycoinSerializer bsRef = new PaycoinSerializer(params, false, false);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        
        PaycoinSerializer bs = new PaycoinSerializer(params, lazy, retain);
        Transaction t1;
        Transaction tRef;
        t1 = (Transaction) bs.deserialize(ByteBuffer.wrap(txBytes));
        tRef = (Transaction) bsRef.deserialize(ByteBuffer.wrap(txBytes));
        
        //verify our reference PaycoinSerializer produces matching byte array.
        bos.reset();
        bsRef.serialize(tRef, bos);
        assertTrue(Arrays.equals(bos.toByteArray(), txBytes));
        
        //check lazy and retain status survive both before and after a serialization
        assertEquals(!lazy, t1.isParsed());
        if (t1.isParsed())
            assertEquals(retain, t1.isCached());
        
        serDeser(bs, t1, txBytes, null, null);
        
        assertEquals(lazy, !t1.isParsed());
        if (t1.isParsed())
            assertEquals(retain, t1.isCached());
        
        //compare to ref tx
        bos.reset();
        bsRef.serialize(tRef, bos);
        serDeser(bs, t1, bos.toByteArray(), null, null);
        
        //retrieve a value from a child
        t1.getInputs();
        assertTrue(t1.isParsed());
        if (t1.getInputs().size() > 0) {
            assertTrue(t1.isParsed());
            TransactionInput tin = t1.getInputs().get(0);
            assertEquals(!lazy, tin.isParsed());
            if (tin.isParsed())
                assertEquals(retain, tin.isCached());    
            
            //does it still match ref tx?
            serDeser(bs, t1, bos.toByteArray(), null, null);
        }
        
        //refresh tx
        t1 = (Transaction) bs.deserialize(ByteBuffer.wrap(txBytes));
        tRef = (Transaction) bsRef.deserialize(ByteBuffer.wrap(txBytes));
        
        //add an input
        if (t1.getInputs().size() > 0) {
            
            t1.addInput(t1.getInputs().get(0));
            
            //replicate on reference tx
            tRef.addInput(tRef.getInputs().get(0));
            
            assertFalse(t1.isCached());
            assertTrue(t1.isParsed());
            
            bos.reset();
            bsRef.serialize(tRef, bos);
            byte[] source = bos.toByteArray();
            //confirm we still match the reference tx.
            serDeser(bs, t1, source, null, null);
        }
        
    }
    
    private void serDeser(PaycoinSerializer bs, Message message, byte[] sourceBytes, byte[] containedBytes, byte[] containingBytes) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bs.serialize(message, bos);
        byte[] b1 = bos.toByteArray();
        
        Message m2 = bs.deserialize(ByteBuffer.wrap(b1));

        assertEquals(message, m2);
 
        bos.reset();
        bs.serialize(m2, bos);
        byte[] b2 = bos.toByteArray(); 
        assertTrue(Arrays.equals(b1, b2));
        
        if (sourceBytes != null) {
            assertTrue(arrayContains(sourceBytes, b1));
            
            assertTrue(arrayContains(sourceBytes, b2));
        }
        
        if (containedBytes != null) {
            assertTrue(arrayContains(b1, containedBytes));
        }
        if (containingBytes != null) {
            assertTrue(arrayContains(containingBytes, b1));
        }
    }
    
    public static boolean arrayContains(byte[] sup, byte[] sub) {
        if (sup.length < sub.length)
            return false;       
        
        String superstring = Utils.HEX.encode(sup);
        String substring = Utils.HEX.encode(sub);
        
        int ind = superstring.indexOf(substring);
        
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < superstring.indexOf(substring); i++)
            sb.append(" ");
        
        //System.out.println(superstring);
        //System.out.println(sb.append(substring).toString());
        //System.out.println();
        return ind > -1;
        
    }
}
