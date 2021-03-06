/*
 * Copyright (C) 2008 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package edu.berkeley.security.eventtracker.eventdata;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;

public class EventDbAdapter extends AbstractDbAdapter {

	public static final String KEY_NAME = "eventname";
	public static final String KEY_NOTES = "notes";
	public static final String KEY_START_TIME = "startTime";
	public static final String KEY_END_TIME = "endTime";
	public static final String KEY_ROWID = "_id";
	private static final String DATABASE_TABLE = "eventData";

	/**
	 * Constructor - takes the context to allow the database to be
	 * opened/created
	 * 
	 * @param ctx
	 *            the Context within which to work
	 */
	public EventDbAdapter(Context ctx) {
		super(ctx);
	}

	/**
	 * Create a new entry in the database corresponding to the given eventName,
	 * notes, and startTime
	 */
	public Long createEvent(String eventName, String notes, long startTime,
			long endTime) {
		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_NAME, eventName);
		initialValues.put(KEY_NOTES, notes);
		initialValues.put(KEY_START_TIME, startTime);
		initialValues.put(KEY_END_TIME, endTime);
		return mDb.insert(DATABASE_TABLE, null, initialValues);
	}

	/**
	 * Delete the activity with the given rowId
	 * 
	 * @param rowId
	 *            id of note to delete
	 * @return true if deleted, false otherwise
	 */
	public boolean deleteEvent(Long rowId) {

		return mDb.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
	}

	/**
	 * Return a Cursor over the list of all events in the database
	 * 
	 * @return Cursor over all notes
	 */
	public Cursor fetchAllEvents() {
		return mDb.query(DATABASE_TABLE, new String[] { KEY_ROWID, KEY_NAME,
				KEY_NOTES, KEY_START_TIME, KEY_END_TIME }, null, null, null,
				null, null);
	}

	/**
	 * Return a Cursor over the list of event that correspond to today. Sorted
	 * by end Time
	 * 
	 * @return Cursor
	 */
	public Cursor fetchSortedEvents() {
		String orderBy = "startTime DESC";
		String limitClause = "20";
		return mDb.query(DATABASE_TABLE, new String[] { KEY_ROWID, KEY_NAME,
				KEY_NOTES, KEY_START_TIME, KEY_END_TIME }, null, null, null,
				null, orderBy, limitClause);
	}

	/**
	 * Return a Cursor positioned at the note that matches the given rowId
	 * 
	 * @param rowId
	 *            id of note to retrieve
	 * @return Cursor positioned to matching note, if found
	 * @throws SQLException
	 *             if note could not be found/retrieved
	 */
	public Cursor fetchEvent(long rowId) throws SQLException {

		Cursor mCursor =

		mDb.query(true, DATABASE_TABLE, new String[] { KEY_ROWID, KEY_NAME,
				KEY_NOTES, KEY_START_TIME, KEY_END_TIME }, KEY_ROWID + "="
				+ rowId, null, null, null, null, null);
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}

	public ArrayList<String> getEvents() {
		ArrayList<String> toReturn = new ArrayList<String>();
		Cursor c = this.fetchAllEvents();
		if (c == null) {
			return null;
		}
		if (c.getCount() > 0) {
			while (c.moveToNext()) {
				String activity = c.getString(c
						.getColumnIndex((EventDbAdapter.KEY_NAME)));
				if (!toReturn.contains(activity))
					toReturn.add(activity);
			}
		}

		c.close();
		return toReturn;
	}

	public ArrayList<String> getNotes() {
		ArrayList<String> toReturn = new ArrayList<String>();
		Cursor c = this.fetchAllEvents();
		if (c == null) {
			return null;
		}
		if (c.getCount() > 0) {

			while (c.moveToNext()) {
				String notes = c.getString(c
						.getColumnIndex((EventDbAdapter.KEY_NOTES)));
				if (!toReturn.contains(notes))
					toReturn.add(notes);
			}
		}

		c.close();
		return toReturn;
	}

	/**
	 * Update the event using the details provided.
	 * 
	 * @param rowId
	 *            id of event to update.
	 * @param title
	 *            value to set event title to.
	 * @param notes
	 *            value to set event notes to.
	 * @param startTime
	 *            value to set event start time to.
	 * @param endTime
	 *            value to set event end time to to.
	 * 
	 * @return true if the note was successfully updated, false otherwise.
	 */
	public boolean updateEvent(Long rowId, String title, String notes,
			Long startTime, Long endTime) {
		ContentValues args = new ContentValues();
		args.put(KEY_NAME, title);
		args.put(KEY_NOTES, notes);
		args.put(KEY_START_TIME, startTime);
		args.put(KEY_END_TIME, endTime);
		return mDb.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
	}
}
