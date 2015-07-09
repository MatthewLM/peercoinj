/*
 * Copyright 2011 Google Inc.
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
import com.ligerzero459.paycoinj.core.PeerAddress;
import com.ligerzero459.paycoinj.core.Utils;
import org.junit.Test;

import java.net.InetAddress;

import static com.ligerzero459.paycoinj.core.Utils.HEX;
import static org.junit.Assert.assertEquals;

public class PeerAddressTest
{
    @Test
    public void testPeerAddressRoundtrip() throws Exception {
        // copied verbatim from https://en.bitcoin.it/wiki/Protocol_specification#Network_address
        String fromSpec = "010000000000000000000000000000000000ffff0a000001208d";
        PeerAddress pa = new PeerAddress(MainNetParams.get(),
                HEX.decode(fromSpec), 0, 0);
        String reserialized = HEX.encode(pa.paycoinSerialize());
        assertEquals(reserialized,fromSpec );
    }

    @Test
    public void testpaycoinSerialize() throws Exception {
        PeerAddress pa = new PeerAddress(InetAddress.getByName(null), 8333, 0);
        assertEquals("000000000000000000000000000000000000ffff7f000001208d",
                Utils.HEX.encode(pa.paycoinSerialize()));
    }
}
