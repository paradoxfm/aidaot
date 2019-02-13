package ru.megazlo.aidaot;

import android.app.*;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;

import com.android.billingclient.api.BillingClient;

import org.androidannotations.annotations.*;
import org.joda.time.*;

import java.io.IOException;
import java.util.*;

import ru.megazlo.aidaot.component.LapsAdapter;
import ru.megazlo.aidaot.dto.LapsState;

@EActivity(R.layout.activity_main)
@OptionsMenu(R.menu.menu_about)
public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener, BottomNavigationView.OnNavigationItemReselectedListener {

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
	@ViewById(R.id.bottom_menu)
	protected BottomNavigationView bottomNavigationView;

	private BillingClient mBillingClient;

	private MediaPlayer player;

	private PendingIntent pendingIntent;

	private boolean doubleBackToExitPressedOnce;

	//@AfterInject
	protected void afterInject() {
		//mBillingClient = BillingClient.newBuilder(this).setListener(this).build();
	}

	@AfterViews
	protected void afterViews() {
		final LocalDateTime t = new LocalDateTime(2019, 3, 1, 1, 0);
		final LocalDateTime now = new LocalDateTime();
		if (now.isAfter(t)) {
			Toast.makeText(this, "Test period ended", Toast.LENGTH_SHORT).show();
			this.finish();
		}
		setSupportActionBar(toolbar);
		getSupportActionBar().setIcon(R.drawable.ic_logo_app_simple);
		preparePlayer();
		fabAdd.setEnabled(false);
		bottomNavigationView.setOnNavigationItemSelectedListener(this);
		bottomNavigationView.setOnNavigationItemReselectedListener(this);
		Timer timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {
			public void run() {
				runOnUiThread(() -> {
					LocalTime tm = new LocalTime(new Date().getTime());
					officialTime.setText(String.format("Official time: %s", tm.toString("HH:mm:ss")));
				});
			}
		}, 0, 1000);
	}

	@OptionsItem(R.id.action_login)
	protected void actionAbout() {
		View dialogView = getLayoutInflater().inflate(R.layout.alert_about, null);
		AlertDialog.Builder b = new AlertDialog.Builder(this).setView(dialogView).setPositiveButton(R.string.ok, null);
		b.show();
	}

	@Background(delay = 300L)
	protected void preparePlayer() {
		try {
			AssetFileDescriptor fd = getAssets().openFd("aida.mp3");
			MediaPlayer p = new MediaPlayer();
			p.setDataSource(fd.getFileDescriptor(), fd.getStartOffset(), fd.getLength());
			p.prepare();
			p.setLooping(false);
			player = p;
			enableAdding();
			player.setOnCompletionListener(mp -> mp.setVolume(1, 1));
			player.setOnErrorListener((mp, what, extra) -> {
				Toast.makeText(this, "MediaPlayer error", Toast.LENGTH_LONG).show();
				return false;
			});
			player.setVolume(1, 1);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		stopAndRelease();
	}

	private void stopAndRelease() {
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

	@OnActivityResult(ACT_CODE)
	protected void onSetupResult(int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			bottomNavigationView.setVisibility(View.VISIBLE);
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
		if (doubleBackToExitPressedOnce) {
			super.onBackPressed();
			return;
		}
		this.doubleBackToExitPressedOnce = true;
		Toast.makeText(this, getString(R.string.press_to_exit), Toast.LENGTH_SHORT).show();
		new Handler().postDelayed(() -> doubleBackToExitPressedOnce = false, 2000);
	}

	/*@Override
	public void onBackPressed() {
		if (state.getLaps().size() > 0) {
			Snackbar.make(mainContain, R.string.want_exit, Snackbar.LENGTH_LONG).setAction(R.string.ok, view -> super.onBackPressed()).show();
		} else {
			super.onBackPressed();
		}
	}*/

	private void setupTimers(Date timeStart, int countStarts, int intervalStarts) {
		state.setStart(new LocalTime(timeStart.getTime()).withSecondOfMinute(0).withMillisOfSecond(0));
		state.setLapTime(intervalStarts);
		final LapsAdapter adapter = new LapsAdapter(this);
		list.setAdapter(adapter);
		state.getLaps().clear();
		for (int i = 0; i < countStarts; i++) {
			final LocalTime ot = state.getStart().plusMinutes(i * intervalStarts);
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
			final int pos = intent.getIntExtra("pos", -1);
			updateAfterOt(pos);
			if (player != null) {
				//player.seekTo(0);
				player.start();
			}
			configureNextAlarm(pos);
		}
	}

	@UiThread(delay = 120000)
	protected void updateAfterOt(int pos) {
		if (pos != -1) {
			final int index = pos - 1;
			Log.i(TAG, "scroll to pos " + index);
			state.getLaps().get(index).setAfterOt(true);
			refreshList();
			//list.smoothScrollToPosition(index < 1 ? 1 : index - 1);
		}
	}

	private void refreshList() {
		final LapsAdapter adapter = (LapsAdapter) list.getAdapter();
		adapter.notifyDataSetChanged();
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
		refreshList();
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

	private void actionAddItem() {
		LocalTime ot = state.getStart().plusMinutes(state.getLaps().size() * state.getLapTime());
		final LapItem lap = new LapItem().setPosition(state.getLaps().size() + 1).setTime(ot).setAlarmTime(ot.minusMinutes(2));
		state.getLaps().add(lap);
		final LapsAdapter adapter = (LapsAdapter) list.getAdapter();
		adapter.add(lap);
		adapter.notifyDataSetChanged();
	}

	private void actionClearAlarms() {
		stopAndRelease();
		list.setAdapter(null);
		bottomNavigationView.setVisibility(View.GONE);
		fabAdd.show();
		preparePlayer();
	}

	private void actionChangeMuteCurrent() {
		player.setVolume(0, 0);
	}

	@Override
	public boolean onNavigationItemSelected(@NonNull MenuItem mi) {
		switch (mi.getItemId()) {
			case R.id.action_add:
				actionAddItem();
				break;
			case R.id.action_mute:
				actionChangeMuteCurrent();
				return true;
			case R.id.action_clear:
				actionClearAlarms();
				break;
		}
		Log.i(TAG, "bottom item selected");
		return false;
	}

	@Override
	public void onNavigationItemReselected(@NonNull MenuItem mi) {
		final boolean inverse = !mi.isCheckable();
		mi.setCheckable(inverse);
		mi.setChecked(inverse);
	}
}
