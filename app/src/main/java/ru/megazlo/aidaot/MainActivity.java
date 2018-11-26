package ru.megazlo.aidaot;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Build;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;

import org.androidannotations.annotations.*;
import org.joda.time.DateTime;
import org.joda.time.LocalTime;

import java.io.IOException;
import java.util.*;

import ru.megazlo.aidaot.component.LapsAdapter;
import ru.megazlo.aidaot.component.ShowcaseUtil;
import ru.megazlo.aidaot.dto.LapsState;

@EActivity(R.layout.activity_main)
public class MainActivity extends AppCompatActivity {

	private final static int ACT_CODE = 34564;
	private final static String TIME_ACTION = "ru.megazlo.aidaot.TIME_ACTION";
	private final static String TAG = MainActivity.class.getSimpleName();

	@ViewById(R.id.toolbar)
	protected Toolbar toolbar;
	@ViewById(R.id.fab_add)
	protected FloatingActionButton fabAdd;
	/*@ViewById(R.id.fab_start)
	protected FloatingActionButton fabStart;*/
	@ViewById(R.id.list_items)
	protected ListView list;
	@ViewById(R.id.official_time)
	protected TextView officialTime;
	@SystemService
	protected AlarmManager alarmManager;
	@ViewById(R.id.main_contain)
	protected ConstraintLayout mainContain;
	@InstanceState
	protected LapsState state = new LapsState();

	private MediaPlayer player;

	private PendingIntent pendingIntent;

	//private Gson gson;

	/*@AfterExtras
	protected void config() {
		gson = Converters.registerLocalTime(new GsonBuilder()).create();
	}*/

	@AfterViews
	protected void afterViews() {
		setSupportActionBar(toolbar);
		getSupportActionBar().setIcon(R.drawable.ic_logo_app_simple);
		preparePlayer();
		fabAdd.setEnabled(false);

		Timer timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {
			public void run() {
				runOnUiThread(() -> {
					LocalTime tm = new LocalTime(new Date().getTime());
					officialTime.setText(String.format("Official time: %s", tm.toString("HH.mm.ss")));
				});
			}
		}, 0, 1000);

		ShowcaseUtil.builder(ShowcaseUtil.MAIN_VIEW, this)
				.add(R.id.official_time, R.string.help_official_time).add(R.id.fab_add, R.string.help_fab_add)
				.add(R.id.list_items, R.string.help_list_items)
				.run();

