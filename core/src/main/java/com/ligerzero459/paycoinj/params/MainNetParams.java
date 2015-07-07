/*
 * Copyright 2013 Google Inc.
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

package com.ligerzero459.paycoinj.params;

import com.ligerzero459.paycoinj.core.NetworkParameters;
import com.ligerzero459.paycoinj.core.Sha256Hash;
import com.ligerzero459.paycoinj.core.Utils;

import static com.google.common.base.Preconditions.checkState;

/**
 * Parameters for the main production network on which people trade goods and services.
 */
public class MainNetParams extends NetworkParameters {
    public MainNetParams() {
        super();
        interval = INTERVAL;
        targetTimespan = TARGET_TIMESPAN;
        maxTarget = Utils.decodeCompactBits(0x1d00ffffL);
        dumpedPrivateKeyHeader = 183;
        addressHeader = 55;
        p2shHeader = 117;
        acceptableAddressCodes = new int[] { addressHeader, p2shHeader };
        port = 9901;
        packetMagic= 0xaaaaaaaaL;
        genesisBlock.setDifficultyTarget(0x1e0fffffL);
        genesisBlock.setTime(1417219210L);
        genesisBlock.setNonce(716560L);
        id = ID_MAINNET;
        spendableCoinbaseDepth = 500;
        String genesisHash = genesisBlock.getHashAsString();
        checkState(genesisHash.equals("00000e5695fbec8e36c10064491946ee3b723a9fa640fc0e25d3b8e4737e53e3"), genesisHash);

        checkpoints.put(276, new Sha256Hash("0000000000000073a6b02412fc261414fde6304366ca7d90cc0c175516d52272"));
        checkpoints.put(6888, new Sha256Hash("00000000000000077ae1fcd6f73b21cb62e1410db57f4a5decb2728cdfc2df84"));
        checkpoints.put(7880, new Sha256Hash("0000000000001017d14badb06d831f101d94c823715113b7b6b11b7649250ce6"));
        checkpoints.put(8047, new Sha256Hash("94e1cf0e8ced93a0f4b34d4845493d07b7c2fb1b92d1e9d2681464bdc2547728"));
        checkpoints.put(8048, new Sha256Hash("0a909aca8cf1c12b412d286c88f1e41ceab5336a231772b8da05022c5f4c544c"));
        checkpoints.put(8049, new Sha256Hash("28a3efae5e2295ff571943e6b7906f4ac0e7b9c17cad90715d13de2613a71b29"));
        checkpoints.put(8050, new Sha256Hash("56e81e95c989a4c18151c19a0e53d3cbc39314f933eea6f16e0eaa1b2529d027"));
        checkpoints.put(8051, new Sha256Hash("dc26f8766497644ba86ff8bb8bb13b8be3912299af1046f0e9e0488b393172d0"));
        checkpoints.put(8052, new Sha256Hash("0fc44b803dc4d8572980e3c3437bc9b8f0a00d2583ad8792ac76fcd1299793af"));
        checkpoints.put(8053, new Sha256Hash("b543bb95ad48dbc1bcf76c100ab5f7fcdc68b4f57e15bfcad6091680446c51bc"));
        checkpoints.put(8054, new Sha256Hash("e40395afad71babe79627563f0f1401ca1f4d082eebc6e7576d7fe8445ac7606"));
        checkpoints.put(8055, new Sha256Hash("24046864d432565d46481b469611d0f97b9ac0fd3dff0a8f71fb7d8e3a983994"));
        checkpoints.put(50000, new Sha256Hash("41f755dea72e670055b705f50bdb0ab790dd9c4aff2c9b7b79faffdd7a73843c"));
        checkpoints.put(50001, new Sha256Hash("47fabbdfaf07959435ef810cdd8df7f7a27ca47b9dee6a55e441ce07eddd9ffe"));
        checkpoints.put(50002, new Sha256Hash("ba831699caae7b8ba109db7aa0a3d78fdd6242b2033ba275e6c1a7bde182b1b5"));
        checkpoints.put(50003, new Sha256Hash("ed192804d7f7ff66ec9ac25f51e8663a81dfbeec135d8f9bdbeb583316fa67b1"));
        checkpoints.put(50004, new Sha256Hash("f08445ee30d51cb872bd09c9a1cf89a31a3a100cfa8ac0965c324696b204cca7"));
        checkpoints.put(50005, new Sha256Hash("f9134f2047e4aa45aa2735443d23bf872d6b4c9cc8177602dfa1eab8f6aadb78"));
        checkpoints.put(50006, new Sha256Hash("4724c3034aa1bc54d1f4e383b17e5d3a9dd4764f455fddfa7ba54fb86a468440"));
        checkpoints.put(50007, new Sha256Hash("45612abb33f82c02f1b2eced3784f17f300ce988c3cf87dd34266f62a7b80934"));
        checkpoints.put(50008, new Sha256Hash("18ceab3202f1f7e1208fc8241de41c47265dc5f5b0599130b06a26da9139e3d6"));
        checkpoints.put(52500, new Sha256Hash("5241240023621728f1db66ea054d43dc019a721199b928b7b1dd6864bc161ea2"));
        checkpoints.put(55002, new Sha256Hash("c097ca76d17aaaa627a2232d27fbd1c818fd26591a0a913031e3e69490b1bdf4"));
        checkpoints.put(57500, new Sha256Hash("e279228f63471dbee3b4412e13934926a2e25e0995b3ec212a0608907da6a97d"));
        checkpoints.put(60000, new Sha256Hash("417ed8dee6e991aa46bbd395f95a1d39f9e501d74c93d46be422725659929f74"));
        checkpoints.put(62500, new Sha256Hash("1244aca23d2c49340faab580cc0f02db0fe59f8034a09b35e8f07151c26a3c94"));
        checkpoints.put(65000, new Sha256Hash("bee8277e4f19a5544d1803caf657e5653abbbaea09dc212ec864c4a4a3672a7e"));
        checkpoints.put(67500, new Sha256Hash("4396e62e9e37f6c204888b92dee34718b45500e56f69d2c539c433bbf4058ed5"));
        checkpoints.put(70000, new Sha256Hash("cd11d1a944de0760ad4fd0ccdb76001b1afdda90a8bc3f2026c7ff2c1cab0a56"));
        checkpoints.put(72500, new Sha256Hash("ab17834d97c22e100f3e79f6fd617d616247330b3b515984517f3a762d8b8a05"));
        checkpoints.put(75000, new Sha256Hash("61cefd4b5f190250292f1d61747ff2fc990084f9874dbea09c11cdac9463e0b3"));
        checkpoints.put(77500, new Sha256Hash("97a87ba804a824a1ce4a198319fd1d12ba375ad64cb3f2c9b1395c8a6766cdd2"));
        checkpoints.put(80000, new Sha256Hash("de942516cb95331ec8090ecc211aa78ab5e78baaf34df2d2e94219fa3e8abef6"));
        checkpoints.put(82500, new Sha256Hash("0ede1320d4d054b61e2edce28dd9944136a63b6e8c1ad865bee75f605c738d62"));
        checkpoints.put(85000, new Sha256Hash("225e5c2910f22b322f30f4c79e3c0a7dfa17c89b6d7a656982474fbf7cba2c99"));
        checkpoints.put(87500, new Sha256Hash("ad8752cccfab86c7e04385c669c5fa835505ec33786aba4e94210354ca52551d"));
        checkpoints.put(90000, new Sha256Hash("de8abf266c7b734bf734e27e61d613a4807077e64617acb3fdc5cd17257eaf4e"));
        checkpoints.put(92500, new Sha256Hash("411d07045281cfacb06f42e27cf1e9e74fe2acfd071fc274d1ac4b7c55d8193d"));
        checkpoints.put(95000, new Sha256Hash("05764957f120b162e870de399fd8a0674c88337d3eda0851cce3cb53a8c716c8"));
        checkpoints.put(100000, new Sha256Hash("3e57c57d272ceae1285c40cbb741bc7087915c00fbb5a572da3f6c5a3dbcc17c"));
        checkpoints.put(150000, new Sha256Hash("a555e059996273d721ccae4d6520b502cc26840244e529f2adf8404581971f71"));
        checkpoints.put(200000, new Sha256Hash("1b395fd2bd19f5d89fbabc9a98224fcfff94dc531805a1dba0ec02358ee3ba31"));
        checkpoints.put(210000, new Sha256Hash("7252634dc1c8d06b8f41dbfb577273e08171db3ecdb26dc09c239daba8ee180f"));

        dnsSeeds = new String[] {
                "dnsseed.paycoin.com"
              , "dnsseed.paycoinfoundation.org"
              , "dnsseed.xpydev.org"
        };
    }

    private static MainNetParams instance;
    public static synchronized MainNetParams get() {
        if (instance == null) {
            instance = new MainNetParams();
        }
        return instance;
    }

    @Override
    public String getPaymentProtocolId() {
        return PAYMENT_PROTOCOL_ID_MAINNET;
    }
}
