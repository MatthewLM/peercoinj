/*
 * Copyright 2015 NuBits Develoeprs
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
import com.matthewmitchell.peercoinj.core.Coin;

/**
 * Interface for dealing with the ShapeShift API: https://shapeshift.io/api.html
 *
 * @author Matthew Mitchell
 */
public abstract class ShapeShiftInterface {
	
	public Callbacks cbks = null;
	private boolean shouldStop = false;
	
    /**
     * Set the callbacks for the API responses
     */
	public void setCallbacks(Callbacks cbks) {
		this.cbks = cbks;
	}
	
    /**
     * After this is called shouldStop() will return true. Can be overriden to do any stopping behaviour. After this is called
     * no more calls to methods of implementing classes should be made
     */
	public void stop() {
		shouldStop = true;
	}
	
    /**
     * Returns true after stop() has been called
     */
	public boolean shouldStop() {
		return shouldStop;
	}
	
    /**
     * Initiate a method call to "sendamount". This allows one to send a fixed amount of destination coins for a particular rate.
     *
     * @params destCoin The coin to send coins to on the other side.
     * @params destAddr The address coins should be sent to.
     * @params amount The amount of coins to be received by the destination address.
     * @params refund An address to receive refunds in case a transaction goes wrong.
     */
	public abstract void sendAmount(ShapeShiftCoin destCoin, Address destAddr, ShapeShiftMonetary amount, Address refund);

    /**
     * Initiate a method call to "cancelpending". The allows one to cancel a fixed amount transaction created by sendAmount.
     *
     * @params depositAddr The deposit address of the fixed amount transaction to cancel.
     */
	public abstract void cancelPending(Address depositAddr);

    /**
     * Initiate a method call to "shift". This allows arbitrary amounts to be send to an address, with no guarenteed rate.
     *
     * @params destCoin The coin to send coins to on the other side.
     * @params destAddr The address coins should be sent to.
     * @params refund An address to receive refunds in case a transaction goes wrong.
     */
	public abstract void shift(ShapeShiftCoin destCoin, Address destAddr, Address refund);

    /**
     * Initiate a method call to "limit".
     *
     * @params destCoin The coin to obtain information
     */
	public abstract void limit(ShapeShiftCoin destCoin);

    /**
     * Initiate a method call to "rate".
     *
     * @params destCoin The coin to obtain information
     */
	public abstract void rate(ShapeShiftCoin destCoin);

    /**
     * Initiate a method call to "marketinfo".
     *
     * @params destCoin The coin to obtain information
     */
	public abstract void marketInfo(ShapeShiftCoin destCoin);
	
	public static abstract class Callbacks {
		
        /**
         * Response for sendAmount requests.
         *
         * @params deposit The address to deposit coins to.
         * @params amount The amount of coins that need to be deposited.
         * @params expiry The unix timestamp at which the transaction expires.
         * @params rate The rate which will be fulfilled.
         */
		public void sendAmountResponse(Address deposit, Coin amount, long expiry, Coin rate) {
			throw new UnsupportedOperationException("sendAmountResponse not implemented");
		}
		
        /**
         * cancelPending success
         */
		public void cancelPendingResponse() {
			throw new UnsupportedOperationException("cancelPendingResponse not implemented");
		}
		
        /**
         * Response for shift requests.
         *
         * @params deposit The address to deposit coins to.
         */
		public void shiftResponse(Address deposit) {
			throw new UnsupportedOperationException("shiftResponse not implemented");
		}
		
        /**
         * Response for limit requests.
         *
         * @params max The maximum allowed deposit for the destination coin.
         * @params min The minimum allowed deposit for the destination coin.
         */
		public void limitResponse(Coin max, Coin min) {
			throw new UnsupportedOperationException("limitResponse not implemented");
		}
		
        /**
         * Response for rate requests.
         *
         * @params rate The estimated rate for transactions to the destination coin.
         */
		public void rateResponse(Coin rate) {
			throw new UnsupportedOperationException("rateResponse not implemented");
		}
		
        /**
         * Response for marketinfo requests.
         *
         * @params rate The estimated rate for transactions to the destination coin.
         * @params fee The fee that will be taken from the transaction to the destination coin
         * @params max The maximum allowed deposit for the destination coin.
         * @params min The minimum allowed deposit for the destination coin.
         */
		public void marketInfoResponse(ShapeShiftMonetary rate, ShapeShiftMonetary fee, Coin max, Coin min) {
			throw new UnsupportedOperationException("rateResponse not implemented");
		}
		
        /**
         * On an error with a call to the API.
         *
         * @params networkCode The HTTP code of the error
         * @params text The text of the error
         */
        public abstract void networkError(int networkCode, String text);
		
	}
	
}
