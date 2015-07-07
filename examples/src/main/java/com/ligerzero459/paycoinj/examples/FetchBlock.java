/*
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

package com.ligerzero459.paycoinj.examples;

import com.ligerzero459.paycoinj.core.*;
import com.ligerzero459.paycoinj.params.MainNetParams;
import com.ligerzero459.paycoinj.store.BlockStore;
import com.ligerzero459.paycoinj.store.MemoryBlockStore;
import com.ligerzero459.paycoinj.store.ValidHashStore;
import com.ligerzero459.paycoinj.utils.BriefLogFormatter;
import java.io.File;

import java.net.InetAddress;
import java.util.concurrent.Future;

/**
 * Downloads the block given a block hash from the localhost node and prints it out.
 */
public class FetchBlock {
    public static void main(String[] args) throws Exception {
        BriefLogFormatter.init();
        System.out.println("Connecting to node");
        final NetworkParameters params = MainNetParams.get();

        BlockStore blockStore = new MemoryBlockStore(params);
        ValidHashStore validHashStore = new ValidHashStore(new File("."));
        BlockChain chain = new BlockChain(params, blockStore, validHashStore);
        PeerGroup peerGroup = new PeerGroup(params, chain);
        peerGroup.startAsync();
        peerGroup.awaitRunning();
        PeerAddress addr = new PeerAddress(InetAddress.getLocalHost(), params.getPort());
        peerGroup.addAddress(addr);
        peerGroup.waitForPeers(1).get();
        Peer peer = peerGroup.getConnectedPeers().get(0);

        Sha256Hash blockHash = new Sha256Hash(args[0]);
        Future<Block> future = peer.getBlock(blockHash);
        System.out.println("Waiting for node to send us the requested block: " + blockHash);
        Block block = future.get();
        System.out.println(block);
        peerGroup.stopAsync();
    }
}
