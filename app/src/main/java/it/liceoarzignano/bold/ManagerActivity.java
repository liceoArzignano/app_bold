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
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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

public class ManagerActivity extends AppCompatActivity
        implements AdapterView.OnItemSelectedListener {

    private CoordinatorLayout coordinatorLayout;
    private ImageView mBanner;
    private TextInputLayout mTitleInputLayout; // Student OR Event title
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

    private int objID; // id | id
    private String objTitle = ""; // title | title
    private int objVal; // value | icon
    private String[] subjects;

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
    private Intent callingIntent;

    /**
     * Intent-extra:
     * boolean isEditing
     * boolean isMark
     * int id
     * string title
     * int val
     * string note
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        context = this;

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);
        mBanner = (ImageView) findViewById(R.id.banner_image);
        mTitleInputLayout = (TextInputLayout) findViewById(R.id.title_input_layout);
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
                        .items(subjects)
                        .itemsCallback(new MaterialDialog.ListCallback() {
                            @Override
                            public void onSelection(MaterialDialog dialog,
                                                    View view, int which, CharSequence text) {
                                mSubSelectButton.setText(String.format(getResources().getString(
                                        R.string.selected_subject), text));
                                objTitle = text.toString();
                            }
                        })
                        .show();
            }
        });


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

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                save(view);
            }
        });

        mMarkSeekBar = (SeekBar) findViewById(R.id.mark_seek);
        assert mMarkSeekBar != null;
        mMarkSeekBar.setMax(40);

        mMarkSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                double progressDouble = (double) progress / 4;
                String msg = String.format(getResources().getString(
                        R.string.current_mark), progressDouble);
                mMarkPreview.setText(msg);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                double progress = mMarkSeekBar.getProgress();
                progress *= 25;
                objVal = (int) progress;
                progress /= 100;
                String msg = String.format(getResources().getString(
                        R.string.current_mark), progress);
                Snackbar.make(coordinatorLayout, msg, Snackbar.LENGTH_LONG);
                mMarkPreview.setText(msg);
            }
        });

        Utils.animFabIntro(this, fab,
                isMark ? getString(Utils.isTeacher(this) ?
                        R.string.intro_fab_save_mark_teacher :
                        R.string.intro_fab_save_mark_student) :
                        getString(R.string.intro_fab_save_event),
                getString(R.string.intro_gotit), isMark ? "markKey" : "eventKey");
    }

    /**
     * Parse intent data to set up the UI
     */
    private void setupFromIntent() {
        editMode = callingIntent.getBooleanExtra("isEditing", false);
        isMark = callingIntent.getBooleanExtra("isMark", true);
        mBanner.setBackgroundResource(isMark ? R.drawable.newmark : R.drawable.newevent);

        if (Utils.isTeacher(context) || !isMark) {
            mTitleInputLayout.setVisibility(View.VISIBLE);
            mTitleInput.setVisibility(View.VISIBLE);
            mTitleInput.setHint(getString(isMark ? R.string.hint_student : R.string.hint_event));
        }

        mSubSelectButton.setVisibility(Utils.isTeacher(context) ? View.GONE : View.VISIBLE);

        // Set up Events UI if needed
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

            mDatePicker.setVisibility(View.VISIBLE);
            Calendar calendar = Calendar.getInstance();
            mYear = calendar.get(Calendar.YEAR);
            mMonth = calendar.get(Calendar.MONTH) + 1;
            mDay = calendar.get(Calendar.DAY_OF_MONTH);
            mDate = Utils.rightDate(mYear, mMonth, mDay);
            mDatePicker.setText(String.format(getResources().getString(
                    R.string.current_date), mDate));
            mDatePicker.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDialog(33);
                }
            });
        }

        // Load intent data
        if (editMode) {
            objID = callingIntent.getIntExtra("id", -1);
            objTitle = callingIntent.getStringExtra("title");
            objVal = callingIntent.getIntExtra("val", 0);
            String objNote = callingIntent.getStringExtra("note");

            toolbar.setTitle(getString(isMark ? R.string.update_mark : R.string.update_event));
            mTitleInput.setText(objTitle);

            if (!isMark) {
                mEventSpinner.setSelection((!Utils.isTeacher(context) && objVal == 4) ?
                        3 : objVal);
                mDatePicker.setText(String.format(
                        getResources().getString(R.string.current_date), objNote));
            } else {
                mNotesInput.setText(objNote);
                double markValuePreview = (double) objVal / 100;
                mMarkPreview.setText(String.format(getResources().getString(
                        R.string.current_mark), markValuePreview));
                mMarkSeekBar.setProgress((int) markValuePreview * 4);
                mSubSelectButton.setText(String.format(getResources().getString(
                        R.string.selected_subject), objTitle));
            }
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        if (id == 33) {
            return new DatePickerDialog(this, dpickerListener, mYear, mMonth - 1, mDay);
        }
        return null;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        objVal = position;
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
    private void save(final View fabView) {
        if (isMark) {
            saveMark(fabView);
        } else {
            // Save event
            saveEvent(fabView);
        }
    }

    /**
     * Save mark
     * @param fab: fab that will be animated when the mark is saved
     */
    private void saveMark(final View fab) {
        if (Utils.isTeacher(this)) {
            objTitle = mTitleInput.getText().toString();
        }

        if (!objTitle.isEmpty() && objVal != 0) {
            Utils.animFab(false, (FloatingActionButton) fab);
            it.liceoarzignano.bold.marks.DatabaseConnection databaseConnection =
                    it.liceoarzignano.bold.marks.DatabaseConnection.getInstance(context);

            Mark mark = new Mark(objID, objTitle,
                    objVal, mNotesInput.getText().toString());

            if (editMode) {
                databaseConnection.updateMark(mark);
            } else {
                databaseConnection.addMark(mark);
            }

            Snackbar.make(fab, getString(R.string.saved), Snackbar.LENGTH_SHORT).show();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent markList = new Intent(context, MarkListActivity.class);
                    startActivity(markList);
                    finish();
                }
            }, 1000);

        } else {
            Snackbar.make(fab, getString(R.string.manager_invalid),
                    Snackbar.LENGTH_SHORT).show();
        }
    }

    /**
     * Save event
     * @param fab: fab that will be animated when the event is saved
     */
    private void saveEvent(View fab) {
        objTitle = mTitleInput.getText().toString();

        if (!objTitle.isEmpty()) {
            Utils.animFab(false, (FloatingActionButton) fab);
            it.liceoarzignano.bold.events.DatabaseConnection databaseConnection =
                    it.liceoarzignano.bold.events.DatabaseConnection.getInstance(context);

            Event event = new Event(objID, objTitle, mDate, mEventSpinner.getSelectedItemPosition());

            if (editMode) {
                databaseConnection.updateEvent(event);
            } else {
                databaseConnection.addEvent(event);
            }

            Snackbar.make(fab, getString(R.string.saved), Snackbar.LENGTH_SHORT).show();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent eventList = new Intent(context, EventListActivity.class);
                    startActivity(eventList);
                    finish();
                }
            }, 1000);

        } else {
            Snackbar.make(fab, getString(R.string.manager_invalid), Snackbar.LENGTH_SHORT).show();
        }
    }
}

