/*
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

package com.matthewmitchell.peercoinj.params;

import com.google.common.collect.Lists;
import com.matthewmitchell.peercoinj.core.NetworkParameters;

import java.util.Collection;
import java.util.List;

/**
 * Utility class that holds all the registered NetworkParameters types used for Address auto discovery.
 * By default only MainNetParams is used. 
 */
public class Networks {

    /** Registered networks */
    private static List<NetworkParameters> networks = Lists.newArrayList((NetworkParameters)MainNetParams.get());

    public static List<NetworkParameters> get() {
        return networks;
    }

    public static NetworkParameters get(String id) {

        for (NetworkParameters network: networks) {
            if (network.getId().equals(id))
                return network;
        }
        return null;

    }

    public static void register(NetworkParameters network) {
        register(Lists.newArrayList(network));
    }

    public static void register(Collection<? extends NetworkParameters> networks) {
        Networks.networks.addAll(networks);
    }

    public static void unregister(NetworkParameters network) {
        networks.remove(network);
    }

}

