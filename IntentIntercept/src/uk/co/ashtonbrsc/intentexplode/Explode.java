//   Copyright 2012 Intrications (intrications.com)
//
//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.

package uk.co.ashtonbrsc.intentexplode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import uk.co.ashtonbrsc.android.intentintercept.R;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.ClipboardManager;
import android.text.Editable;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.LeadingMarginSpan;
import android.text.style.ParagraphStyle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.ShareActionProvider;

//TODO add icon -which icon - app icons???
//TODO add bitmaps/images (from intent extras?)
//TODO add getCallingActivity() - will only give details for startActivityForResult();

public class Explode extends SherlockActivity {

	private static final String INTENT_EDITED = "intent_edited";
	private static final int STANDARD_INDENT_SIZE_IN_DIP = 10;
	private String intentDetailsHtml;
	private ShareActionProvider shareActionProvider;
	private EditText action;
	private EditText data;
	private EditText type;
	private Intent editableIntent;
	private TextView categoriesHeader;
	private LinearLayout categoriesLayout;
	private LinearLayout flagsLayout;
	private LinearLayout extrasLayout;
	private LinearLayout activitiesLayout;
	private TextView activitiesHeader;
	private Button resendIntentButton;
	private Button resetIntentButton;
	private float density;
	private Intent originalIntent;
	protected boolean textWatchersActive;

