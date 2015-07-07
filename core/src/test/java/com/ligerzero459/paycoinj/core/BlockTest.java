/**
 * Copyright 2011 Google Inc.
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

import com.ligerzero459.paycoinj.core.Block;
import com.ligerzero459.paycoinj.core.ECKey;
import com.ligerzero459.paycoinj.core.NetworkParameters;
import com.ligerzero459.paycoinj.core.Transaction;
import com.ligerzero459.paycoinj.core.VerificationException;
import com.ligerzero459.paycoinj.params.UnitTestParams;
import com.ligerzero459.paycoinj.script.ScriptOpCodes;
import com.ligerzero459.paycoinj.core.Block;
import com.ligerzero459.paycoinj.core.Coin;
import com.ligerzero459.paycoinj.core.ECKey;
import com.ligerzero459.paycoinj.core.NetworkParameters;
import com.ligerzero459.paycoinj.core.Sha256Hash;
import com.ligerzero459.paycoinj.core.Transaction;
import com.ligerzero459.paycoinj.core.TransactionInput;
import com.ligerzero459.paycoinj.core.TransactionOutPoint;
import com.ligerzero459.paycoinj.core.TransactionOutput;
import com.ligerzero459.paycoinj.core.VerificationException;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.util.Arrays;

import static com.ligerzero459.paycoinj.core.Utils.HEX;
import static org.junit.Assert.*;

public class BlockTest {
    static final NetworkParameters params = UnitTestParams.get();

    public static final byte[] blockBytes;

    static {
        // Block d189225fbecb12296349d548fef503bc095fbc827dfd442706e48b8965eea482
        // One with lots of transactions in, so a good test of the merkle tree hashing.
        blockBytes = HEX.decode("010000000b194def0b06da93c8f3d2a56a7711d10b8683e19c5a5728b9c09eeb09b049dc30601989fa74c20ac0f0bd7dafb2366387c1972678d7898d043a765a26817313ef6f7754f640121c000000001301000000ef6f7754010000000000000000000000000000000000000000000000000000000000000000ffffffff0f04ef6f775402cf00062f503253482fffffffff010000000000000000000000000001000000ef6f77540114c2113eef9019a569db01e4da6f52968d364fd13935d0435c9178f9afb9a596020000004a493046022100f0427ecc7c416c93431c7de02083d0ba7a5dd1c2237239b4bd8aff7bbde519a7022100a50966f428e759af308e4c78695b4b0a2d9f518033d241a4d3c32bd5c6fe151e01ffffffff02000000000000000000c0e2b70400000000232102c341250eda19f1b669b9e6b82a81d7fbdbb01fb0f59ca187d3393e570f667e26ac0000000001000000d96e775401440a5ab399014832e30441375ab763254168cc069382be80d00e4acdacc9a34c00000000494830450220744781c2d9da39951e9f3da98a13015d753264fdc2cf658bebf7de2b7b1d5d3d02210086b579cbfcfcbe1f5f58b0ad8c6351cba06c1fba3b8bc60166ee95526e89333101ffffffff0253359103000000001976a914f2ebbe496745c2c1f9d3d5ba0814aa30a1d73f7688ac4ddefc00000000001976a9145e549182ab3bcb9c2f65db3a6b43abe400b635a188ac0000000001000000d96e7754012c72dfc2285007f690c4a5d7cc25008b85f2d6a8eeadca986475eaec8dd5307b00000000494830450221009212cd2b87d3f3d9c95170d3559dace64bd48e9ecd383547d26a42ddae6d62fe022009476f7b9e117abafbd07904622b1ea1c61326a5e6737be575b7ce0b233fbb1d01ffffffff02e5c56b04000000001976a91408df80f316f5ef29aee97cba48be9ffe1887e64088accb742200000000001976a914de34e5be279826250105b0cc3b50e881e72499dd88ac0000000001000000d96e775401c051999a2798a87afcf722c79bf2c507d3974a4cd5e10f423af5c1e8b40bdcfe00000000494830450220754aab8bcc477172e3debc9b239aba2ef88f7d7b7521157ef5eacc8f4470f59c02210084590bbe846fea202510f20a3b79c29c36123b9203d52c121df676e59091d93d01ffffffff02ed4b7004000000001976a9147ebf943ccad1818a2fed35f2b35ada37bccedea888acf3631e00000000001976a9142697273c47945ffe092522c78e8e16abebc8f6fa88ac0000000001000000d96e775401d9550157e54316064d747ddd84eeeb1c71a1a8cc1bd2a5c8084fe220a6458a4f00000000494830450221008e28822f10265e93b39ca1e399f6a856848599a8132686c8ec3ce89b22e4694002205da37f77230cb9188b7af9e55d50640dd559702380293b45297a712e4831cff201ffffffff02c9ea8204000000001976a914e4018858ed65c2b83ba4d2cbde7839cc99b2bc5f88acc7010b00000000001976a9140c772fa26f15373f77d6d1c92ed68a78942f077f88ac0000000001000000da6e7754010ed08ef79ba04be6f0e8fe2cb95ea82cab1fd491b4b7f487dd8e37c3296e1730000000006b4830450220183fb89ead221967bc0130f8a91a5dd482715b7d91b3ff8a7ac00817604094920221009d5698860697c3453c15e1d021b6f16eea74adb89ea8533595f7b46062cc3e4d0121032918c481ec2cd2acf0e4ddaab81551e6bf442f9fb680751164326a9316450ca2ffffffff020c1c7b04000000001976a914a8502c56626ec97ecd3d3b11f1caa65fec98d75488ac22b81300000000001976a914e8ed878566f9b520cdb5ed4663bcd0209e35466388ac0000000001000000d86e775401537076f09ccf6a88d238de0893ca3439a5f9bb9f32c941af8925f46b8f6ae731000000006b4830450220115cb4596f17781550db5d47f4f9f2e3ee6d5a0248e049ff249238ebeec864da022100b83eda2130bfa719cc8ad2fb6270b8d4a7a757c1d963c4c99f1ef7090fc44f000121036500a64b04066a130dfc88c326725998ee17dea00e449be4bf1ef87805cbc959ffffffff029d593403000000001976a914990c359625d4e39e02dee0c5398f59da1b05746288ac8792eb00000000001976a91471231e3dcdc8a3bf7a2ee65e4b10005bf2c0f4cb88ac0000000001000000d86e775401a35fbb5f98d091c465051eb1408ad9b67b80eaf315510c960977c466c3697663000000006b4830450221008fba19d2e5851e9e9a27b82295cf5193c55964af4db7c1cec21733c26761174d022014cc9f1cafde69acd69b7849f991fe0576620aac8edde8272e2cd2f0e97fd93b01210324d951a5c7caee96cf7a46dbf5d8685ed4163be962e14ba692605ab79fb5009affffffff0280577803000000001976a9140bd7aaad1dd3f48e5a041621bc9b99634dda294e88ac19053e00000000001976a9149e9c1266f1d1dbcd6c650c08abc9fc35be33b50088ac0000000001000000d86e775401fcb9dfa58fe0da8dfae1c8307d6534836361c81c2ef4c770426460d04e8d1ac8000000006c493046022100dbbce4582d1bcba75fee86331a7f9374948289b46565cc3d51d454907ca49a09022100ed4707c99e6251a2037fa8c8fccc655fa43dfc41caf98e6793d743d819c8cf48012102f3dacd8d665ead7b4c6f2510cf0dc440f3a52e7424063e81e4eee2da516314feffffffff02d3209902000000001976a914c994acde36a8aab7833cfde33708059a5209421488ac91fb2300000000001976a914e2463d83628dd657c5789221529d7610b1d574f688ac0000000001000000d86e775401b2c1843bab0ff07a65aba5f77ff3c49bb7a51e598f537f86e383d4ad0096e75c000000006b48304502205f6fa87ff80c2dc11741ce729abde8c823629d2125f5b4c0c8dfcab0f55c978f0221009158bd0318930f546e59c6b0a415cb3d5da34cb52caf0bc077dd1f122c3ac1ee0121027223d408557c54cce992e5f777b126985ef6dbc64dda6c38e9b4e240012817faffffffff02bf115e01000000001976a9140f1c7641338709f2f868a0d627a76991a65cd98c88acd04f9900000000001976a914edd3f06519ee3fbfa0d8f48434cb0b4c4c376f0688ac0000000001000000d86e7754012c562fa779b62f5f7842f68b31c382e41bcd17f9dcbb4fd6826f55a55db2fdd4000000006b483045022100f376c93c8913e142ed335910b210e5c56134360f4eb5837ed0944c5275854f7702201d67fc240566f6a1367a73e1d2940a1e582fb25d40bfd1decf13f5ddd82f988e0121035dd998aca2cb00ac5a6e895c8bc7203152772d059e749b188fc870be8cd92c9bffffffff02e3a55900000000001976a914ffe94f04dd435b7a2e9a4e9f5d3b069c85c4c36188ac6cff2b00000000001976a9142cdae2d3bce9f664581ac75b9fee898afd08c90c88ac0000000001000000f06e775402cefdf584b1b47f898ced5c9bb608c0d4d49a2e0d27d29617b5332dd64220b6b5000000006b48304502210090d662fdbd78b532fb0c9c4066d08a3127a11a8a5f1d61a343803edb84f329690220103cae52eafa2fb603d53d26ea535cd71ee0d6befeff3c67c3fc2f90e0bd937f0121025d51808deb8ec80820c604e6fa46cd826272fc35480366d21ac99fd1bcd674a6ffffffff4d0399290b081ae9c2ceb694969b7f517c321933aad6e5b6117a78b907beb102000000006c49304602210084c2f440052cbbf363a756f81137c8fa7611ee07c3712431e9e6791b15cd039a0221008a6eb74e305490fc38abecb9afd6fb0409f9542ea96ed8fe59b9d7d01847571f0121024a0c36831f4417cdf4d9f3680f4e4ced2de3065cef1cf3c45052774dd5955bb1ffffffff020f160c00000000001976a9147f661bc88cfb7132ee47b22dc8f1f10a5e122a1f88ac330a0503000000001976a914f6e8212bdac720f0af2ead4b0606fcb2549fe43388ac0000000001000000f06e7754011a50de6ba508b040601e876d025b99db02e9aa81b918918522344bff2b4f60b5000000006c493046022100c3bfea8ca6aa822d6ecad4ab4d90d95a33afd61e5db0a1829a5c6142f67a2406022100dc2dab9ddf52fc51d1b6864fe03cfab1b35a5310cbff1c09161b2e48408042b90121034046b0ebee8063df2211c0f46bbafc85869fc0b4822f0897b98cab713d894708ffffffff02217ef601000000001976a914bce72af2f5aa1dadc277e5aed27eb3993327ca0688acdec78100000000001976a91415276799a41e90cfe51a8c3a1da3481d8114de2788ac0000000001000000ef6e7754017a4f49f78b42654ae1f1821c8a5347c916dca4d345423ea4a37af8f2d5a23c8d000000006a473044022012d6962f98233b8cbf5740f08b728683de637c5cbd7f65fa99c941811bdf663802205d650ddcf9fdf20133b89553cc634f2fe281122ea62cbcb214902ec684856daa012102adf65a0c428d82faea73dbfabf29fbf4abb7930a00120e8dd14ec9e195647c98ffffffff02dd740e01000000001976a9145533133455f4549fc3946658f80596ba20055af688ac907c9800000000001976a91414353b5294a797fa8c3450fc7fe121f140a599b688ac0000000001000000ef6e7754019a0bb6b22dc02f2f96478f4ebe9a344102115799d6b0d84bd91b2534caabb6fc000000006c493046022100aafffb3c078a71298344a80b72e74e2d11b9647c205f3af1eb788f33c41bae4f022100ab1c5b7f970f69183ff55e01ceb31d6dd80041de89244de64953aa2c4054efe801210277641d4af3bf857357c118abf6819b4900ce1fab268dc13abf37612a90830aefffffffff0292976001000000001976a9148776fe715dbd8a2d8f74172c16e152a3bd39d17188ac33871200000000001976a9145e02e7795047e8a83ad1956462ffcc6a51ef724988ac0000000001000000ef6e775401f06b6a432ecb1f89472eb70b20b6b596698a30d0ce607a2453c1d60ba3059ea6000000006a4730440220036da96d15a8aae41883b195227a733a4413ebb30a94d1634192174f406c2ddb0220025aff61429af6396151054505bdacd38ee283bbc066d90d983ca63b361f9f430121036058f3159f93ec525c3a7e18a7174b646e8beb81c0cb2c8b004057e3642b9816ffffffff02040c2501000000001976a9141dc31d411f5242624e0644917f779e31cdcf703e88ac02891100000000001976a9143fdbf5db3031b9e99f1bb70a6102f260dac960e688ac0000000001000000ef6e775401bc3e2e6815e3955108eecb953cd71fa469b452ccdcc830117104352daa5c6fef000000006a47304402203ac05559534eb33e0c53a696deaf46d15b5232c340d39382ba68588e18f700f50220434f8729e5a8adade6262501b8bf0dcc0e51a351580ac3f9f0b26699fec7ff9d012103796ab300c41a51de465bc0654ad4e1c9d7924719704cd1becc5ede061b462bcfffffffff02a1877c00000000001976a91432ef92eccec1d54cc79550226d1b7b1ba4b597b388acb13b1200000000001976a914a9bd7e0b8a05583af0183f78b06b2e1bc8c77b8c88ac0000000001000000d96e77540182feef193bee1858fb6c99591ba64e7a93303e732401f2c3d8119dcba6532cf4000000006c493046022100818cea2621c839ce7870fc46d8a21873b014491952a46c592a0616029327042802210099bf2b19dcb8487916d3a19f6a9c5b0fcfd0db4de0bfab185f30edaab19952c7012103c638e82b06bc47da972fe3606fb1fa116de0e4af001325b10166533a02164324ffffffff02aefd0200000000001976a9144feb1059bf55ef4fd9776f4c9f79df46ffe8687888acad7a1900000000001976a91444e4c53860c3222883a79e60291c1b46a1d9857c88ac00000000473045022100c31e8f974ee9277363569a55dcadb8f2f990c3342c22dad92524018b58e407b702207184b5d63e0b130bb875296b162e849e3755735610f0e12e4cb8f1916809a1c2");
    }

    @Test
    public void testBlockVerification() throws Exception {
        Block block = new Block(params, blockBytes);
        block.verify();
        assertEquals("d189225fbecb12296349d548fef503bc095fbc827dfd442706e48b8965eea482", block.getHashAsString());
    }
    
    @SuppressWarnings("deprecation")
    @Test
    public void testDate() throws Exception {
        Block block = new Block(params, blockBytes);
        assertEquals("27 Nov 2014 18:39:43 GMT", block.getTime().toGMTString());
    }

    @Test
    public void testBadTransactions() throws Exception {
        Block block = new Block(params, blockBytes);
        // Re-arrange so the coinbase transaction is not first.
        Transaction tx1 = block.transactions.get(0);
        Transaction tx2 = block.transactions.get(1);
        block.transactions.set(0, tx2);
        block.transactions.set(1, tx1);
        try {
            block.verify();
            fail();
        } catch (VerificationException e) {
            // We should get here.
        }
    }

    @Test
    public void testHeaderParse() throws Exception {
        Block block = new Block(params, blockBytes);
        Block header = block.cloneAsHeader();
        Block reparsed = new Block(params, header.peercoinSerialize());
        assertEquals(reparsed, header);
    }

    @Test
    public void testPeercoinSerialization() throws Exception {
        // We have to be able to reserialize everything exactly as we found it for hashing to work. This test also
        // proves that transaction serialization works, along with all its subobjects like scripts and in/outpoints.
        //
        // NB: This tests the PEERCOIN proprietary serialization protocol. A different test checks Java serialization
        // of transactions.
        Block block = new Block(params, blockBytes);
        assertArrayEquals(blockBytes, block.peercoinSerialize());
    }

    @Test
    public void testJavaSerialiazation() throws Exception {
        Block block = new Block(params, blockBytes);
        Transaction tx = block.transactions.get(1);

        // Serialize using Java.
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(tx);
        oos.close();
        byte[] javaBits = bos.toByteArray();
        // Deserialize again.
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(javaBits));
        Transaction tx2 = (Transaction) ois.readObject();
        ois.close();

        // Note that this will actually check the transactions are equal by doing peercoin serialization and checking
        // the bytestreams are the same! A true "deep equals" is not implemented for Transaction. The primary purpose
        // of this test is to ensure no errors occur during the Java serialization/deserialization process.
        assertEquals(tx, tx2);
    }
    
    @Test
    public void testUpdateLength() {
        NetworkParameters params = UnitTestParams.get();
        Block block = params.getGenesisBlock().createNextBlockWithCoinbase(new ECKey().getPubKey());
        assertEquals(block.peercoinSerialize().length, block.length);
        final int origBlockLen = block.length;
        Transaction tx = new Transaction(params);
        // this is broken until the transaction has > 1 input + output (which is required anyway...)
        //assertTrue(tx.length == tx.peercoinSerialize().length && tx.length == 8);
        byte[] outputScript = new byte[10];
        Arrays.fill(outputScript, (byte) ScriptOpCodes.OP_FALSE);
        tx.addOutput(new TransactionOutput(params, null, Coin.SATOSHI, outputScript));
        tx.addInput(new TransactionInput(params, null, new byte[] {(byte) ScriptOpCodes.OP_FALSE},
                new TransactionOutPoint(params, 0, Sha256Hash.create(new byte[] {1}))));
        int origTxLength = 8 + 2 + 8 + 1 + 10 + 40 + 1 + 1 + 4; //ppcoin: 4 for timestamp
        assertEquals(tx.peercoinSerialize().length, tx.length);
        assertEquals(origTxLength, tx.length);
        block.addTransaction(tx);
        assertEquals(block.peercoinSerialize().length, block.length);
        assertEquals(origBlockLen + tx.length, block.length);
        block.getTransactions().get(1).getInputs().get(0).setScriptBytes(new byte[] {(byte) ScriptOpCodes.OP_FALSE, (byte) ScriptOpCodes.OP_FALSE});
        assertEquals(block.length, origBlockLen + tx.length);
        assertEquals(tx.length, origTxLength + 1);
        block.getTransactions().get(1).getInputs().get(0).setScriptBytes(new byte[] {});
        assertEquals(block.length, block.peercoinSerialize().length);
        assertEquals(block.length, origBlockLen + tx.length);
        assertEquals(tx.length, origTxLength - 1);
        block.getTransactions().get(1).addInput(new TransactionInput(params, null, new byte[] {(byte) ScriptOpCodes.OP_FALSE},
                new TransactionOutPoint(params, 0, Sha256Hash.create(new byte[] {1}))));
        assertEquals(block.length, origBlockLen + tx.length);
        assertEquals(tx.length, origTxLength + 41); // - 1 + 40 + 1 + 1
    }
}

