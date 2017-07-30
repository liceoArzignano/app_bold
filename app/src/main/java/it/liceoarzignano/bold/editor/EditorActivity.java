package it.liceoarzignano.bold.editor;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import it.liceoarzignano.bold.R;
import it.liceoarzignano.bold.events.Event2;
import it.liceoarzignano.bold.events.EventsHandler;
import it.liceoarzignano.bold.marks.Mark2;
import it.liceoarzignano.bold.marks.MarksHandler;
import it.liceoarzignano.bold.news.News2;
import it.liceoarzignano.bold.news.NewsHandler;
import it.liceoarzignano.bold.utils.DateUtils;
import it.liceoarzignano.bold.utils.PrefsUtils;

public class EditorActivity extends AppCompatActivity {
    public static final String EXTRA_ID = "extra_id";
    public static final String EXTRA_IS_MARK = "extra_is_mark";
    public static final String EXTRA_IS_NEWS = "extra_is_news";

    private CoordinatorLayout mCoordinator;
    private RelativeLayout mTitleLayout;
    private EditText mTitleText;
    private RelativeLayout mSubjectLayout;
    private TextView mSubjectView;
    private EditText mNotesText;
    private RelativeLayout mCategoryLayout;
    private Spinner mCategorySpinner;
    private RelativeLayout mValueLayout;
    private TextView mValueView;
    private TextView mDateView;

    private Date mDate;
    private DatePickerDialog.OnDateSetListener mDateSetListener;
    private MarksHandler mMarksHandler;
    private EventsHandler mEventsHandler;

    private long mId;
    private boolean mIsEdit;
    private boolean mIsMark;
    private int mValue;
    private double mDialogVal;

