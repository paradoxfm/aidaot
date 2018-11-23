package ru.megazlo.aidaot.component;

import android.app.Activity;
import android.support.annotation.IdRes;
import android.support.annotation.StringRes;
import android.view.View;

import ru.megazlo.aidaot.R;
import uk.co.deanwild.materialshowcaseview.*;

/** Created by iGurkin on 23.11.2018. */
public class ShowcaseUtil {

	public final static String MAIN_VIEW = "200001";
	public final static String SETUP_VIEW = "200002";

	private final static int DELAY = 300;
	private final static int PADDING = 20;

	private MaterialShowcaseSequence seq;

	private Activity activity;

	private ShowcaseUtil(String onceId, Activity activity) {
		this.activity = activity;
		init(onceId);
	}

	private void init(String onceId) {
		final ShowcaseConfig config = new ShowcaseConfig();
		config.setDelay(DELAY);
		config.setShapePadding(PADDING);
		seq = onceId == null ? new MaterialShowcaseSequence(activity) : new MaterialShowcaseSequence(activity, onceId);
		seq.setConfig(config);
	}

	public static ShowcaseUtil builder(String onceId, Activity activity) {
		return new ShowcaseUtil(onceId, activity);
	}

	public ShowcaseUtil add(@IdRes int id, @StringRes int text) {
		final View view = activity.findViewById(id);
		return add(view, text, R.string.ok);
	}

	public ShowcaseUtil add(View target, @StringRes int text) {
		return add(target, text, R.string.ok);
	}

	public ShowcaseUtil add(@IdRes int id, @StringRes int text, @StringRes int buttonText) {
		final View view = activity.findViewById(id);
		return add(view, text, buttonText);
	}

	public ShowcaseUtil add(View target, @StringRes int text, @StringRes int buttonText) {
		final MaterialShowcaseView.Builder bld = new MaterialShowcaseView.Builder(activity);
		bld.setTarget(target).setDismissText(buttonText).setContentText(text).setDelay(DELAY).setShapePadding(PADDING);
		seq.addSequenceItem(bld.build());
		return this;
	}

	public void run() {
		seq.start();
	}
}
