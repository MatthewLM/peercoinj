package com.matthewmitchell.peercoinj.tools;

import com.matthewmitchell.peercoinj.core.*;
import com.matthewmitchell.peercoinj.params.MainNetParams;
import com.matthewmitchell.peercoinj.store.*;
import com.matthewmitchell.peercoinj.utils.BlockFileLoader;
import com.google.common.base.Preconditions;

import java.io.File;
import java.io.IOException;

/** Very thin wrapper around {@link com.matthewmitchell.peercoinj.utils.BlockFileLoader} */
public class BlockImporter {
    public static void main(String[] args) throws BlockStoreException, VerificationException, PrunedException, IOException {
        System.out.println("USAGE: BlockImporter prod (Mem|SPV) validHashStore [blockStore]");
        System.out.println("       blockStore is required unless type is Mem or MemFull");
        System.out.println("       eg BlockImporter prod H2 /home/user/peercoinj.h2store");
        System.out.println("       Does full verification if the store supports it");
        Preconditions.checkArgument(args.length == 2 || args.length == 3);
        
        NetworkParameters params;
        params = MainNetParams.get();
        
        BlockStore store;
        if (args[1].equals("Mem")) {
            Preconditions.checkArgument(args.length == 3);
            store = new MemoryBlockStore(params);
        } else if (args[1].equals("SPV")) {
            Preconditions.checkArgument(args.length == 4);
            store = new SPVBlockStore(params, new File(args[3]));
        } else {
            System.err.println("Unknown store " + args[1]);
            return;
        }
        
        ValidHashStore validHashStore = new ValidHashStore(new File(args[2]));
        AbstractBlockChain chain = new BlockChain(params, store, validHashStore);
        
        BlockFileLoader loader = new BlockFileLoader(params, BlockFileLoader.getReferenceClientBlockFileList());
        
        for (Block block : loader)
            chain.add(block);
    }
}
