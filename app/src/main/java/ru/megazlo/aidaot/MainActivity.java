package ru.megazlo.aidaot;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import org.androidannotations.annotations.*;

@EActivity(R.layout.activity_main)
public class MainActivity extends AppCompatActivity {

	private final static int ACT_CODE = 34564;

	@ViewById(R.id.toolbar)
	protected Toolbar toolbar;
	@ViewById(R.id.fab_add)
	protected FloatingActionButton fabAdd;
	@ViewById(R.id.fab_start)
	protected FloatingActionButton fabStart;

	@AfterViews
	protected void afterViews() {
		setSupportActionBar(toolbar);
	}

	@Click(R.id.fab_add)
	protected void showSetup() {
		startActivityForResult(new Intent(this, SetupActivityGen.class), ACT_CODE);
	}

	@Click(R.id.fab_start)
	protected void startCompetition() {

	}

	@OnActivityResult(ACT_CODE)
	protected void onSetupResult(int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			fabAdd.hide();
			fabStart.show();
		}
	}

}
