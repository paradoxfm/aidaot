package ru.megazlo.aidaot;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.github.florent37.singledateandtimepicker.SingleDateAndTimePicker;
import com.shawnlin.numberpicker.NumberPicker;

import org.androidannotations.annotations.*;

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

	@AfterViews
	protected void afterViews() {
		setSupportActionBar(toolbar);
	}

	@Click(R.id.fab_done)
	protected void showSetup() {
		final Intent intent = new Intent()
				.putExtra("time_start", timePicker.getDate())
				.putExtra("count_starts", countStarts.getValue())
				.putExtra("interval_starts", intervalStarts.getValue());
		setResult(RESULT_OK, intent);
		finish();
	}
}
