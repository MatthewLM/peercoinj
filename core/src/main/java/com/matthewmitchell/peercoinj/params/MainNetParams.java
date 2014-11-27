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

package com.matthewmitchell.peercoinj.params;

import com.matthewmitchell.peercoinj.core.NetworkParameters;
import com.matthewmitchell.peercoinj.core.Sha256Hash;
import com.matthewmitchell.peercoinj.core.Utils;

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
        packetMagic= 0xe6e8e9e5L;
        genesisBlock.setDifficultyTarget(0x1d00ffffL);
        genesisBlock.setTime(1345084287L);
        genesisBlock.setNonce(2179302059L);
        id = ID_MAINNET;
        spendableCoinbaseDepth = 500;
        String genesisHash = genesisBlock.getHashAsString();
        checkState(genesisHash.equals("0000000032fe677166d54963b62a4677d8957e87c508eaa4fd7eb1c880cd27e3"), genesisHash);

        checkpoints.put(19080, new Sha256Hash("000000000000bca54d9ac17881f94193fd6a270c1bb21c3bf0b37f588a40dbd7"));
        checkpoints.put(30583, new Sha256Hash("d39d1481a7eecba48932ea5913be58ad3894c7ee6d5a8ba8abeb772c66a6696e"));
        checkpoints.put(99999, new Sha256Hash("27fd5e1de16a4270eb8c68dee2754a64da6312c7c3a0e99a7e6776246be1ee3f"));

        dnsSeeds = new String[] {
                "seed.ppcoin.net", "tnseed.ppcoin.net"
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
