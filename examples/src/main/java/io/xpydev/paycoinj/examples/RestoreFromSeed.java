package io.xpydev.paycoinj.examples;

import io.xpydev.paycoinj.core.*;
import io.xpydev.paycoinj.net.discovery.DnsDiscovery;
import io.xpydev.paycoinj.params.MainNetParams;
import io.xpydev.paycoinj.store.SPVBlockStore;
import io.xpydev.paycoinj.store.ValidHashStore;
import io.xpydev.paycoinj.wallet.DeterministicSeed;

import java.io.File;

/**
 * The following example shows you how to restore a HD wallet from a previously generated deterministic seed.
 * In this example we manually setup the blockchain, peer group, etc. You can also use the WalletAppKit which provides a restoreWalletFromSeed function to load a wallet from a deterministic seed.
 */
public class RestoreFromSeed {

    public static void main(String[] args) throws Exception {
        NetworkParameters params = MainNetParams.get();

        // Peercoinj supports hierarchical deterministic wallets (or "HD Wallets"): https://github.com/peercoin/bips/blob/master/bip-0032.mediawiki
        // HD wallets allow you to restore your wallet simply from a root seed. This seed can be represented using a short mnemonic sentence as described in BIP 39: https://github.com/peercoin/bips/blob/master/bip-0039.mediawiki

        // Here we restore our wallet from a seed with no passphrase. Also have a look at the BackupToMnemonicSeed.java example that shows how to backup a wallet by creating a mnemonic sentence.
        String seedCode = "yard impulse luxury drive today throw farm pepper survey wreck glass federal";
        String passphrase = "";
        Long creationtime = 1409478661L;

        DeterministicSeed seed = new DeterministicSeed(seedCode, null, passphrase, creationtime);

        // The wallet class provides a easy fromSeed() function that loads a new wallet from a given seed.
        Wallet wallet = Wallet.fromSeed(params, seed);

        // Because we are importing an existing wallet which might already have transactions we must re-download the blockchain to make the wallet picks up these transactions
        // You can find some information about this in the guides: https://peercoinj.github.io/working-with-the-wallet#setup
        // To do this we clear the transactions of the wallet and delete a possible existing blockchain file before we download the blockchain again further down.
        System.out.println(wallet.toString());
        wallet.clearTransactions(0);
        File chainFile = new File("restore-from-seed.spvchain");
        if (chainFile.exists()) {
            chainFile.delete();
        }

        // Setting up the BlochChain, the BlocksStore and connecting to the network.
        SPVBlockStore chainStore = new SPVBlockStore(params, chainFile);
        ValidHashStore validHashStore = new ValidHashStore(new File("."));
        BlockChain chain = new BlockChain(params, chainStore, validHashStore);
        PeerGroup peers = new PeerGroup(params, chain);
        peers.addPeerDiscovery(new DnsDiscovery(params));

        // Now we need to hook the wallet up to the blockchain and the peers. This registers event listeners that notify our wallet about new transactions.
        chain.addWallet(wallet);
        peers.addWallet(wallet);

        DownloadListener bListener = new DownloadListener() {
            @Override
            public void doneDownload() {
                System.out.println("blockchain downloaded");
            }
        };

        // Now we re-download the blockchain. This replays the chain into the wallet. Once this is completed our wallet should know of all its transactions and print the correct balance.
        peers.startAsync();
        peers.awaitRunning();
        peers.startBlockChainDownload(bListener);

        bListener.await();

        // Print a debug message with the details about the wallet. The correct balance should now be displayed.
        System.out.println(wallet.toString());

        // shutting down again
        peers.stopAsync();
        peers.awaitTerminated();

    }
}
