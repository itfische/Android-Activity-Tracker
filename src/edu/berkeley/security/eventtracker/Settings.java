package edu.berkeley.security.eventtracker;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class Settings extends EventActivity {
	private boolean isTracking;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        
        findViewById(R.id.toolbar_right_option).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent settingsIntent = new Intent(Settings.this, Settings.class);
				settingsIntent.putExtra(getString(R.string.isTracking), isTracking);
				startActivity(settingsIntent);
			}
		});
        
        findViewById(R.id.toolbar_left_option).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent editIntent = new Intent(Settings.this, EditEvent.class);
				editIntent.putExtra(getString(R.string.isTracking), isTracking);
				startActivity(editIntent);
			}
		});
        
        isTracking = this.getIntent().getBooleanExtra("isTracking", false);
        ((TextView) findViewById(R.id.toolbar_center)).setText(
        		isTracking ? R.string.toolbarTracking : R.string.toolbarNotTracking);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		isTracking = savedInstanceState.getBoolean(getString(R.string.isTracking));
        ((TextView) findViewById(R.id.toolbar_center)).setText(
        		isTracking ? R.string.toolbarTracking : R.string.toolbarNotTracking);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean(getString(R.string.isTracking), isTracking);
	}

	@Override
	protected boolean isTracking() {
		return isTracking;
	}

	@Override
	protected int getLayoutResource() {
		return R.layout.settings;
	}
}
