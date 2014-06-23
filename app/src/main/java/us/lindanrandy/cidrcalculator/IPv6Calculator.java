/*
 * $Id: IPv6Calculator.java 36 2012-07-07 23:54:43Z rmceoin@gmail.com $
 *  
 * Copyright (C) 2010 Randy McEoin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package us.lindanrandy.cidrcalculator;

import java.math.BigInteger;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;

import us.lindanrandy.cidrcalculator.InetAddresses;

//import com.google.common.net.InetAddresses;

public class IPv6Calculator extends Activity {
	
	private String TAG="IPv6Calculator";
	private boolean debug = true;
	
	public final int DEFAULT_BITS=64;
	private TextView msgIPAddress;
	private String CurrentIPv6;
	private int CurrentBitsIPv6;
	Animation anim = null;

	class MulticastAddress {
		String address;
		int description;
		public MulticastAddress(String address,int description){
			this.address=address;
			this.description=description;
		}
	}

	MulticastAddress [] multicastAddresses={
			new MulticastAddress("ff02::1",R.string.allnodes),
			new MulticastAddress("ff02::2",R.string.allrouters),
			new MulticastAddress("ff02::9",R.string.allriprouters),
			new MulticastAddress("ff05::101",R.string.allntpservers),
			new MulticastAddress("ff05::1:3",R.string.alldhcpservers),
	};

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		
		if (debug) Log.d(TAG,"onCreate()");

		anim = AnimationUtils.loadAnimation( this, R.anim.highlight );

		setContentView(R.layout.ipv6calc);

		SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
		CurrentIPv6 = settings.getString(Preferences.PREFERENCE_CURRENTIPv6, "");
		CurrentBitsIPv6 = settings.getInt(Preferences.PREFERENCE_CURRENTBITSIPv6, DEFAULT_BITS);

		Spinner s2 = (Spinner) findViewById(R.id.ipv6subnetmasks);
		ArrayAdapter<CharSequence> subnetmask_adapter = ArrayAdapter.createFromResource(
			this, R.array.ipv6subnetmasks, android.R.layout.simple_spinner_item);
		subnetmask_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		s2.setAdapter(subnetmask_adapter);

		s2.setOnItemSelectedListener(mSubnetMaskSelectedListener); 
		s2.setSelection(CurrentBitsIPv6 - 1);

		Button calculate = (Button)findViewById(R.id.ipv6calculate);
		calculate.setOnClickListener(mCalculateListener);

		Button reset = (Button)findViewById(R.id.ipv6reset);
		reset.setOnClickListener(mResetListener);

		msgIPAddress = (TextView)findViewById(R.id.ipv6address);
		if (CurrentIPv6 != "")
		{
			msgIPAddress.setText(CurrentIPv6);
		}

	}

	@Override
	protected void onStop(){
		super.onStop();

		if (debug) Log.d(TAG,"onStop()");
//    	updateResults(false);
		// Save user preferences. We need an Editor object to
		// make changes. All objects are from android.context.Context
		SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		if (debug) Log.d(TAG,"storing CurrentIPv6=" + CurrentIPv6);
		editor.putString(Preferences.PREFERENCE_CURRENTIPv6, CurrentIPv6);
		editor.putInt(Preferences.PREFERENCE_CURRENTBITSIPv6, CurrentBitsIPv6);

		// Don't forget to commit your edits!!!
		editor.commit();
	}

	private OnItemSelectedListener mSubnetMaskSelectedListener = new OnItemSelectedListener()
	{
		public void onItemSelected(AdapterView<?> parent, View v, int position, long id)
		{
			updateResults(true);
		}
		public void onNothingSelected(AdapterView<?> parent)
		{
		}
	};

	/**
	 * Clear the results fields: Address range, Maximum addresses and Wildcard.
	 */
	private void ClearResults()
	{
		TextView msgAddressRange = (TextView)findViewById(R.id.v6address_range);
		TextView msgMaximumAddresses = (TextView)findViewById(R.id.v6maximum_addresses);
		TextView msgInfo = (TextView)findViewById(R.id.v6info);

		msgAddressRange.setText("");
		msgMaximumAddresses.setText("");
		msgInfo.setText("");
	}

	/** Given an IPv6 address, convert it into a BigInteger.
	* @param addr
	* @return the integer representation of the InetAddress
	*
	*  @throws IllegalArgumentException if the address is not an IPv6
	*  address.
    * from: package org.jboss.netty.handler.ipfilter by 
    * @author frederic bregier
	*//*
	private static BigInteger ipv6AddressToBigInteger(InetAddress addr) {
		byte[] ipv6;
		if (addr instanceof Inet4Address) {
//			ipv6 = getIpV6FromIpV4((Inet4Address) addr);
			ipv6=new byte[16];
			ipv6[0]=-1;
		} else {
			ipv6 = addr.getAddress();
		}
		if (ipv6[0] == -1) {
			return new BigInteger(1, ipv6);
		}
		return new BigInteger(ipv6);
	}*/

	/** Convert a big integer into an IPv6 address.
	* @param addr
	* @return the inetAddress from the integer
	*
	* @throws UnknownHostException if the big integer is too large,
	*  and thus an invalid IPv6 address.
	* from: package org.jboss.netty.handler.ipfilter by 
	* @author frederic bregier
	*/
	private static InetAddress bigIntToIPv6Address(BigInteger addr)
		throws UnknownHostException {
		byte[] a = new byte[16];
		byte[] b = addr.toByteArray();
		if (b.length > 16 && !(b.length == 17 && b[0] == 0)) {
			throw new UnknownHostException("invalid IPv6 address (too big)");
		}
		if (b.length == 16) {
			return InetAddress.getByAddress(b);
		}
		// handle the case where the IPv6 address starts with "FF".
		if (b.length == 17) {
			System.arraycopy(b, 1, a, 0, 16);
		} else {
			// copy the address into a 16 byte array, zero-filled.
			int p = 16 - b.length;
			for (int i = 0; i < b.length; i ++) {
				a[p + i] = b[i];
			}
		}
		return InetAddress.getByAddress(a);
	}
	
	/**
	 * Retrieve the values from the view and perform the calculation.
	 * 
	 * @param updateView - if true then update the view, otherwise just perform the calculation
	 * @return true if a successful calculation was performed
	 */
	private boolean updateResults(boolean updateView)
	{
		TextView msgAddressRange = (TextView)findViewById(R.id.v6address_range);
		TextView msgMaximumAddresses = (TextView)findViewById(R.id.v6maximum_addresses);
		TextView msgInfo = (TextView)findViewById(R.id.v6info);
//    	TextView msgIPBinaryNetwork = (TextView)findViewById(R.id.ipv6_binary_network);
//    	TextView msgIPBinaryHost = (TextView)findViewById(R.id.ipv6_binary_host);
//    	TextView msgIPBinaryNetmask = (TextView)findViewById(R.id.ipv6_binary_netmask);
		Spinner ipv6subnetmasks_spinner = (Spinner)findViewById(R.id.ipv6subnetmasks);

		String ip = msgIPAddress.getText().toString();

		try {
			byte[] addr = InetAddresses.ipStringToBytes(ip);
			if (addr == null) {
				if (debug) Log.d(TAG,"cannot textToNumericFormatV6");
				return false;
			}
			
			
			InetAddress hostAddress = InetAddress.getByAddress(addr);
			
//			InetAddress hostAddress = InetAddresses.forString(ip);
			if (debug) Log.d(TAG,"hostAddress="+hostAddress);
			
			if ((hostAddress instanceof Inet6Address) == false) {
				if (debug) Log.d(TAG,"not IPv6 address");
			}
			
			if (debug) Log.d(TAG,"compressed = "+InetAddresses.toAddrString(hostAddress));
			
			BigInteger ip128bit=new BigInteger(hostAddress.getAddress());
			if (debug) Log.d(TAG,"ip128bit="+ip128bit.toString());

			int bitlength = ipv6subnetmasks_spinner.getSelectedItemPosition() + 1;
			if (debug) Log.d(TAG,"bitlength="+bitlength);
			BigInteger ip128bitmask=BigInteger.ONE;
			ip128bitmask=ip128bitmask.shiftLeft(128-bitlength);
			ip128bitmask=ip128bitmask.subtract(BigInteger.ONE);
			if (debug) Log.d(TAG,"ip128bitmask="+ip128bitmask.toString(16));

			BigInteger firstip = ip128bitmask.xor(new BigInteger("ffffffffffffffffffffffffffffffff",16));
			firstip = firstip.and(ip128bit);
			if (debug) Log.d(TAG,"firstip="+firstip.toString(16));

			byte[] firstIPbytes=firstip.toByteArray();
			if (debug) Log.d(TAG,"firstIPbytes.length="+ firstIPbytes.length);
			InetAddress firstIPv6=bigIntToIPv6Address(firstip);
			
			BigInteger lastip=firstip.or(ip128bitmask);
			if (debug) Log.d(TAG,"lastip="+lastip.toString(16));
			InetAddress lastIPv6=bigIntToIPv6Address(lastip);

			BigInteger maximumAddresses;
			if (ip128bitmask.equals(BigInteger.ZERO))
			{
				maximumAddresses = BigInteger.ZERO;
			}else
			{
				maximumAddresses = ip128bitmask.subtract(BigInteger.ONE);
			}

			String addressInfo="";
			if (hostAddress.isAnyLocalAddress()) {
				addressInfo=addStringWithSpace(addressInfo,getString(R.string.any_local));
			}
			if (hostAddress.isLinkLocalAddress()) {
				addressInfo=addStringWithSpace(addressInfo,getString(R.string.link_local));
			}
			if (hostAddress.isLoopbackAddress()) {
				addressInfo=addStringWithSpace(addressInfo,getString(R.string.loopback));
			}
			if (hostAddress.isMCGlobal()) {
				addressInfo=addStringWithSpace(addressInfo,getString(R.string.mcglobal));
			}
			if (hostAddress.isMCLinkLocal()) {
				addressInfo=addStringWithSpace(addressInfo,getString(R.string.mclink_local));
			}
			if (hostAddress.isMCNodeLocal()) {
				addressInfo=addStringWithSpace(addressInfo,getString(R.string.mcnode_local));
			}
			if (hostAddress.isMCOrgLocal()) {
				addressInfo=addStringWithSpace(addressInfo,getString(R.string.mcorg_local));
			}
			if (hostAddress.isMCSiteLocal()) {
				addressInfo=addStringWithSpace(addressInfo,getString(R.string.mcsite_local));
			}
			if (hostAddress.isMulticastAddress()) {
				addressInfo=addStringWithSpace(addressInfo,getString(R.string.multicast));

				for (int i=0; i < multicastAddresses.length; i++) {
					String address=multicastAddresses[i].address;
					byte[] multiaddr = InetAddresses.ipStringToBytes(address);

					InetAddress multicastAddress = InetAddress.getByAddress(multiaddr);
					if (debug) Log.d(TAG,"checking "+hostAddress.getHostAddress()+
							" against "+multicastAddress.getHostAddress());
					if (hostAddress.getHostAddress().equals(
							multicastAddress.getHostAddress())) {
						addressInfo=addStringWithSpace(addressInfo,
								getString(multicastAddresses[i].description));
						break;
					}
				}
			}
			if (InetAddresses.isMappedIPv4Address(ip)) {
				addressInfo=addStringWithSpace(addressInfo,getString(R.string.mappedipv4));
			}

			
			msgAddressRange.setText(firstIPv6.getHostAddress() +" - "+
					lastIPv6.getHostAddress());
			msgMaximumAddresses.setText(maximumAddresses.toString());
			msgInfo.setText(addressInfo);

			CurrentIPv6=ip;
			CurrentBitsIPv6=bitlength;

		} catch (UnknownHostException e) {
			e.printStackTrace();
			ClearResults();
			return false;
		} catch (NumberFormatException e) {
			e.printStackTrace();
			ClearResults();
			return false;
		}

		if (updateView)
		{
			msgAddressRange.startAnimation(anim);
//        	updateHistory(CurrentIP, CurrentBits);
		}

		return true;
	}

	private String addStringWithSpace(String str1, String str2)
	{
		String out=str1;
		if (str1.length()!=0) {
			out+=", ";
		}
		out+=str2;
		return out;
	}

	private OnClickListener mCalculateListener = new OnClickListener()
	{
		public void onClick(View v)
		{
			doCalculate();
		}
	};

	private void doCalculate()
	{
		if (updateResults(true) == false)
		{
			TextView msgAddressRange = (TextView)findViewById(R.id.v6address_range);

			msgAddressRange.setText(R.string.err_bad_ip);
		}
	}

	private OnClickListener mResetListener = new OnClickListener()
	{
		public void onClick(View v)
		{
			CurrentIPv6="";
			CurrentBitsIPv6=DEFAULT_BITS;
			
			updateFields();
			
			ClearResults();
		}
	};

	private void updateFields()
	{
		msgIPAddress.setText(CurrentIPv6);
		
		Spinner ipv6subnetmask_spinner = (Spinner)findViewById(R.id.ipv6subnetmasks);
		ipv6subnetmask_spinner.setSelection(CurrentBitsIPv6 - 1);
	}

}
