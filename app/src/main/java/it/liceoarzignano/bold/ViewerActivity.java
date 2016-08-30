package it.liceoarzignano.bold;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import io.realm.Realm;
import io.realm.RealmResults;
import it.liceoarzignano.bold.events.Event;
import it.liceoarzignano.bold.events.EventListActivity;
import it.liceoarzignano.bold.marks.Mark;
import it.liceoarzignano.bold.marks.MarkListActivity;

public class ViewerActivity extends AppCompatActivity {
    private static Context fContext;
    private static Realm realm;
    private String title = "Viewer";
    private long id;
    private boolean isMark;
    private Button mShare;
    private FloatingActionButton fab;
    private Button mDelete;
    private Button mView;
    private Mark mark = new Mark();
    private Event event = new Event();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewer);

        fContext = this;
        realm = Realm.getInstance(BoldApp.getAppRealmConfiguration());

        Intent i = getIntent();
        id = i.getLongExtra("id", -1);
        isMark = i.getBooleanExtra("isMark", true);

        if (isMark) {
            mark = realm.where(Mark.class).equalTo("id", id).findFirst();
        } else {
            event = realm.where(Event.class).equalTo("id", id).findFirst();
        }

        title = isMark ? mark.getTitle() : event.getTitle();

        String date = isMark ? mark.getDate() : event.getDate();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        ImageView toolbarImage = (ImageView) findViewById(R.id.toolbar_image);

        if (toolbarImage != null) {
            toolbarImage.setImageResource(isMark ? R.drawable.newmark : R.drawable.newevent);
        }

        if (!title.isEmpty() && toolbar != null) {
            toolbar.setTitle(title);
        }
        setSupportActionBar(toolbar);

        mShare = (Button) findViewById(R.id.share);
        mDelete = (Button) findViewById(R.id.delete);
        mView = (Button) findViewById(R.id.more);
        TextView mValue = (TextView) findViewById(R.id.value);
        TextView mValueTitle = (TextView) findViewById(R.id.value_title);
        TextView mDate = (TextView) findViewById(R.id.dates);
        TextView mNotes = (TextView) findViewById(R.id.notes);
        TextView mNotesTitle = (TextView) findViewById(R.id.notes_title);

        mDate.setText(date);
        int value = isMark ? mark.getValue() : 0;
        final String sVal = isMark ? String.valueOf((double) value / 100) : "0";

        if (isMark) {
            String note = mark.getContent();
            mValueTitle.setText(getString(R.string.viewer_values));
            mView.setText(
                    String.format(getResources().getString(R.string.viewer_more_marks), title));
            mValue.setText(sVal);
            StringBuilder builder = new StringBuilder();
            builder.append(note);
            if (!note.isEmpty()) {
                builder.append('\n');
            }
            builder.append(getString(mark.getIsFirstQuarter() ?
                    R.string.viewer_first_quarter : R.string.viewer_second_quarter));
            mNotes.setText(builder.toString());
        } else {
            mValueTitle.setText(getString(R.string.viewer_category));
            mValue.setText(Utils.eventCategoryToString(event.getIcon()));
            mView.setVisibility(View.GONE);
            mNotesTitle.setVisibility(View.GONE);
        }


        mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Utils.hasApi21()) {
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
                Resources res = getResources();

                String msg = isMark ?
                        !Utils.isTeacher(fContext) ?
                                String.format(res.getString(R.string.viewer_share_student),
                                        sVal, title) :
                                String.format(res.getString(R.string.viewer_share_teacher),
                                        title, sVal)
                        : String.format(res.getString(R.string.viewer_share_event), title, sVal);


                final Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT, msg);

                if (Utils.hasApi21()) {
                    ((AnimatedVectorDrawable) mShare.getCompoundDrawables()[1]).start();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            startActivity(Intent.createChooser(shareIntent,
                                    getString(R.string.viewer_share)));
                        }
                    }, 1000);
                } else {
                    startActivity(Intent.createChooser(shareIntent,
                            getString(R.string.viewer_share)));
                }
            }
        });

        mDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Utils.hasApi21()) {
                    ((AnimatedVectorDrawable) mDelete.getCompoundDrawables()[1]).start();
                }

                if (isMark) {
                    RealmResults<Mark> results =
                            realm.where(Mark.class).equalTo("id", id).findAll();
                    realm.beginTransaction();
                    results.deleteAllFromRealm();
                    realm.commitTransaction();
                } else {
                    RealmResults<Event> results =
                            realm.where(Event.class).equalTo("id", id).findAll();
                    realm.beginTransaction();
                    results.deleteAllFromRealm();
                    realm.commitTransaction();
                }

                Snackbar.make(v, getString(R.string.removed), Snackbar.LENGTH_SHORT).show();
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
                Intent editIntent = new Intent(fContext, ManagerActivity.class);

                editIntent.putExtra("isEditing", true);
                editIntent.putExtra("isMark", isMark);
                editIntent.putExtra("id", id);

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

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
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