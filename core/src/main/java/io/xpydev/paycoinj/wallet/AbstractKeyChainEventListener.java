package io.xpydev.paycoinj.wallet;

import io.xpydev.paycoinj.core.ECKey;

import java.util.List;

public class AbstractKeyChainEventListener implements KeyChainEventListener {
    @Override
    public void onKeysAdded(List<ECKey> keys) {
    }
}

