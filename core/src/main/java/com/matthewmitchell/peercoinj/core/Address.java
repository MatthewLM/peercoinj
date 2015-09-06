/**
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

package com.matthewmitchell.peercoinj.core;

import com.matthewmitchell.peercoinj.params.Networks;
import com.matthewmitchell.peercoinj.script.Script;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * <p>A Peercoin address looks like 1MsScoe2fTJoq4ZPdQgqyhgWeoNamYPevy and is derived from an elliptic curve public key
 * plus a set of network parameters. Not to be confused with a {@link PeerAddress} or {@link AddressMessage}
 * which are about network (TCP) addresses.</p>
 *
 * <p>A standard address is built by taking the RIPE-MD160 hash of the public key bytes, with a version prefix and a
 * checksum suffix, then encoding it textually as base58. The version prefix is used to both denote the network for
 * which the address is valid (see {@link NetworkParameters}, and also to indicate how the bytes inside the address
 * should be interpreted. Whilst almost all addresses today are hashes of public keys, another (currently unsupported
 * type) can contain a hash of a script instead.</p>
 */
public class Address extends VersionedChecksummedBytes {
    /**
     * An address is a RIPEMD160 hash of a public key, therefore is always 160 bits or 20 bytes.
     */
    public static final int LENGTH = 20;

    transient final List<NetworkParameters> params;

    /**
     * Construct an address from a list of parameters, the address version, and the hash160 form. Example:<p>
     *
     * <pre>new Address(Arrays.asList(NetworkParameters.prodNet()), NetworkParameters.getAddressHeader(), Hex.decode("4a22c3c4cbb31e4d03b15550636762bda0baf85a"));</pre>
     */
    public Address(List<NetworkParameters> paramsList, int version, byte[] hash160) throws WrongNetworkException {

        super(version, hash160);
        checkNotNull(paramsList);
        checkArgument(hash160.length == 20, "Addresses are 160-bit hashes, so you must provide 20 bytes");

        for (NetworkParameters params: paramsList) {
            if (!isAcceptableVersion(params, version))
                throw new WrongNetworkException(version, params.getAcceptableAddressCodes());
        }

        this.params = paramsList;

    }

    /**
     * Construct an address from parameters, the address version, and the hash160 form. Example:<p>
     *
     * <pre>new Address(NetworkParameters.prodNet(), NetworkParameters.getAddressHeader(), Hex.decode("4a22c3c4cbb31e4d03b15550636762bda0baf85a"));</pre>
     */
    public Address(NetworkParameters params, int version, byte[] hash160) throws WrongNetworkException {
        this(Arrays.asList(params), version, hash160);	
    }

    /** Returns an Address that represents the given P2SH script hash. */
    public static Address fromP2SHHash(NetworkParameters params, byte[] hash160) {
        try {
            return new Address(params, params.getP2SHHeader(), hash160);
        } catch (WrongNetworkException e) {
            throw new RuntimeException(e);  // Cannot happen.
        }
    }

    /** Returns an Address that represents the script hash extracted from the given scriptPubKey */
    public static Address fromP2SHScript(NetworkParameters params, Script scriptPubKey) {
        checkArgument(scriptPubKey.isPayToScriptHash(), "Not a P2SH script");
        return fromP2SHHash(params, scriptPubKey.getPubKeyHash());
    }

    /**
     * Construct an address from parameters and the hash160 form. Example:<p>
     *
     * <pre>new Address(Arrays.asList(NetworkParameters.prodNet()), Hex.decode("4a22c3c4cbb31e4d03b15550636762bda0baf85a"));</pre>
     */
    public Address(List<NetworkParameters> params, byte[] hash160) {
        super(params.get(0).getAddressHeader(), hash160);
        checkArgument(hash160.length == 20, "Addresses are 160-bit hashes, so you must provide 20 bytes");
        this.params = params;
    }

    public Address(NetworkParameters params, byte[] hash160) {
        this(Arrays.asList(params), hash160);
    }

