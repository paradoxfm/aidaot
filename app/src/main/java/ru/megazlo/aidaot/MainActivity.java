package ru.megazlo.aidaot;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.*;

import org.androidannotations.annotations.*;
import org.joda.time.DateTime;
import org.joda.time.LocalTime;

import java.io.IOException;
import java.util.*;

import ru.megazlo.aidaot.component.LapsAdapter;
import ru.megazlo.aidaot.component.ShowcaseUtil;

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

	private List<LapItem> laps = new ArrayList<>();

	private MediaPlayer player;

	@AfterViews
	protected void afterViews() {
		setSupportActionBar(toolbar);
		//getSupportActionBar().setIcon(R.mipmap.ic_launcher);
		preparePlayer();
		fabAdd.setEnabled(false);

		Timer timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {
			public void run() {
				runOnUiThread(() -> {
					LocalTime tm = new LocalTime(new Date().getTime());
					officialTime.setText(tm.toString("HH.mm.ss"));
				});
			}
		}, 0, 1000);

		ShowcaseUtil.builder(ShowcaseUtil.MAIN_VIEW, this)
				.add(R.id.official_time, R.string.help_official_time).add(R.id.fab_add, R.string.help_fab_add)
				.add(R.id.list_items, R.string.help_list_items)
				.run();
	}

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
		super.onDestroy();
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

	private void setupTimers(Date timeStart, int countStarts, int intervalStarts) {
		LocalTime start = new LocalTime(timeStart.getTime()).withSecondOfMinute(0).withMillisOfSecond(0);
		final LapsAdapter adapter = new LapsAdapter(this);
		list.setAdapter(adapter);
		for (int i = 0; i < countStarts; i++) {
			final LocalTime ot = start.plusMinutes(i * intervalStarts);
			final LapItem lap = new LapItem().setPosition(i + 1).setTime(ot).setAlarmTime(ot.minusMinutes(2));
			laps.add(lap);
			if (i == 0) {
				configAlarm(lap.getPosition(), lap.getAlarmTime());
			}
		}
		adapter.addAll(laps);
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
		for (LapItem lp : laps) {
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
		final PendingIntent pendingIntent = createPendingIntent(pos);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			alarmManager.setExactAndAllowWhileIdle(ALARM_TYPE, dateTime.toCalendar(null).getTimeInMillis(), pendingIntent);
		} else {
			alarmManager.setExact(ALARM_TYPE, dateTime.toCalendar(null).getTimeInMillis(), pendingIntent);
		}
	}
}
