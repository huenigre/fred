/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */
package freenet.support;

import java.util.Locale;

import junit.framework.TestCase;

/**
 * Test case for {@link freenet.support.TimeUtil} class.
 * 
 * @author Alberto Bacchelli &lt;sback@freenetproject.org&gt;
 */
public class TimeUtilTest extends TestCase {

	//1w+1d+1h+1m+1s+1ms
	private long oneForTermLong = 694861001;
	
	@Override
	protected void setUp() throws Exception {
		Locale.setDefault(Locale.US);
	}
	
	/**
	 * Tests formatTime(long,int,boolean) method
	 * trying the biggest long value
	 */
	public void testFormatTime_LongIntBoolean_MaxValue() {
		String expectedForMaxLongValue = "15250284452w3d7h12m55.807s";
		assertEquals(TimeUtil.formatTime(Long.MAX_VALUE,6,true),
				expectedForMaxLongValue);
	}

	/**
	 * Tests formatTime(long,int) method
	 * trying the biggest long value
	 */
	public void testFormatTime_LongInt() {
		String expectedForMaxLongValue = "15250284452w3d7h12m55s";
		assertEquals(TimeUtil.formatTime(Long.MAX_VALUE,6),
				expectedForMaxLongValue);
	}
	
	/**
	 * Tests formatTime(long) method
	 * trying the biggest long value
	 */
	public void testFormatTime_Long() {
		//it uses two terms by default
		String expectedForMaxLongValue = "15250284452w3d";
		assertEquals(TimeUtil.formatTime(Long.MAX_VALUE),
				expectedForMaxLongValue);
	}
	
	/**
	 * Tests formatTime(long) method
	 * using known values.
	 * They could be checked using Google Calculator
	 * http://www.google.com/intl/en/help/features.html#calculator
	 */
	public void testFormatTime_KnownValues() {
		Long methodLong;
		String[][] valAndExpected = {
				//one week
				{"604800000","1w"},	
				//one day
				{"86400000","1d"},	
				//one hour
				{"3600000","1h"},	
				//one minute
				{"60000","1m"},		
				//one second
				{"1000","1s"}		
		};
		for(int i = 0; i < valAndExpected.length; i++) {
			methodLong = Long.valueOf(valAndExpected[i][0]);
			assertEquals(TimeUtil.formatTime(methodLong.longValue()),
					valAndExpected[i][1]); }	
	}
	
	/**
	 * Tests formatTime(long,int) method
	 * using a long value that generate every possible
	 * term kind. It tests if the maxTerms arguments
	 * works correctly
	 */
	public void testFormatTime_LongIntBoolean_maxTerms() {
		String[] valAndExpected = {
				//0 terms
				"",					
				//1 term
				"1w",				
				//2 terms
				"1w1d",				
				//3 terms
				"1w1d1h",			
				//4 terms
				"1w1d1h1m",			
				//5 terms
				"1w1d1h1m1s",		
				//6 terms
				"1w1d1h1m1.001s"	
		};
		for(int i = 0; i < valAndExpected.length; i++)
			assertEquals(TimeUtil.formatTime(oneForTermLong,i,true),
					valAndExpected[i]);
	}
	
	/**
	 * Tests formatTime(long,int) method
	 * using one millisecond time interval. 
	 * It tests if the withSecondFractions argument
	 * works correctly
	 */
	public void testFormatTime_LongIntBoolean_milliseconds() {
		long methodValue = 1;	//1ms
		assertEquals(TimeUtil.formatTime(methodValue,6,false),"0");
		assertEquals(TimeUtil.formatTime(methodValue,6,true),"0.001s");
	}
	
	/**
	 * Tests formatTime(long,int) method
	 * using a long value that generate every possible
	 * term kind. It tests if the maxTerms arguments
	 * works correctly
	 */
	public void testFormatTime_LongIntBoolean_tooManyTerms() {	
		try {
			TimeUtil.formatTime(oneForTermLong,7);
			fail("Expected IllegalArgumentException not thrown"); }
		catch (IllegalArgumentException anException) {
			assertNotNull(anException); }
	}

}