    @Override
    protected void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);

        mId = getIntent().getLongExtra(EXTRA_ID, -1);
        mIsEdit = mId != -1;
        mIsMark = getIntent().getBooleanExtra(EXTRA_IS_MARK, true);

        setContentView(R.layout.activity_editor);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(getString(mIsMark ?
                mIsEdit ? R.string.editor_update_mark : R.string.editor_new_mark :
                mIsEdit ? R.string.editor_update_event : R.string.editor_new_event));
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_clear);
        toolbar.setNavigationOnClickListener(v -> askQuit());

        mCoordinator = (CoordinatorLayout) findViewById(R.id.coordinator_layout);
        mTitleLayout = (RelativeLayout) findViewById(R.id.editor_title_layout);
        mTitleText = (EditText) findViewById(R.id.editor_title_text);
        mSubjectView = (TextView) findViewById(R.id.editor_subject_selector);
        mSubjectLayout = (RelativeLayout) findViewById(R.id.editor_subject_layout);
        mNotesText = (EditText) findViewById(R.id.editor_notes_text);
        mCategoryLayout = (RelativeLayout) findViewById(R.id.editor_category_layout);
        mCategorySpinner = (Spinner) findViewById(R.id.editor_category_spinner);
        mValueLayout = (RelativeLayout) findViewById(R.id.editor_value_layout);
        mValueView = (TextView) findViewById(R.id.editor_value_view);
        mDateView = (TextView) findViewById(R.id.editor_date_view);

        mMarksHandler = MarksHandler.getInstance(this);
        mEventsHandler = EventsHandler.getInstance(this);

        if (mIsEdit) {
            if (getIntent().getBooleanExtra(EXTRA_IS_NEWS, false)) {
                loadNews();
            } else {
                loadUi();
            }
        } else {
            mDate = DateUtils.getDate(0);
        }

        if (mIsMark) {
            initMarkUi();
        } else {
            initEventUi();
        }

        mDateSetListener = ((view, year, month, dayOfMonth) -> {
            mDate = DateUtils.intToDate(year, month, dayOfMonth);
            mDateView.setText(DateUtils.dateToString(mDate));
        });
        mDateView.setOnClickListener(v -> showDatePicker());
        mDateView.setText(DateUtils.dateToString(mDate));
    }

    @Override
    public void onBackPressed() {
        askQuit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_save) {
            save();
        }

        return super.onOptionsItemSelected(item);
    }


    private void initMarkUi() {
        if (PrefsUtils.isTeacher(this)) {
            mTitleLayout.setVisibility(View.VISIBLE);
            mTitleText.setHint(getString(R.string.editor_hint_student));
        } else {
            mSubjectLayout.setVisibility(View.VISIBLE);
        }
        mValueLayout.setVisibility(View.VISIBLE);
        mValueView.setText(String.format(Locale.ENGLISH, "%.2f", (double) mValue / 100));

        String[] items;
        switch (PrefsUtils.getAddress(this)) {
            case "1":
                items = getResources().getStringArray(R.array.subjects_lists_1);
                break;
            case "2":
                items = getResources().getStringArray(R.array.subjects_lists_2);
                break;
            case "3":
                items = getResources().getStringArray(R.array.subjects_lists_3);
                break;
            case "4":
                items = getResources().getStringArray(R.array.subjects_lists_4);
                break;
            case "5":
                items = getResources().getStringArray(R.array.subjects_lists_5);
                break;
            default:
                items = getResources().getStringArray(R.array.subjects_lists_0);
                break;
        }
        mSubjectView.setOnClickListener(v -> new MaterialDialog.Builder(this)
                .title(R.string.editor_hint_subject)
                .items((CharSequence[]) items)
                .itemsCallback((dialog, view, which, text) -> mSubjectView.setText(text))
                .show());


        mValueLayout.setOnClickListener(v -> new MaterialDialog.Builder(this)
                .title(R.string.editor_dialog_mark_title)
                .customView(getValuePickerView(), false)
                .positiveText(android.R.string.ok)
                .neutralText(android.R.string.cancel)
                .onPositive((dialog, which) -> {
                    mValue = (int) (mDialogVal * 25);
                    mValueView.setText(String.format(Locale.ENGLISH, "%.2f", mDialogVal / 4));
                })
                .show());
    }

    private void initEventUi() {
        mTitleLayout.setVisibility(View.VISIBLE);
        mTitleText.setHint(getString(R.string.editor_hint_event));
        mCategoryLayout.setVisibility(View.VISIBLE);

        String[] items = getResources().getStringArray(R.array.event_categories);
        mCategorySpinner.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, items));
        mCategorySpinner.setSelection(mValue);
    }

    private void loadUi() {
        if (mIsMark) {
            Mark2 mark = mMarksHandler.get(mId);
            if (mark == null) {
                mIsEdit = false;
                return;
            }
            if (PrefsUtils.isTeacher(this)) {
                mTitleText.setText(mark.getSubject());
            } else {
                mSubjectView.setText(mark.getSubject());
            }
            mNotesText.setText(mark.getDescription());
            mDate = new Date(mark.getDate());
            mValue = mark.getValue();
        } else {
            Event2 event = mEventsHandler.get(mId);
            if (event == null) {
                mIsEdit = false;
                return;
            }
            mTitleText.setText(event.getTitle());
            mNotesText.setText(event.getDescription());
            mValue = event.getCategory();
            mDate = new Date(event.getDate());
        }

        mDateView.setText(DateUtils.dateToString(mDate));
    }

    private void loadNews() {
        NewsHandler handler = NewsHandler.getInstance(this);
        News2 news = handler.get(mId);
        if (news == null) {
            return;
        }

        mIsMark = false;
        mIsEdit = false;
        mDate = new Date(news.getDate());
        mTitleText.setText(news.getTitle());
        mDateView.setText(DateUtils.dateToString(mDate));
        mNotesText.setText(String.format("%1$s\n%2$s", news.getDescription(), news.getUrl()));
        mValue = 6;
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(mDate);
        new DatePickerDialog(this, mDateSetListener, calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
        .show();
    }

    private void askQuit() {
        new MaterialDialog.Builder(this)
                .title(R.string.editor_cancel_title)
                .content(R.string.editor_cancel_message)
                .positiveText(android.R.string.yes)
                .negativeText(R.string.editor_cancel_discard)
                .onPositive((dialog, which) -> finish())
                .show();
    }

    private View getValuePickerView() {
        ViewGroup viewGroup = (ViewGroup) findViewById(R.id.dialog_root);
        View dialog = LayoutInflater.from(this).inflate(R.layout.dialog_seekbar, viewGroup);
        TextView preview = (TextView) dialog.findViewById(R.id.dialog_value);
        preview.setText(String.format(Locale.ENGLISH, "%.2f", (double) mValue / 100));

        mDialogVal = mValue / 25;
        SeekBar seekBar = (SeekBar) dialog.findViewById(R.id.dialog_seek_bar);
        seekBar.setProgress(mValue / 40);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                preview.setText(String.format(Locale.ENGLISH, "%.2f", (double) progress / 4));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mDialogVal = seekBar.getProgress();
                preview.setText(String.format(Locale.ENGLISH, "%.2f", mDialogVal / 4));
            }
        });

        return dialog;
    }

    private void save() {
        if (mIsMark) {
            if (mValue == 0 || (mTitleText.getText().toString().isEmpty() &&
                    mSubjectView.getText().toString().isEmpty())) {
                Snackbar.make(mCoordinator, getString(R.string.editor_error),
                        Snackbar.LENGTH_SHORT).show();
            } else {
                saveMark();
            }
        } else {
            if (mTitleText.getText().toString().isEmpty()) {
                Snackbar.make(mCoordinator, getString(R.string.editor_error),
                        Snackbar.LENGTH_SHORT).show();
            } else {
                saveEvent();
            }
        }

    }

    private void saveMark() {
        Mark2 mark = new Mark2();
        mark.setSubject((PrefsUtils.isTeacher(this) ?
                mTitleText.getText() : mSubjectView.getText()).toString());
        mark.setValue(mValue);
        mark.setDate(mDate.getTime());
        mark.setFirstQuarter(PrefsUtils.isFirstQuarter(this, mDate));
        mark.setDescription(mNotesText.getText().toString());

        if (mIsEdit) {
            mark.setId(mId);
            mMarksHandler.update(mark);
        } else {
            mMarksHandler.add(mark);
        }

        Snackbar.make(mCoordinator, getString(R.string.editor_saved), Snackbar.LENGTH_LONG).show();
        new Handler().postDelayed(this::finish, 800);
    }

    private void saveEvent() {
        Event2 event = new Event2();
        event.setTitle(mTitleText.getText().toString());
        event.setCategory(mCategorySpinner.getSelectedItemPosition());
        event.setDate(mDate.getTime());
        event.setDescription(mNotesText.getText().toString());

        if (mIsEdit) {
            event.setId(mId);
            mEventsHandler.update(event);
        } else {
            mEventsHandler.add(event);
        }

        Snackbar.make(mCoordinator, getString(R.string.editor_saved), Snackbar.LENGTH_LONG).show();
        new Handler().postDelayed(this::finish, 800);
    }
}
