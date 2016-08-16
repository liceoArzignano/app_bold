package it.liceoarzignano.bold;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import it.liceoarzignano.bold.events.Event;
import it.liceoarzignano.bold.events.EventListActivity;
import it.liceoarzignano.bold.marks.Mark;
import it.liceoarzignano.bold.marks.MarkListActivity;
import it.liceoarzignano.bold.realm.RealmController;

public class ManagerActivity extends AppCompatActivity
        implements AdapterView.OnItemSelectedListener {

    private CoordinatorLayout coordinatorLayout;
    private ImageView mBanner;
    private EditText mTitleInput;
    private LinearLayout mEventSpinnerLayout;
    private Spinner mEventSpinner;
    private Button mSubSelectButton;
    private EditText mNotesInput;
    private TextView mMarkPreview;
    private SeekBar mMarkSeekBar;
    private Button mDatePicker;
    private Toolbar toolbar;
    private Context context;

    private long objID; // id | id
    private Mark objMark;
    private Event objEvent;
    private String[] subjects;
    private String title;
    private String notes;
    private String date;
    private int value;

    private int mYear, mMonth, mDay;
    private String mDate;
    private final DatePickerDialog.OnDateSetListener dpickerListener
            = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            mYear = year;
            mMonth = monthOfYear + 1;
            mDay = dayOfMonth;
            mDate = Utils.rightDate(mYear, mMonth, mDay);
            mDatePicker.setText(String.format(getResources().getString(R.string.current_date),
                    mDate));
        }
    };

    private boolean editMode = false;
    private boolean isMark = true;
    private boolean hasSaved = false;
    private Intent callingIntent;

    private RealmController controller;

    /**
     * Intent-extra:
     * boolean isEditing
     * boolean isMark
     * long id
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        context = this;
        controller = RealmController.with(this);

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);
        mBanner = (ImageView) findViewById(R.id.banner_image);
        mTitleInput = (EditText) findViewById(R.id.title_input);
        mEventSpinnerLayout = (LinearLayout) findViewById(R.id.event_spinner_layout);
        mEventSpinner = (Spinner) findViewById(R.id.event_spinner);
        mSubSelectButton = (Button) findViewById(R.id.subjects_selector);
        mNotesInput = (EditText) findViewById(R.id.input_notes);
        mMarkPreview = (TextView) findViewById(R.id.mark_preview);
        mMarkSeekBar = (SeekBar) findViewById(R.id.mark_seek);
        mDatePicker = (Button) findViewById(R.id.datepicker_button);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        context = this;

        callingIntent = getIntent();
        setupFromIntent();

        mSubSelectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MaterialDialog.Builder(context)
                        .title(R.string.select_subject)
                        .items((CharSequence[]) subjects)
                        .itemsCallback(new MaterialDialog.ListCallback() {
                            @Override
                            public void onSelection(MaterialDialog dialog,
                                                    View view, int which, CharSequence text) {
                                mSubSelectButton.setText(String.format(getResources().getString(
                                        R.string.selected_subject), text));
                                title = text.toString();
                            }
                        })
                        .show();
            }
        });

        if (!isMark) {
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
                        Snackbar.make(coordinatorLayout, getString(R.string.editor_text_too_long),
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

        switch (Utils.getAddress(context)) {
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

        if (fab != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    save(view);
                }
            });
        }

        mMarkSeekBar = (SeekBar) findViewById(R.id.mark_seek);
        mMarkSeekBar.setMax(40);

        mMarkSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                double progressDouble = (double) progress / 4;
                String msg = String.format(getResources().getString(
                        R.string.current_mark), String.valueOf(progressDouble));
                mMarkPreview.setText(msg);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                double progress = mMarkSeekBar.getProgress();
                progress *= 25;
                value = (int) progress;
                progress /= 100;
                String msg = String.format(getResources().getString(
                        R.string.current_mark), String.valueOf(progress));
                Snackbar.make(coordinatorLayout, msg, Snackbar.LENGTH_LONG);
                mMarkPreview.setText(msg);
            }
        });

        Utils.animFabIntro(this, fab,
                isMark ? getString(Utils.isTeacher(this) ?
                        R.string.intro_fab_save_mark_teacher :
                        R.string.intro_fab_save_mark_student) :
                        getString(R.string.intro_fab_save_event),
                isMark ? "markKey" : "eventKey");
    }

    /**
     * Parse intent data to set up the UI
     */
    private void setupFromIntent() {
        Calendar calendar = Calendar.getInstance();
        Mark loadMark;
        Event loadEvent;

        editMode = callingIntent.getBooleanExtra("isEditing", false);
        isMark = callingIntent.getBooleanExtra("isMark", true);
        mBanner.setBackgroundResource(isMark ? R.drawable.newmark : R.drawable.newevent);
        mSubSelectButton.setVisibility(Utils.isTeacher(context) ? View.GONE : View.VISIBLE);
        mYear = calendar.get(Calendar.YEAR);
        mMonth = calendar.get(Calendar.MONTH) + 1;
        mDay = calendar.get(Calendar.DAY_OF_MONTH);
        mDate = Utils.rightDate(mYear, mMonth, mDay);
        mDatePicker.setText(String.format(getResources().getString(
                R.string.current_date), mDate));
        mDatePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //noinspection deprecation
                showDialog(33);
            }
        });

        if (Utils.isTeacher(context) || !isMark) {
            mTitleInput.setVisibility(View.VISIBLE);
            mTitleInput.setHint(getString(isMark ? R.string.hint_student : R.string.hint_event));
        }

        // Show events UI if needed
        if (!isMark) {
            mEventSpinnerLayout.setVisibility(View.VISIBLE);
            mNotesInput.setVisibility(View.GONE);
            mSubSelectButton.setVisibility(View.GONE);
            mMarkPreview.setVisibility(View.GONE);
            mMarkSeekBar.setVisibility(View.GONE);

            List<String> categories = new ArrayList<>();
            categories.add(getString(R.string.event_spinner_test));
            categories.add(getString(R.string.event_spinner_school));
            categories.add(getString(R.string.event_spinner_bday));
            if (Utils.isTeacher(context)) {
                categories.add(getString(R.string.event_spinner_hang_out));
            }
            categories.add(getString(R.string.event_spinner_other));

            ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(
                    this, android.R.layout.simple_dropdown_item_1line, categories);
            mEventSpinner.setAdapter(dataAdapter);
        }

        // Load intent data
        if (editMode) {
            objID = callingIntent.getLongExtra("id", -1);
            if (editMode) {
                if (isMark) {
                    loadMark = controller.getMark(objID);
                    title = loadMark.getTitle();
                    notes = loadMark.getContent();
                    value = loadMark.getValue();
                    date = loadMark.getDate();
                } else {
                    loadEvent = controller.getEvent(objID);
                    title = loadEvent.getTitle();
                    value = loadEvent.getIcon();
                    date = loadEvent.getDate();
                }
            }

            toolbar.setTitle(getString(isMark ? R.string.update_mark : R.string.update_event));
            mTitleInput.setText(title);
            mDatePicker.setText(String.format(
                    getResources().getString(R.string.current_date), date));

            if (isMark) {
                mNotesInput.setText(notes);
                double markValuePreview = (double) value / 100;
                mMarkSeekBar.setProgress((int) markValuePreview * 4);
                mSubSelectButton.setText(String.format(getResources().getString(
                        R.string.selected_subject), title));
            } else {
                mEventSpinner.setSelection(!Utils.isTeacher(context) && value == 4 ? 3 : value);
            }
            mMarkPreview.setText(String.format(getResources().getString(R.string.current_mark),
                    editMode ? String.valueOf((double) value / 100) : "0.0"));
        }
    }

    @Override
    public void onBackPressed() {
        if (hasSaved) {
            Intent editIntent = new Intent(this, ViewerActivity.class);

            editIntent.putExtra("isEditing", true);
            editIntent.putExtra("isMark", isMark);
            editIntent.putExtra("id", objID);

            if (Utils.hasApi21()) {
                View sharedElement = findViewById(R.id.banner_image);

                ActivityOptionsCompat options = ActivityOptionsCompat
                        .makeSceneTransitionAnimation(this,
                                sharedElement, "imageShared");
                ActivityCompat.startActivity(this,
                        editIntent, options.toBundle());
            } else {
                startActivity(editIntent);
            }
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
            return new DatePickerDialog(this, dpickerListener, mYear, mMonth - 1, mDay);
        }
        return null;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        objEvent.setIcon(position);
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
            objMark = new Mark();
            saveMark(fabView);
        } else {
            objEvent = new Event();
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

            objMark.setId(objID);
            objMark.setTitle(title);
            objMark.setContent(mNotesInput.getText().toString());
            objMark.setValue(value);
            objMark.setDate(mDate);

            objID = editMode ? controller.updateMark(objMark) : controller.addMark(objMark);

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

        if (!title.isEmpty()) {
            Utils.animFab((FloatingActionButton) fab);

            objEvent.setId(objID);
            objEvent.setTitle(title);
            objEvent.setIcon(mEventSpinner.getSelectedItemPosition());
            objEvent.setDate(mDate);

            objID = editMode ? controller.updateEvent(objEvent) : controller.addEvent(objEvent);

            hasSaved = true;

            Snackbar.make(fab, getString(R.string.saved), Snackbar.LENGTH_SHORT).show();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    onBackPressed();
                }
            }, 1000);

        } else {
            Snackbar.make(fab, getString(R.string.manager_invalid), Snackbar.LENGTH_SHORT).show();
        }
    }
}
