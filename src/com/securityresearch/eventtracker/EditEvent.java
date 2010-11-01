package com.securityresearch.eventtracker;


import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewStub;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class EditEvent extends Activity {
	private static final int trackingStringID = R.string.toolbarTracking;
	private static final int notTrackingStringID = R.string.toolbarNotTracking;
	private static final int previousEventTextID = R.string.previousActivityText;
	private static final int previousEventDefaultID = R.string.previousActivityDefault;

	private EventDbAdapter mDbHelper;
	private EventEntry currentEvent;
	private EventEntry previousEvent;

	private ArrayList<String> autoCompleteActivities=new ArrayList<String>();
	private ArrayList<String> autoCompleteLocations=new ArrayList<String>();

	private ArrayAdapter<String> adapterActivities;
	private ArrayAdapter<String> adapterLocations;
	private AutoCompleteTextView editTextEventName;
	private AutoCompleteTextView editTextEventLocation;
	private Button previousActivityBar;
	private Button nextActivityButton;
	private Button stopTrackingButton;
	private TextView textViewStartTime;
	private TextView textViewIsTracking;


	/** Called when the activity is first created. */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		ViewStub v =(ViewStub) findViewById(R.id.content_view);
		v.setLayoutResource(R.layout.edit_event);
		v.inflate();

		mDbHelper = new EventDbAdapter(this);
		mDbHelper.open();

		initializeToolbar();
		initializeEditTexts();
		initializeAutoComplete();

		textViewStartTime = (TextView) findViewById(R.id.startTime);
		previousActivityBar = (Button) findViewById(R.id.previous_activity_bar);
		previousActivityBar.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				startListEventsActivity();
			}
		});

		initializeActivityButtons();
		editTextEventName.setHint(getString(R.string.eventNameHint));
		editTextEventLocation.setHint(getString(R.string.eventLocationHint));
	}

	/**
	 * Initializes the NextActivity and StopTracking buttons.
	 */
	private void initializeActivityButtons() {
		nextActivityButton=(Button)findViewById(R.id.NextActivityButton);
		stopTrackingButton=(Button)findViewById(R.id.StopTrackingButton);

		nextActivityButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finishCurrentActivity(true);
				focusOnNothing();
			}
		});

		stopTrackingButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finishCurrentActivity(false);
				focusOnNothing();

			}
		});
	}

	/**
	 * Finishes the currently running activity and start tracking
	 * a new activity, if specified.
	 * @param createNewActivity Whether or not to start tracking a new activity.
	 */
	private void finishCurrentActivity(boolean createNewActivity) {
		currentEvent.mEndTime = System.currentTimeMillis();
		updateAutoComplete();
		updateDatabase(currentEvent);
		previousEvent = currentEvent;
		currentEvent = createNewActivity ? new EventEntry() : null;
		updateUI();
	}

	/**
	 * Initializes the AutoCompleteTextViews and intializes references to related views.
	 */
	private void initializeEditTexts() {
		editTextEventName = (AutoCompleteTextView) findViewById(R.id.editEventName);
		editTextEventLocation = (AutoCompleteTextView) findViewById(R.id.editLocation);
//		TODO uncomment these to disable soft keyboard
//		editTextEventName.setInputType(0); 
//		editTextEventLocation.setInputType(0);

		adapterActivities = new ArrayAdapter<String>(this,
				android.R.layout.simple_dropdown_item_1line, autoCompleteActivities);
		adapterLocations = new ArrayAdapter<String>(this,
				android.R.layout.simple_dropdown_item_1line, autoCompleteLocations);

		editTextEventName.setAdapter(adapterActivities);
		editTextEventLocation.setAdapter(adapterLocations);


		editTextEventName.addTextChangedListener(new StartTrackingListener());
		editTextEventLocation.addTextChangedListener(new StartTrackingListener());
	}

	/**
	 * Listens for a text change and creates a new event if one doesn't exist.
	 * @author AlexD
	 *
	 */
	private class StartTrackingListener implements TextWatcher {

		@Override
		public void afterTextChanged(Editable s) { 
			if (s.length() != 0 && currentEvent == null) {
				currentEvent = new EventEntry();
				updateStartTime();
				updateTrackingUI();
			}
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {}

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {}
	}

	/**
	 * Initializes the toolbar onClickListeners and intializes references to toolbar views.
	 */
	private void initializeToolbar() {		
		textViewIsTracking = (TextView) findViewById(R.id.toolbar_center);
		((ImageView) findViewById(R.id.toolbar_left_option)).setImageResource(R.drawable.list_icon);

		findViewById(R.id.toolbar_right_option).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				startSettingsActivity();
			}
		});

		findViewById(R.id.toolbar_left_option).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				startListEventsActivity();
			}
		});
	}

	/**
	 * Launches the Settings activity.
	 */
	private void startSettingsActivity() {
		Intent settingsIntent = new Intent(EditEvent.this, Settings.class);
		settingsIntent.putExtra(getString(R.string.isTracking), isTracking());
		startActivity(settingsIntent);
	}

	/**
	 * Launches the ListEvents activity.
	 */
	private void startListEventsActivity() {
		Intent listIntent = new Intent(EditEvent.this, ListEvents.class);
		listIntent.putExtra(getString(R.string.isTracking), isTracking());
		startActivity(listIntent);
	}

	/**
	 * Updates the UI using the currentEvent and previousEvent.
	 */
	private void updateUI() {
		updateTrackingUI();
		fillViewWithEventInfo();
	}

	@Override
	protected void onResume() {
		super.onResume();


		List<EventEntry> events = getLatestEvents(2);
		if (events.size() != 0) {
			EventEntry event = events.remove(0);
			if (event.mEndTime != 0) {
				// We aren't tracking
				currentEvent = null;
				previousEvent = event;
			} else {
				// We are tracking
				currentEvent = event;
				previousEvent = events.size() != 0 ? events.remove(0) : null;
			}
		} else {
			currentEvent = null;
			previousEvent = null;
		}
		initializeAutoComplete();
		updateUI();

		focusOnNothing();

	}

	/**
	 * Fills the text entries and views with the correct info based on the
	 * current/previous events.
	 */
	private void fillViewWithEventInfo() {
		if (currentEvent != null) {
			editTextEventName.setText(currentEvent.mName != null ? currentEvent.mName : "");
			editTextEventLocation.setText(currentEvent.mLocation != null ? currentEvent.mLocation : "");
			// TODO make this not in ListEvents
			textViewStartTime.setText(ListEvents.getDateString(currentEvent.mStartTime));
		} else {
			editTextEventName.setText("");
			editTextEventLocation.setText("");
			textViewStartTime.setText("");
		}
		previousActivityBar.setText(getPreviousEventString());
	}

	private void updateStartTime() {
		if (currentEvent != null)
			textViewStartTime.setText(ListEvents.getDateString(currentEvent.mStartTime));
		else
			textViewStartTime.setText("");
	}

	/**
	 * @return The text that the previous event bar should have, based on the previousEvent.
	 */
	private String getPreviousEventString() {
		String previousActivityLabel = getString(previousEventTextID);
		String previousEventString = previousEvent != null ? previousEvent.mName : getString(previousEventDefaultID); 
		return previousActivityLabel + " " + previousEventString;
	}

	@Override
	protected void onPause() {
		super.onPause();
		updateAutoComplete();
		updateDatabase(currentEvent);
	}

	/**
	 * Changes the appearance of this activity to reflect the fact that this activity is now tracking
	 */
	private void updateTrackingUI(){
		boolean isTracking = isTracking();
		textViewIsTracking.setText(isTracking ? trackingStringID : notTrackingStringID);
		nextActivityButton.setEnabled(isTracking);
		stopTrackingButton.setEnabled(isTracking);	

	}

	/**
	 * Creates a database entry for the EventEntry that is in progress
	 * Updates the EventEntry with its corresponding rowID for the database
	 * @param entry- the entry of the activity thats in progress
	 * @return false if an error occurred, otherwise true. 
	 */
	private boolean updateDatabase(EventEntry event) {
		if (event == null)
			return true;
		event.mName = editTextEventName.getText().toString();
		event.mLocation = editTextEventLocation.getText().toString();
		if(event.mDbRowID==-1) {
			event.mDbRowID = mDbHelper.createEvent(event.mName, event.mLocation, event.mStartTime, event.mEndTime);
			return event.mDbRowID != -1;
		} else {
			return mDbHelper.updateEvent(event.mDbRowID, event.mName, 
					event.mLocation, event.mStartTime,event.mEndTime);
		}
	}

	/**
	 * Queries the database for the events in sorted order.
	 * @param numEvents The number of events to return.
	 * @return  A list of EventEntry objects.
	 */
	private List<EventEntry> getLatestEvents(int numEvents){
		List<EventEntry> eventList=new LinkedList<EventEntry>();
		Cursor sortedEvents=mDbHelper.fetchSortedEvents();
		if (sortedEvents.getCount() > 0) {
			while (sortedEvents.moveToNext() && numEvents > 0) {
				long dbRowID =		getLong(sortedEvents, EventDbAdapter.KEY_ROWID);
				String name =		getString(sortedEvents, EventDbAdapter.KEY_NAME);
				String location =	getString(sortedEvents, EventDbAdapter.KEY_LOCATION);
				long startTime =	getLong(sortedEvents, EventDbAdapter.KEY_START_TIME);
				long endTime =		getLong(sortedEvents, EventDbAdapter.KEY_END_TIME);
				eventList.add(new EventEntry(dbRowID, name, location, startTime, endTime));
				numEvents--;
			}
		}
		return eventList;
	}

	/**
	 * @param cursor A cursor at a particular row.
	 * @param columnName The name of the column in the DB.
	 * @return The Long at the column with the given name, or null
	 * 		   it doesn't exist.
	 */
	private long getLong(Cursor cursor, String columnName) {
		return cursor.getLong(cursor.getColumnIndex(columnName));
	}

	/**
	 * 
	 * @param cursor A cursor at a particular row.
	 * @param columnName The name of the column in the DB.
	 * @return The String at the colum with the given name.
	 */
	private String getString(Cursor cursor, String columnName) {
		return cursor.getString(cursor.getColumnIndex(columnName));
	}

	private void updateAutoComplete(){
		String activity=editTextEventName.getText().toString();
		String location=editTextEventLocation.getText().toString();
		if(!mDbHelper.getEvents().contains(activity))
			adapterActivities.add(activity);
		if(!mDbHelper.getLocations().contains(location))
			adapterLocations.add(location);
	}

	/**
	 * @return Whether or not an activity is currently being tracked.
	 */
	private boolean isTracking() {
		return currentEvent != null;
	}

	private void initializeAutoComplete(){

		adapterActivities.clear();
		adapterLocations.clear();
		for(String event: mDbHelper.getEvents()){

			adapterActivities.add(event);
		}
		for(String location: mDbHelper.getLocations()){
			adapterLocations.add(location);
		}

	}
	private void focusOnNothing(){
	LinearLayout dummy=(LinearLayout)findViewById(R.id.dummyLayout);
	editTextEventName.clearFocus();
	editTextEventLocation.clearFocus();
	dummy.requestFocus();
	}
	/**
	 * A local, in-memory version of a Event database entry. This is pushed and pulled
	 * from the database when necessary.  
	 * 
	 * @author AlexD
	 *
	 */
	private class EventEntry {
		long mDbRowID=-1;
		String mName="";
		String mLocation="";
		long mStartTime;
		long mEndTime;

		public EventEntry() {
			mStartTime = System.currentTimeMillis();
		}

		public EventEntry(long dbRowID, String name, String location, long startTime, long endTime){
			this.mDbRowID = dbRowID;
			this.mName=name;
			this.mLocation=location;
			this.mStartTime=startTime;
			this.mEndTime=endTime;
		}
	}
}