    /**
     * Construct an address from a list of parameters and the standard "human readable" form. Example:<p>
     *
     * <pre>new Address(Arrays.asList(NetworkParameters.prodNet()), "17kzeh4N8g49GFvdDzSf8PjaPfyoD1MndL");</pre><p>
     *
     * @param paramsList The expected NetworkParameters or null if you don't want validation.
     * @param address The textual form of the address, such as "17kzeh4N8g49GFvdDzSf8PjaPfyoD1MndL"
     * @throws AddressFormatException if the given address doesn't parse or the checksum is invalid
     * @throws WrongNetworkException if the given address is valid but for a different chain (eg testnet vs prodnet)
     */
    public Address(@Nullable List<NetworkParameters> paramsList, String address) throws AddressFormatException {

        super(address);

        if (paramsList != null) {

            for (NetworkParameters params: paramsList) {
                if (!isAcceptableVersion(params, version)) {
                    throw new WrongNetworkException(version, params.getAcceptableAddressCodes());
                }
            }

            this.params = paramsList;

        } else {

            ArrayList<NetworkParameters> paramsFound = new ArrayList<NetworkParameters>();

            for (NetworkParameters p : Networks.get())
                if (isAcceptableVersion(p, version))
                    paramsFound.add(p);

            if (paramsFound.isEmpty())
                throw new AddressFormatException("No network found for " + address);

            this.params = paramsFound;

        }

    }

    /**
     * Construct an address from a standard "human readable" form. Example:<p>
     *
     * <pre>new Address("17kzeh4N8g49GFvdDzSf8PjaPfyoD1MndL");</pre><p>
     *
     * @param address The textual form of the address, such as "17kzeh4N8g49GFvdDzSf8PjaPfyoD1MndL"
     * @throws AddressFormatException if the given address doesn't parse or the checksum is invalid
     * @throws WrongNetworkException if the given address is valid but for a different chain (eg testnet vs prodnet)
     */
    public Address(String address) throws AddressFormatException {
        this((List<NetworkParameters>) null, address);
    }

    /**
     * Construct an address from parameters and the standard "human readable" form. Example:<p>
     *
     * <pre>new Address(NetworkParameters.prodNet(), "17kzeh4N8g49GFvdDzSf8PjaPfyoD1MndL");</pre><p>
     *
     * @param params The expected NetworkParameters or null if you don't want validation.
     * @param address The textual form of the address, such as "17kzeh4N8g49GFvdDzSf8PjaPfyoD1MndL"
     * @throws AddressFormatException if the given address doesn't parse or the checksum is invalid
     * @throws WrongNetworkException if the given address is valid but for a different chain (eg testnet vs prodnet)
     */
    public Address(@Nullable NetworkParameters params, String address) throws AddressFormatException {
        this(params == null ? null : Arrays.asList(params), address);
    }

    /** The (big endian) 20 byte hash that is the core of a Peercoin address. */
    public byte[] getHash160() {
        return bytes;
    }

    /*
     * Returns true if this address is a Pay-To-Script-Hash (P2SH) address for the given network.
     * See also https://github.com.matthewmitchell/bips/blob/master/bip-0013.mediawiki: Address Format for pay-to-script-hash
     */
    public boolean isP2SHAddress(NetworkParameters params) {
        return params != null && this.version == params.p2shHeader;
    }

    public boolean isSelectedP2SHAddress() {
        return isP2SHAddress(params.get(0));
    }

    /**
     * Examines the version byte of the address and attempts to find the matching NetworkParameters. If you aren't sure
     * which network the address is intended for (eg, it was provided by a user), you can use this to decide if it is
     * compatible with the current wallet. You should be able to handle a null response from this method. Note that the
     * parameters returned is not necessarily the same as the one the Address was created with.
     *
     * @return a list of NetworkParameters representing the networks the address is intended for, or null if unknown.
     */
    public List<NetworkParameters> getParameters() {
        return params;
    }

    /**
     * Examines the version byte of the address and attempts to find the matching NetworkParameters. This is similar to
     * getParameters() but returns the first NetworkParameters in the list.
     *
     * @return a NetworkParameters representing the network the address is intended for, or null if unknown.
     */
    public NetworkParameters getSelectedParameters() {

        if (params == null)
            return null;

        return params.get(0);

    }

    /**
     * Given an address, examines the version byte and attempts to find matching NetworkParameters. If you aren't sure
     * which network the address is intended for (eg, it was provided by a user), you can use this to decide if it is
     * compatible with the current wallet.
     * @return a NetworkParameters or null if the string wasn't of a known version.
     */
    @Nullable
    public static List<NetworkParameters> getParametersFromAddress(String address) throws AddressFormatException {
        try {
            return new Address(address).getParameters();
        } catch (WrongNetworkException e) {
            throw new RuntimeException(e);  // Cannot happen.
        }
    }

    /**
     * Check if a given address version is valid given the NetworkParameters.
     */
    private static boolean isAcceptableVersion(NetworkParameters params, int version) {
        for (int v : params.getAcceptableAddressCodes()) {
            if (version == v) {
                return true;
            }
        }
        return false;
    }
}
