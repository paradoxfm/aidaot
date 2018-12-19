package ru.megazlo.aidaot;

import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.github.florent37.singledateandtimepicker.SingleDateAndTimePicker;
import com.shawnlin.numberpicker.NumberPicker;

import org.androidannotations.annotations.*;
import org.joda.time.LocalTime;

@EActivity(R.layout.activity_setup)
public class SetupActivity extends AppCompatActivity {

	@ViewById(R.id.toolbar)
	protected Toolbar toolbar;
	@ViewById(R.id.time_start)
	protected SingleDateAndTimePicker timePicker;
	@ViewById(R.id.count_starts)
	protected NumberPicker countStarts;
	@ViewById(R.id.interval_starts)
	protected NumberPicker intervalStarts;
	@ViewById(R.id.setup_contain)
	protected LinearLayout setupContain;

	@AfterViews
	protected void afterViews() {
		setSupportActionBar(toolbar);
		timePicker.setStepMinutes(1);
	}

	@Click(R.id.fab_done)
	protected void showSetup() {
		final int valRes = valid();
		if (valRes != -1) {
			Toast.makeText(this, valRes, Toast.LENGTH_SHORT).show();
			Snackbar.make(setupContain, R.string.ask_cancel_setup, Snackbar.LENGTH_LONG).setAction(R.string.cancel, view -> finish()).show();
			return;
		}
		final Intent intent = new Intent()
				.putExtra("time_start", timePicker.getDate())
				.putExtra("count_starts", countStarts.getValue())
				.putExtra("interval_starts", intervalStarts.getValue());
		setResult(RESULT_OK, intent);
		finish();
	}

	private int valid() {
		LocalTime start = new LocalTime(timePicker.getDate()).withSecondOfMinute(0).withMillisOfSecond(0);
		if (LocalTime.now().compareTo(start.minusMinutes(2)) != -1) {
			return R.string.warn_first_ot;
		}
		return -1;
	}
}