	private static final Map<Integer, String> FLAGS_MAP = new HashMap<Integer, String>() {
		{
			put(new Integer(Intent.FLAG_GRANT_READ_URI_PERMISSION),
					"FLAG_GRANT_READ_URI_PERMISSION");
			put(new Integer(Intent.FLAG_GRANT_WRITE_URI_PERMISSION),
					"FLAG_GRANT_WRITE_URI_PERMISSION");
			put(new Integer(Intent.FLAG_FROM_BACKGROUND),
					"FLAG_FROM_BACKGROUND");
			put(new Integer(Intent.FLAG_DEBUG_LOG_RESOLUTION),
					"FLAG_DEBUG_LOG_RESOLUTION");
			put(new Integer(Intent.FLAG_EXCLUDE_STOPPED_PACKAGES),
					"FLAG_EXCLUDE_STOPPED_PACKAGES");
			put(new Integer(Intent.FLAG_INCLUDE_STOPPED_PACKAGES),
					"FLAG_INCLUDE_STOPPED_PACKAGES");
			put(new Integer(Intent.FLAG_ACTIVITY_NO_HISTORY),
					"FLAG_ACTIVITY_NO_HISTORY");
			put(new Integer(Intent.FLAG_ACTIVITY_SINGLE_TOP),
					"FLAG_ACTIVITY_SINGLE_TOP");
			put(new Integer(Intent.FLAG_ACTIVITY_NEW_TASK),
					"FLAG_ACTIVITY_NEW_TASK");
			put(new Integer(Intent.FLAG_ACTIVITY_MULTIPLE_TASK),
					"FLAG_ACTIVITY_MULTIPLE_TASK");
			put(new Integer(Intent.FLAG_ACTIVITY_CLEAR_TOP),
					"FLAG_ACTIVITY_CLEAR_TOP");
			put(new Integer(Intent.FLAG_ACTIVITY_FORWARD_RESULT),
					"FLAG_ACTIVITY_FORWARD_RESULT");
			put(new Integer(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP),
					"FLAG_ACTIVITY_PREVIOUS_IS_TOP");
			put(new Integer(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS),
					"FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS");
			put(new Integer(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT),
					"FLAG_ACTIVITY_BROUGHT_TO_FRONT");
			put(new Integer(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED),
					"FLAG_ACTIVITY_RESET_TASK_IF_NEEDED");
			put(new Integer(Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY),
					"FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY");
			put(new Integer(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET),
					"FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET");
			put(new Integer(Intent.FLAG_ACTIVITY_NO_USER_ACTION),
					"FLAG_ACTIVITY_NO_USER_ACTION");
			put(new Integer(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT),
					"FLAG_ACTIVITY_REORDER_TO_FRONT");
			put(new Integer(Intent.FLAG_ACTIVITY_NO_ANIMATION),
					"FLAG_ACTIVITY_NO_ANIMATION");
			put(new Integer(Intent.FLAG_ACTIVITY_CLEAR_TASK),
					"FLAG_ACTIVITY_CLEAR_TASK");
			put(new Integer(Intent.FLAG_ACTIVITY_TASK_ON_HOME),
					"FLAG_ACTIVITY_TASK_ON_HOME");
			put(new Integer(Intent.FLAG_RECEIVER_REGISTERED_ONLY),
					"FLAG_RECEIVER_REGISTERED_ONLY");
			put(new Integer(Intent.FLAG_RECEIVER_REPLACE_PENDING),
					"FLAG_RECEIVER_REPLACE_PENDING");
			put(new Integer(Intent.FLAG_RECEIVER_FOREGROUND),
					"FLAG_RECEIVER_FOREGROUND");
			put(new Integer(0x08000000),
					"FLAG_RECEIVER_REGISTERED_ONLY_BEFORE_BOOT");
			put(new Integer(0x04000000), "FLAG_RECEIVER_BOOT_UPGRADE");
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		originalIntent = (Intent) getIntent().clone();

		editableIntent = (Intent) getIntent().clone();

		editableIntent.setComponent(null);

		setContentView(R.layout.explode);

		setupVariables();

		setupTextWatchers();

		showInitialIntentDetails();

		if (savedInstanceState != null
				&& savedInstanceState.getBoolean(INTENT_EDITED)) {
			showResetIntentButton();
		}

	}

	private void showInitialIntentDetails() {

		action.setText(editableIntent.getAction());
		if (editableIntent.getDataString() != null) {
			data.setText(editableIntent.getDataString());
		}
		type.setText(getIntent().getType());

		Set<String> categories = editableIntent.getCategories();
		StringBuilder stringBuilder = new StringBuilder();
		if (categories != null) {
			stringBuilder.append("Categories:");
			for (String category : categories) {
				stringBuilder.append(category).append("<br>");
				TextView text2 = new TextView(this);
				text2.setText(category);
				text2.setTextAppearance(this, R.style.TextFlags);
				categoriesLayout.addView(text2);
			}
		} else {
			categoriesHeader.setVisibility(View.GONE);
			// addTextToLayout("NONE", Typeface.NORMAL, categoriesLayout);
		}

		ArrayList<String> flagsStrings = getFlags();
		if (flagsStrings.size() > 0) {
			for (String thisFlagString : flagsStrings) {
				addTextToLayout(thisFlagString, Typeface.NORMAL, flagsLayout);
			}
		} else {
			addTextToLayout("NONE", Typeface.NORMAL, flagsLayout);
		}
		try {
			Bundle intentBundle = editableIntent.getExtras();
			if (intentBundle != null) {
				Set<String> keySet = intentBundle.keySet();
				stringBuilder.append("<br><b><u>Bundle:</u></b><br>");
				int count = 0;

				for (String key : keySet) {
					count++;
					Object thisObject = intentBundle.get(key);
					addTextToLayout("EXTRA " + count, Typeface.BOLD, extrasLayout);
					String thisClass = thisObject.getClass().getName();
					if (thisClass != null) {
						addTextToLayout("Class: " + thisClass, Typeface.ITALIC,
								STANDARD_INDENT_SIZE_IN_DIP, extrasLayout);
					}
					addTextToLayout("Key: " + key, Typeface.ITALIC,
							STANDARD_INDENT_SIZE_IN_DIP, extrasLayout);
					if (thisObject instanceof String || thisObject instanceof Long
							|| thisObject instanceof Integer
							|| thisObject instanceof Boolean) {
						addTextToLayout("Value: " + thisObject.toString(),
								Typeface.ITALIC, STANDARD_INDENT_SIZE_IN_DIP,
								extrasLayout);
					} else if (thisObject instanceof java.util.ArrayList) {
						addTextToLayout("Values: ", Typeface.ITALIC, extrasLayout);
						ArrayList thisArrayList = (ArrayList) thisObject;
						for (Object thisArrayListObject : thisArrayList) {
							addTextToLayout(thisArrayListObject.toString(),
									Typeface.ITALIC, STANDARD_INDENT_SIZE_IN_DIP,
									extrasLayout);
						}
					}
				}
			} else {
				addTextToLayout("NONE", Typeface.NORMAL, extrasLayout);
			}
		} catch (Exception e) {
			// TODO Should make this red to highlight error
			addTextToLayout("ERROR EXTRACTING EXTRAS", Typeface.NORMAL, extrasLayout);
			e.printStackTrace();
		}

		checkAndShowMatchingActivites();

		// resolveInfo = pm.queryIntentServices(intent, 0);
		// stringBuilder.append("<br><b><u>" + resolveInfo.size()
		// + " services match this intent:</u></b><br>");
		// for (int i = 0; i < resolveInfo.size(); i++) {
		// ResolveInfo info = resolveInfo.get(i);
		// ActivityInfo activityinfo = info.activityInfo;
		// stringBuilder.append(activityinfo.packageName + "<br>");
		// }

		// intentDetailsHtml = stringBuilder.toString();
		// (((TextView) findViewById(R.id.text))).setText(intentDetailsHtml);
		refreshUI();
	}

	private ArrayList<String> getFlags() {
		ArrayList<String> flagsStrings = new ArrayList<String>();
		int flags = editableIntent.getFlags();
		Set<Entry<Integer, String>> set = FLAGS_MAP.entrySet();
		Iterator<Entry<Integer, String>> i = set.iterator();
		while (i.hasNext()) {
			Entry<Integer, String> thisFlag = (Entry<Integer, String>) i.next();
			if ((flags & thisFlag.getKey()) != 0) {
				flagsStrings.add(thisFlag.getValue());
			}
		}
		return flagsStrings;
	}

	private void checkAndShowMatchingActivites() {

		activitiesLayout.removeAllViews();
		PackageManager pm = getPackageManager();
		List<ResolveInfo> resolveInfo = pm.queryIntentActivities(
				editableIntent, 0);

		// Remove Intent Intercept from matching activities
		int numberOfMatchingActivities = resolveInfo.size() - 1;

		if (numberOfMatchingActivities < 1) {
			activitiesHeader.setText("NO ACTIVITIES MATCH THIS INTENT");
		} else {
			activitiesHeader.setText(numberOfMatchingActivities
					+ " ACTIVITIES MATCH THIS INTENT");
			for (int i = 0; i <= numberOfMatchingActivities; i++) {
				ResolveInfo info = resolveInfo.get(i);
				ActivityInfo activityinfo = info.activityInfo;
				if (!activityinfo.packageName.equals(getPackageName())) {
					addTextToLayout(activityinfo.loadLabel(pm) + " ("
							+ activityinfo.packageName + " - "
							+ activityinfo.name + ")", Typeface.NORMAL,
							activitiesLayout);
				}
			}
		}
	}

	private void addTextToLayout(String text, int typeface, int paddingLeft,
			LinearLayout layout) {
		TextView textView = new TextView(this);
		ParagraphStyle style_para = new LeadingMarginSpan.Standard(0,
				(int) (STANDARD_INDENT_SIZE_IN_DIP * density));
		SpannableString styledText = new SpannableString(text);
		styledText.setSpan(style_para, 0, styledText.length(),
				Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
		textView.setText(styledText);
		textView.setTextAppearance(this, R.style.TextFlags);
		textView.setTypeface(null, typeface);
		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT);
		params.setMargins((int) (paddingLeft * density), 0, 0, 0);
		layout.addView(textView, params);
	}

	private void addTextToLayout(String text, int typeface, LinearLayout layout) {
		addTextToLayout(text, typeface, 0, layout);
	}

	private void setupVariables() {
		action = (EditText) findViewById(R.id.action);
		data = (EditText) findViewById(R.id.data);
		type = (EditText) findViewById(R.id.type);
		categoriesHeader = (TextView) findViewById(R.id.categories_header);
		categoriesLayout = (LinearLayout) findViewById(R.id.categories_layout);
		flagsLayout = (LinearLayout) findViewById(R.id.flags_layout);
		extrasLayout = (LinearLayout) findViewById(R.id.extras_layout);
		activitiesHeader = (TextView) findViewById(R.id.activities_header);
		activitiesLayout = (LinearLayout) findViewById(R.id.activities_layout);
		resendIntentButton = (Button) findViewById(R.id.resend_intent_button);
		resetIntentButton = (Button) findViewById(R.id.reset_intent_button);

		DisplayMetrics metrics = new DisplayMetrics();
		WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		wm.getDefaultDisplay().getMetrics(metrics);
		density = metrics.density;
	}

	private void setupTextWatchers() {
		action.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				if (textWatchersActive) {
					try {
						editableIntent.setAction(action.getText().toString());
						showResetIntentButton();
						refreshUI();
					} catch (Exception e) {
						Toast.makeText(Explode.this, e.getMessage(),
								Toast.LENGTH_SHORT).show();
						e.printStackTrace();
					}
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});
		data.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				if (textWatchersActive) {
					try {
						String dataString = data.getText().toString();
						String savedType = editableIntent.getType(); // setData
																		// clears
						// type
						// so we save it
						editableIntent.setData(Uri.parse(dataString));
						type.setText(savedType); // and re-set it
						showResetIntentButton();
						refreshUI();
					} catch (Exception e) {
						Toast.makeText(Explode.this, e.getMessage(),
								Toast.LENGTH_SHORT).show();
						e.printStackTrace();
					}
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});
		type.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				if (textWatchersActive) {
					try {
						editableIntent.setType(type.getText().toString());
						showResetIntentButton();
						refreshUI();
					} catch (Exception e) {
						Toast.makeText(Explode.this, e.getMessage(),
								Toast.LENGTH_SHORT).show();
						e.printStackTrace();
					}
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});
	}

	private void showResetIntentButton() {
		resendIntentButton.setText("Send Edited Intent");
		resetIntentButton.setVisibility(View.VISIBLE);
	}

	public void onSendIntent(View v) {
		try {
			startActivity(editableIntent);
		} catch (Exception e) {
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}
	}

	public void onResetIntent(View v) {
		startActivity(originalIntent);
		finish();
	}

	public void copyIntentDetails() {
		ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
		clipboard.setText(getIntentDetailsString());
		Toast.makeText(this, R.string.intent_details_copied_to_clipboard,
				Toast.LENGTH_SHORT).show();
	}

	public void refreshUI() {
		// if (!intent.getAction().equals(getIntent().getAction())
		// || (intent.getDataString() != null && !intent.getDataString()
		// .equals(getIntent().getDataString()))
		// || !intent.getType().equals(getIntent().getType())) {
		//
		// }
		checkAndShowMatchingActivites();
		if (shareActionProvider != null) {
			Intent share = createShareIntent();
			shareActionProvider.setShareIntent(share);
		}
		return;
	}

	private Intent createShareIntent() {
		Intent share = new Intent(Intent.ACTION_SEND);
		share.setType("text/plain");
		share.putExtra(Intent.EXTRA_TEXT, getIntentDetailsString());
		return share;
	}

	private Spanned getIntentDetailsString() { // TODO make sure this has all
												// the details
		StringBuilder stringBuilder = new StringBuilder();

		stringBuilder.append("<b><u>ACTION:</u></b> ")
				.append(editableIntent.getAction()).append("<br>");
		stringBuilder.append("<b><u>DATA:</u></b> ")
				.append(editableIntent.getData()).append("<br>");
		stringBuilder.append("<b><u>TYPE:</u></b> ")
				.append(editableIntent.getType()).append("<br>");

		Set<String> categories = editableIntent.getCategories();
		if (categories != null) {
			stringBuilder.append("<b><u>CATEGORIES:</u></b><br>");
			for (String category : categories) {
				stringBuilder.append(category).append("<br>");
			}
		}

		stringBuilder.append("<br><b><u>FLAGS:</u></b><br>");
		ArrayList<String> flagsStrings = getFlags();
		if (flagsStrings.size() > 0) {
			for (String thisFlagString : flagsStrings) {
				stringBuilder.append(thisFlagString).append("<br>");
			}
		} else {
			stringBuilder.append("NONE").append("<br>");
		}

		try {
			Bundle intentBundle = editableIntent.getExtras();
			if (intentBundle != null) {
				Set<String> keySet = intentBundle.keySet();
				stringBuilder.append("<br><b><u>EXTRAS:</u></b><br>");
				int count = 0;

				for (String key : keySet) {
					count++;
					Object thisObject = intentBundle.get(key);
					stringBuilder.append("<u>EXTRA ").append(count)
							.append(":</u><br>");
					String thisClass = thisObject.getClass().getName();
					if (thisClass != null) {
						stringBuilder.append("Class: ").append(thisClass)
								.append("<br>");
					}
					stringBuilder.append("Key: ").append(key).append("<br>");

					if (thisObject instanceof String || thisObject instanceof Long
							|| thisObject instanceof Integer
							|| thisObject instanceof Boolean) {
						stringBuilder.append("Value: " + thisObject.toString())
								.append("<br>");
					} else if (thisObject instanceof java.util.ArrayList) {
						stringBuilder.append("Values:<br>");
						ArrayList thisArrayList = (ArrayList) thisObject;
						for (Object thisArrayListObject : thisArrayList) {
							stringBuilder.append(thisArrayListObject.toString()
									+ "<br>");
						}
					}
				}
			}
		} catch (Exception e) {
			stringBuilder.append("<br><b><u>BUNDLE:</u></b><br>");
			stringBuilder.append("<font color=\"red\">Error extracting extras<br><font>");
			e.printStackTrace();
		}

		PackageManager pm = getPackageManager();
		List<ResolveInfo> resolveInfo = pm.queryIntentActivities(
				editableIntent, 0);

		// Remove Intent Intercept from matching activities
		int numberOfMatchingActivities = resolveInfo.size() - 1;

		if (numberOfMatchingActivities < 1) {
			stringBuilder
					.append("<br><b><u>NO ACTIVITIES MATCH THIS INTENT</u></b><br>");
		} else {
			stringBuilder.append("<br><b><u>" + numberOfMatchingActivities
					+ " ACTIVITIES MATCH THIS INTENT:</u></b><br>");
			for (int i = 0; i <= numberOfMatchingActivities; i++) {
				ResolveInfo info = resolveInfo.get(i);
				ActivityInfo activityinfo = info.activityInfo;
				if (!activityinfo.packageName.equals(getPackageName())) {
					stringBuilder.append(activityinfo.loadLabel(pm) + " ("
							+ activityinfo.packageName + " - "
							+ activityinfo.name + ")<br>");
				}
			}
		}

		intentDetailsHtml = stringBuilder.toString();
		return Html.fromHtml(intentDetailsHtml);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.menu, menu);
		MenuItem actionItem = menu.findItem(R.id.share);
		shareActionProvider = (ShareActionProvider) actionItem
				.getActionProvider();
		shareActionProvider
				.setShareHistoryFileName(ShareActionProvider.DEFAULT_SHARE_HISTORY_FILE_NAME);
		refreshUI();
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.copy:
			copyIntentDetails();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onPause() {
		super.onPause();
		textWatchersActive = false;
	}

	@Override
	protected void onResume() {
		super.onResume();
		overridePendingTransition(0, 0); // inihibit new activity animation when
											// resetting intent details
		textWatchersActive = true;
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean(INTENT_EDITED,
				resetIntentButton.getVisibility() == View.VISIBLE);
	}
}