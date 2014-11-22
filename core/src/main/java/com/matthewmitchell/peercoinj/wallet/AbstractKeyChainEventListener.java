package com.matthewmitchell.peercoinj.wallet;

import com.matthewmitchell.peercoinj.core.ECKey;

import java.util.List;

public class AbstractKeyChainEventListener implements KeyChainEventListener {
    @Override
    public void onKeysAdded(List<ECKey> keys) {
    }
}

