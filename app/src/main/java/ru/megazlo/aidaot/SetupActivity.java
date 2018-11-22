package ru.megazlo.aidaot;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import org.androidannotations.annotations.*;


@EActivity(R.layout.activity_setup)
public class SetupActivity extends AppCompatActivity {

	@ViewById(R.id.toolbar)
	protected Toolbar toolbar;


	@AfterViews
	protected void afterViews() {
		setSupportActionBar(toolbar);
	}

	@Click(R.id.fab_done)
	protected void showSetup() {
		/*Intent intent = new Intent().putExtra("name", etName.getText().toString());
		setResult(RESULT_OK, intent);*/
		setResult(RESULT_OK);
		finish();
	}
}
