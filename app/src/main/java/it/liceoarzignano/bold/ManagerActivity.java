package it.liceoarzignano.bold;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import it.liceoarzignano.bold.events.Event;
import it.liceoarzignano.bold.events.EventListActivity;
import it.liceoarzignano.bold.marks.Mark;
import it.liceoarzignano.bold.marks.MarkListActivity;
import it.liceoarzignano.bold.realm.RealmController;

public class ManagerActivity extends AppCompatActivity
        implements AdapterView.OnItemSelectedListener {

    private Context mContext;
    private Intent mCallingIntent;
    private RealmController controller;

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
    // Mark value
    private RelativeLayout mMarkValueLayout;
    private TextView mMarkPreview;
    // Date picker
    private RelativeLayout mDatePickerLayout;
    private TextView mDatePicker;

    private long mObjId;
    private Mark mMark;
    private Event mEvent;
    private int value;
    private String[] subjects;
    private String title;
    private String notes;
    private double dialogValue;

    private boolean editMode = false;
    private boolean isMark = true;
    private boolean hasSaved = false;

    private int year;
    private int month;
    private int day;
    private String mDate;
    private final DatePickerDialog.OnDateSetListener dpickerListener
            = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            ManagerActivity.this.year = year;
            month = monthOfYear + 1;
            day = dayOfMonth;
            mDate = Utils.rightDate(ManagerActivity.this.year, month, day);
            mDatePicker.setText(mDate);
        }
    };

    /*
     * Intent-extra:
     * boolean isEditing
     * boolean isMark
     * long id
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager);

        mContext = this;
        controller = RealmController.with(this);

        mCallingIntent = getIntent();
        isMark = mCallingIntent.getBooleanExtra("isMark", true);
        editMode = mCallingIntent.getBooleanExtra("isEditing", false);

        // Toolbar
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle(getString(isMark ?
                (editMode ? R.string.update_mark : R.string.new_mark) :
                (editMode ? R.string.update_event : R.string.new_event)));
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);
        mTitleLayout = (RelativeLayout) findViewById(R.id.title_layout);
        mTitleInput = (EditText) findViewById(R.id.title_input);
        mSubjectLayout = (RelativeLayout) findViewById(R.id.subject_layout);
        mSubjectSelector = (TextView) findViewById(R.id.subjects_selector);
        mNotesInput = (EditText) findViewById(R.id.notes_input);
        mEventSpinnerLayout = (RelativeLayout) findViewById(R.id.event_spinner_layout);
        mEventSpinner = (Spinner) findViewById(R.id.event_spinner);
        mMarkValueLayout = (RelativeLayout) findViewById(R.id.mark_value_layout);
        mMarkPreview = (TextView) findViewById(R.id.mark_preview);
        mDatePickerLayout = (RelativeLayout) findViewById(R.id.date_layout);
        mDatePicker = (TextView) findViewById(R.id.datepicker_button);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        setupFromIntent();
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                save(view);
            }
        });

        // Animate layout transition for shared fab animation when editing
        if (editMode) {
            LinearLayout mRootLayout = (LinearLayout) findViewById(R.id.manager_layout);
            mRootLayout.setAlpha(0f);
            mRootLayout.animate().alpha(1f).setDuration(750).setStartDelay(250).start();
        }

        Utils.animFabIntro(this, fab,
                getString(isMark ?
                        R.string.intro_fab_save_mark_title : R.string.intro_fab_save_event_title),
                getString(isMark ? R.string.intro_fab_save_mark : R.string.intro_fab_save_event),
                isMark ? "markManKey" : "eventManKey");

    }

    /**
     * Parse intent data to set up the UI
     */
    private void setupFromIntent() {
        Calendar calendar = Calendar.getInstance();
        Mark loadMark;
        Event loadEvent;
        String date;

        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH) + 1;
        day = calendar.get(Calendar.DAY_OF_MONTH);
        mDate = Utils.rightDate(year, month, day);
        mDatePicker.setText(mDate);
        mDatePickerLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //noinspection deprecation
                showDialog(33);
            }
        });

        // Load intent data
        if (editMode) {
            mObjId = mCallingIntent.getLongExtra("id", -1);
            if (isMark) {
                loadMark = controller.getMark(mObjId);
                title = loadMark.getTitle();
                notes = loadMark.getContent();
                value = loadMark.getValue();
                date = loadMark.getDate();
                double dValue = value;
                mMarkPreview.setText(String.format(Locale.ENGLISH, "%.2f", dValue / 100d));
            } else {
                loadEvent = controller.getEvent(mObjId);
                title = loadEvent.getTitle();
                value = loadEvent.getIcon();
                date = loadEvent.getDate();
                notes = loadEvent.getNote();
                mEventSpinner.setSelection(!Utils.isTeacher(mContext) && value == 4 ? 3 : value);
            }

            mTitleInput.setText(title);
            mDatePicker.setText(date);
            mNotesInput.setText(notes);
        }

        // Setup UI
        if (Utils.isTeacher(mContext) || !isMark) {
            mTitleInput.setHint(getString(isMark ? R.string.hint_student : R.string.hint_event));
            if (isMark) {
                mSubjectSelector.setVisibility(View.GONE);
            }
        } else {
            mTitleLayout.setVisibility(View.GONE);
        }

        if (isMark) {
            // Hide events-related items
            mEventSpinnerLayout.setVisibility(View.GONE);

            // Subjects list
            switch (Utils.getAddress(mContext)) {
                case "1":
                    subjects = getResources().getStringArray(R.array.subjects_lists_1);
                    break;
                case "2":
                    subjects = getResources().getStringArray(R.array.subjects_lists_2);
                    break;
                case "3":
                    subjects = getResources().getStringArray(R.array.subjects_lists_3);
                    break;
                case "4":
                    subjects = getResources().getStringArray(R.array.subjects_lists_4);
                    break;
                case "5":
                    subjects = getResources().getStringArray(R.array.subjects_lists_5);
                    break;
                default:
                    subjects = getResources().getStringArray(R.array.subjects_lists_0);
                    break;
            }

            // Subject selector
            mSubjectSelector.setText(editMode ? String.format(getResources()
                    .getString(R.string.selected_subject), title) :
                    getString(R.string.select_subject));
            mSubjectLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new MaterialDialog.Builder(mContext)
                            .title(R.string.select_subject)
                            .items((CharSequence[]) subjects)
                            .itemsCallback(new MaterialDialog.ListCallback() {
                                @Override
                                public void onSelection(MaterialDialog dialog,
                                                        View view, int which, CharSequence text) {
                                    mSubjectSelector.setText(String.format(getResources().getString(
                                            R.string.selected_subject), text));
                                    title = text.toString();
                                }
                            })
                            .show();
                }
            });

            // Mark value
            LayoutInflater mInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
            ViewGroup mGroup = (ViewGroup) findViewById(R.id.dialog_root);
            final View mDialogLayout = mInflater.inflate(R.layout.dialog_seekbar, mGroup);
            final TextView mPreview = (TextView) mDialogLayout.findViewById(R.id.value);
            final SeekBar mSeekBar = (SeekBar) mDialogLayout.findViewById(R.id.seekBar);

            mPreview.setText(editMode ? String.valueOf((double) value / 100) : "0.0");
            mSeekBar.setMax(40);
            mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {}
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    mPreview.setText(String.valueOf((double) progress / 4));
                }
                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    double progress = mSeekBar.getProgress();
                    dialogValue = mSeekBar.getProgress();
                    mPreview.setText(String.valueOf(progress / 4));
                }
            });

            mMarkValueLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new MaterialDialog.Builder(mContext)
                            .title(R.string.dialog_select_mark)
                            .customView(mDialogLayout, false)
                            .positiveText(android.R.string.ok)
                            .negativeText(android.R.string.cancel)
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog,
                                                    @NonNull DialogAction which) {
                                    value = (int) (dialogValue * 25);
                                    // Force English locale to use the "." instead of ","
                                    mMarkPreview.setText(String.format(Locale.ENGLISH, "%.2f",
                                            (double) (dialogValue / 4)));
                                }
                            })
                            .show();
                }
            });
        } else {
            // Hide marks-related items
            mSubjectLayout.setVisibility(View.GONE);
            mMarkValueLayout.setVisibility(View.GONE);

            // Event categories
            List<String> categories = new ArrayList<>();
            categories.add(getString(R.string.event_spinner_test));
            categories.add(getString(R.string.event_spinner_school));
            categories.add(getString(R.string.event_spinner_bday));
            if (Utils.isTeacher(mContext)) {
                categories.add(getString(R.string.event_spinner_hang_out));
            }
            categories.add(getString(R.string.event_spinner_other));

            // Event spinner
            ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(
                    this, android.R.layout.simple_dropdown_item_1line, categories);
            mEventSpinner.setAdapter(dataAdapter);

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
                                new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        String str = s.subSequence(0, 25) + "\u2026";
                                        mTitleInput.setText(str);
                                    }
                                }).show();
                    }
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        if (hasSaved) {
            Intent editIntent = new Intent(this, ViewerActivity.class);

            editIntent.putExtra("isEditing", true);
            editIntent.putExtra("isMark", isMark);
            editIntent.putExtra("id", mObjId);

            startActivity(editIntent);
        } else {
            Intent listIntent = new Intent(this, isMark ?
                    MarkListActivity.class : EventListActivity.class);
            startActivity(listIntent);
        }

        finish();
    }

    @SuppressWarnings("deprecation")
    @Override
    protected Dialog onCreateDialog(int id) {
        if (id == 33) {
            return new DatePickerDialog(this, dpickerListener, year, month - 1, day);
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
            title = mTitleInput.getText().toString();
        }

        if (title != null && !title.isEmpty() && value != 0) {
            Utils.animFab((FloatingActionButton) fab);

            mMark.setId(mObjId);
            mMark.setTitle(title);
            mMark.setContent(mNotesInput.getText().toString());
            mMark.setValue(value);
            mMark.setDate(mDate);

            mObjId = editMode ? controller.updateMark(mMark) : controller.addMark(mMark);

            hasSaved = true;

            Snackbar.make(fab, getString(R.string.saved), Snackbar.LENGTH_SHORT).show();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    onBackPressed();
                }
            }, 1000);
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
        title = mTitleInput.getText().toString();

        if (title.isEmpty()) {
            Snackbar.make(fab, getString(R.string.manager_invalid), Snackbar.LENGTH_SHORT).show();
        } else {
            Utils.animFab((FloatingActionButton) fab);

            mEvent.setId(mObjId);
            mEvent.setTitle(title);
            mEvent.setIcon(mEventSpinner.getSelectedItemPosition());
            mEvent.setDate(mDate);
            mEvent.setNote(mNotesInput.getText().toString());

            mObjId = editMode ? controller.updateEvent(mEvent) : controller.addEvent(mEvent);

            hasSaved = true;

            Snackbar.make(fab, getString(R.string.saved), Snackbar.LENGTH_SHORT).show();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    onBackPressed();
                }
            }, 1000);
        }
    }
}
