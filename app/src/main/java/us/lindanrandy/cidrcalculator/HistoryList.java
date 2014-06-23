/* $Id: HistoryList.java 13 2009-04-25 04:09:55Z rmceoin $
 * 
 * Copyright (C) 2009 Randy McEoin
 * 
 * Original version came from Notepad sample.
 * 
 * Copyright (C) 2007 The Android Open Source Project
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

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

/**
 * Displays the history of IP addresses. Will display notes from the {@link Uri}
 * provided in the intent if there is one, otherwise defaults to displaying the
 * contents of the {@link HistoryProvider}
 */
public class HistoryList extends ListActivity implements LoaderManager.LoaderCallbacks<Cursor>{
    private static final String TAG = "HistoryList";

    // Menu item ids
    public static final int MENU_ITEM_DELETE_ALL = Menu.FIRST;
    public static final int MENU_ITEM_DELETE = Menu.FIRST + 1;

    /**
     * The columns we are interested in from the database
     */
    private static final String[] PROJECTION = new String[] {
            History._ID, // 0
            History.IP, // 1
            History.BITS, // 2
    };

    /** The index of the IP column */
    private static final int COLUMN_INDEX_IP = 1;
   
	// This is the Adapter being used to display the list's data.
	SimpleCursorAdapter mAdapter;
	
	// If non-null, this is the current filter the user has provided.
	String mCurFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setDefaultKeyMode(DEFAULT_KEYS_SHORTCUT);

        // If no data was given in the intent (because we were started
        // as a MAIN activity), then use our default content provider.
        Intent intent = getIntent();
        if (intent.getData() == null) {
            intent.setData(History.CONTENT_URI);
        }

        // Inform the list we provide context menus for items
        getListView().setOnCreateContextMenuListener(this);
/*
        // Perform a managed query. The Activity will handle closing and requerying the cursor
        // when needed.
        Cursor cursor = managedQuery(getIntent().getData(), PROJECTION, null, null,
                History.DEFAULT_SORT_ORDER);

        // Used to map history entries from the database to views
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.historylist_item, cursor,
                new String[] { History.IP, History.BITS },
                new int[] { R.id.history_ip, R.id.history_bits });
*/
        mAdapter = new SimpleCursorAdapter(this,
        		R.layout.historylist_item, null,
                new String[] { History.IP, History.BITS },
                new int[] { R.id.history_ip, R.id.history_bits }, 0);
        
        setListAdapter(mAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

    	menu.add(Menu.NONE, MENU_ITEM_DELETE_ALL , Menu.NONE, R.string.delete_all)
    		.setIcon(android.R.drawable.ic_menu_delete);

        // Generate any additional actions that can be performed on the
        // overall list.  In a normal install, there are no additional
        // actions found here, but this allows other applications to extend
        // our menu with their own actions.
        Intent intent = new Intent(null, getIntent().getData());
        intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
        menu.addIntentOptions(Menu.CATEGORY_ALTERNATIVE, 0, 0,
                new ComponentName(this, HistoryList.class), null, intent, 0, null);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case MENU_ITEM_DELETE_ALL:
        	Dialog about = new AlertDialog.Builder(this)
        	.setIcon(android.R.drawable.ic_delete)
        	.setTitle(R.string.delete_all)
        	.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
        		public void onClick(DialogInterface dialog, int whichButton) {
                    getContentResolver().delete(History.CONTENT_URI, null, null);
        		}
        	})
        	.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
        		public void onClick(DialogInterface dialog, int whichButton) {
        		}
        	}) 
        	.setMessage(R.string.dialog_delete_history_msg)
        	.create();
        	about.show();

        	return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
        AdapterView.AdapterContextMenuInfo info;
        try {
             info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        } catch (ClassCastException e) {
            Log.e(TAG, "bad menuInfo", e);
            return;
        }

        Cursor cursor = (Cursor) getListAdapter().getItem(info.position);
        if (cursor == null) {
            // For some reason the requested item isn't available, do nothing
            return;
        }

        // Setup the menu header
        menu.setHeaderTitle(cursor.getString(COLUMN_INDEX_IP));

        // Add a menu item to delete the note
        menu.add(0, MENU_ITEM_DELETE, 0, R.string.delete);
    }
        
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info;
        try {
             info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        } catch (ClassCastException e) {
            Log.e(TAG, "bad menuInfo", e);
            return false;
        }

        switch (item.getItemId()) {
            case MENU_ITEM_DELETE: {
                // Delete the IP that the context menu is for
                Uri historyUri = ContentUris.withAppendedId(getIntent().getData(), info.id);
                getContentResolver().delete(historyUri, null, null);
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        Uri uri = ContentUris.withAppendedId(getIntent().getData(), id);
        
        String action = getIntent().getAction();
        if (Intent.ACTION_PICK.equals(action) || Intent.ACTION_GET_CONTENT.equals(action)) {
            // The caller is waiting for us to return a note selected by
            // the user.  The have clicked on one, so return it now.
            setResult(RESULT_OK, new Intent().setData(uri));
            finish();
        }
    }

	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		// This is called when a new Loader needs to be created. This
		// sample only has one Loader, so we don't care about the ID.
		// First, pick the base URI to use depending on whether we are
		// currently filtering.
		Uri baseUri;
		if (mCurFilter != null) {
//			baseUri = Uri.withAppendedPath(History.CONTENT_FILTER_URI,
//					Uri.encode(mCurFilter));
			baseUri = History.CONTENT_URI;
		} else {
			baseUri = History.CONTENT_URI;
		}

		// Now create and return a CursorLoader that will take care of
		// creating a Cursor for the data being displayed.
//		String select = "((" + Contacts.DISPLAY_NAME + " NOTNULL) AND ("
//				+ Contacts.HAS_PHONE_NUMBER + "=1) AND ("
//				+ Contacts.DISPLAY_NAME + " != '' ))";
		return new CursorLoader(this, baseUri,
				PROJECTION, null, null,
				History.IP + " COLLATE LOCALIZED ASC");
	}

	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		// Swap the new cursor in. (The framework will take care of closing the
		// old cursor once we return.)
		mAdapter.swapCursor(data);

		// The list should now be shown.
//		if (isResumed()) {
//			setListShown(true);
//		} else {
//			setListShownNoAnimation(true);
//		}
	}

	public void onLoaderReset(Loader<Cursor> loader) {
		// This is called when the last Cursor provided to onLoadFinished()
		// above is about to be closed. We need to make sure we are no
		// longer using it.
		mAdapter.swapCursor(null);
	}
}
