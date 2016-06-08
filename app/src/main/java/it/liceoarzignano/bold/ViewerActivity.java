package it.liceoarzignano.bold;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.transition.Transition;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import it.liceoarzignano.bold.events.Event;
import it.liceoarzignano.bold.events.EventListActivity;
import it.liceoarzignano.bold.marks.Mark;
import it.liceoarzignano.bold.marks.MarkListActivity;
import it.liceoarzignano.bold.settings.AnalyticsTracker;

public class ViewerActivity extends AppCompatActivity {
    private static Context fContext;
    private String title = "Viewer";
    private String note;
    private int value;
    private int id;
    private boolean isMark;
    private Button mShare;
    private FloatingActionButton fab;
    private Button mDelete;
    private Button mView;

    private Mark mark;
    private Event event;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewer);

        fContext = this;

        Intent i = getIntent();
        id = i.getIntExtra("id", -1);
        isMark = i.getBooleanExtra("isMark", true);
        if (isMark) {
            mark = new it.liceoarzignano.bold.marks.DatabaseConnection(this).getMark(id);
        } else {
            event = new it.liceoarzignano.bold.events.DatabaseConnection(this).getEvent(id);
        }

        Log.d("OHAI", id + ", " + isMark + ", " + id);

        title = isMark ? mark.getTitle() : event.getTitle();
        note = isMark ? mark.getContent() : event.getValue();
        value = isMark ? mark.getValue() : event.getIcon();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        final ImageView toolbarImage = (ImageView) findViewById(R.id.toolbar_image);

        toolbarImage.setImageResource(isMark ? R.drawable.newmark : R.drawable.newevent);

        if (!title.isEmpty() && toolbar != null) {
            toolbar.setTitle(title);
        }
        setSupportActionBar(toolbar);

        mShare = (Button) findViewById(R.id.share);
        mDelete = (Button) findViewById(R.id.delete);
        mView = (Button) findViewById(R.id.more);
        TextView mValue = (TextView) findViewById(R.id.value);
        TextView mValueTitle = (TextView) findViewById(R.id.value_title);
        TextView mNotes = (TextView) findViewById(R.id.notes);

        if (isMark) {
            mView.setText(String.format(getResources().getString(R.string.markview_more), title));
        } else {
            assert mValueTitle != null;
            mValueTitle.setVisibility(View.GONE);
            assert mValue != null;
            mValue.setVisibility(View.GONE);
            mView.setVisibility(View.GONE);
        }

        final double markVal = (double) value / 100;
        assert mValue != null;
        final String sVal = markVal + "";
        mValue.setText(sVal);

        if (!note.isEmpty() && mNotes != null) {
            mNotes.setText(note);
        }

        mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AnalyticsTracker.trackEvent("MarkViewer: More", getApplicationContext());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    ((AnimatedVectorDrawable) mView.getCompoundDrawables()[1]).start();

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            MarkListActivity.showFilteredMarks(title);
                            finish();
                        }
                    }, 1800);
                } else {
                    MarkListActivity.showFilteredMarks(title);
                    finish();
                }
            }
        });

        mShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AnalyticsTracker.trackEvent(isMark ? "MarkViewer: Share" : "EventViewer: Share",
                        getApplicationContext());
                Resources res = getResources();

                String msg = isMark ?
                        !Utils.isTeacher(fContext) ?
                                String.format(res.getString(R.string.markview_share_student),
                                        title, sVal) :
                                String.format(res.getString(R.string.markview_share_teacher),
                                        title, sVal)
                        : String.format(res.getString(R.string.eventview_share), title, sVal);


                final Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT, msg);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    ((AnimatedVectorDrawable) mShare.getCompoundDrawables()[1]).start();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            startActivity(Intent.createChooser(shareIntent, getString(R.string.markview_share)));
                        }
                    }, 1000);
                } else {
                    startActivity(Intent.createChooser(shareIntent, getString(R.string.markview_share)));
                }
            }
        });

        mDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AnalyticsTracker.trackEvent(isMark ? "MarkViewer: Delete" : "EventViewer: Delete",
                        getApplicationContext());
                it.liceoarzignano.bold.marks.DatabaseConnection databaseConnectionMark =
                        it.liceoarzignano.bold.marks.DatabaseConnection.getInstance(fContext);
                it.liceoarzignano.bold.events.DatabaseConnection databaseConnectionEvent =
                        it.liceoarzignano.bold.events.DatabaseConnection.getInstance(fContext);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    ((AnimatedVectorDrawable) mDelete.getCompoundDrawables()[1]).start();
                }

                if (isMark) {
                    databaseConnectionMark.deleteMark(databaseConnectionMark.getMark(id));
                } else {
                    databaseConnectionEvent.deleteEvent(databaseConnectionEvent.getEvent(id));
                }

                Snackbar.make(v, getString(R.string.deleted), Snackbar.LENGTH_SHORT).show();
                if (isMark) {
                    MarkListActivity.refreshList(getApplicationContext());
                } else {
                    EventListActivity.refreshList(getApplicationContext());
                }
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        finish();
                    }
                }, 800);
            }
        });

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fab.hide();
                final Intent editIntent = new Intent(fContext, ManagerActivity.class);

                editIntent.putExtra("isEditing", true);
                editIntent.putExtra("isMark", isMark);
                editIntent.putExtra("id", id);
                editIntent.putExtra("title", title);
                editIntent.putExtra("val", value);
                editIntent.putExtra("note", note);

                if (Utils.hasApi21()) {
                    View sharedElement = findViewById(R.id.toolbar_image);

                    ActivityOptionsCompat options = ActivityOptionsCompat
                            .makeSceneTransitionAnimation(ViewerActivity.this,
                                    sharedElement, "imageShared");
                    ActivityCompat.startActivity(ViewerActivity.this,
                            editIntent, options.toBundle());
                } else {
                    startActivity(editIntent);
                }

                finish();
            }
        });

        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                fab.show();
            }
        }, 500);

    }

    @Override
    public void onBackPressed() {
        finish();
    }
}