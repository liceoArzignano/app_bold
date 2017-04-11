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
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import it.liceoarzignano.bold.events.Event;
import it.liceoarzignano.bold.events.EventsController;
import it.liceoarzignano.bold.marks.Mark;
import it.liceoarzignano.bold.marks.MarksController;
import it.liceoarzignano.bold.news.News;
import it.liceoarzignano.bold.news.NewsController;
import it.liceoarzignano.bold.utils.DateUtils;
import it.liceoarzignano.bold.utils.PrefsUtils;

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
    private Date mDate;
    private DatePickerDialog.OnDateSetListener mDatePickerListener;

    /*
     * Intent-extra:
     * boolean isEditing
     * boolean isMark
     * long id
     * long newsToEvent
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager);

        mMarksController = new MarksController(((BoldApp) getApplicationContext()).getConfig());
        mEventsController = new EventsController(((BoldApp) getApplicationContext()).getConfig());

        mDate = Calendar.getInstance().getTime();

        mCallingIntent = getIntent();
        isMark = mCallingIntent.getBooleanExtra("isMark", true);
        isEditMode = mCallingIntent.getBooleanExtra("isEditing", false);

        // Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(getString(isMark ?
                (isEditMode ? R.string.update_mark : R.string.new_mark) :
                (isEditMode ? R.string.update_event : R.string.new_event)));
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mDatePickerListener = (view, year, month, day) -> {
            mYear = year;
            mMonth = month;
            mDay = day;
            mDate = DateUtils.intToDate(mYear, mMonth, mDay);
            mDatePicker.setText(DateUtils.dateToString(mDate));
        };

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
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        setupFromIntent();
        fab.setOnClickListener(v -> save(fab));

        // Animate layout transition for shared fab animation when editing
        if (isEditMode) {
            LinearLayout rootLayout = (LinearLayout) findViewById(R.id.manager_layout);
            rootLayout.setAlpha(0f);
            rootLayout.animate().alpha(1f).setDuration(750).setStartDelay(250).start();
        }
        fab.setVisibility(View.VISIBLE);
    }

    /**
     * Parse intent data to set up the UI
     */
    private void setupFromIntent() {
        Mark loadMark;
        Event loadEvent;
        Date loadDate;
        String loadNotes;
        double value = 0;

        // Init calendar
        Calendar calendar = Calendar.getInstance();
        mDatePicker.setText(DateUtils.dateToString(mDate));

        // Load intent data
        if (isEditMode) {
            mObjId = mCallingIntent.getLongExtra("id", -1);

            if (isMark) {
                loadMark = mMarksController.getById(mObjId).first();
                mTitle = loadMark.getTitle();
                loadNotes = loadMark.getNote();
                mValue = loadMark.getValue();
                loadDate = loadMark.getDate();
                value = mValue;
                mMarkPreview.setText(String.format(Locale.ENGLISH, "%.2f", value / 100d));
            } else {
                loadEvent = mEventsController.getById(mObjId).first();
                mTitle = loadEvent.getTitle();
                loadNotes = loadEvent.getNote();
                mValue = loadEvent.getIcon();
                loadDate = loadEvent.getDate();
            }

            mTitleInput.setText(mTitle);
            mDatePicker.setText(DateUtils.dateToString(loadDate));
            mNotesInput.setText(loadNotes);
            calendar.setTime(loadDate);
        }

        // Setup UI
        if (PrefsUtils.isTeacher(this) || !isMark) {
            mTitleInput.setHint(getString(isMark ? R.string.hint_student : R.string.hint_event));
            mSubjectLayout.setVisibility(View.GONE);
        } else {
            mTitleLayout.setVisibility(View.GONE);
        }

        mYear = calendar.get(Calendar.YEAR);
        mMonth = calendar.get(Calendar.MONTH) + 1;
        mDay = calendar.get(Calendar.DAY_OF_MONTH);
        //noinspection deprecation
        mDatePickerLayout.setOnClickListener(v -> showDialog(33));

        if (isMark) {
            setupMarkUi(value);
        } else {
            setupEventUi();
        }

        // News to event
        setupNewsUi(mCallingIntent.getLongExtra("newsToEvent", -1));
    }

    @SuppressWarnings("deprecation")
    @Override
    protected Dialog onCreateDialog(int id) {
        if (id == 33) {
            return new DatePickerDialog(this, mDatePickerListener, mYear, mMonth - 1, mDay);
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
     * @param fab: fab view that's animated when the content has been successfully saved
     */
    private void save(FloatingActionButton fab) {
        if (isMark) {
            mMark = new Mark();
            saveMark(fab);
        } else {
            mEvent = new Event();
            saveEvent(fab);
        }
    }

    /**
     * Save mark
     *
     * @param fab: fab that will be animated when the mark is saved
     */
    private void saveMark(FloatingActionButton fab) {
        if (PrefsUtils.isTeacher(this)) {
            mTitle = mTitleInput.getText().toString();
        }

        if (mTitle != null && !mTitle.isEmpty() && mValue != 0) {
            new Handler().postDelayed(fab::hide, 500);


            mMark.setId(mObjId);
            mMark.setTitle(mTitle);
            mMark.setNote(mNotesInput.getText().toString());
            mMark.setValue(mValue);
            mMark.setDate(mDate, PrefsUtils.isFirstQuarter(this, mDate));

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
    private void saveEvent(FloatingActionButton fab) {
        mTitle = mTitleInput.getText().toString();

        if (mTitle.isEmpty()) {
            Snackbar.make(fab, getString(R.string.manager_invalid), Snackbar.LENGTH_SHORT).show();
        } else {
            new Handler().postDelayed(fab::hide, 500);


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

    /**
     * Setup marks ui
     *
     * @param value mark value
     */
    private void setupMarkUi(double value) {
        // Hide events-related items
        mEventSpinnerLayout.setVisibility(View.GONE);

        // Subjects list
        switch (PrefsUtils.getAddress(this)) {
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
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        ViewGroup group = (ViewGroup) findViewById(R.id.dialog_root);
        final View dialog = inflater.inflate(R.layout.dialog_seekbar, group);
        final TextView preview = (TextView) dialog.findViewById(R.id.dialog_value);
        final SeekBar seekBar = (SeekBar) dialog.findViewById(R.id.dialog_seekBar);

        preview.setText(isEditMode ? String.valueOf((double) mValue / 100) : "0.0");
        seekBar.setMax(40);
        seekBar.setProgress((int) value / 40);
        mDialogVal = mValue / 25;
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                preview.setText(String.valueOf((double) progress / 4));
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                double progress = seekBar.getProgress();
                mDialogVal = seekBar.getProgress();
                preview.setText(String.valueOf(progress / 4));
            }
        });

        mMarkValueLayout.setOnClickListener(view -> new MaterialDialog.Builder(this)
                .title(R.string.dialog_select_mark)
                .customView(dialog, false)
                .positiveText(android.R.string.ok)
                .negativeText(android.R.string.cancel)
                .onPositive((dialogView, which) -> {
                    mValue = (int) (mDialogVal * 25);
                    // Force English locale to use the "." instead of ","
                    mMarkPreview.setText(String.format(Locale.ENGLISH, "%.2f",
                            (double) (mDialogVal / 4)));
                })
                .show());
    }

    /**
     * Setup events ui
     */
    private void setupEventUi() {
        // Hide marks-related items
        mSubjectLayout.setVisibility(View.GONE);
        mMarkValueLayout.setVisibility(View.GONE);

        // Event categories
        List<String> categories = new ArrayList<>();
        categories.add(getString(R.string.event_spinner_test));
        categories.add(getString(R.string.event_spinner_school));
        categories.add(getString(R.string.event_spinner_bday));
        categories.add(getString(R.string.event_spinner_homework));
        categories.add(getString(R.string.event_spinner_reminder));
        categories.add(getString(R.string.event_spinner_hang_out));
        categories.add(getString(R.string.event_spinner_other));

        // Event spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, categories);
        mEventSpinner.setAdapter(adapter);
        if (isEditMode) {
            mEventSpinner.setSelection(mValue);
        }

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
                            snackView -> mTitleInput.setText(String.format("%1$s \u2026",
                                    s.subSequence(0, 25)))).show();
                }
            }
        });
    }

    /**
     * Setup news ui
     *
     * @param newsId news id
     */
    private void setupNewsUi(long newsId) {
        if (newsId > 0) {
            NewsController newsController =
                    new NewsController(((BoldApp) getApplicationContext()).getConfig());
            News news = newsController.getById(newsId).first();
            mTitleInput.setText(news.getTitle());
            mNotesInput.setText(String.format("%1$s\n%2$s", news.getMessage(), news.getUrl()));
            mEventSpinner.setSelection(1);
        }
    }
}
