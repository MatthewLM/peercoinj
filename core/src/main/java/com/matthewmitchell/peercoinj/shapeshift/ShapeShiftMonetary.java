/*
 * Copyright (C) 2015 NuBits Developers
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

import static com.google.common.math.IntMath.pow;
import com.google.common.math.LongMath;
import com.matthewmitchell.peercoinj.core.Coin;
import com.matthewmitchell.peercoinj.core.Monetary;
import com.matthewmitchell.peercoinj.utils.MonetaryFormat;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;

/**
 * A ShapeShiftMonetary is a {@link com.matthewmitchell.peercoinj.core.Monetary} implementation for ShapeShift coins
 *
 * @author Matthew Mitchell
 */
public class ShapeShiftMonetary implements Monetary {

	private final int smallestUnitExponent;
	private long value;
	private transient MonetaryFormat plainFormat;
	
	private void setFormat() {
		plainFormat = new MonetaryFormat().shift(0).minDecimals(0).repeatOptionalDecimals(1, smallestUnitExponent).noCode();
	}
	
    /**
     * Constructs a ShapeShiftMonetary instance given a monetary amount and the number of decimal places.
     */
	public ShapeShiftMonetary(long value, int smallestUnitExponent) {
		this.value = value;
		this.smallestUnitExponent = smallestUnitExponent;
		setFormat();
	}
	
	public ShapeShiftMonetary(Coin coin, Coin rate, int smallestUnitExponent) {
		this(coin.value * pow(10, smallestUnitExponent) / rate.value, smallestUnitExponent);
	}
	
	public ShapeShiftMonetary(Coin coin, ShapeShiftMonetary rate, int smallestUnitExponent) {
		this(coin.value * rate.value / pow(10, coin.smallestUnitExponent()), smallestUnitExponent);
	}
	
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		setFormat();
	}
	
	@Override
	public int smallestUnitExponent() {
		return smallestUnitExponent;
	}

	@Override
	public long getValue() {
		return value;
	}

	@Override
	public int signum() {
		if (this.value == 0)
            return 0;
        return this.value < 0 ? -1 : 1;
	}

	@Override
	public String toPlainString() {
		return plainFormat.format(this).toString();
	}
	
	public void subEqual(ShapeShiftMonetary operand) {
		this.value -= operand.value;
	}
	
	private BigInteger getBigIntegerOne() {
		return BigInteger.valueOf(LongMath.pow(10, smallestUnitExponent));
	}
	
	private BigInteger toBigInteger() {
		return BigInteger.valueOf(value);
	}
	
	private Coin convertedToCoin(BigInteger converted) {
		
		if (converted.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) > 0
                || converted.compareTo(BigInteger.valueOf(0)) < 0)
            throw new ArithmeticException("Out of range");
        try {
            return Coin.valueOf(converted.longValue());
        } catch (IllegalArgumentException x) {
            throw new ArithmeticException("Out of range: " + x.getMessage());
        }
		
	}
	
	public Coin toCoinRate() {
		
		final BigInteger converted = getBigIntegerOne().multiply(BigInteger.valueOf(Coin.COIN.value)).divide(BigInteger.valueOf(value));
		return convertedToCoin(converted);
		
	}
	
	public Coin toCoinUsingRate(Coin rate) {
		
		final BigInteger converted = toBigInteger().multiply(BigInteger.valueOf(rate.value)).divide(getBigIntegerOne());
		return convertedToCoin(converted);
		
	}
	
}
