package it.liceoarzignano.bold;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import it.liceoarzignano.bold.events.Event;
import it.liceoarzignano.bold.events.EventListActivity;
import it.liceoarzignano.bold.events.EventsController;
import it.liceoarzignano.bold.marks.Mark;
import it.liceoarzignano.bold.marks.MarkListActivity;
import it.liceoarzignano.bold.marks.MarksController;
import it.liceoarzignano.bold.news.News;
import it.liceoarzignano.bold.news.NewsController;

public class ManagerActivity extends AppCompatActivity
        implements AdapterView.OnItemSelectedListener {

    private Intent mCallingIntent;
    private MarksController mMarksController;
    private EventsController mEventsController;

    private CoordinatorLayout mCoordinatorLayout;
    // Title
    private RelativeLayout mTitleLayout;
    private EditText mTitleInput;
    // Subject
    private RelativeLayout mSubjectLayout;
    private TextView mSubjectSelector;
    // Notes
    private EditText mNotesInput;
    // Event category
    private RelativeLayout mEventSpinnerLayout;
    private Spinner mEventSpinner;
    // Mark mValue
    private RelativeLayout mMarkValueLayout;
    private TextView mMarkPreview;
    // Date picker
    private RelativeLayout mDatePickerLayout;
    private TextView mDatePicker;

    private long mObjId;
    private Mark mMark;
    private Event mEvent;
    private int mValue;
    private String[] mSubjects;
    private String mTitle;
    private double mDialogVal;

    private boolean isEditMode = false;
    private boolean isMark = true;

    private int mYear;
    private int mMonth;
    private int mDay;
    private String mDate;
    private final DatePickerDialog.OnDateSetListener dpickerListener
            = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int mDialogYear, int mDialogMonth, int mDialogDay) {
            mYear = mDialogYear;
            mMonth = mDialogMonth + 1;
            mDay = mDialogDay;
            mDate = Utils.rightDate(mYear, mMonth, mDay);
            mDatePicker.setText(mDate);
        }
    };

    /*
     * Intent-extra:
     * boolean isEditing
     * boolean isMark
     * long mId
     * long newsToEvent
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager);

        mMarksController = new MarksController(((BoldApp) getApplicationContext()).getConfig());
        mEventsController = new EventsController(((BoldApp) getApplicationContext()).getConfig());

        // Init calendar
        Calendar mCal = Calendar.getInstance();
        mYear = mCal.get(Calendar.YEAR);
        mMonth = mCal.get(Calendar.MONTH) + 1;
        mDay = mCal.get(Calendar.DAY_OF_MONTH);

        mCallingIntent = getIntent();
        isMark = mCallingIntent.getBooleanExtra("isMark", true);
        isEditMode = mCallingIntent.getBooleanExtra("isEditing", false);

        // Toolbar
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle(getString(isMark ?
                (isEditMode ? R.string.update_mark : R.string.new_mark) :
                (isEditMode ? R.string.update_event : R.string.new_event)));
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator_layout);
        mTitleLayout = (RelativeLayout) findViewById(R.id.manager_title_layout);
        mTitleInput = (EditText) findViewById(R.id.manager_title_input);
        mSubjectLayout = (RelativeLayout) findViewById(R.id.manager_subject_layout);
        mSubjectSelector = (TextView) findViewById(R.id.manager_subjects_selector);
        mNotesInput = (EditText) findViewById(R.id.manager_notes_input);
        mEventSpinnerLayout = (RelativeLayout) findViewById(R.id.manager_event_spinner_layout);
        mEventSpinner = (Spinner) findViewById(R.id.manager_event_spinner);
        mMarkValueLayout = (RelativeLayout) findViewById(R.id.manager_mark_value_layout);
        mMarkPreview = (TextView) findViewById(R.id.manager_mark_preview);
        mDatePickerLayout = (RelativeLayout) findViewById(R.id.manager_date_layout);
        mDatePicker = (TextView) findViewById(R.id.manager_datepicker_button);
        FloatingActionButton mFab = (FloatingActionButton) findViewById(R.id.fab);

        setupFromIntent();
        mFab.setOnClickListener(this::save);

        // Animate layout transition for shared fab animation when editing
        if (isEditMode) {
            LinearLayout mRootLayout = (LinearLayout) findViewById(R.id.manager_layout);
            mRootLayout.setAlpha(0f);
            mRootLayout.animate().alpha(1f).setDuration(750).setStartDelay(250).start();
        }
        mFab.setVisibility(View.VISIBLE);
    }

    /**
     * Parse intent data to set up the UI
     */
    private void setupFromIntent() {
        Mark mLoadMark;
        Event mLoadEvent;
        String mLoadDate;
        String mLoadNotes;
        double mDoubleVal = 0;

        // Event categories
        List<String> mCategories = new ArrayList<>();
        mCategories.add(getString(R.string.event_spinner_test));
        mCategories.add(getString(R.string.event_spinner_school));
        mCategories.add(getString(R.string.event_spinner_bday));
        mCategories.add(getString(R.string.event_spinner_homework));
        mCategories.add(getString(R.string.event_spinner_reminder));
        mCategories.add(getString(R.string.event_spinner_hang_out));
        mCategories.add(getString(R.string.event_spinner_other));

        // Event spinner
        ArrayAdapter<String> mAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, mCategories);
        mEventSpinner.setAdapter(mAdapter);

        mDate = Utils.getToday();
        mDatePicker.setText(mDate);
        //noinspection deprecation
        mDatePickerLayout.setOnClickListener(v -> showDialog(33));

        // Load intent data
        if (isEditMode) {
            mObjId = mCallingIntent.getLongExtra("id", -1);

            if (isMark) {
                mLoadMark = mMarksController.getById(mObjId).first();
                mTitle = mLoadMark.getTitle();
                mLoadNotes = mLoadMark.getNote();
                mValue = mLoadMark.getValue();
                mLoadDate = mLoadMark.getDate();
                mDoubleVal = mValue;
                mMarkPreview.setText(String.format(Locale.ENGLISH, "%.2f", mDoubleVal / 100d));
            } else {
                mLoadEvent = mEventsController.getById(mObjId).first();
                mTitle = mLoadEvent.getTitle();
                mLoadNotes = mLoadEvent.getNote();
                mValue = mLoadEvent.getIcon();
                mLoadDate = mLoadEvent.getDate();
                mEventSpinner.setSelection(mValue);
            }

            mTitleInput.setText(mTitle);
            mDatePicker.setText(mLoadDate);
            mNotesInput.setText(mLoadNotes);
        }

        // Setup UI
        if (Utils.isTeacher(this) || !isMark) {
            mTitleInput.setHint(getString(isMark ? R.string.hint_student : R.string.hint_event));
            mSubjectLayout.setVisibility(View.GONE);
        } else {
            mTitleLayout.setVisibility(View.GONE);
        }

        if (isMark) {
            // Hide events-related items
            mEventSpinnerLayout.setVisibility(View.GONE);

            // Subjects list
            switch (Utils.getAddress(this)) {
                case "1":
                    mSubjects = getResources().getStringArray(R.array.subjects_lists_1);
                    break;
                case "2":
                    mSubjects = getResources().getStringArray(R.array.subjects_lists_2);
                    break;
                case "3":
                    mSubjects = getResources().getStringArray(R.array.subjects_lists_3);
                    break;
                case "4":
                    mSubjects = getResources().getStringArray(R.array.subjects_lists_4);
                    break;
                case "5":
                    mSubjects = getResources().getStringArray(R.array.subjects_lists_5);
                    break;
                default:
                    mSubjects = getResources().getStringArray(R.array.subjects_lists_0);
                    break;
            }

            // Subject selector
            mSubjectSelector.setText(isEditMode ? mTitle : getString(R.string.select_subject));
            mSubjectLayout.setOnClickListener(v -> new MaterialDialog.Builder(this)
                    .title(R.string.select_subject)
                    .items((CharSequence[]) mSubjects)
                    .itemsCallback((dialog, view, which, text) -> {
                        mSubjectSelector.setText(text);
                        mTitle = text.toString();
                    })
                    .show());

            // Mark mValue
            LayoutInflater mInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
            ViewGroup mGroup = (ViewGroup) findViewById(R.id.dialog_root);
            final View mDialogLayout = mInflater.inflate(R.layout.dialog_seekbar, mGroup);
            final TextView mPreview = (TextView) mDialogLayout.findViewById(R.id.dialog_value);
            final SeekBar mSeekBar = (SeekBar) mDialogLayout.findViewById(R.id.dialog_seekBar);

            mPreview.setText(isEditMode ? String.valueOf((double) mValue / 100) : "0.0");
            mSeekBar.setMax(40);
            mSeekBar.setProgress((int) mDoubleVal / 40);
            mDialogVal = mValue / 25;
            mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    mPreview.setText(String.valueOf((double) progress / 4));
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    double progress = mSeekBar.getProgress();
                    mDialogVal = mSeekBar.getProgress();
                    mPreview.setText(String.valueOf(progress / 4));
                }
            });

            mMarkValueLayout.setOnClickListener(view -> new MaterialDialog.Builder(this)
                    .title(R.string.dialog_select_mark)
                    .customView(mDialogLayout, false)
                    .positiveText(android.R.string.ok)
                    .negativeText(android.R.string.cancel)
                    .onPositive((dialog, which) -> {
                        mValue = (int) (mDialogVal * 25);
                        // Force English locale to use the "." instead of ","
                        mMarkPreview.setText(String.format(Locale.ENGLISH, "%.2f",
                                (double) (mDialogVal / 4)));
                    })
                    .show());
        } else {
            // Hide marks-related items
            mSubjectLayout.setVisibility(View.GONE);
            mMarkValueLayout.setVisibility(View.GONE);

            // Title fixer
            mTitleInput.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                }

                @Override
                public void onTextChanged(final CharSequence s, int start, int before, int count) {
                    if (s.length() > 26) {
                        Snackbar.make(mCoordinatorLayout, getString(R.string.editor_text_too_long),
                                Snackbar.LENGTH_LONG).setAction(
                                getString(R.string.editor_text_too_long_fix),
                                v -> mTitleInput.setText(String.format("%1$s \u2026",
                                        s.subSequence(0, 25)))).show();
                    }
                }
            });
        }

        // News to event
        long mNewsId = mCallingIntent.getLongExtra("newsToEvent", -1);
        if (mNewsId > 0) {
            NewsController mNewsController =
                    new NewsController(((BoldApp) getApplicationContext()).getConfig());
            News mNews = mNewsController.getById(mNewsId).first();
            mTitleInput.setText(mNews.getTitle());
            mNotesInput.setText(String.format("%1$s\n%2$s", mNews.getMessage(),
                    mNews.getUrl()));
            mEventSpinner.setSelection(1);
        }
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, isMark ? MarkListActivity.class : EventListActivity.class));
        finish();
    }

    @SuppressWarnings("deprecation")
    @Override
    protected Dialog onCreateDialog(int id) {
        if (id == 33) {
            return new DatePickerDialog(this, dpickerListener, mYear, mMonth - 1, mDay);
        }
        return null;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        mEvent.setIcon(position);
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
    }

    /**
     * Save the mark / event using data collected from the various
     * UI components
     *
     * @param fabView: fab view that's animated when the content has been successfully saved
     */
    private void save(View fabView) {
        if (isMark) {
            mMark = new Mark();
            saveMark(fabView);
        } else {
            mEvent = new Event();
            saveEvent(fabView);
        }
    }

    /**
     * Save mark
     *
     * @param fab: fab that will be animated when the mark is saved
     */
    private void saveMark(View fab) {
        if (Utils.isTeacher(this)) {
            mTitle = mTitleInput.getText().toString();
        }

        if (mTitle != null && !mTitle.isEmpty() && mValue != 0) {
            Utils.animFab((FloatingActionButton) fab, false);

            mMark.setId(mObjId);
            mMark.setTitle(mTitle);
            mMark.setNote(mNotesInput.getText().toString());
            mMark.setValue(mValue);
            mMark.setDate(mDatePicker.getText().toString(),
                    Utils.isFirstQuarter(this, mDatePicker.getText().toString()));

            mObjId = isEditMode ? mMarksController.update(mMark) : mMarksController.add(mMark);

            Snackbar.make(fab, getString(R.string.saved), Snackbar.LENGTH_SHORT).show();
            new Handler().postDelayed(this::onBackPressed, 1000);
        } else {
            Snackbar.make(fab, getString(R.string.manager_invalid),
                    Snackbar.LENGTH_SHORT).show();
        }
    }

    /**
     * Save event
     *
     * @param fab: fab that will be animated when the event is saved
     */
    private void saveEvent(View fab) {
        mTitle = mTitleInput.getText().toString();

        if (mTitle.isEmpty()) {
            Snackbar.make(fab, getString(R.string.manager_invalid), Snackbar.LENGTH_SHORT).show();
        } else {
            Utils.animFab((FloatingActionButton) fab, false);

            mEvent.setId(mObjId);
            mEvent.setTitle(mTitle);
            mEvent.setIcon(mEventSpinner.getSelectedItemPosition());
            mEvent.setDate(mDate);
            mEvent.setNote(mNotesInput.getText().toString());

            mObjId = isEditMode ? mEventsController.update(mEvent) : mEventsController.add(mEvent);

            Snackbar.make(fab, getString(R.string.saved), Snackbar.LENGTH_SHORT).show();
            new Handler().postDelayed(this::onBackPressed, 1000);
        }
    }
}
