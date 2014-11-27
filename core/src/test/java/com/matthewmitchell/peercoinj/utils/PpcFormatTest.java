/*
 * Copyright 2014 Adam Mackler
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

package com.matthewmitchell.peercoinj.utils;

import com.matthewmitchell.peercoinj.core.Coin;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.math.BigDecimal;
import java.text.*;
import java.text.AttributedCharacterIterator.Attribute;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import static com.matthewmitchell.peercoinj.core.Coin.*;
import static com.matthewmitchell.peercoinj.core.NetworkParameters.MAX_MONEY;
import static com.matthewmitchell.peercoinj.utils.PpcAutoFormat.Style.CODE;
import static com.matthewmitchell.peercoinj.utils.PpcAutoFormat.Style.SYMBOL;
import static com.matthewmitchell.peercoinj.utils.PpcFixedFormat.REPEATING_DOUBLETS;
import static com.matthewmitchell.peercoinj.utils.PpcFixedFormat.REPEATING_TRIPLETS;
import static java.text.NumberFormat.Field.DECIMAL_SEPARATOR;
import static java.util.Locale.*;
import static org.junit.Assert.*;

@RunWith(Parameterized.class)
public class PpcFormatTest {

    @Parameters
    public static Set<Locale[]> data() {
        Set<Locale[]> localeSet = new HashSet<Locale[]>();
        for (Locale locale : Locale.getAvailableLocales()) {
            localeSet.add(new Locale[]{locale});
        }
        return localeSet;
    }

    public PpcFormatTest(Locale defaultLocale) {
        Locale.setDefault(defaultLocale);
    }
 
    @Test
    public void prefixTest() { // prefix b/c symbol is prefixed
        PpcFormat usFormat = PpcFormat.getSymbolInstance(Locale.US);
        assertEquals("฿1.00", usFormat.format(COIN));
        assertEquals("฿1.01", usFormat.format(101000000));
        assertEquals("₥฿0.01", usFormat.format(1000));
        assertEquals("₥฿1,011.00", usFormat.format(101100000));
        assertEquals("₥฿1,000.01", usFormat.format(100001000));
        assertEquals("µ฿1,000,001.00", usFormat.format(100000100));
        assertEquals("µ฿1,000,000.10", usFormat.format(100000010));
        assertEquals("µ฿1,000,000.01", usFormat.format(100000001));
        assertEquals("µ฿1.00", usFormat.format(100));
        assertEquals("µ฿0.10", usFormat.format(10));
        assertEquals("µ฿0.01", usFormat.format(1));
    }

    @Test
    public void suffixTest() {
        PpcFormat deFormat = PpcFormat.getSymbolInstance(Locale.GERMANY);
        // int
        assertEquals("1,00 ฿", deFormat.format(100000000));
        assertEquals("1,01 ฿", deFormat.format(101000000));
        assertEquals("1.011,00 ₥฿", deFormat.format(101100000));
        assertEquals("1.000,01 ₥฿", deFormat.format(100001000));
        assertEquals("1.000.001,00 µ฿", deFormat.format(100000100));
        assertEquals("1.000.000,10 µ฿", deFormat.format(100000010));
        assertEquals("1.000.000,01 µ฿", deFormat.format(100000001));
    }

    @Test
    public void defaultLocaleTest() {
        assertEquals(
             "Default Locale is " + Locale.getDefault().toString(),
             PpcFormat.getInstance().pattern(), PpcFormat.getInstance(Locale.getDefault()).pattern()
        );
        assertEquals(
            "Default Locale is " + Locale.getDefault().toString(),
            PpcFormat.getCodeInstance().pattern(),
            PpcFormat.getCodeInstance(Locale.getDefault()).pattern()
       );
    }

    @Test
    public void symbolCollisionTest() {
        Locale[] locales = PpcFormat.getAvailableLocales();
        for (int i = 0; i < locales.length; ++i) {
            String cs = ((DecimalFormat)NumberFormat.getCurrencyInstance(locales[i])).
                        getDecimalFormatSymbols().getCurrencySymbol();
            if (cs.contains("฿")) {
                PpcFormat bf = PpcFormat.getSymbolInstance(locales[i]);
                String coin = bf.format(COIN);
                assertTrue(coin.contains("Ƀ"));
                assertFalse(coin.contains("฿"));
                String milli = bf.format(valueOf(10000));
                assertTrue(milli.contains("₥Ƀ"));
                assertFalse(milli.contains("฿"));
                String micro = bf.format(valueOf(100));
                assertTrue(micro.contains("µɃ"));
                assertFalse(micro.contains("฿"));
                PpcFormat ff = PpcFormat.builder().scale(0).locale(locales[i]).pattern("¤#.#").build();
                assertEquals("Ƀ", ((PpcFixedFormat)ff).symbol());
                assertEquals("Ƀ", ff.coinSymbol());
                coin = ff.format(COIN);
                assertTrue(coin.contains("Ƀ"));
                assertFalse(coin.contains("฿"));
                PpcFormat mlff = PpcFormat.builder().scale(3).locale(locales[i]).pattern("¤#.#").build();
                assertEquals("₥Ƀ", ((PpcFixedFormat)mlff).symbol());
                assertEquals("Ƀ", mlff.coinSymbol());
                milli = mlff.format(valueOf(10000));
                assertTrue(milli.contains("₥Ƀ"));
                assertFalse(milli.contains("฿"));
                PpcFormat mcff = PpcFormat.builder().scale(6).locale(locales[i]).pattern("¤#.#").build();
                assertEquals("µɃ", ((PpcFixedFormat)mcff).symbol());
                assertEquals("Ƀ", mcff.coinSymbol());
                micro = mcff.format(valueOf(100));
                assertTrue(micro.contains("µɃ"));
                assertFalse(micro.contains("฿"));
            }
            if (cs.contains("Ƀ")) {  // NB: We don't know of any such existing locale, but check anyway.
                PpcFormat bf = PpcFormat.getInstance(locales[i]);
                String coin = bf.format(COIN);
                assertTrue(coin.contains("฿"));
                assertFalse(coin.contains("Ƀ"));
                String milli = bf.format(valueOf(10000));
                assertTrue(milli.contains("₥฿"));
                assertFalse(milli.contains("Ƀ"));
                String micro = bf.format(valueOf(100));
                assertTrue(micro.contains("µ฿"));
                assertFalse(micro.contains("Ƀ"));
            }
        }
    }

    @Test
    public void argumentTypeTest() {
        PpcFormat usFormat = PpcFormat.getSymbolInstance(Locale.US);
        // longs are tested above
        // Coin
        assertEquals("µ฿1,000,000.01", usFormat.format(COIN.add(valueOf(1))));
        // Integer
        assertEquals("µ฿21,474,836.47" ,usFormat.format(Integer.MAX_VALUE));
        assertEquals("(µ฿21,474,836.48)" ,usFormat.format(Integer.MIN_VALUE));
        // Long
        assertEquals("µ฿92,233,720,368,547,758.07" ,usFormat.format(Long.MAX_VALUE));
        assertEquals("(µ฿92,233,720,368,547,758.08)" ,usFormat.format(Long.MIN_VALUE));
        // BigInteger
        assertEquals("µ฿0.10" ,usFormat.format(java.math.BigInteger.TEN));
        assertEquals("฿0.00" ,usFormat.format(java.math.BigInteger.ZERO));
        // BigDecimal
        assertEquals("฿1.00" ,usFormat.format(java.math.BigDecimal.ONE));
        assertEquals("฿0.00" ,usFormat.format(java.math.BigDecimal.ZERO));
        // use of Double not encouraged but no way to stop user from converting one to BigDecimal
        assertEquals(
            "฿179,769,313,486,231,570,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000.00",
            usFormat.format(java.math.BigDecimal.valueOf(Double.MAX_VALUE)));
        assertEquals("฿0.00", usFormat.format(java.math.BigDecimal.valueOf(Double.MIN_VALUE)));
        assertEquals(
            "฿340,282,346,638,528,860,000,000,000,000,000,000,000.00",
            usFormat.format(java.math.BigDecimal.valueOf(Float.MAX_VALUE)));
        // Bad type
        try {
            usFormat.format("1");
            fail("should not have tried to format a String");
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void columnAlignmentTest() {
        PpcFormat germany = PpcFormat.getCoinInstance(2,PpcFixedFormat.REPEATING_PLACES);
        char separator = germany.symbols().getDecimalSeparator();
        Coin[] rows = {MAX_MONEY, MAX_MONEY.subtract(SATOSHI), Coin.parseCoin("1234"),
                       COIN, COIN.add(SATOSHI), COIN.subtract(SATOSHI),
                        COIN.divide(1000).add(SATOSHI), COIN.divide(1000), COIN.divide(1000).subtract(SATOSHI),
                       valueOf(100), valueOf(1000), valueOf(10000),
                       SATOSHI};
        FieldPosition fp = new FieldPosition(DECIMAL_SEPARATOR);
        String[] output = new String[rows.length];
        int[] indexes = new int[rows.length];
        int maxIndex = 0;
        for (int i = 0; i < rows.length; i++) {
            output[i] = germany.format(rows[i], new StringBuffer(), fp).toString();
            indexes[i] = fp.getBeginIndex();
            if (indexes[i] > maxIndex) maxIndex = indexes[i];
        }
        for (int i = 0; i < output.length; i++) {
            // uncomment to watch printout
            // System.out.println(repeat(" ", (maxIndex - indexes[i])) + output[i]);
            assertEquals(output[i].indexOf(separator), indexes[i]);
        }
    }

    @Test
    public void repeatingPlaceTest() {
        PpcFormat mega = PpcFormat.getInstance(-6, US);
        Coin value = MAX_MONEY.subtract(SATOSHI);
        assertEquals("20.99999999999999", mega.format(value, 0, PpcFixedFormat.REPEATING_PLACES));
        assertEquals("20.99999999999999", mega.format(value, 0, PpcFixedFormat.REPEATING_PLACES));
        assertEquals("20.99999999999999", mega.format(value, 1, PpcFixedFormat.REPEATING_PLACES));
        assertEquals("20.99999999999999", mega.format(value, 2, PpcFixedFormat.REPEATING_PLACES));
        assertEquals("20.99999999999999", mega.format(value, 3, PpcFixedFormat.REPEATING_PLACES));
        assertEquals("20.99999999999999", mega.format(value, 0, PpcFixedFormat.REPEATING_DOUBLETS));
        assertEquals("20.99999999999999", mega.format(value, 1, PpcFixedFormat.REPEATING_DOUBLETS));
        assertEquals("20.99999999999999", mega.format(value, 2, PpcFixedFormat.REPEATING_DOUBLETS));
        assertEquals("20.99999999999999", mega.format(value, 3, PpcFixedFormat.REPEATING_DOUBLETS));
        assertEquals("20.99999999999999", mega.format(value, 0, PpcFixedFormat.REPEATING_TRIPLETS));
        assertEquals("20.99999999999999", mega.format(value, 1, PpcFixedFormat.REPEATING_TRIPLETS));
        assertEquals("20.99999999999999", mega.format(value, 2, PpcFixedFormat.REPEATING_TRIPLETS));
        assertEquals("20.99999999999999", mega.format(value, 3, PpcFixedFormat.REPEATING_TRIPLETS));
        assertEquals("1.00000005", PpcFormat.getCoinInstance(US).
                                   format(COIN.add(Coin.valueOf(5)), 0, PpcFixedFormat.REPEATING_PLACES));
    }

    @Test
    public void characterIteratorTest() {
        PpcFormat usFormat = PpcFormat.getInstance(Locale.US);
        AttributedCharacterIterator i = usFormat.formatToCharacterIterator(parseCoin("1234.5"));
        java.util.Set<Attribute> a = i.getAllAttributeKeys();
        assertTrue("Missing currency attribute", a.contains(NumberFormat.Field.CURRENCY));
        assertTrue("Missing integer attribute", a.contains(NumberFormat.Field.INTEGER));
        assertTrue("Missing fraction attribute", a.contains(NumberFormat.Field.FRACTION));
        assertTrue("Missing decimal separator attribute", a.contains(NumberFormat.Field.DECIMAL_SEPARATOR));
        assertTrue("Missing grouping separator attribute", a.contains(NumberFormat.Field.GROUPING_SEPARATOR));
        assertTrue("Missing currency attribute", a.contains(NumberFormat.Field.CURRENCY));

        char c;
        i = PpcFormat.getCodeInstance(Locale.US).formatToCharacterIterator(new BigDecimal("0.19246362747414458"));
        // formatted as "µPPC 192,463.63"
        assertEquals(0, i.getBeginIndex());
        assertEquals(15, i.getEndIndex());
        int n = 0;
        for(c = i.first(); i.getAttribute(NumberFormat.Field.CURRENCY) != null; c = i.next()) {
            n++;
        }
        assertEquals(4, n);
        n = 0;
        for(i.next(); i.getAttribute(NumberFormat.Field.INTEGER) != null && i.getAttribute(NumberFormat.Field.GROUPING_SEPARATOR) != NumberFormat.Field.GROUPING_SEPARATOR; c = i.next()) {
            n++;
        }
        assertEquals(3, n);
        assertEquals(NumberFormat.Field.INTEGER, i.getAttribute(NumberFormat.Field.INTEGER));
        n = 0;
        for(c = i.next(); i.getAttribute(NumberFormat.Field.INTEGER) != null; c = i.next()) {
            n++;
        }
        assertEquals(3, n);
        assertEquals(NumberFormat.Field.DECIMAL_SEPARATOR, i.getAttribute(NumberFormat.Field.DECIMAL_SEPARATOR));
        n = 0;
        for(c = i.next(); c != CharacterIterator.DONE; c = i.next()) {
            n++;
            assertNotNull(i.getAttribute(NumberFormat.Field.FRACTION));
        }
        assertEquals(2,n);

        // immutability check
        PpcFormat fa = PpcFormat.getSymbolInstance(US);
        PpcFormat fb = PpcFormat.getSymbolInstance(US);
        assertEquals(fa, fb);
        assertEquals(fa.hashCode(), fb.hashCode());
        fa.formatToCharacterIterator(COIN.multiply(1000000));
        assertEquals(fa, fb);
        assertEquals(fa.hashCode(), fb.hashCode());
        fb.formatToCharacterIterator(COIN.divide(1000000));
        assertEquals(fa, fb);
        assertEquals(fa.hashCode(), fb.hashCode());
    }

    @Test
    public void parseTest() throws java.text.ParseException {
        PpcFormat us = PpcFormat.getSymbolInstance(Locale.US);
        PpcFormat usCoded = PpcFormat.getCodeInstance(Locale.US);
        // Coins
        assertEquals(valueOf(200000000), us.parseObject("PPC2"));
        assertEquals(valueOf(200000000), us.parseObject("PPC2"));
        assertEquals(valueOf(200000000), us.parseObject("฿2"));
        assertEquals(valueOf(200000000), us.parseObject("Ƀ2"));
        assertEquals(valueOf(200000000), us.parseObject("2"));
        assertEquals(valueOf(200000000), usCoded.parseObject("PPC 2"));
        assertEquals(valueOf(200000000), usCoded.parseObject("PPC 2"));
        assertEquals(valueOf(200000000), us.parseObject("฿2.0"));
        assertEquals(valueOf(200000000), us.parseObject("Ƀ2.0"));
        assertEquals(valueOf(200000000), us.parseObject("2.0"));
        assertEquals(valueOf(200000000), us.parseObject("PPC2.0"));
        assertEquals(valueOf(200000000), us.parseObject("PPC2.0"));
        assertEquals(valueOf(200000000), usCoded.parseObject("฿ 2"));
        assertEquals(valueOf(200000000), usCoded.parseObject("Ƀ 2"));
        assertEquals(valueOf(200000000), usCoded.parseObject(" 2"));
        assertEquals(valueOf(200000000), usCoded.parseObject("PPC 2"));
        assertEquals(valueOf(200000000), usCoded.parseObject("PPC 2"));
        assertEquals(valueOf(202222420000000L), us.parseObject("2,022,224.20"));
        assertEquals(valueOf(202222420000000L), us.parseObject("฿2,022,224.20"));
        assertEquals(valueOf(202222420000000L), us.parseObject("Ƀ2,022,224.20"));
        assertEquals(valueOf(202222420000000L), us.parseObject("PPC2,022,224.20"));
        assertEquals(valueOf(202222420000000L), us.parseObject("PPC2,022,224.20"));
        assertEquals(valueOf(220200000000L), us.parseObject("2,202.0"));
        assertEquals(valueOf(2100000000000000L), us.parseObject("21000000.00000000"));
        // MilliCoins
        assertEquals(valueOf(200000), usCoded.parseObject("mPPC 2"));
        assertEquals(valueOf(200000), usCoded.parseObject("mPPC 2"));
        assertEquals(valueOf(200000), usCoded.parseObject("m฿ 2"));
        assertEquals(valueOf(200000), usCoded.parseObject("mɃ 2"));
        assertEquals(valueOf(200000), us.parseObject("mPPC2"));
        assertEquals(valueOf(200000), us.parseObject("mPPC2"));
        assertEquals(valueOf(200000), us.parseObject("₥฿2"));
        assertEquals(valueOf(200000), us.parseObject("₥Ƀ2"));
        assertEquals(valueOf(200000), us.parseObject("₥2"));
        assertEquals(valueOf(200000), usCoded.parseObject("₥PPC 2.00"));
        assertEquals(valueOf(200000), usCoded.parseObject("₥PPC 2.00"));
        assertEquals(valueOf(200000), usCoded.parseObject("₥PPC 2"));
        assertEquals(valueOf(200000), usCoded.parseObject("₥PPC 2"));
        assertEquals(valueOf(200000), usCoded.parseObject("₥฿ 2"));
        assertEquals(valueOf(200000), usCoded.parseObject("₥Ƀ 2"));
        assertEquals(valueOf(200000), usCoded.parseObject("₥ 2"));
        assertEquals(valueOf(202222400000L), us.parseObject("₥฿2,022,224"));
        assertEquals(valueOf(202222420000L), us.parseObject("₥Ƀ2,022,224.20"));
        assertEquals(valueOf(202222400000L), us.parseObject("m฿2,022,224"));
        assertEquals(valueOf(202222420000L), us.parseObject("mɃ2,022,224.20"));
        assertEquals(valueOf(202222400000L), us.parseObject("₥PPC2,022,224"));
        assertEquals(valueOf(202222400000L), us.parseObject("₥PPC2,022,224"));
        assertEquals(valueOf(202222400000L), us.parseObject("mPPC2,022,224"));
        assertEquals(valueOf(202222400000L), us.parseObject("mPPC2,022,224"));
        assertEquals(valueOf(202222420000L), us.parseObject("₥2,022,224.20"));
        assertEquals(valueOf(202222400000L), usCoded.parseObject("₥฿ 2,022,224"));
        assertEquals(valueOf(202222420000L), usCoded.parseObject("₥Ƀ 2,022,224.20"));
        assertEquals(valueOf(202222400000L), usCoded.parseObject("m฿ 2,022,224"));
        assertEquals(valueOf(202222420000L), usCoded.parseObject("mɃ 2,022,224.20"));
        assertEquals(valueOf(202222400000L), usCoded.parseObject("₥PPC 2,022,224"));
        assertEquals(valueOf(202222400000L), usCoded.parseObject("₥PPC 2,022,224"));
        assertEquals(valueOf(202222400000L), usCoded.parseObject("mPPC 2,022,224"));
        assertEquals(valueOf(202222400000L), usCoded.parseObject("mPPC 2,022,224"));
        assertEquals(valueOf(202222420000L), usCoded.parseObject("₥ 2,022,224.20"));
        // Microcoins
        assertEquals(valueOf(435), us.parseObject("µ฿4.35"));
        assertEquals(valueOf(435), us.parseObject("uɃ4.35"));
        assertEquals(valueOf(435), us.parseObject("u฿4.35"));
        assertEquals(valueOf(435), us.parseObject("µɃ4.35"));
        assertEquals(valueOf(435), us.parseObject("uPPC4.35"));
        assertEquals(valueOf(435), us.parseObject("uPPC4.35"));
        assertEquals(valueOf(435), us.parseObject("µPPC4.35"));
        assertEquals(valueOf(435), us.parseObject("µPPC4.35"));
        assertEquals(valueOf(435), usCoded.parseObject("uPPC 4.35"));
        assertEquals(valueOf(435), usCoded.parseObject("uPPC 4.35"));
        assertEquals(valueOf(435), usCoded.parseObject("µPPC 4.35"));
        assertEquals(valueOf(435), usCoded.parseObject("µPPC 4.35"));
        // fractional satoshi; round up
        assertEquals(valueOf(435), us.parseObject("uPPC4.345"));
        assertEquals(valueOf(435), us.parseObject("uPPC4.345"));
        // negative with mu symbol
        assertEquals(valueOf(-1), usCoded.parseObject("(µ฿ 0.01)"));
        assertEquals(valueOf(-10), us.parseObject("(µPPC0.100)"));
        assertEquals(valueOf(-10), us.parseObject("(µPPC0.100)"));

        // Same thing with addition of custom code, symbol
        us = PpcFormat.builder().locale(US).style(SYMBOL).symbol("£").code("XYZ").build();
        usCoded = PpcFormat.builder().locale(US).scale(0).symbol("£").code("XYZ").
                            pattern("¤ #,##0.00").build();
        // Coins
        assertEquals(valueOf(200000000), us.parseObject("XYZ2"));
        assertEquals(valueOf(200000000), us.parseObject("PPC2"));
        assertEquals(valueOf(200000000), us.parseObject("PPC2"));
        assertEquals(valueOf(200000000), us.parseObject("£2"));
        assertEquals(valueOf(200000000), us.parseObject("฿2"));
        assertEquals(valueOf(200000000), us.parseObject("Ƀ2"));
        assertEquals(valueOf(200000000), us.parseObject("2"));
        assertEquals(valueOf(200000000), usCoded.parseObject("XYZ 2"));
        assertEquals(valueOf(200000000), usCoded.parseObject("PPC 2"));
        assertEquals(valueOf(200000000), usCoded.parseObject("PPC 2"));
        assertEquals(valueOf(200000000), us.parseObject("£2.0"));
        assertEquals(valueOf(200000000), us.parseObject("฿2.0"));
        assertEquals(valueOf(200000000), us.parseObject("Ƀ2.0"));
        assertEquals(valueOf(200000000), us.parseObject("2.0"));
        assertEquals(valueOf(200000000), us.parseObject("XYZ2.0"));
        assertEquals(valueOf(200000000), us.parseObject("PPC2.0"));
        assertEquals(valueOf(200000000), us.parseObject("PPC2.0"));
        assertEquals(valueOf(200000000), usCoded.parseObject("£ 2"));
        assertEquals(valueOf(200000000), usCoded.parseObject("฿ 2"));
        assertEquals(valueOf(200000000), usCoded.parseObject("Ƀ 2"));
        assertEquals(valueOf(200000000), usCoded.parseObject(" 2"));
        assertEquals(valueOf(200000000), usCoded.parseObject("XYZ 2"));
        assertEquals(valueOf(200000000), usCoded.parseObject("PPC 2"));
        assertEquals(valueOf(200000000), usCoded.parseObject("PPC 2"));
        assertEquals(valueOf(202222420000000L), us.parseObject("2,022,224.20"));
        assertEquals(valueOf(202222420000000L), us.parseObject("£2,022,224.20"));
        assertEquals(valueOf(202222420000000L), us.parseObject("฿2,022,224.20"));
        assertEquals(valueOf(202222420000000L), us.parseObject("Ƀ2,022,224.20"));
        assertEquals(valueOf(202222420000000L), us.parseObject("XYZ2,022,224.20"));
        assertEquals(valueOf(202222420000000L), us.parseObject("PPC2,022,224.20"));
        assertEquals(valueOf(202222420000000L), us.parseObject("PPC2,022,224.20"));
        assertEquals(valueOf(220200000000L), us.parseObject("2,202.0"));
        assertEquals(valueOf(2100000000000000L), us.parseObject("21000000.00000000"));
        // MilliCoins
        assertEquals(valueOf(200000), usCoded.parseObject("mXYZ 2"));
        assertEquals(valueOf(200000), usCoded.parseObject("mPPC 2"));
        assertEquals(valueOf(200000), usCoded.parseObject("mPPC 2"));
        assertEquals(valueOf(200000), usCoded.parseObject("m£ 2"));
        assertEquals(valueOf(200000), usCoded.parseObject("m฿ 2"));
        assertEquals(valueOf(200000), usCoded.parseObject("mɃ 2"));
        assertEquals(valueOf(200000), us.parseObject("mXYZ2"));
        assertEquals(valueOf(200000), us.parseObject("mPPC2"));
        assertEquals(valueOf(200000), us.parseObject("mPPC2"));
        assertEquals(valueOf(200000), us.parseObject("₥£2"));
        assertEquals(valueOf(200000), us.parseObject("₥฿2"));
        assertEquals(valueOf(200000), us.parseObject("₥Ƀ2"));
        assertEquals(valueOf(200000), us.parseObject("₥2"));
        assertEquals(valueOf(200000), usCoded.parseObject("₥XYZ 2.00"));
        assertEquals(valueOf(200000), usCoded.parseObject("₥PPC 2.00"));
        assertEquals(valueOf(200000), usCoded.parseObject("₥PPC 2.00"));
        assertEquals(valueOf(200000), usCoded.parseObject("₥XYZ 2"));
        assertEquals(valueOf(200000), usCoded.parseObject("₥PPC 2"));
        assertEquals(valueOf(200000), usCoded.parseObject("₥PPC 2"));
        assertEquals(valueOf(200000), usCoded.parseObject("₥£ 2"));
        assertEquals(valueOf(200000), usCoded.parseObject("₥฿ 2"));
        assertEquals(valueOf(200000), usCoded.parseObject("₥Ƀ 2"));
        assertEquals(valueOf(200000), usCoded.parseObject("₥ 2"));
        assertEquals(valueOf(202222400000L), us.parseObject("₥£2,022,224"));
        assertEquals(valueOf(202222400000L), us.parseObject("₥฿2,022,224"));
        assertEquals(valueOf(202222420000L), us.parseObject("₥Ƀ2,022,224.20"));
        assertEquals(valueOf(202222400000L), us.parseObject("m£2,022,224"));
        assertEquals(valueOf(202222400000L), us.parseObject("m฿2,022,224"));
        assertEquals(valueOf(202222420000L), us.parseObject("mɃ2,022,224.20"));
        assertEquals(valueOf(202222400000L), us.parseObject("₥XYZ2,022,224"));
        assertEquals(valueOf(202222400000L), us.parseObject("₥PPC2,022,224"));
        assertEquals(valueOf(202222400000L), us.parseObject("₥PPC2,022,224"));
        assertEquals(valueOf(202222400000L), us.parseObject("mXYZ2,022,224"));
        assertEquals(valueOf(202222400000L), us.parseObject("mPPC2,022,224"));
        assertEquals(valueOf(202222400000L), us.parseObject("mPPC2,022,224"));
        assertEquals(valueOf(202222420000L), us.parseObject("₥2,022,224.20"));
        assertEquals(valueOf(202222400000L), usCoded.parseObject("₥£ 2,022,224"));
        assertEquals(valueOf(202222400000L), usCoded.parseObject("₥฿ 2,022,224"));
        assertEquals(valueOf(202222420000L), usCoded.parseObject("₥Ƀ 2,022,224.20"));
        assertEquals(valueOf(202222400000L), usCoded.parseObject("m£ 2,022,224"));
        assertEquals(valueOf(202222400000L), usCoded.parseObject("m฿ 2,022,224"));
        assertEquals(valueOf(202222420000L), usCoded.parseObject("mɃ 2,022,224.20"));
        assertEquals(valueOf(202222400000L), usCoded.parseObject("₥XYZ 2,022,224"));
        assertEquals(valueOf(202222400000L), usCoded.parseObject("₥PPC 2,022,224"));
        assertEquals(valueOf(202222400000L), usCoded.parseObject("₥PPC 2,022,224"));
        assertEquals(valueOf(202222400000L), usCoded.parseObject("mXYZ 2,022,224"));
        assertEquals(valueOf(202222400000L), usCoded.parseObject("mPPC 2,022,224"));
        assertEquals(valueOf(202222400000L), usCoded.parseObject("mPPC 2,022,224"));
        assertEquals(valueOf(202222420000L), usCoded.parseObject("₥ 2,022,224.20"));
        // Microcoins
        assertEquals(valueOf(435), us.parseObject("µ£4.35"));
        assertEquals(valueOf(435), us.parseObject("µ฿4.35"));
        assertEquals(valueOf(435), us.parseObject("uɃ4.35"));
        assertEquals(valueOf(435), us.parseObject("u£4.35"));
        assertEquals(valueOf(435), us.parseObject("u฿4.35"));
        assertEquals(valueOf(435), us.parseObject("µɃ4.35"));
        assertEquals(valueOf(435), us.parseObject("uXYZ4.35"));
        assertEquals(valueOf(435), us.parseObject("uPPC4.35"));
        assertEquals(valueOf(435), us.parseObject("uPPC4.35"));
        assertEquals(valueOf(435), us.parseObject("µXYZ4.35"));
        assertEquals(valueOf(435), us.parseObject("µPPC4.35"));
        assertEquals(valueOf(435), us.parseObject("µPPC4.35"));
        assertEquals(valueOf(435), usCoded.parseObject("uXYZ 4.35"));
        assertEquals(valueOf(435), usCoded.parseObject("uPPC 4.35"));
        assertEquals(valueOf(435), usCoded.parseObject("uPPC 4.35"));
        assertEquals(valueOf(435), usCoded.parseObject("µXYZ 4.35"));
        assertEquals(valueOf(435), usCoded.parseObject("µPPC 4.35"));
        assertEquals(valueOf(435), usCoded.parseObject("µPPC 4.35"));
        // fractional satoshi; round up
        assertEquals(valueOf(435), us.parseObject("uXYZ4.345"));
        assertEquals(valueOf(435), us.parseObject("uPPC4.345"));
        assertEquals(valueOf(435), us.parseObject("uPPC4.345"));
        // negative with mu symbol
        assertEquals(valueOf(-1), usCoded.parseObject("µ£ -0.01"));
        assertEquals(valueOf(-1), usCoded.parseObject("µ฿ -0.01"));
        assertEquals(valueOf(-10), us.parseObject("(µXYZ0.100)"));
        assertEquals(valueOf(-10), us.parseObject("(µPPC0.100)"));
        assertEquals(valueOf(-10), us.parseObject("(µPPC0.100)"));

        // parse() method as opposed to parseObject
        try {
            PpcFormat.getInstance().parse("abc");
            fail("bad parse must raise exception");
        } catch (ParseException e) {}
    }

    @Test
    public void parseMetricTest() throws ParseException {
        PpcFormat cp = PpcFormat.getCodeInstance(Locale.US);
        PpcFormat sp = PpcFormat.getSymbolInstance(Locale.US);
        // coin
        assertEquals(parseCoin("1"), cp.parseObject("PPC 1.00"));
        assertEquals(parseCoin("1"), sp.parseObject("PPC1.00"));
        assertEquals(parseCoin("1"), cp.parseObject("฿ 1.00"));
        assertEquals(parseCoin("1"), sp.parseObject("฿1.00"));
        assertEquals(parseCoin("1"), cp.parseObject("B⃦ 1.00"));
        assertEquals(parseCoin("1"), sp.parseObject("B⃦1.00"));
        assertEquals(parseCoin("1"), cp.parseObject("Ƀ 1.00"));
        assertEquals(parseCoin("1"), sp.parseObject("Ƀ1.00"));
        // milli
        assertEquals(parseCoin("0.001"), cp.parseObject("mPPC 1.00"));
        assertEquals(parseCoin("0.001"), sp.parseObject("mPPC1.00"));
        assertEquals(parseCoin("0.001"), cp.parseObject("m฿ 1.00"));
        assertEquals(parseCoin("0.001"), sp.parseObject("m฿1.00"));
        assertEquals(parseCoin("0.001"), cp.parseObject("mB⃦ 1.00"));
        assertEquals(parseCoin("0.001"), sp.parseObject("mB⃦1.00"));
        assertEquals(parseCoin("0.001"), cp.parseObject("mɃ 1.00"));
        assertEquals(parseCoin("0.001"), sp.parseObject("mɃ1.00"));
        assertEquals(parseCoin("0.001"), cp.parseObject("₥PPC 1.00"));
        assertEquals(parseCoin("0.001"), sp.parseObject("₥PPC1.00"));
        assertEquals(parseCoin("0.001"), cp.parseObject("₥฿ 1.00"));
        assertEquals(parseCoin("0.001"), sp.parseObject("₥฿1.00"));
        assertEquals(parseCoin("0.001"), cp.parseObject("₥B⃦ 1.00"));
        assertEquals(parseCoin("0.001"), sp.parseObject("₥B⃦1.00"));
        assertEquals(parseCoin("0.001"), cp.parseObject("₥Ƀ 1.00"));
        assertEquals(parseCoin("0.001"), sp.parseObject("₥Ƀ1.00"));
        // micro
        assertEquals(parseCoin("0.000001"), cp.parseObject("uPPC 1.00"));
        assertEquals(parseCoin("0.000001"), sp.parseObject("uPPC1.00"));
        assertEquals(parseCoin("0.000001"), cp.parseObject("u฿ 1.00"));
        assertEquals(parseCoin("0.000001"), sp.parseObject("u฿1.00"));
        assertEquals(parseCoin("0.000001"), cp.parseObject("uB⃦ 1.00"));
        assertEquals(parseCoin("0.000001"), sp.parseObject("uB⃦1.00"));
        assertEquals(parseCoin("0.000001"), cp.parseObject("uɃ 1.00"));
        assertEquals(parseCoin("0.000001"), sp.parseObject("uɃ1.00"));
        assertEquals(parseCoin("0.000001"), cp.parseObject("µPPC 1.00"));
        assertEquals(parseCoin("0.000001"), sp.parseObject("µPPC1.00"));
        assertEquals(parseCoin("0.000001"), cp.parseObject("µ฿ 1.00"));
        assertEquals(parseCoin("0.000001"), sp.parseObject("µ฿1.00"));
        assertEquals(parseCoin("0.000001"), cp.parseObject("µB⃦ 1.00"));
        assertEquals(parseCoin("0.000001"), sp.parseObject("µB⃦1.00"));
        assertEquals(parseCoin("0.000001"), cp.parseObject("µɃ 1.00"));
        assertEquals(parseCoin("0.000001"), sp.parseObject("µɃ1.00"));
        // satoshi
        assertEquals(parseCoin("0.00000001"), cp.parseObject("uPPC 0.01"));
        assertEquals(parseCoin("0.00000001"), sp.parseObject("uPPC0.01"));
        assertEquals(parseCoin("0.00000001"), cp.parseObject("u฿ 0.01"));
        assertEquals(parseCoin("0.00000001"), sp.parseObject("u฿0.01"));
        assertEquals(parseCoin("0.00000001"), cp.parseObject("uB⃦ 0.01"));
        assertEquals(parseCoin("0.00000001"), sp.parseObject("uB⃦0.01"));
        assertEquals(parseCoin("0.00000001"), cp.parseObject("uɃ 0.01"));
        assertEquals(parseCoin("0.00000001"), sp.parseObject("uɃ0.01"));
        assertEquals(parseCoin("0.00000001"), cp.parseObject("µPPC 0.01"));
        assertEquals(parseCoin("0.00000001"), sp.parseObject("µPPC0.01"));
        assertEquals(parseCoin("0.00000001"), cp.parseObject("µ฿ 0.01"));
        assertEquals(parseCoin("0.00000001"), sp.parseObject("µ฿0.01"));
        assertEquals(parseCoin("0.00000001"), cp.parseObject("µB⃦ 0.01"));
        assertEquals(parseCoin("0.00000001"), sp.parseObject("µB⃦0.01"));
        assertEquals(parseCoin("0.00000001"), cp.parseObject("µɃ 0.01"));
        assertEquals(parseCoin("0.00000001"), sp.parseObject("µɃ0.01"));
        // cents
        assertEquals(parseCoin("0.01234567"), cp.parseObject("cPPC 1.234567"));
        assertEquals(parseCoin("0.01234567"), sp.parseObject("cPPC1.234567"));
        assertEquals(parseCoin("0.01234567"), cp.parseObject("c฿ 1.234567"));
        assertEquals(parseCoin("0.01234567"), sp.parseObject("c฿1.234567"));
        assertEquals(parseCoin("0.01234567"), cp.parseObject("cB⃦ 1.234567"));
        assertEquals(parseCoin("0.01234567"), sp.parseObject("cB⃦1.234567"));
        assertEquals(parseCoin("0.01234567"), cp.parseObject("cɃ 1.234567"));
        assertEquals(parseCoin("0.01234567"), sp.parseObject("cɃ1.234567"));
        assertEquals(parseCoin("0.01234567"), cp.parseObject("¢PPC 1.234567"));
        assertEquals(parseCoin("0.01234567"), sp.parseObject("¢PPC1.234567"));
        assertEquals(parseCoin("0.01234567"), cp.parseObject("¢฿ 1.234567"));
        assertEquals(parseCoin("0.01234567"), sp.parseObject("¢฿1.234567"));
        assertEquals(parseCoin("0.01234567"), cp.parseObject("¢B⃦ 1.234567"));
        assertEquals(parseCoin("0.01234567"), sp.parseObject("¢B⃦1.234567"));
        assertEquals(parseCoin("0.01234567"), cp.parseObject("¢Ƀ 1.234567"));
        assertEquals(parseCoin("0.01234567"), sp.parseObject("¢Ƀ1.234567"));
        // dekacoins
        assertEquals(parseCoin("12.34567"), cp.parseObject("daPPC 1.234567"));
        assertEquals(parseCoin("12.34567"), sp.parseObject("daPPC1.234567"));
        assertEquals(parseCoin("12.34567"), cp.parseObject("da฿ 1.234567"));
        assertEquals(parseCoin("12.34567"), sp.parseObject("da฿1.234567"));
        assertEquals(parseCoin("12.34567"), cp.parseObject("daB⃦ 1.234567"));
        assertEquals(parseCoin("12.34567"), sp.parseObject("daB⃦1.234567"));
        assertEquals(parseCoin("12.34567"), cp.parseObject("daɃ 1.234567"));
        assertEquals(parseCoin("12.34567"), sp.parseObject("daɃ1.234567"));
        // hectocoins
        assertEquals(parseCoin("123.4567"), cp.parseObject("hPPC 1.234567"));
        assertEquals(parseCoin("123.4567"), sp.parseObject("hPPC1.234567"));
        assertEquals(parseCoin("123.4567"), cp.parseObject("h฿ 1.234567"));
        assertEquals(parseCoin("123.4567"), sp.parseObject("h฿1.234567"));
        assertEquals(parseCoin("123.4567"), cp.parseObject("hB⃦ 1.234567"));
        assertEquals(parseCoin("123.4567"), sp.parseObject("hB⃦1.234567"));
        assertEquals(parseCoin("123.4567"), cp.parseObject("hɃ 1.234567"));
        assertEquals(parseCoin("123.4567"), sp.parseObject("hɃ1.234567"));
        // kilocoins
        assertEquals(parseCoin("1234.567"), cp.parseObject("kPPC 1.234567"));
        assertEquals(parseCoin("1234.567"), sp.parseObject("kPPC1.234567"));
        assertEquals(parseCoin("1234.567"), cp.parseObject("k฿ 1.234567"));
        assertEquals(parseCoin("1234.567"), sp.parseObject("k฿1.234567"));
        assertEquals(parseCoin("1234.567"), cp.parseObject("kB⃦ 1.234567"));
        assertEquals(parseCoin("1234.567"), sp.parseObject("kB⃦1.234567"));
        assertEquals(parseCoin("1234.567"), cp.parseObject("kɃ 1.234567"));
        assertEquals(parseCoin("1234.567"), sp.parseObject("kɃ1.234567"));
        // megacoins
        assertEquals(parseCoin("1234567"), cp.parseObject("MPPC 1.234567"));
        assertEquals(parseCoin("1234567"), sp.parseObject("MPPC1.234567"));
        assertEquals(parseCoin("1234567"), cp.parseObject("M฿ 1.234567"));
        assertEquals(parseCoin("1234567"), sp.parseObject("M฿1.234567"));
        assertEquals(parseCoin("1234567"), cp.parseObject("MB⃦ 1.234567"));
        assertEquals(parseCoin("1234567"), sp.parseObject("MB⃦1.234567"));
        assertEquals(parseCoin("1234567"), cp.parseObject("MɃ 1.234567"));
        assertEquals(parseCoin("1234567"), sp.parseObject("MɃ1.234567"));
    }

    @Test
    public void parsePositionTest() {
        PpcFormat usCoded = PpcFormat.getCodeInstance(Locale.US);
        // Test the field constants
        FieldPosition intField = new FieldPosition(NumberFormat.Field.INTEGER);
        assertEquals(
          "987,654,321",
          usCoded.format(valueOf(98765432123L), new StringBuffer(), intField).
          substring(intField.getBeginIndex(), intField.getEndIndex())
        );
        FieldPosition fracField = new FieldPosition(NumberFormat.Field.FRACTION);
        assertEquals(
          "23",
          usCoded.format(valueOf(98765432123L), new StringBuffer(), fracField).
          substring(fracField.getBeginIndex(), fracField.getEndIndex())
        );

        // for currency we use a locale that puts the units at the end
        PpcFormat de = PpcFormat.getSymbolInstance(Locale.GERMANY);
        PpcFormat deCoded = PpcFormat.getCodeInstance(Locale.GERMANY);
        FieldPosition currField = new FieldPosition(NumberFormat.Field.CURRENCY);
        assertEquals(
          "µ฿",
          de.format(valueOf(98765432123L), new StringBuffer(), currField).
          substring(currField.getBeginIndex(), currField.getEndIndex())
        );
        assertEquals(
          "µPPC",
          deCoded.format(valueOf(98765432123L), new StringBuffer(), currField).
          substring(currField.getBeginIndex(), currField.getEndIndex())
        );
        assertEquals(
          "₥฿",
          de.format(valueOf(98765432000L), new StringBuffer(), currField).
          substring(currField.getBeginIndex(), currField.getEndIndex())
        );
        assertEquals(
          "mPPC",
          deCoded.format(valueOf(98765432000L), new StringBuffer(), currField).
          substring(currField.getBeginIndex(), currField.getEndIndex())
        );
        assertEquals(
          "฿",
          de.format(valueOf(98765000000L), new StringBuffer(), currField).
          substring(currField.getBeginIndex(), currField.getEndIndex())
        );
        assertEquals(
          "PPC",
          deCoded.format(valueOf(98765000000L), new StringBuffer(), currField).
          substring(currField.getBeginIndex(), currField.getEndIndex())
        );
    }

    @Test
    public void currencyCodeTest() {
        /* Insert needed space AFTER currency-code */
        PpcFormat usCoded = PpcFormat.getCodeInstance(Locale.US);
        assertEquals("µPPC 0.01", usCoded.format(1));
        assertEquals("PPC 1.00", usCoded.format(COIN));

        /* Do not insert unneeded space BEFORE currency-code */
        PpcFormat frCoded = PpcFormat.getCodeInstance(Locale.FRANCE);
        assertEquals("0,01 µPPC", frCoded.format(1));
        assertEquals("1,00 PPC", frCoded.format(COIN));

        /* Insert needed space BEFORE currency-code: no known currency pattern does this? */

        /* Do not insert unneeded space AFTER currency-code */
        PpcFormat deCoded = PpcFormat.getCodeInstance(Locale.ITALY);
        assertEquals("µPPC 0,01", deCoded.format(1));
        assertEquals("PPC 1,00", deCoded.format(COIN));
    }

    @Test
    public void coinScaleTest() throws Exception {
        PpcFormat coinFormat = PpcFormat.getCoinInstance(Locale.US);
        assertEquals("1.00", coinFormat.format(Coin.COIN));
        assertEquals("-1.00", coinFormat.format(Coin.COIN.negate()));
        assertEquals(Coin.parseCoin("1"), coinFormat.parseObject("1.00"));
        assertEquals(valueOf(1000000), coinFormat.parseObject("0.01"));
        assertEquals(Coin.parseCoin("1000"), coinFormat.parseObject("1,000.00"));
        assertEquals(Coin.parseCoin("1000"), coinFormat.parseObject("1000"));
    }

    @Test
    public void millicoinScaleTest() throws Exception {
        PpcFormat coinFormat = PpcFormat.getMilliInstance(Locale.US);
        assertEquals("1,000.00", coinFormat.format(Coin.COIN));
        assertEquals("-1,000.00", coinFormat.format(Coin.COIN.negate()));
        assertEquals(Coin.parseCoin("0.001"), coinFormat.parseObject("1.00"));
        assertEquals(valueOf(1000), coinFormat.parseObject("0.01"));
        assertEquals(Coin.parseCoin("1"), coinFormat.parseObject("1,000.00"));
        assertEquals(Coin.parseCoin("1"), coinFormat.parseObject("1000"));
    }

    @Test
    public void microcoinScaleTest() throws Exception {
        PpcFormat coinFormat = PpcFormat.getMicroInstance(Locale.US);
        assertEquals("1,000,000.00", coinFormat.format(Coin.COIN));
        assertEquals("-1,000,000.00", coinFormat.format(Coin.COIN.negate()));
        assertEquals("1,000,000.10", coinFormat.format(Coin.COIN.add(valueOf(10))));
        assertEquals(Coin.parseCoin("0.000001"), coinFormat.parseObject("1.00"));
        assertEquals(valueOf(1), coinFormat.parseObject("0.01"));
        assertEquals(Coin.parseCoin("0.001"), coinFormat.parseObject("1,000.00"));
        assertEquals(Coin.parseCoin("0.001"), coinFormat.parseObject("1000"));
    }

    @Test
    public void testGrouping() throws Exception {
        PpcFormat usCoin = PpcFormat.getInstance(0, Locale.US, 1, 2, 3);
        assertEquals("0.1", usCoin.format(Coin.parseCoin("0.1")));
        assertEquals("0.010", usCoin.format(Coin.parseCoin("0.01")));
        assertEquals("0.001", usCoin.format(Coin.parseCoin("0.001")));
        assertEquals("0.000100", usCoin.format(Coin.parseCoin("0.0001")));
        assertEquals("0.000010", usCoin.format(Coin.parseCoin("0.00001")));
        assertEquals("0.000001", usCoin.format(Coin.parseCoin("0.000001")));

        // no more than two fractional decimal places for the default coin-denomination
        assertEquals("0.01", PpcFormat.getCoinInstance(Locale.US).format(Coin.parseCoin("0.005")));

        PpcFormat usMilli = PpcFormat.getInstance(3, Locale.US, 1, 2, 3);
        assertEquals("0.1", usMilli.format(Coin.parseCoin("0.0001")));
        assertEquals("0.010", usMilli.format(Coin.parseCoin("0.00001")));
        assertEquals("0.001", usMilli.format(Coin.parseCoin("0.000001")));
        // even though last group is 3, that would result in fractional satoshis, which we don't do
        assertEquals("0.00010", usMilli.format(Coin.valueOf(10)));
        assertEquals("0.00001", usMilli.format(Coin.valueOf(1)));

        PpcFormat usMicro = PpcFormat.getInstance(6, Locale.US, 1, 2, 3);
        assertEquals("0.1", usMicro.format(Coin.valueOf(10)));
        // even though second group is 2, that would result in fractional satoshis, which we don't do
        assertEquals("0.01", usMicro.format(Coin.valueOf(1)));
    }


    /* These just make sure factory methods don't raise exceptions.
     * Other tests inspect their return values. */
    @Test
    public void factoryTest() {
        PpcFormat coded = PpcFormat.getInstance(0, 1, 2, 3);
        PpcFormat.getInstance(PpcAutoFormat.Style.CODE);
        PpcAutoFormat symbolic = (PpcAutoFormat)PpcFormat.getInstance(PpcAutoFormat.Style.SYMBOL);
        assertEquals(2, symbolic.fractionPlaces());
        PpcFormat.getInstance(PpcAutoFormat.Style.CODE, 3);
        assertEquals(3, ((PpcAutoFormat)PpcFormat.getInstance(PpcAutoFormat.Style.SYMBOL, 3)).fractionPlaces());
        PpcFormat.getInstance(PpcAutoFormat.Style.SYMBOL, Locale.US, 3);
        PpcFormat.getInstance(PpcAutoFormat.Style.CODE, Locale.US);
        PpcFormat.getInstance(PpcAutoFormat.Style.SYMBOL, Locale.US);
        PpcFormat.getCoinInstance(2, PpcFixedFormat.REPEATING_PLACES);
        PpcFormat.getMilliInstance(1, 2, 3);
        PpcFormat.getInstance(2);
        PpcFormat.getInstance(2, Locale.US);
        PpcFormat.getCodeInstance(3);
        PpcFormat.getSymbolInstance(3);
        PpcFormat.getCodeInstance(Locale.US, 3);
        PpcFormat.getSymbolInstance(Locale.US, 3);
        try {
            PpcFormat.getInstance(SMALLEST_UNIT_EXPONENT + 1);
            fail("should not have constructed an instance with denomination less than satoshi");
        } catch (IllegalArgumentException e) {}
    }
    @Test
    public void factoryArgumentsTest() {
        Locale locale;
        if (Locale.getDefault().equals(GERMANY)) locale = FRANCE;
        else locale = GERMANY;
        assertEquals(PpcFormat.getInstance(), PpcFormat.getCodeInstance());
        assertEquals(PpcFormat.getInstance(locale), PpcFormat.getCodeInstance(locale));
        assertEquals(PpcFormat.getInstance(PpcAutoFormat.Style.CODE), PpcFormat.getCodeInstance());
        assertEquals(PpcFormat.getInstance(PpcAutoFormat.Style.SYMBOL), PpcFormat.getSymbolInstance());
        assertEquals(PpcFormat.getInstance(PpcAutoFormat.Style.CODE,3), PpcFormat.getCodeInstance(3));
        assertEquals(PpcFormat.getInstance(PpcAutoFormat.Style.SYMBOL,3), PpcFormat.getSymbolInstance(3));
        assertEquals(PpcFormat.getInstance(PpcAutoFormat.Style.CODE,locale), PpcFormat.getCodeInstance(locale));
        assertEquals(PpcFormat.getInstance(PpcAutoFormat.Style.SYMBOL,locale), PpcFormat.getSymbolInstance(locale));
        assertEquals(PpcFormat.getInstance(PpcAutoFormat.Style.CODE,locale,3), PpcFormat.getCodeInstance(locale,3));
        assertEquals(PpcFormat.getInstance(PpcAutoFormat.Style.SYMBOL,locale,3), PpcFormat.getSymbolInstance(locale,3));
        assertEquals(PpcFormat.getCoinInstance(), PpcFormat.getInstance(0));
        assertEquals(PpcFormat.getMilliInstance(), PpcFormat.getInstance(3));
        assertEquals(PpcFormat.getMicroInstance(), PpcFormat.getInstance(6));
        assertEquals(PpcFormat.getCoinInstance(3), PpcFormat.getInstance(0,3));
        assertEquals(PpcFormat.getMilliInstance(3), PpcFormat.getInstance(3,3));
        assertEquals(PpcFormat.getMicroInstance(3), PpcFormat.getInstance(6,3));
        assertEquals(PpcFormat.getCoinInstance(3,4,5), PpcFormat.getInstance(0,3,4,5));
        assertEquals(PpcFormat.getMilliInstance(3,4,5), PpcFormat.getInstance(3,3,4,5));
        assertEquals(PpcFormat.getMicroInstance(3,4,5), PpcFormat.getInstance(6,3,4,5));
        assertEquals(PpcFormat.getCoinInstance(locale), PpcFormat.getInstance(0,locale));
        assertEquals(PpcFormat.getMilliInstance(locale), PpcFormat.getInstance(3,locale));
        assertEquals(PpcFormat.getMicroInstance(locale), PpcFormat.getInstance(6,locale));
        assertEquals(PpcFormat.getCoinInstance(locale,4,5), PpcFormat.getInstance(0,locale,4,5));
        assertEquals(PpcFormat.getMilliInstance(locale,4,5), PpcFormat.getInstance(3,locale,4,5));
        assertEquals(PpcFormat.getMicroInstance(locale,4,5), PpcFormat.getInstance(6,locale,4,5));
    }

    @Test
    public void autoDecimalTest() {
        PpcFormat codedZero = PpcFormat.getCodeInstance(Locale.US, 0);
        PpcFormat symbolZero = PpcFormat.getSymbolInstance(Locale.US, 0);
        assertEquals("฿1", symbolZero.format(COIN));
        assertEquals("PPC 1", codedZero.format(COIN));
        assertEquals("µ฿1,000,000", symbolZero.format(COIN.subtract(SATOSHI)));
        assertEquals("µPPC 1,000,000", codedZero.format(COIN.subtract(SATOSHI)));
        assertEquals("µ฿1,000,000", symbolZero.format(COIN.subtract(Coin.valueOf(50))));
        assertEquals("µPPC 1,000,000", codedZero.format(COIN.subtract(Coin.valueOf(50))));
        assertEquals("µ฿999,999", symbolZero.format(COIN.subtract(Coin.valueOf(51))));
        assertEquals("µPPC 999,999", codedZero.format(COIN.subtract(Coin.valueOf(51))));
        assertEquals("฿1,000", symbolZero.format(COIN.multiply(1000)));
        assertEquals("PPC 1,000", codedZero.format(COIN.multiply(1000)));
        assertEquals("µ฿1", symbolZero.format(Coin.valueOf(100)));
        assertEquals("µPPC 1", codedZero.format(Coin.valueOf(100)));
        assertEquals("µ฿1", symbolZero.format(Coin.valueOf(50)));
        assertEquals("µPPC 1", codedZero.format(Coin.valueOf(50)));
        assertEquals("µ฿0", symbolZero.format(Coin.valueOf(49)));
        assertEquals("µPPC 0", codedZero.format(Coin.valueOf(49)));
        assertEquals("µ฿0", symbolZero.format(Coin.valueOf(1)));
        assertEquals("µPPC 0", codedZero.format(Coin.valueOf(1)));
        assertEquals("µ฿500,000", symbolZero.format(Coin.valueOf(49999999)));
        assertEquals("µPPC 500,000", codedZero.format(Coin.valueOf(49999999)));

        assertEquals("µ฿499,500", symbolZero.format(Coin.valueOf(49950000)));
        assertEquals("µPPC 499,500", codedZero.format(Coin.valueOf(49950000)));
        assertEquals("µ฿499,500", symbolZero.format(Coin.valueOf(49949999)));
        assertEquals("µPPC 499,500", codedZero.format(Coin.valueOf(49949999)));
        assertEquals("µ฿500,490", symbolZero.format(Coin.valueOf(50049000)));
        assertEquals("µPPC 500,490", codedZero.format(Coin.valueOf(50049000)));
        assertEquals("µ฿500,490", symbolZero.format(Coin.valueOf(50049001)));
        assertEquals("µPPC 500,490", codedZero.format(Coin.valueOf(50049001)));
        assertEquals("µ฿500,000", symbolZero.format(Coin.valueOf(49999950)));
        assertEquals("µPPC 500,000", codedZero.format(Coin.valueOf(49999950)));
        assertEquals("µ฿499,999", symbolZero.format(Coin.valueOf(49999949)));
        assertEquals("µPPC 499,999", codedZero.format(Coin.valueOf(49999949)));
        assertEquals("µ฿500,000", symbolZero.format(Coin.valueOf(50000049)));
        assertEquals("µPPC 500,000", codedZero.format(Coin.valueOf(50000049)));
        assertEquals("µ฿500,001", symbolZero.format(Coin.valueOf(50000050)));
        assertEquals("µPPC 500,001", codedZero.format(Coin.valueOf(50000050)));

        PpcFormat codedTwo = PpcFormat.getCodeInstance(Locale.US, 2);
        PpcFormat symbolTwo = PpcFormat.getSymbolInstance(Locale.US, 2);
        assertEquals("฿1.00", symbolTwo.format(COIN));
        assertEquals("PPC 1.00", codedTwo.format(COIN));
        assertEquals("µ฿999,999.99", symbolTwo.format(COIN.subtract(SATOSHI)));
        assertEquals("µPPC 999,999.99", codedTwo.format(COIN.subtract(SATOSHI)));
        assertEquals("฿1,000.00", symbolTwo.format(COIN.multiply(1000)));
        assertEquals("PPC 1,000.00", codedTwo.format(COIN.multiply(1000)));
        assertEquals("µ฿1.00", symbolTwo.format(Coin.valueOf(100)));
        assertEquals("µPPC 1.00", codedTwo.format(Coin.valueOf(100)));
        assertEquals("µ฿0.50", symbolTwo.format(Coin.valueOf(50)));
        assertEquals("µPPC 0.50", codedTwo.format(Coin.valueOf(50)));
        assertEquals("µ฿0.49", symbolTwo.format(Coin.valueOf(49)));
        assertEquals("µPPC 0.49", codedTwo.format(Coin.valueOf(49)));
        assertEquals("µ฿0.01", symbolTwo.format(Coin.valueOf(1)));
        assertEquals("µPPC 0.01", codedTwo.format(Coin.valueOf(1)));

        PpcFormat codedThree = PpcFormat.getCodeInstance(Locale.US, 3);
        PpcFormat symbolThree = PpcFormat.getSymbolInstance(Locale.US, 3);
        assertEquals("฿1.000", symbolThree.format(COIN));
        assertEquals("PPC 1.000", codedThree.format(COIN));
        assertEquals("µ฿999,999.99", symbolThree.format(COIN.subtract(SATOSHI)));
        assertEquals("µPPC 999,999.99", codedThree.format(COIN.subtract(SATOSHI)));
        assertEquals("฿1,000.000", symbolThree.format(COIN.multiply(1000)));
        assertEquals("PPC 1,000.000", codedThree.format(COIN.multiply(1000)));
        assertEquals("₥฿0.001", symbolThree.format(Coin.valueOf(100)));
        assertEquals("mPPC 0.001", codedThree.format(Coin.valueOf(100)));
        assertEquals("µ฿0.50", symbolThree.format(Coin.valueOf(50)));
        assertEquals("µPPC 0.50", codedThree.format(Coin.valueOf(50)));
        assertEquals("µ฿0.49", symbolThree.format(Coin.valueOf(49)));
        assertEquals("µPPC 0.49", codedThree.format(Coin.valueOf(49)));
        assertEquals("µ฿0.01", symbolThree.format(Coin.valueOf(1)));
        assertEquals("µPPC 0.01", codedThree.format(Coin.valueOf(1)));
    }


    @Test
    public void symbolsCodesTest() {
        PpcFixedFormat coin = (PpcFixedFormat)PpcFormat.getCoinInstance(US);
        assertEquals("PPC", coin.code());
        assertEquals("฿", coin.symbol());
        PpcFixedFormat cent = (PpcFixedFormat)PpcFormat.getInstance(2, US);
        assertEquals("cPPC", cent.code());
        assertEquals("¢฿", cent.symbol());
        PpcFixedFormat milli = (PpcFixedFormat)PpcFormat.getInstance(3, US);
        assertEquals("mPPC", milli.code());
        assertEquals("₥฿", milli.symbol());
        PpcFixedFormat micro = (PpcFixedFormat)PpcFormat.getInstance(6, US);
        assertEquals("µPPC", micro.code());
        assertEquals("µ฿", micro.symbol());
        PpcFixedFormat deka = (PpcFixedFormat)PpcFormat.getInstance(-1, US);
        assertEquals("daPPC", deka.code());
        assertEquals("da฿", deka.symbol());
        PpcFixedFormat hecto = (PpcFixedFormat)PpcFormat.getInstance(-2, US);
        assertEquals("hPPC", hecto.code());
        assertEquals("h฿", hecto.symbol());
        PpcFixedFormat kilo = (PpcFixedFormat)PpcFormat.getInstance(-3, US);
        assertEquals("kPPC", kilo.code());
        assertEquals("k฿", kilo.symbol());
        PpcFixedFormat mega = (PpcFixedFormat)PpcFormat.getInstance(-6, US);
        assertEquals("MPPC", mega.code());
        assertEquals("M฿", mega.symbol());
        PpcFixedFormat noSymbol = (PpcFixedFormat)PpcFormat.getInstance(4, US);
        try {
            noSymbol.symbol();
            fail("non-standard denomination has no symbol()");
        } catch (IllegalStateException e) {}
        try {
            noSymbol.code();
            fail("non-standard denomination has no code()");
        } catch (IllegalStateException e) {}

        PpcFixedFormat symbolCoin = (PpcFixedFormat)PpcFormat.builder().locale(US).scale(0).
                                                              symbol("B\u20e6").build();
        assertEquals("PPC", symbolCoin.code());
        assertEquals("B⃦", symbolCoin.symbol());
        PpcFixedFormat symbolCent = (PpcFixedFormat)PpcFormat.builder().locale(US).scale(2).
                                                              symbol("B\u20e6").build();
        assertEquals("cPPC", symbolCent.code());
        assertEquals("¢B⃦", symbolCent.symbol());
        PpcFixedFormat symbolMilli = (PpcFixedFormat)PpcFormat.builder().locale(US).scale(3).
                                                               symbol("B\u20e6").build();
        assertEquals("mPPC", symbolMilli.code());
        assertEquals("₥B⃦", symbolMilli.symbol());
        PpcFixedFormat symbolMicro = (PpcFixedFormat)PpcFormat.builder().locale(US).scale(6).
                                                               symbol("B\u20e6").build();
        assertEquals("µPPC", symbolMicro.code());
        assertEquals("µB⃦", symbolMicro.symbol());
        PpcFixedFormat symbolDeka = (PpcFixedFormat)PpcFormat.builder().locale(US).scale(-1).
                                                              symbol("B\u20e6").build();
        assertEquals("daPPC", symbolDeka.code());
        assertEquals("daB⃦", symbolDeka.symbol());
        PpcFixedFormat symbolHecto = (PpcFixedFormat)PpcFormat.builder().locale(US).scale(-2).
                                                               symbol("B\u20e6").build();
        assertEquals("hPPC", symbolHecto.code());
        assertEquals("hB⃦", symbolHecto.symbol());
        PpcFixedFormat symbolKilo = (PpcFixedFormat)PpcFormat.builder().locale(US).scale(-3).
                                                              symbol("B\u20e6").build();
        assertEquals("kPPC", symbolKilo.code());
        assertEquals("kB⃦", symbolKilo.symbol());
        PpcFixedFormat symbolMega = (PpcFixedFormat)PpcFormat.builder().locale(US).scale(-6).
                                                              symbol("B\u20e6").build();
        assertEquals("MPPC", symbolMega.code());
        assertEquals("MB⃦", symbolMega.symbol());

        PpcFixedFormat codeCoin = (PpcFixedFormat)PpcFormat.builder().locale(US).scale(0).
                                                            code("PPC").build();
        assertEquals("PPC", codeCoin.code());
        assertEquals("฿", codeCoin.symbol());
        PpcFixedFormat codeCent = (PpcFixedFormat)PpcFormat.builder().locale(US).scale(2).
                                                            code("PPC").build();
        assertEquals("cPPC", codeCent.code());
        assertEquals("¢฿", codeCent.symbol());
        PpcFixedFormat codeMilli = (PpcFixedFormat)PpcFormat.builder().locale(US).scale(3).
                                                             code("PPC").build();
        assertEquals("mPPC", codeMilli.code());
        assertEquals("₥฿", codeMilli.symbol());
        PpcFixedFormat codeMicro = (PpcFixedFormat)PpcFormat.builder().locale(US).scale(6).
                                                             code("PPC").build();
        assertEquals("µPPC", codeMicro.code());
        assertEquals("µ฿", codeMicro.symbol());
        PpcFixedFormat codeDeka = (PpcFixedFormat)PpcFormat.builder().locale(US).scale(-1).
                                                            code("PPC").build();
        assertEquals("daPPC", codeDeka.code());
        assertEquals("da฿", codeDeka.symbol());
        PpcFixedFormat codeHecto = (PpcFixedFormat)PpcFormat.builder().locale(US).scale(-2).
                                                             code("PPC").build();
        assertEquals("hPPC", codeHecto.code());
        assertEquals("h฿", codeHecto.symbol());
        PpcFixedFormat codeKilo = (PpcFixedFormat)PpcFormat.builder().locale(US).scale(-3).
                                                            code("PPC").build();
        assertEquals("kPPC", codeKilo.code());
        assertEquals("k฿", codeKilo.symbol());
        PpcFixedFormat codeMega = (PpcFixedFormat)PpcFormat.builder().locale(US).scale(-6).
                                                            code("PPC").build();
        assertEquals("MPPC", codeMega.code());
        assertEquals("M฿", codeMega.symbol());

        PpcFixedFormat symbolCodeCoin = (PpcFixedFormat)PpcFormat.builder().locale(US).scale(0).
                                                                  symbol("B\u20e6").code("PPC").build();
        assertEquals("PPC", symbolCodeCoin.code());
        assertEquals("B⃦", symbolCodeCoin.symbol());
        PpcFixedFormat symbolCodeCent = (PpcFixedFormat)PpcFormat.builder().locale(US).scale(2).
                                                                  symbol("B\u20e6").code("PPC").build();
        assertEquals("cPPC", symbolCodeCent.code());
        assertEquals("¢B⃦", symbolCodeCent.symbol());
        PpcFixedFormat symbolCodeMilli = (PpcFixedFormat)PpcFormat.builder().locale(US).scale(3).
                                                                   symbol("B\u20e6").code("PPC").build();
        assertEquals("mPPC", symbolCodeMilli.code());
        assertEquals("₥B⃦", symbolCodeMilli.symbol());
        PpcFixedFormat symbolCodeMicro = (PpcFixedFormat)PpcFormat.builder().locale(US).scale(6).
                                                                   symbol("B\u20e6").code("PPC").build();
        assertEquals("µPPC", symbolCodeMicro.code());
        assertEquals("µB⃦", symbolCodeMicro.symbol());
        PpcFixedFormat symbolCodeDeka = (PpcFixedFormat)PpcFormat.builder().locale(US).scale(-1).
                                                                  symbol("B\u20e6").code("PPC").build();
        assertEquals("daPPC", symbolCodeDeka.code());
        assertEquals("daB⃦", symbolCodeDeka.symbol());
        PpcFixedFormat symbolCodeHecto = (PpcFixedFormat)PpcFormat.builder().locale(US).scale(-2).
                                                                   symbol("B\u20e6").code("PPC").build();
        assertEquals("hPPC", symbolCodeHecto.code());
        assertEquals("hB⃦", symbolCodeHecto.symbol());
        PpcFixedFormat symbolCodeKilo = (PpcFixedFormat)PpcFormat.builder().locale(US).scale(-3).
                                                                  symbol("B\u20e6").code("PPC").build();
        assertEquals("kPPC", symbolCodeKilo.code());
        assertEquals("kB⃦", symbolCodeKilo.symbol());
        PpcFixedFormat symbolCodeMega = (PpcFixedFormat)PpcFormat.builder().locale(US).scale(-6).
                                                                  symbol("B\u20e6").code("PPC").build();
        assertEquals("MPPC", symbolCodeMega.code());
        assertEquals("MB⃦", symbolCodeMega.symbol());
    }

    /* copied from CoinFormatTest.java and modified */
    @Test
    public void parse() throws Exception {
        PpcFormat coin = PpcFormat.getCoinInstance(Locale.US);
        assertEquals(Coin.COIN, coin.parseObject("1"));
        assertEquals(Coin.COIN, coin.parseObject("1."));
        assertEquals(Coin.COIN, coin.parseObject("1.0"));
        assertEquals(Coin.COIN, PpcFormat.getCoinInstance(Locale.GERMANY).parseObject("1,0"));
        assertEquals(Coin.COIN, coin.parseObject("01.0000000000"));
        // TODO work with express positive sign
        // assertEquals(Coin.COIN, coin.parseObject("+1.0"));
        assertEquals(Coin.COIN.negate(), coin.parseObject("-1"));
        assertEquals(Coin.COIN.negate(), coin.parseObject("-1.0"));

        assertEquals(Coin.CENT, coin.parseObject(".01"));

        PpcFormat milli = PpcFormat.getMilliInstance(Locale.US);
        assertEquals(Coin.MILLICOIN, milli.parseObject("1"));
        assertEquals(Coin.MILLICOIN, milli.parseObject("1.0"));
        assertEquals(Coin.MILLICOIN, milli.parseObject("01.0000000000"));
        // TODO work with express positive sign
        //assertEquals(Coin.MILLICOIN, milli.parseObject("+1.0"));
        assertEquals(Coin.MILLICOIN.negate(), milli.parseObject("-1"));
        assertEquals(Coin.MILLICOIN.negate(), milli.parseObject("-1.0"));

        PpcFormat micro = PpcFormat.getMicroInstance(Locale.US);
        assertEquals(Coin.MICROCOIN, micro.parseObject("1"));
        assertEquals(Coin.MICROCOIN, micro.parseObject("1.0"));
        assertEquals(Coin.MICROCOIN, micro.parseObject("01.0000000000"));
        // TODO work with express positive sign
        // assertEquals(Coin.MICROCOIN, micro.parseObject("+1.0"));
        assertEquals(Coin.MICROCOIN.negate(), micro.parseObject("-1"));
        assertEquals(Coin.MICROCOIN.negate(), micro.parseObject("-1.0"));
    }

    /* Copied (and modified) from CoinFormatTest.java */
    @Test
    public void ppcRounding() throws Exception {
        PpcFormat coinFormat = PpcFormat.getCoinInstance(Locale.US);
        assertEquals("0", PpcFormat.getCoinInstance(Locale.US, 0).format(ZERO));
        assertEquals("0", coinFormat.format(ZERO, 0));
        assertEquals("0.00", PpcFormat.getCoinInstance(Locale.US, 2).format(ZERO));
        assertEquals("0.00", coinFormat.format(ZERO, 2));

        assertEquals("1", PpcFormat.getCoinInstance(Locale.US, 0).format(COIN));
        assertEquals("1", coinFormat.format(COIN, 0));
        assertEquals("1.0", PpcFormat.getCoinInstance(Locale.US, 1).format(COIN));
        assertEquals("1.0", coinFormat.format(COIN, 1));
        assertEquals("1.00", PpcFormat.getCoinInstance(Locale.US, 2, 2).format(COIN));
        assertEquals("1.00", coinFormat.format(COIN, 2, 2));
        assertEquals("1.00", PpcFormat.getCoinInstance(Locale.US, 2, 2, 2).format(COIN));
        assertEquals("1.00", coinFormat.format(COIN, 2, 2, 2));
        assertEquals("1.00", PpcFormat.getCoinInstance(Locale.US, 2, 2, 2, 2).format(COIN));
        assertEquals("1.00", coinFormat.format(COIN, 2, 2, 2, 2));
        assertEquals("1.000", PpcFormat.getCoinInstance(Locale.US, 3).format(COIN));
        assertEquals("1.000", coinFormat.format(COIN, 3));
        assertEquals("1.0000", PpcFormat.getCoinInstance(US, 4).format(COIN));
        assertEquals("1.0000", coinFormat.format(COIN, 4));

        final Coin justNot = COIN.subtract(SATOSHI);
        assertEquals("1", PpcFormat.getCoinInstance(US, 0).format(justNot));
        assertEquals("1", coinFormat.format(justNot, 0));
        assertEquals("1.0", PpcFormat.getCoinInstance(US, 1).format(justNot));
        assertEquals("1.0", coinFormat.format(justNot, 1));
        final Coin justNotUnder = Coin.valueOf(99995000);
        assertEquals("1.00", PpcFormat.getCoinInstance(US, 2, 2).format(justNot));
        assertEquals("1.00", coinFormat.format(justNot, 2, 2));
        assertEquals("1.00", PpcFormat.getCoinInstance(US, 2, 2).format(justNotUnder));
        assertEquals("1.00", coinFormat.format(justNotUnder, 2, 2));
        assertEquals("1.00", PpcFormat.getCoinInstance(US, 2, 2, 2).format(justNot));
        assertEquals("1.00", coinFormat.format(justNot, 2, 2, 2));
        assertEquals("0.999950", PpcFormat.getCoinInstance(US, 2, 2, 2).format(justNotUnder));
        assertEquals("0.999950", coinFormat.format(justNotUnder, 2, 2, 2));
        assertEquals("0.99999999", PpcFormat.getCoinInstance(US, 2, 2, 2, 2).format(justNot));
        assertEquals("0.99999999", coinFormat.format(justNot, 2, 2, 2, 2));
        assertEquals("0.99999999", PpcFormat.getCoinInstance(US, 2, REPEATING_DOUBLETS).format(justNot));
        assertEquals("0.99999999", coinFormat.format(justNot, 2, REPEATING_DOUBLETS));
        assertEquals("0.999950", PpcFormat.getCoinInstance(US, 2, 2, 2, 2).format(justNotUnder));
        assertEquals("0.999950", coinFormat.format(justNotUnder, 2, 2, 2, 2));
        assertEquals("0.999950", PpcFormat.getCoinInstance(US, 2, REPEATING_DOUBLETS).format(justNotUnder));
        assertEquals("0.999950", coinFormat.format(justNotUnder, 2, REPEATING_DOUBLETS));
        assertEquals("1.000", PpcFormat.getCoinInstance(US, 3).format(justNot));
        assertEquals("1.000", coinFormat.format(justNot, 3));
        assertEquals("1.0000", PpcFormat.getCoinInstance(US, 4).format(justNot));
        assertEquals("1.0000", coinFormat.format(justNot, 4));

        final Coin slightlyMore = COIN.add(SATOSHI);
        assertEquals("1", PpcFormat.getCoinInstance(US, 0).format(slightlyMore));
        assertEquals("1", coinFormat.format(slightlyMore, 0));
        assertEquals("1.0", PpcFormat.getCoinInstance(US, 1).format(slightlyMore));
        assertEquals("1.0", coinFormat.format(slightlyMore, 1));
        assertEquals("1.00", PpcFormat.getCoinInstance(US, 2, 2).format(slightlyMore));
        assertEquals("1.00", coinFormat.format(slightlyMore, 2, 2));
        assertEquals("1.00", PpcFormat.getCoinInstance(US, 2, 2, 2).format(slightlyMore));
        assertEquals("1.00", coinFormat.format(slightlyMore, 2, 2, 2));
        assertEquals("1.00000001", PpcFormat.getCoinInstance(US, 2, 2, 2, 2).format(slightlyMore));
        assertEquals("1.00000001", coinFormat.format(slightlyMore, 2, 2, 2, 2));
        assertEquals("1.00000001", PpcFormat.getCoinInstance(US, 2, REPEATING_DOUBLETS).format(slightlyMore));
        assertEquals("1.00000001", coinFormat.format(slightlyMore, 2, REPEATING_DOUBLETS));
        assertEquals("1.000", PpcFormat.getCoinInstance(US, 3).format(slightlyMore));
        assertEquals("1.000", coinFormat.format(slightlyMore, 3));
        assertEquals("1.0000", PpcFormat.getCoinInstance(US, 4).format(slightlyMore));
        assertEquals("1.0000", coinFormat.format(slightlyMore, 4));

        final Coin pivot = COIN.add(SATOSHI.multiply(5));
        assertEquals("1.00000005", PpcFormat.getCoinInstance(US, 8).format(pivot));
        assertEquals("1.00000005", coinFormat.format(pivot, 8));
        assertEquals("1.00000005", PpcFormat.getCoinInstance(US, 7, 1).format(pivot));
        assertEquals("1.00000005", coinFormat.format(pivot, 7, 1));
        assertEquals("1.0000001", PpcFormat.getCoinInstance(US, 7).format(pivot));
        assertEquals("1.0000001", coinFormat.format(pivot, 7));

        final Coin value = Coin.valueOf(1122334455667788l);
        assertEquals("11,223,345", PpcFormat.getCoinInstance(US, 0).format(value));
        assertEquals("11,223,345", coinFormat.format(value, 0));
        assertEquals("11,223,344.6", PpcFormat.getCoinInstance(US, 1).format(value));
        assertEquals("11,223,344.6", coinFormat.format(value, 1));
        assertEquals("11,223,344.5567", PpcFormat.getCoinInstance(US, 2, 2).format(value));
        assertEquals("11,223,344.5567", coinFormat.format(value, 2, 2));
        assertEquals("11,223,344.556678", PpcFormat.getCoinInstance(US, 2, 2, 2).format(value));
        assertEquals("11,223,344.556678", coinFormat.format(value, 2, 2, 2));
        assertEquals("11,223,344.55667788", PpcFormat.getCoinInstance(US, 2, 2, 2, 2).format(value));
        assertEquals("11,223,344.55667788", coinFormat.format(value, 2, 2, 2, 2));
        assertEquals("11,223,344.55667788", PpcFormat.getCoinInstance(US, 2, REPEATING_DOUBLETS).format(value));
        assertEquals("11,223,344.55667788", coinFormat.format(value, 2, REPEATING_DOUBLETS));
        assertEquals("11,223,344.557", PpcFormat.getCoinInstance(US, 3).format(value));
        assertEquals("11,223,344.557", coinFormat.format(value, 3));
        assertEquals("11,223,344.5567", PpcFormat.getCoinInstance(US, 4).format(value));
        assertEquals("11,223,344.5567", coinFormat.format(value, 4));

        PpcFormat megaFormat = PpcFormat.getInstance(-6, US);
        assertEquals("21.00", megaFormat.format(MAX_MONEY));
        assertEquals("21", megaFormat.format(MAX_MONEY, 0));
        assertEquals("11.22334455667788", megaFormat.format(value, 0, REPEATING_DOUBLETS));
        assertEquals("11.223344556677", megaFormat.format(Coin.valueOf(1122334455667700l), 0, REPEATING_DOUBLETS));
        assertEquals("11.22334455667788", megaFormat.format(value, 0, REPEATING_TRIPLETS));
        assertEquals("11.223344556677", megaFormat.format(Coin.valueOf(1122334455667700l), 0, REPEATING_TRIPLETS));
    }

    @Test
    public void negativeTest() throws Exception {
        assertEquals("-1,00 PPC", PpcFormat.getInstance(FRANCE).format(COIN.multiply(-1)));
        assertEquals("PPC -1,00", PpcFormat.getInstance(ITALY).format(COIN.multiply(-1)));
        assertEquals("฿ -1,00", PpcFormat.getSymbolInstance(ITALY).format(COIN.multiply(-1)));
        assertEquals("PPC -1.00", PpcFormat.getInstance(JAPAN).format(COIN.multiply(-1)));
        assertEquals("฿-1.00", PpcFormat.getSymbolInstance(JAPAN).format(COIN.multiply(-1)));
        assertEquals("(PPC 1.00)", PpcFormat.getInstance(US).format(COIN.multiply(-1)));
        assertEquals("(฿1.00)", PpcFormat.getSymbolInstance(US).format(COIN.multiply(-1)));
        // assertEquals("PPC -१.००", PpcFormat.getInstance(Locale.forLanguageTag("hi-IN")).format(COIN.multiply(-1)));
        assertEquals("PPC -๑.๐๐", PpcFormat.getInstance(new Locale("th","TH","TH")).format(COIN.multiply(-1)));
        assertEquals("Ƀ-๑.๐๐", PpcFormat.getSymbolInstance(new Locale("th","TH","TH")).format(COIN.multiply(-1)));
    }

    /* Warning: these tests assume the state of Locale data extant on the platform on which
     * they were written: openjdk 7u21-2.3.9-5 */
    @Test
    public void equalityTest() throws Exception {
        // First, autodenominator
        assertEquals(PpcFormat.getInstance(), PpcFormat.getInstance());
        assertEquals(PpcFormat.getInstance().hashCode(), PpcFormat.getInstance().hashCode());

        assertNotEquals(PpcFormat.getCodeInstance(), PpcFormat.getSymbolInstance());
        assertNotEquals(PpcFormat.getCodeInstance().hashCode(), PpcFormat.getSymbolInstance().hashCode());

        assertEquals(PpcFormat.getSymbolInstance(5), PpcFormat.getSymbolInstance(5));
        assertEquals(PpcFormat.getSymbolInstance(5).hashCode(), PpcFormat.getSymbolInstance(5).hashCode());

        assertNotEquals(PpcFormat.getSymbolInstance(5), PpcFormat.getSymbolInstance(4));
        assertNotEquals(PpcFormat.getSymbolInstance(5).hashCode(), PpcFormat.getSymbolInstance(4).hashCode());

        /* The underlying formatter is mutable, and its currency code
         * and symbol may be reset each time a number is
         * formatted or parsed.  Here we check to make sure that state is
         * ignored when comparing for equality */
        // when formatting
        PpcAutoFormat a = (PpcAutoFormat)PpcFormat.getSymbolInstance(US);
        PpcAutoFormat b = (PpcAutoFormat)PpcFormat.getSymbolInstance(US);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        a.format(COIN.multiply(1000000));
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        b.format(COIN.divide(1000000));
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        // when parsing
        a = (PpcAutoFormat)PpcFormat.getSymbolInstance(US);
        b = (PpcAutoFormat)PpcFormat.getSymbolInstance(US);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        a.parseObject("mPPC2");
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        b.parseObject("µ฿4.35");
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());

        // FRANCE and GERMANY have different pattterns
        assertNotEquals(PpcFormat.getInstance(FRANCE).hashCode(), PpcFormat.getInstance(GERMANY).hashCode());
        // TAIWAN and CHINA differ only in the Locale and Currency, i.e. the patterns and symbols are
        // all the same (after setting the currency symbols to peercoins)
        assertNotEquals(PpcFormat.getInstance(TAIWAN), PpcFormat.getInstance(CHINA));
        // but they hash the same because of the DecimalFormatSymbols.hashCode() implementation

        assertEquals(PpcFormat.getSymbolInstance(4), PpcFormat.getSymbolInstance(4));
        assertEquals(PpcFormat.getSymbolInstance(4).hashCode(), PpcFormat.getSymbolInstance(4).hashCode());

        assertNotEquals(PpcFormat.getSymbolInstance(4), PpcFormat.getSymbolInstance(5));
        assertNotEquals(PpcFormat.getSymbolInstance(4).hashCode(), PpcFormat.getSymbolInstance(5).hashCode());

        // Fixed-denomination
        assertEquals(PpcFormat.getCoinInstance(), PpcFormat.getCoinInstance());
        assertEquals(PpcFormat.getCoinInstance().hashCode(), PpcFormat.getCoinInstance().hashCode());

        assertEquals(PpcFormat.getMilliInstance(), PpcFormat.getMilliInstance());
        assertEquals(PpcFormat.getMilliInstance().hashCode(), PpcFormat.getMilliInstance().hashCode());

        assertEquals(PpcFormat.getMicroInstance(), PpcFormat.getMicroInstance());
        assertEquals(PpcFormat.getMicroInstance().hashCode(), PpcFormat.getMicroInstance().hashCode());

        assertEquals(PpcFormat.getInstance(-6), PpcFormat.getInstance(-6));
        assertEquals(PpcFormat.getInstance(-6).hashCode(), PpcFormat.getInstance(-6).hashCode());

        assertNotEquals(PpcFormat.getCoinInstance(), PpcFormat.getMilliInstance());
        assertNotEquals(PpcFormat.getCoinInstance().hashCode(), PpcFormat.getMilliInstance().hashCode());

        assertNotEquals(PpcFormat.getCoinInstance(), PpcFormat.getMicroInstance());
        assertNotEquals(PpcFormat.getCoinInstance().hashCode(), PpcFormat.getMicroInstance().hashCode());

        assertNotEquals(PpcFormat.getMilliInstance(), PpcFormat.getMicroInstance());
        assertNotEquals(PpcFormat.getMilliInstance().hashCode(), PpcFormat.getMicroInstance().hashCode());

        assertNotEquals(PpcFormat.getInstance(SMALLEST_UNIT_EXPONENT),
                        PpcFormat.getInstance(SMALLEST_UNIT_EXPONENT - 1));
        assertNotEquals(PpcFormat.getInstance(SMALLEST_UNIT_EXPONENT).hashCode(),
                        PpcFormat.getInstance(SMALLEST_UNIT_EXPONENT - 1).hashCode());

        assertNotEquals(PpcFormat.getCoinInstance(TAIWAN), PpcFormat.getCoinInstance(CHINA));

        assertNotEquals(PpcFormat.getCoinInstance(2,3), PpcFormat.getCoinInstance(2,4));
        assertNotEquals(PpcFormat.getCoinInstance(2,3).hashCode(), PpcFormat.getCoinInstance(2,4).hashCode());

        assertNotEquals(PpcFormat.getCoinInstance(2,3), PpcFormat.getCoinInstance(2,3,3));
        assertNotEquals(PpcFormat.getCoinInstance(2,3).hashCode(), PpcFormat.getCoinInstance(2,3,3).hashCode());


    }

    @Test
    public void attributeTest() throws Exception {
        String codePat = PpcFormat.getCodeInstance(Locale.US).pattern();
        assertTrue(codePat.contains("PPC") && ! codePat.contains("(^|[^฿])฿([^฿]|$)") && ! codePat.contains("(^|[^¤])¤([^¤]|$)"));
        String symPat = PpcFormat.getSymbolInstance(Locale.US).pattern();
        assertTrue(symPat.contains("฿") && !symPat.contains("PPC") && !symPat.contains("¤¤"));

        assertEquals("PPC #,##0.00;(PPC #,##0.00)", PpcFormat.getCodeInstance(Locale.US).pattern());
        assertEquals("฿#,##0.00;(฿#,##0.00)", PpcFormat.getSymbolInstance(Locale.US).pattern());
        assertEquals('0', PpcFormat.getInstance(Locale.US).symbols().getZeroDigit());
        // assertEquals('०', PpcFormat.getInstance(Locale.forLanguageTag("hi-IN")).symbols().getZeroDigit());
        // TODO will this next line work with other JREs?
        assertEquals('๐', PpcFormat.getInstance(new Locale("th","TH","TH")).symbols().getZeroDigit());
    }

    @Test
    public void toStringTest() {
        assertEquals("Auto-format ฿#,##0.00;(฿#,##0.00)", PpcFormat.getSymbolInstance(Locale.US).toString());
        assertEquals("Auto-format ฿#,##0.0000;(฿#,##0.0000)", PpcFormat.getSymbolInstance(Locale.US, 4).toString());
        assertEquals("Auto-format PPC #,##0.00;(PPC #,##0.00)", PpcFormat.getCodeInstance(Locale.US).toString());
        assertEquals("Auto-format PPC #,##0.0000;(PPC #,##0.0000)", PpcFormat.getCodeInstance(Locale.US, 4).toString());
        assertEquals("Coin-format #,##0.00", PpcFormat.getCoinInstance(Locale.US).toString());
        assertEquals("Millicoin-format #,##0.00", PpcFormat.getMilliInstance(Locale.US).toString());
        assertEquals("Microcoin-format #,##0.00", PpcFormat.getMicroInstance(Locale.US).toString());
        assertEquals("Coin-format #,##0.000", PpcFormat.getCoinInstance(Locale.US,3).toString());
        assertEquals("Coin-format #,##0.000(####)(#######)", PpcFormat.getCoinInstance(Locale.US,3,4,7).toString());
        assertEquals("Kilocoin-format #,##0.000", PpcFormat.getInstance(-3,Locale.US,3).toString());
        assertEquals("Kilocoin-format #,##0.000(####)(#######)", PpcFormat.getInstance(-3,Locale.US,3,4,7).toString());
        assertEquals("Decicoin-format #,##0.000", PpcFormat.getInstance(1,Locale.US,3).toString());
        assertEquals("Decicoin-format #,##0.000(####)(#######)", PpcFormat.getInstance(1,Locale.US,3,4,7).toString());
        assertEquals("Dekacoin-format #,##0.000", PpcFormat.getInstance(-1,Locale.US,3).toString());
        assertEquals("Dekacoin-format #,##0.000(####)(#######)", PpcFormat.getInstance(-1,Locale.US,3,4,7).toString());
        assertEquals("Hectocoin-format #,##0.000", PpcFormat.getInstance(-2,Locale.US,3).toString());
        assertEquals("Hectocoin-format #,##0.000(####)(#######)", PpcFormat.getInstance(-2,Locale.US,3,4,7).toString());
        assertEquals("Megacoin-format #,##0.000", PpcFormat.getInstance(-6,Locale.US,3).toString());
        assertEquals("Megacoin-format #,##0.000(####)(#######)", PpcFormat.getInstance(-6,Locale.US,3,4,7).toString());
        assertEquals("Fixed (-4) format #,##0.000", PpcFormat.getInstance(-4,Locale.US,3).toString());
        assertEquals("Fixed (-4) format #,##0.000(####)", PpcFormat.getInstance(-4,Locale.US,3,4).toString());
        assertEquals("Fixed (-4) format #,##0.000(####)(#######)",
                     PpcFormat.getInstance(-4, Locale.US, 3, 4, 7).toString());

        assertEquals("Auto-format ฿#,##0.00;(฿#,##0.00)",
                     PpcFormat.builder().style(SYMBOL).code("USD").locale(US).build().toString());
        assertEquals("Auto-format #.##0,00 $",
                     PpcFormat.builder().style(SYMBOL).symbol("$").locale(GERMANY).build().toString());
        assertEquals("Auto-format #.##0,0000 $",
                     PpcFormat.builder().style(SYMBOL).symbol("$").fractionDigits(4).locale(GERMANY).build().toString());
        assertEquals("Auto-format PPC#,00฿;PPC-#,00฿",
                     PpcFormat.builder().style(SYMBOL).locale(GERMANY).pattern("¤¤#¤").build().toString());
        assertEquals("Coin-format PPC#,00฿;PPC-#,00฿",
                     PpcFormat.builder().scale(0).locale(GERMANY).pattern("¤¤#¤").build().toString());
        assertEquals("Millicoin-format PPC#.00฿;PPC-#.00฿",
                     PpcFormat.builder().scale(3).locale(US).pattern("¤¤#¤").build().toString());
    }

    @Test
    public void patternDecimalPlaces() {
        /* The pattern format provided by DecimalFormat includes specification of fractional digits,
         * but we ignore that because we have alternative mechanism for specifying that.. */
        PpcFormat f = PpcFormat.builder().locale(US).scale(3).pattern("¤¤ #.0").fractionDigits(3).build();
        assertEquals("Millicoin-format PPC #.000;PPC -#.000", f.toString());
        assertEquals("mPPC 1000.000", f.format(COIN));
    }

    @Test
    public void builderTest() {
        Locale locale;
        if (Locale.getDefault().equals(GERMANY)) locale = FRANCE;
        else locale = GERMANY;

        assertEquals(PpcFormat.builder().build(), PpcFormat.getCoinInstance());
        try {
            PpcFormat.builder().scale(0).style(CODE);
            fail("Invoking both scale() and style() on a Builder should raise exception");
        } catch (IllegalStateException e) {}
        try {
            PpcFormat.builder().style(CODE).scale(0);
            fail("Invoking both style() and scale() on a Builder should raise exception");
        } catch (IllegalStateException e) {}

        PpcFormat built = PpcFormat.builder().style(PpcAutoFormat.Style.CODE).fractionDigits(4).build();
        assertEquals(built, PpcFormat.getCodeInstance(4));
        built = PpcFormat.builder().style(PpcAutoFormat.Style.SYMBOL).fractionDigits(4).build();
        assertEquals(built, PpcFormat.getSymbolInstance(4));

        built = PpcFormat.builder().scale(0).build();
        assertEquals(built, PpcFormat.getCoinInstance());
        built = PpcFormat.builder().scale(3).build();
        assertEquals(built, PpcFormat.getMilliInstance());
        built = PpcFormat.builder().scale(6).build();
        assertEquals(built, PpcFormat.getMicroInstance());

        built = PpcFormat.builder().locale(locale).scale(0).build();
        assertEquals(built, PpcFormat.getCoinInstance(locale));
        built = PpcFormat.builder().locale(locale).scale(3).build();
        assertEquals(built, PpcFormat.getMilliInstance(locale));
        built = PpcFormat.builder().locale(locale).scale(6).build();
        assertEquals(built, PpcFormat.getMicroInstance(locale));

        built = PpcFormat.builder().minimumFractionDigits(3).scale(0).build();
        assertEquals(built, PpcFormat.getCoinInstance(3));
        built = PpcFormat.builder().minimumFractionDigits(3).scale(3).build();
        assertEquals(built, PpcFormat.getMilliInstance(3));
        built = PpcFormat.builder().minimumFractionDigits(3).scale(6).build();
        assertEquals(built, PpcFormat.getMicroInstance(3));

        built = PpcFormat.builder().fractionGroups(3,4).scale(0).build();
        assertEquals(built, PpcFormat.getCoinInstance(2,3,4));
        built = PpcFormat.builder().fractionGroups(3,4).scale(3).build();
        assertEquals(built, PpcFormat.getMilliInstance(2,3,4));
        built = PpcFormat.builder().fractionGroups(3,4).scale(6).build();
        assertEquals(built, PpcFormat.getMicroInstance(2,3,4));

        built = PpcFormat.builder().pattern("#,####.#").scale(6).locale(GERMANY).build();
        assertEquals("100.0000,00", built.format(COIN));
        built = PpcFormat.builder().pattern("#,####.#").scale(6).locale(GERMANY).build();
        assertEquals("-100.0000,00", built.format(COIN.multiply(-1)));
        built = PpcFormat.builder().localizedPattern("#.####,#").scale(6).locale(GERMANY).build();
        assertEquals("100.0000,00", built.format(COIN));

        built = PpcFormat.builder().pattern("¤#,####.#").style(CODE).locale(GERMANY).build();
        assertEquals("฿-1,00", built.format(COIN.multiply(-1)));
        built = PpcFormat.builder().pattern("¤¤ #,####.#").style(SYMBOL).locale(GERMANY).build();
        assertEquals("PPC -1,00", built.format(COIN.multiply(-1)));
        built = PpcFormat.builder().pattern("¤¤##,###.#").scale(3).locale(US).build();
        assertEquals("mPPC1,000.00", built.format(COIN));
        built = PpcFormat.builder().pattern("¤ ##,###.#").scale(3).locale(US).build();
        assertEquals("₥฿ 1,000.00", built.format(COIN));

        try {
            PpcFormat.builder().pattern("¤¤##,###.#").scale(4).locale(US).build().format(COIN);
            fail("Pattern with currency sign and non-standard denomination should raise exception");
        } catch (IllegalStateException e) {}

        try {
            PpcFormat.builder().localizedPattern("¤¤##,###.#").scale(4).locale(US).build().format(COIN);
            fail("Localized pattern with currency sign and non-standard denomination should raise exception");
        } catch (IllegalStateException e) {}

        built = PpcFormat.builder().style(SYMBOL).symbol("B\u20e6").locale(US).build();
        assertEquals("B⃦1.00", built.format(COIN));
        built = PpcFormat.builder().style(CODE).code("PPC").locale(US).build();
        assertEquals("PPC 1.00", built.format(COIN));
        built = PpcFormat.builder().style(SYMBOL).symbol("$").locale(GERMANY).build();
        assertEquals("1,00 $", built.format(COIN));
        // Setting the currency code on a DecimalFormatSymbols object can affect the currency symbol.
        built = PpcFormat.builder().style(SYMBOL).code("USD").locale(US).build();
        assertEquals("฿1.00", built.format(COIN));

        built = PpcFormat.builder().style(SYMBOL).symbol("B\u20e6").locale(US).build();
        assertEquals("₥B⃦1.00", built.format(COIN.divide(1000)));
        built = PpcFormat.builder().style(CODE).code("PPC").locale(US).build();
        assertEquals("mPPC 1.00", built.format(COIN.divide(1000)));

        built = PpcFormat.builder().style(SYMBOL).symbol("B\u20e6").locale(US).build();
        assertEquals("µB⃦1.00", built.format(valueOf(100)));
        built = PpcFormat.builder().style(CODE).code("PPC").locale(US).build();
        assertEquals("µPPC 1.00", built.format(valueOf(100)));

        /* The prefix of a pattern can have number symbols in quotes.
         * Make sure our custom negative-subpattern creator handles this. */
        built = PpcFormat.builder().pattern("'#'¤#0").scale(0).locale(US).build();
        assertEquals("#฿-1.00", built.format(COIN.multiply(-1)));
        built = PpcFormat.builder().pattern("'#0'¤#0").scale(0).locale(US).build();
        assertEquals("#0฿-1.00", built.format(COIN.multiply(-1)));
        // this is an escaped quote between two hash marks in one set of quotes, not
        // two adjacent quote-enclosed hash-marks:
        built = PpcFormat.builder().pattern("'#''#'¤#0").scale(0).locale(US).build();
        assertEquals("#'#฿-1.00", built.format(COIN.multiply(-1)));
        built = PpcFormat.builder().pattern("'#0''#'¤#0").scale(0).locale(US).build();
        assertEquals("#0'#฿-1.00", built.format(COIN.multiply(-1)));
        built = PpcFormat.builder().pattern("'#0#'¤#0").scale(0).locale(US).build();
        assertEquals("#0#฿-1.00", built.format(COIN.multiply(-1)));
        built = PpcFormat.builder().pattern("'#0'E'#'¤#0").scale(0).locale(US).build();
        assertEquals("#0E#฿-1.00", built.format(COIN.multiply(-1)));
        built = PpcFormat.builder().pattern("E'#0''#'¤#0").scale(0).locale(US).build();
        assertEquals("E#0'#฿-1.00", built.format(COIN.multiply(-1)));
        built = PpcFormat.builder().pattern("E'#0#'¤#0").scale(0).locale(US).build();
        assertEquals("E#0#฿-1.00", built.format(COIN.multiply(-1)));
        built = PpcFormat.builder().pattern("E'#0''''#'¤#0").scale(0).locale(US).build();
        assertEquals("E#0''#฿-1.00", built.format(COIN.multiply(-1)));
        built = PpcFormat.builder().pattern("''#0").scale(0).locale(US).build();
        assertEquals("'-1.00", built.format(COIN.multiply(-1)));

        // immutability check for fixed-denomination formatters, w/ & w/o custom pattern
        PpcFormat a = PpcFormat.builder().scale(3).build();
        PpcFormat b = PpcFormat.builder().scale(3).build();
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        a.format(COIN.multiply(1000000));
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        b.format(COIN.divide(1000000));
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        a = PpcFormat.builder().scale(3).pattern("¤#.#").build();
        b = PpcFormat.builder().scale(3).pattern("¤#.#").build();
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        a.format(COIN.multiply(1000000));
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        b.format(COIN.divide(1000000));
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());

    }

}