		/*SharedPreferences pref = getPreferences(MODE_PRIVATE);
		String strState = pref.getString("state", "");
		//strState = "";
		if (strState != null && strState.length() > 0) {
			state = gson.fromJson(strState, LapsState.class);
		}
		if (state.getLaps().size() > 0) {
			final LapsAdapter adapter = new LapsAdapter(this);
			adapter.addAll(state.getLaps());
			list.setAdapter(adapter);
		}*/
	}

	/*@Override
	protected void onCreate(@Nullable Bundle sis) {
		super.onCreate(sis);
		if (TIME_ACTION.equals(getIntent().getAction())) {

		}
	}*/

	@Background(delay = 1000L)
	protected void preparePlayer() {
		try {
			AssetFileDescriptor fd = getAssets().openFd("aida.mp3");
			MediaPlayer p = new MediaPlayer();
			p.setDataSource(fd.getFileDescriptor(), fd.getStartOffset(), fd.getLength());
			p.prepare();
			p.setLooping(false);
			player = p;
			enableAdding();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onDestroy() {
		/*if (state.getLaps().size() > 0) {
			SharedPreferences pref = getPreferences(MODE_PRIVATE);
			SharedPreferences.Editor edit = pref.edit();
			String json = gson.toJson(state);
			edit.putString("state", json).commit();
		}*/
		super.onDestroy();
		if (pendingIntent != null) {
			alarmManager.cancel(pendingIntent);
		}
		if (player != null) {
			player.release();
		}
	}

	@UiThread
	protected void enableAdding() {
		fabAdd.setEnabled(true);
	}

	@Click(R.id.fab_add)
	protected void showSetup() {
		startActivityForResult(new Intent(this, SetupActivityGen.class), ACT_CODE);
	}

	/*@Click(R.id.fab_start)
	protected void startCompetition() {
		Toast.makeText(this, "Осталось совсем немного и заиграет музыка", Toast.LENGTH_SHORT).show();
	}*/

	@OnActivityResult(ACT_CODE)
	protected void onSetupResult(int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			fabAdd.hide();
			//fabStart.show();
			final Date timeStart = (Date) data.getSerializableExtra("time_start");
			final int countStarts = data.getIntExtra("count_starts", 1);
			final int intervalStarts = data.getIntExtra("interval_starts", 3);
			if (timeStart != null) {
				setupTimers(timeStart, countStarts, intervalStarts);
			}
		}
	}

	@Override
	public void onBackPressed() {
		if (state.getLaps().size() > 0) {
			Snackbar.make(mainContain, R.string.want_exit, Snackbar.LENGTH_LONG).setAction(R.string.ok, view -> super.onBackPressed()).show();
		} else {
			super.onBackPressed();
		}
	}

	private void setupTimers(Date timeStart, int countStarts, int intervalStarts) {
		LocalTime start = new LocalTime(timeStart.getTime()).withSecondOfMinute(0).withMillisOfSecond(0);
		final LapsAdapter adapter = new LapsAdapter(this);
		list.setAdapter(adapter);
		state.getLaps().clear();
		for (int i = 0; i < countStarts; i++) {
			final LocalTime ot = start.plusMinutes(i * intervalStarts);
			final LapItem lap = new LapItem().setPosition(i + 1).setTime(ot).setAlarmTime(ot.minusMinutes(2));
			state.getLaps().add(lap);
			if (i == 0) {
				configAlarm(lap.getPosition(), lap.getAlarmTime());
			}
		}
		adapter.addAll(state.getLaps());
		adapter.notifyDataSetChanged();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		if (TIME_ACTION.equals(intent.getAction())) {
			Log.i(TAG, "execute alarm");
			if (player != null) {
				//player.seekTo(0);
				player.start();
			}
			configureNextAlarm(intent.getIntExtra("pos", -1));
		}
	}

	private void configureNextAlarm(int pos) {
		LapItem nextItm = null;
		for (LapItem lp : state.getLaps()) {
			if (lp.getPosition() == pos) {
				lp.setStarted(true);
			}
			if (lp.getPosition() == pos + 1) {
				nextItm = lp;
				break;
			}
		}
		if (nextItm != null) {
			configAlarm(nextItm.getPosition(), nextItm.getAlarmTime());
		}
		final LapsAdapter adapter = (LapsAdapter) list.getAdapter();
		adapter.notifyDataSetChanged();
	}

	@Override
	protected void onPause() {
		super.onPause();
		//alarmManager.cancel(pendingIntent);
	}

	private PendingIntent createPendingIntent(int pos) {
		Intent startIntent = new Intent(this, MainActivityGen.class).putExtra("pos", pos);
		startIntent.setAction(TIME_ACTION);
		return PendingIntent.getActivity(this, 0, startIntent, PendingIntent.FLAG_UPDATE_CURRENT);
	}

	private void configAlarm(int pos, LocalTime time) {
		final DateTime dateTime = time.toDateTimeToday();
		Log.i(TAG, "date alarm " + dateTime);
		int ALARM_TYPE = AlarmManager.RTC_WAKEUP;
		pendingIntent = createPendingIntent(pos);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			alarmManager.setExactAndAllowWhileIdle(ALARM_TYPE, dateTime.toCalendar(null).getTimeInMillis(), pendingIntent);
		} else {
			alarmManager.setExact(ALARM_TYPE, dateTime.toCalendar(null).getTimeInMillis(), pendingIntent);
		}
	}
}
