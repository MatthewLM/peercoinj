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

package io.xpydev.paycoinj.params;

import io.xpydev.paycoinj.core.Block;
import io.xpydev.paycoinj.core.NetworkParameters;

import java.math.BigInteger;

/**
 * Network parameters used by the peercoinj unit tests (and potentially your own). This lets you solve a block using
 * {@link io.xpydev.paycoinj.core.Block#solve()} by setting difficulty to the easiest possible.
 */
public class UnitTestParams extends NetworkParameters {
    public UnitTestParams() {
        super();
        id = ID_UNITTESTNET;
        packetMagic = 0x0b110907;
        addressHeader = 55;
        p2shHeader = 117;
        acceptableAddressCodes = new int[] { addressHeader, p2shHeader };
        maxTarget = new BigInteger("00ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff", 16);
        genesisBlock.setTime(System.currentTimeMillis() / 1000);
        genesisBlock.setDifficultyTarget(Block.EASIEST_DIFFICULTY_TARGET);
        genesisBlock.solve();
        port = 18333;
        interval = 10;
        dumpedPrivateKeyHeader = 239;
        targetTimespan = 200000000;  // 6 years. Just a very big number.
        spendableCoinbaseDepth = 5;
        dnsSeeds = null;
    }

    private static UnitTestParams instance;
    public static synchronized UnitTestParams get() {
        if (instance == null) {
            instance = new UnitTestParams();
        }
        return instance;
    }

    @Override
    public String getPaymentProtocolId() {
        return null;
    }
}
