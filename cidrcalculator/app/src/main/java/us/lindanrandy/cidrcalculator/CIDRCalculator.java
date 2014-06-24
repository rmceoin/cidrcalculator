/*
 *
 * Copyright (C) 2008-2014 Randy McEoin
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

import us.lindanrandy.cidrcalculator.CIDRHistory.History;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.inputmethodservice.KeyboardView;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.view.Menu;
import android.widget.ArrayAdapter;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SimpleCursorAdapter.CursorToStringConverter;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.FilterQueryProvider;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.text.InputType;
import android.util.Log;

import static us.lindanrandy.cidrcalculator.R.id.*;

public class CIDRCalculator extends Activity {

	private static final String TAG = CIDRCalculator.class.getSimpleName();
	private static final boolean debug = false;
	
	private TextView msgIPAddress;
	private String CurrentIP;
	private int CurrentBits;
	Animation anim = null;
	private Uri mUri;
    private static final String[] PROJECTION = new String[] {
		History._ID, // 0
		History.IP, // 1
		History.BITS, // 2
	};

	public static final int HISTORY_MENUID = Menu.FIRST;
	public static final int BINCALC_MENUID = Menu.FIRST + 1;
	public static final int IPV6_MENUID = Menu.FIRST + 2;
	public static final int PREFERENCES_MENUID = Menu.FIRST + 3;
	public static final int ABOUT_MENUID = Menu.FIRST + 4;

	public static final int REQUEST_HISTORY = 0;
	public static final int REQUEST_CONVERT = 1;

	CustomKeyboard mCustomKeyboard;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        if (debug) Log.d(TAG,"onCreate()");
        
        anim = AnimationUtils.loadAnimation( this, R.anim.highlight );
       
        setContentView(R.layout.main);

        /*
        Since it gives errors when directly in the xml of the layout, we'll dynamically add
        the keyboard.
           <android.inputmethodservice.KeyboardView
        android:id="@+id/keyboardview"
        android:layout_width="fill_parent"
        android:layout_height="wrap_content"
        android:layout_alignParentBottom="true"
        android:layout_centerHorizontal="true"
        android:focusable="true"
        android:focusableInTouchMode="true"
        android:visibility="gone" />
         */
        RelativeLayout layout = (RelativeLayout) findViewById(R.id.main_outer_layout);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        KeyboardView keyboard = new KeyboardView(this, null);
        keyboard.setId(keyboardview);
        keyboard.setLayoutParams(params);
        keyboard.setFocusable(true);
        keyboard.setFocusableInTouchMode(true);
        keyboard.setVisibility(View.GONE);
        layout.addView(keyboard);

        mCustomKeyboard= new CustomKeyboard(this, keyboardview, R.xml.hexkbd );
        mCustomKeyboard.registerEditText(ipaddress);

        SharedPreferences settings = getPreferences(0);
        CurrentIP = settings.getString("CurrentIP", "");
        CurrentBits = settings.getInt("CurrentBits", 24);

        Spinner s1 = (Spinner) findViewById(bitlength);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.bitlengths, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        s1.setAdapter(adapter);

        Spinner s2 = (Spinner) findViewById(subnetmask);
        ArrayAdapter<CharSequence> subnetmask_adapter = ArrayAdapter.createFromResource(
                this, R.array.subnets, android.R.layout.simple_spinner_item);
        subnetmask_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        s2.setAdapter(subnetmask_adapter);
        
        s1.setOnItemSelectedListener(mBitlengthSelectedListener); 
        s2.setOnItemSelectedListener(mSubnetMaskSelectedListener); 
        s1.setSelection(CurrentBits - 1);
        s2.setSelection(CurrentBits - 1);

        Button calculate = (Button)findViewById(R.id.calculate);
        calculate.setOnClickListener(mCalculateListener);

        Button reset = (Button)findViewById(R.id.reset);
        reset.setOnClickListener(mResetListener);

    	msgIPAddress = (TextView)findViewById(ipaddress);
        if (!CurrentIP.equals(""))
        {
        	msgIPAddress.setText(CurrentIP);
        }
        mUri = History.CONTENT_URI;

        SimpleCursorAdapter adapter2 = new SimpleCursorAdapter(this,
                android.R.layout.simple_dropdown_item_1line, null,
                new String[]{History.IP},
                new int[]{android.R.id.text1}, 0);
        adapter2.setCursorToStringConverter(new HistoryCursorConverter());
        adapter2.setFilterQueryProvider(new FilterQueryProvider() {
            public Cursor runQuery(CharSequence constraint) {
                StringBuilder buffer = null;
                String[] args = null;
                if (constraint != null) {
                    buffer = new StringBuilder();
                    buffer.append("UPPER(");
                    buffer.append(PROJECTION[1]);
                    buffer.append(") GLOB ?");
                    String filter = constraint.toString().toUpperCase() + "*";
                    args = new String[]{filter};
                }
                return getContentResolver().query(mUri, PROJECTION,
                        buffer == null ? null : buffer.toString(),
                        args, History.DEFAULT_SORT_ORDER);
            }
        });
		AutoCompleteTextView ipField = (AutoCompleteTextView) msgIPAddress;
		ipField.setAdapter(adapter2);
		ipField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
		    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		        if (actionId == EditorInfo.IME_ACTION_DONE) {
					doCalculate();
			        Button calculate = (Button)findViewById(R.id.calculate);
			        calculate.requestFocus();
			     // close soft keyboard 
			        InputMethodManager inputManager = (InputMethodManager) 
			        CIDRCalculator.this.getSystemService(Context.INPUT_METHOD_SERVICE); 
			        inputManager.hideSoftInputFromWindow(calculate.getWindowToken(), 
			        		InputMethodManager.HIDE_NOT_ALWAYS); 
		            return true;
		        }
		        return false;
		    }
		});
    }

    @Override
    public void onBackPressed() {
        // NOTE Trap the back key: when the CustomKeyboard is still visible hide it, only when it is invisible, finish activity
        if( mCustomKeyboard.isCustomKeyboardVisible() ) mCustomKeyboard.hideCustomKeyboard(); else this.finish();
    }

    public class HistoryCursorConverter implements
    	CursorToStringConverter
    {
    	public CharSequence convertToString(Cursor theCursor)
    	{
        	if (debug) Log.d(TAG,"convertToString()");
    		// Return the first column of the database cursor
            return theCursor.getString(1);
    	}
    }

    @Override
    protected void onResume(){
    	super.onResume();
    	
    	if (debug) Log.d(TAG,"onResume()");

		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		boolean autocomplete=sp.getBoolean(Preferences.PREFERENCE_AUTOCOMPLETE, true);
		String input_keyboard=sp.getString(Preferences.PREFERENCE_INPUT_KEYBOARD,
				Preferences.PREFERENCE_INPUT_KEYBOARD_DEFAULT);

		AutoCompleteTextView ipField = (AutoCompleteTextView) findViewById(ipaddress);
		if (autocomplete) {
			ipField.setThreshold(3);
		}else
		{
			// fake turning it off by making the threshold out of reach
			ipField.setThreshold(999);
		}

		if (input_keyboard.contentEquals(Preferences.PREFERENCE_INPUT_KEYBOARD_DEFAULT)) {
			ipField.setInputType(InputType.TYPE_CLASS_TEXT);
		} else {
			ipField.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_CLASS_TEXT);
		}
    }
    
    @Override
    protected void onStop(){
    	super.onStop();
    
    	if (debug) Log.d(TAG,"onStop()");
    	updateResults(false);
		// Save user preferences. We need an Editor object to
		// make changes. All objects are from android.context.Context
		SharedPreferences settings = getPreferences(0);
		SharedPreferences.Editor editor = settings.edit();
		if (debug) Log.d(TAG,"storing CurrentIP=" + CurrentIP);
		editor.putString("CurrentIP", CurrentIP);
		editor.putInt("CurrentBits", CurrentBits);
		
		// Don't forget to commit your edits!!!
		editor.commit();
    }

    //  Called only the first time the options menu is displayed.
    //  Create the menu entries.
    //  Menu adds items in the order shown.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	super.onCreateOptionsMenu(menu);
     
    	// Parameters for menu.add are:
    	// group -- Not used here.
    	// id -- Used only when you want to handle and identify the click yourself.
    	// title
    	menu.add(Menu.NONE, HISTORY_MENUID , Menu.NONE, R.string.history)
    		.setIcon(android.R.drawable.ic_menu_recent_history);
    	menu.add(Menu.NONE, BINCALC_MENUID , Menu.NONE, R.string.converter)
			.setIcon(android.R.drawable.ic_menu_share);
    	menu.add(Menu.NONE, IPV6_MENUID , Menu.NONE, R.string.ipv6)
			.setIcon(android.R.drawable.ic_menu_rotate);
    	menu.add(Menu.NONE, PREFERENCES_MENUID , Menu.NONE, R.string.preferences)
			.setIcon(android.R.drawable.ic_menu_preferences);
    	menu.add(Menu.NONE, ABOUT_MENUID , Menu.NONE, R.string.about_dialog_title)
			.setIcon(android.R.drawable.ic_menu_info_details);
    	return true;
    }

	//  Activity callback that lets your handle the selection in the class.
	//  Return true to indicate that you've got it, false to indicate
	//  that it should be handled by a declared handler object for that
	//  item (handler objects are discouraged for reasons of efficiency).
    public boolean onOptionsItemSelected(MenuItem item){
    	switch (item.getItemId()) {
    	case HISTORY_MENUID:
    		Intent intent = new Intent(this, HistoryList.class);
    		intent.setAction(Intent.ACTION_PICK);
    		startActivityForResult(intent, REQUEST_HISTORY);
    		return true;
    	case BINCALC_MENUID:
    		Intent bincalc = new Intent(this, Converter.class);
    		bincalc.putExtra(Converter.EXTRA_IP, CurrentIP);
    		startActivityForResult(bincalc, REQUEST_CONVERT);
    		return true;
    	case IPV6_MENUID:
    		Intent ipv6Intent = new Intent(this, IPv6Calculator.class);
    		startActivity(ipv6Intent);
    		return true;
    	case PREFERENCES_MENUID:
    		Intent prefIntent = new Intent(this, Preferences.class);
    		startActivity(prefIntent);
    		return true;
    	case ABOUT_MENUID:

    		PackageInfo pi;
            String version = "";
			try {
                PackageManager pm = this.getPackageManager();
                String pn = this.getPackageName();
                if (pm==null) {
                    return true;
                }
				pi = pm.getPackageInfo(pn, 0);
				version = pi.versionName;
			} catch (NameNotFoundException e) {
				e.printStackTrace();
			}
    		Dialog about = new AlertDialog.Builder(CIDRCalculator.this)
    		.setIcon(R.drawable.icon)
    		.setTitle(R.string.about_dialog_title)
    		.setPositiveButton(R.string.about_dialog_ok, null)
    		.setMessage(getString(R.string.about_dialog_message,version))
    		.create();
    		about.show();
    		return true;
    	}
    	return false;
    }

    /**
     * Convert a 32-bit unsigned IP to string format.
     * 
     * @param in Integer to convert
     * @return String version of IP
     */
    private String IntIPToString(int in)
    {
    	int quad1 = ((in & 0xFF000000) >> 24) & 0xFF;
    	int quad2 = (in & 0x00FF0000) >> 16;
    	int quad3 = (in & 0x0000FF00) >> 8;
    	int quad4 = (in & 0x000000FF);

        return String.format("%d.%d.%d.%d",quad1,quad2,quad3,quad4);
    }

    /**
     * Update the Subnetmask spinner from the Bitlength spinner.
     */
    private void UpdateSubnetmaskFromBitlength()
    {
        Spinner bitlength_spinner = (Spinner) findViewById(bitlength);
        Spinner subnetmask_spinner = (Spinner) findViewById(subnetmask);
        
        subnetmask_spinner.setSelection(bitlength_spinner.getSelectedItemPosition());
    }

    /**
     * Update the Bitlength spinner from the Subnetmask spinner.
     */
    private void UpdateBitlengthFromSubnetmask()
    {
        Spinner bitlength_spinner = (Spinner) findViewById(bitlength);
        Spinner subnetmask_spinner = (Spinner) findViewById(subnetmask);
        
        bitlength_spinner.setSelection(subnetmask_spinner.getSelectedItemPosition());
    }
    
    private OnItemSelectedListener mBitlengthSelectedListener = new OnItemSelectedListener()
    {
        public void onItemSelected(AdapterView<?> parent, View v, int position, long id)
        {
        	UpdateSubnetmaskFromBitlength();
//        	Log.d(TAG,"OnItemSelectedListener Bitlength, position="+position+" id="+id);
        	updateResults(true);
        }
        public void onNothingSelected(AdapterView<?> parent)
        {
        	UpdateSubnetmaskFromBitlength();
        }
    	
    };

    private OnItemSelectedListener mSubnetMaskSelectedListener = new OnItemSelectedListener()
    {
        public void onItemSelected(AdapterView<?> parent, View v, int position, long id)
        {
        	UpdateBitlengthFromSubnetmask();
        	updateResults(true);
        }
        public void onNothingSelected(AdapterView<?> parent)
        {
        	UpdateBitlengthFromSubnetmask();
        }
    	
    };

    /**
     * Clear the results fields: Address range, Maximum addresses and Wildcard.
     */
    private void ClearResults()
    {
    	TextView msgAddressRange = (TextView)findViewById(address_range);
    	TextView msgMaximumAddresses = (TextView)findViewById(maximum_addresses);
    	TextView msgWildcard = (TextView)findViewById(wildcard);
    	TextView msgIPBinaryNetwork = (TextView)findViewById(ip_binary_network);
    	TextView msgIPBinaryHost = (TextView)findViewById(ip_binary_host);
    	TextView msgIPBinaryNetmask = (TextView)findViewById(ip_binary_netmask);

        msgAddressRange.setText("");
        msgMaximumAddresses.setText("");
        msgWildcard.setText("");
        msgIPBinaryNetwork.setText("");
        msgIPBinaryHost.setText("");
        msgIPBinaryNetmask.setText("");
    }

    public static int stringIPtoInt(String ip) throws Exception
    {
    	String[] quad = ip.split("\\.", 4);
    	if (quad.length != 4)
    	{
    		throw new Exception();
    	}
    	int ip32bit = 0;
        for (String value : quad) {
            if (value.length() < 1) {
                throw new Exception();
            }
            int octet;
            try {
                octet = Integer.parseInt(value);
            } catch (NumberFormatException e) {
                throw new Exception();
            }

            if (octet > 255) {
                throw new Exception();
            }
            ip32bit = ip32bit << 8;
            ip32bit = ip32bit | octet;
        }
    	return ip32bit;
    }
    /**
     * Retrieve the values from the view and perform the calculation.
     * 
     * @param updateView - if true then update the view, otherwise just perform the calculation
     * @return true if a successful calculation was performed
     */
    private boolean updateResults(boolean updateView)
    {
    	TextView msgAddressRange = (TextView)findViewById(address_range);
    	TextView msgMaximumAddresses = (TextView)findViewById(maximum_addresses);
    	TextView msgWildcard = (TextView)findViewById(wildcard);
    	TextView msgIPBinaryNetwork = (TextView)findViewById(ip_binary_network);
    	TextView msgIPBinaryHost = (TextView)findViewById(ip_binary_host);
    	TextView msgIPBinaryNetmask = (TextView)findViewById(ip_binary_netmask);
        Spinner bitlength_spinner = (Spinner)findViewById(bitlength);

        CharSequence ipAddressText = msgIPAddress.getText();
        if (ipAddressText==null) {
            return false;
        }
    	String ip = ipAddressText.toString();
    	int ip32bit;
		try {
			ip32bit = stringIPtoInt(ip);
		} catch (Exception e) {
    		ClearResults();
	        return false;
		}
        String selectedItem = (String)bitlength_spinner.getSelectedItem();
        if (selectedItem==null) {
            return false;
        }
        int bitlength = Integer.parseInt(
                selectedItem.substring(1));
		if (debug) Log.d(TAG,"bitlength="+bitlength);

        int ip32bitmask = (1 << (32-bitlength)) - 1;
        
        int firstip = ip32bit & (~ip32bitmask);
        int lastip = firstip | ip32bitmask;
        
        String ipFirst = IntIPToString(firstip);
        String ipLast = IntIPToString(lastip);
        
        int maximumAddresses;
        if (ip32bitmask>0)
        {
        	maximumAddresses = ip32bitmask - 1;
        }else
        {
        	maximumAddresses = 0;
        }

        String wildcard = IntIPToString(ip32bitmask);
        String binary = Converter.convertIPIntDec2StringBinary(ip32bit);
        int netmask=(~ip32bitmask);
        String binaryNetmask = Converter.convertIPIntDec2StringBinary(netmask);

        if (updateView)
        {
	        msgAddressRange.setText(ipFirst + " - " + ipLast);
	        msgMaximumAddresses.setText(String.format("%d", maximumAddresses));
	        msgWildcard.setText(wildcard);
	        
	        int networkHostCutoff;
	        if (bitlength>=24) {
	        	networkHostCutoff=bitlength+3;
	        } else if (bitlength>=16) {
	        	networkHostCutoff=bitlength+2;
	        } else if (bitlength>=8) {
	        	networkHostCutoff=bitlength+1;
	        }else {
	        	networkHostCutoff=bitlength;
	        }
	        String binary_network=binary.substring(0, networkHostCutoff);
	        String binary_host=binary.substring(networkHostCutoff);
	        msgIPBinaryNetwork.setText(binary_network);
	        msgIPBinaryHost.setText(binary_host);
	        msgIPBinaryNetmask.setText(binaryNetmask);
        }

        CurrentIP=ip;
        CurrentBits=bitlength;

        if (updateView)
        {
        	msgAddressRange.startAnimation(anim);
        	updateHistory(CurrentIP, CurrentBits);
        }
        
        return true;
    }
    
    private void updateHistory(String ip, int bits)
    {
    	if (debug) Log.d(TAG,"updateHistory("+ip+","+bits+")");
    	ContentValues values = new ContentValues();
    	values.put(History.IP, ip);
    	values.put(History.BITS, bits);
        values.put(History.MODIFIED_DATE, System.currentTimeMillis());

        String selection= History.IP + "=?";
        String[] selectionArgs=new String[1];
        selectionArgs[0]=ip;
        
        Cursor cursor=getContentResolver().query(mUri, PROJECTION, selection, selectionArgs, null);

        if ((cursor==null) || cursor.getCount()==0) {
    		Uri uri=getContentResolver().insert(mUri, values);
        	if (debug) Log.d(TAG,"updateHistory: inserted uri="+uri);
        	if (cursor!=null) cursor.close();
        } else {
        	if (debug) Log.d(TAG,"count="+cursor.getCount());
        	cursor.moveToFirst();
        	int id=cursor.getInt(0);
        	Uri uri=ContentUris.withAppendedId(mUri, id);
            if (uri==null) {
                return;
            }
        	int count=getContentResolver().update(uri, values, null, null);
        	if (count==0) {
        		Log.e(TAG,"unable to update");
        	}
            cursor.close();
        	if (debug) Log.d(TAG,"updateHistory: updated "+uri+" with ip="+ip);
        }
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
        if (!updateResults(true))
        {
        	TextView msgAddressRange = (TextView)findViewById(address_range);

        	msgAddressRange.setText(R.string.err_bad_ip);
        }
    }

    private OnClickListener mResetListener = new OnClickListener()
    {
        public void onClick(View v)
        {
        	CurrentIP="";
        	CurrentBits=24;

        	updateFields();
            
        	ClearResults();
        }
    };
    
    private void updateFields()
    {
    	msgIPAddress.setText(CurrentIP);

        Spinner bitlength_spinner = (Spinner)findViewById(bitlength);
        bitlength_spinner.setSelection(CurrentBits - 1);
    }
    
	protected void onActivityResult (int requestCode, int resultCode, Intent data) {
		if (requestCode==REQUEST_HISTORY) {
			if (resultCode==RESULT_OK) {
				Uri uri=data.getData();
				if (debug) Log.d(TAG,"got: "+uri);
                if (uri==null) {
                    return;
                }
		        Cursor cursor=getContentResolver().query(uri, PROJECTION, null, null, null);
		        if (cursor!=null) {
		        	cursor.moveToFirst();
		        	CurrentIP=cursor.getString(1);
		        	CurrentBits=cursor.getInt(2);
		        	cursor.close();

		        	updateFields();
		        	updateResults(true);
		        }
			}
		} else if (requestCode==REQUEST_CONVERT) {
			if (resultCode==RESULT_OK) {
				CurrentIP=data.getStringExtra(Converter.EXTRA_IP);
		    	msgIPAddress.setText(CurrentIP);
			}			
		}
	}
}
