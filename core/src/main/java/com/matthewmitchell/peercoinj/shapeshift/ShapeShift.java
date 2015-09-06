/*
 * Copyright (C) 2015 NuBits Developers
 * Copyright (C) 2015 Matthew Mitchell
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.matthewmitchell.peercoinj.shapeshift;

import com.matthewmitchell.peercoinj.core.Address;
import com.matthewmitchell.peercoinj.core.AddressFormatException;
import com.matthewmitchell.peercoinj.params.Networks;
import com.matthewmitchell.peercoinj.uri.PeercoinURI;
import com.matthewmitchell.peercoinj.uri.PeercoinURIParseException;
import com.matthewmitchell.peercoinj.utils.MonetaryFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * Class to add and contain ShapeShift coins ({@link com.matthewmitchell.peercoinj.shapeshift.ShapeShiftCoin})
 *
 * @author Matthew Mitchell
 */
public class ShapeShift {
	
	private static final Map<String, ShapeShiftCoin> uriToCoins = new HashMap<String, ShapeShiftCoin>();
	
    /**
     * Add all of the inbuilt coins.
     */
	public static void addAllCoins() {
		
		/*                          NAME            URI PREFIX            CODE     DECIMALS    ADDRESS PREFIX    P2SH PREFIX    HAS IMG    TESTED */
		addCoin(new ShapeShiftCoin( "Bitcoin",      "bitcoin",            "BTC",   8,          0,                5));        // Y          Y
		addCoin(new ShapeShiftCoin( "Blackcoin",    "blackcoin",          "BLK",   8,          25,               85));       // Y          Y
		addCoin(new ShapeShiftCoin( "BitcoinDark",  "BitcoinDark",        "BTCD",  8,          60,               85));       // N          Y
		addCoin(new ShapeShiftCoin( "Clams",        "clam",               "CLAM",  8,          137,              13));       // N          Y
		addCoin(new ShapeShiftCoin( "Counterparty", "counterparty_xcp",   "XCP",   8,          0,                5));        // Y          Y
		addCoin(new ShapeShiftCoin( "Dash",         "dash",               "DASH",  8,          76,               16));       // Y          Y
		addCoin(new ShapeShiftCoin( "Digibyte",     "digibyte",           "DGB",   8,          30,               5));        // Y          Y
		addCoin(new ShapeShiftCoin( "Dogecoin",     "dogecoin",           "DOGE",  8,          30,               22));       // Y          Y
		addCoin(new ShapeShiftCoin( "Feathercoin",  "feathercoin",        "FTC",   8,          14,               5));        // N          Y
		addCoin(new ShapeShiftCoin( "GEMZ",         "counterparty_gemz",  "GEMZ",  8,          0,                5));        // N          Y
		addCoin(new ShapeShiftCoin( "Litecoin",     "litecoin",           "LTC",   8,          48,               5));        // Y          Y
		addCoin(new ShapeShiftCoin( "Mastercoin",   "mastercoin",         "MSC",   8,          0,                5));        // N          Y
		addCoin(new ShapeShiftCoin( "Mintcoin",     "mintcoin",           "MINT",  6,          51,               8));        // Y          Y
		addCoin(new ShapeShiftCoin( "Namecoin",     "namecoin",           "NMC",   8,          52,               -1));       // Y          Y
		addCoin(new ShapeShiftCoin( "Novacoin",     "novacoin",           "NVC",   6,          8,                20));       // Y          Y
		addCoin(new ShapeShiftCoin( "NuBits",       "Nu",                 "NBT",   4,          25,               26));       // Y          Y
		addCoin(new ShapeShiftCoin( "Potcoin",      "potcoin",            "POT",   8,          55,               5));        // Y          Y
		addCoin(new ShapeShiftCoin( "Quark",        "quark",              "QRK",   5,          58,               9));        // N          Y
		addCoin(new ShapeShiftCoin( "Reddcoin",     "reddcoin",           "RDD",   8,          61,               5));        // Y          Y
		addCoin(new ShapeShiftCoin( "Shadowcash",   "shadowcoin",         "SDC",   8,          63,               125));      // N          Y
		addCoin(new ShapeShiftCoin( "Startcoin",    "startcoin",          "START", 8,          125,              5));        // N          Y
		addCoin(new ShapeShiftCoin( "Storjcoin X",  "counterparty_sjcx",  "SJCX",  8,          0,                5));        // N          Y
		addCoin(new ShapeShiftCoin( "Swarm",        "counterparty_swarm", "SWARM", 8,          0,                5));        // N          Y
		addCoin(new ShapeShiftCoin( "Tether",       "tether",             "USDT",  8,          0,                5));        // N          Y
		addCoin(new ShapeShiftCoin( "Unobtanium",   "unobtainium",        "UNO",   8,          130,              30));       // N          Y
		addCoin(new ShapeShiftCoin( "Vericoin",     "vericoin",           "VRC",   8,          70,               85));       // Y          Y
		
	}
	
    /**
     * Add a {@link com.matthewmitchell.peercoinj.shapeshift.ShapeShiftCoin}
     */
	public static void addCoin(ShapeShiftCoin coin) {
		uriToCoins.put(coin.getId(), coin);
		Networks.register(coin);
	}
	
    /**
     * Get a {@link com.matthewmitchell.peercoinj.shapeshift.ShapeShiftCoin} for the given uriPrefix
     */
	public static ShapeShiftCoin getCoin(String uriPrefix) {
		return uriToCoins.get(uriPrefix);
	}
	
	private ShapeShift() {
		
	}
	
}
