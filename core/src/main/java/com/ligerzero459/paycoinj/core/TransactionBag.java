/**
 * Copyright 2014 Giannis Dzegoutanis
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

import com.ligerzero459.paycoinj.script.Script;
import com.ligerzero459.paycoinj.wallet.WalletTransaction;

import java.util.Map;

/**
 * This interface is used to abstract the {@link com.ligerzero459.paycoinj.core.Wallet} and the {@link com.ligerzero459.paycoinj.core.Transaction}
 */
public interface TransactionBag {
    /** Returns true if this wallet contains a public key which hashes to the given hash. */
    public boolean isPubKeyHashMine(byte[] pubkeyHash);

    /** Returns true if this wallet is watching transactions for outputs with the script. */
    public boolean isWatchedScript(Script script);

    /** Returns true if this wallet contains a keypair with the given public key. */
    public boolean isPubKeyMine(byte[] pubkey);

    /** Returns true if this wallet knows the script corresponding to the given hash. */
    public boolean isPayToScriptHashMine(byte[] payToScriptHash);

    /** Returns transactions from a specific pool. */
    public Map<Sha256Hash, Transaction> getTransactionPool(WalletTransaction.Pool pool);
}

