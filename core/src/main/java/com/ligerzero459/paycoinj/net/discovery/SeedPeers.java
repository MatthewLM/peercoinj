/**
 * Copyright 2011 Micheal Swiggs
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
package com.ligerzero459.paycoinj.net.discovery;

import com.ligerzero459.paycoinj.core.NetworkParameters;

import javax.annotation.Nullable;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

/**
 * SeedPeers stores a pre-determined list of Paycoin node addresses. These nodes are selected based on being
 * active on the network for a long period of time. The intention is to be a last resort way of finding a connection
 * to the network, in case IRC and DNS fail. The list comes from the Paycoin C++ source code.
 */
public class SeedPeers implements PeerDiscovery {
    private NetworkParameters params;
    private int pnseedIndex;

    public SeedPeers(NetworkParameters params) {
        this.params = params;
    }

    /**
     * Acts as an iterator, returning the address of each node in the list sequentially.
     * Once all the list has been iterated, null will be returned for each subsequent query.
     *
     * @return InetSocketAddress - The address/port of the next node.
     * @throws PeerDiscoveryException
     */
    @Nullable
    public InetSocketAddress getPeer() throws PeerDiscoveryException {
        try {
            return nextPeer();
        } catch (UnknownHostException e) {
            throw new PeerDiscoveryException(e);
        }
    }

    @Nullable
    private InetSocketAddress nextPeer() throws UnknownHostException {
        if (pnseedIndex >= seedAddrs.length) return null;
        return new InetSocketAddress(convertAddress(seedAddrs[pnseedIndex++]),
                params.getPort());
    }

    /**
     * Returns an array containing all the Paycoin nodes within the list.
     */
    @Override
    public InetSocketAddress[] getPeers(long timeoutValue, TimeUnit timeoutUnit) throws PeerDiscoveryException {
        try {
            return allPeers();
        } catch (UnknownHostException e) {
            throw new PeerDiscoveryException(e);
        }
    }

    private InetSocketAddress[] allPeers() throws UnknownHostException {
        InetSocketAddress[] addresses = new InetSocketAddress[seedAddrs.length];
        for (int i = 0; i < seedAddrs.length; ++i) {
            addresses[i] = new InetSocketAddress(convertAddress(seedAddrs[i]), params.getPort());
        }
        return addresses;
    }

    private InetAddress convertAddress(int seed) throws UnknownHostException {
        byte[] v4addr = new byte[4];
        v4addr[0] = (byte) (0xFF & (seed));
        v4addr[1] = (byte) (0xFF & (seed >> 8));
        v4addr[2] = (byte) (0xFF & (seed >> 16));
        v4addr[3] = (byte) (0xFF & (seed >> 24));
        return InetAddress.getByAddress(v4addr);
    }

    public static int[] seedAddrs =
            {
                    0x1c4e9a68, 0xe866ec68, 0x403fec68, 0x83b4ee68, 0xed192d68, 0x66e1ed69, 0xc13ebf6b, 
                    0xb08fdc6b, 0xa01cda6c, 0xcab89f6e, 0x877ca072, 0x0e0b4073, 0x41aae40c, 0x08aa9778, 
                    0x5b2c3679, 0x8f80a87c, 0x02f57589, 0xd5a3cb0e, 0x34b8fa98, 0xfcf5ff9f, 0x58c3d5a2, 
                    0x004215ad, 0x2141f5ad, 0xd0c035ae, 0x44685bae, 0x0ea960ae, 0xf90a18b2, 0xd042a7b8, 
                    0x0c6641b8, 0xc1f160b8, 0x2e0111b9, 0x5f35a6bc, 0x508413bd, 0xfd83cebd, 0x6b2f08bf, 
                    0x15f563c0, 0x95f9d2c4, 0x4945edc5, 0x369a56c5, 0x789e32c6, 0xe55cedd1, 0xa33a81d4, 
                    0xed5ce3d4, 0x37dbe2d5, 0x14b505d5, 0x053e3ad8, 0xf34d81d9, 0x41b6d9d9, 0x825c84dd, 
                    0xb202f117, 0x9e875c17, 0x188e9618, 0xb7729f18, 0xb28fcd18, 0xac65d618, 0x432fef18, 
                    0xd2862b18, 0xa983bb25, 0xa611fa25, 0xbba57926, 0x0e244b26, 0x4655c429, 0x6167fb05, 
                    0xa2698a32, 0xe1359732, 0x580cb932, 0xa514bf32, 0x19241e32, 0xdfe74832, 0x27465932, 
                    0x17a50134, 0x10fe0134, 0x1e8e0634, 0xfa1f0634, 0x766faf36, 0x9d3ba73b, 0x982ae13c, 
                    0x90bf3142, 0x326b9544, 0x4a598d45, 0x16199145, 0x641bf845, 0xbb274045, 0xbedd1046, 
                    0xded3c547, 0x3afbd447, 0x2da6f447, 0x380f4447, 0xdddc4f48, 0x3a498249, 0xca91a349, 
                    0x0aca864a, 0xcde0864a, 0x632e874a, 0x7148ab4c, 0xe437b34c, 0x1e46b54c, 0xa3a5bd4c, 
                    0x36c94f4c, 0x256e2b4e, 0x6ebec050, 0x91c65251, 0x36f6f353, 0xdf131854, 0xde605d54, 
                    0x2209aa55, 0x74c71855, 0x4d115955, 0xde448156, 0x0678a056, 0x8de76e59, 0xce2ecd59, 
                    0x99605759, 0x1da8795b, 0x7eefec5b, 0x365e525b, 0x67549c5d, 0xfa1b255d, 0xe400ae5e, 
                    0x1aaac65e, 0xfca6115f, 0x73481360, 0xdfd6c962, 0xe874f263, 0x34bbf763, 0xd9adf963, 
                    0x0bc32264, 0x807b7f65, 0x1e058368,
            };
    
    @Override
    public void shutdown() {
    }
}
