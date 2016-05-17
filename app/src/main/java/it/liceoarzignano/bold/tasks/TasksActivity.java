package it.liceoarzignano.bold.tasks;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import it.liceoarzignano.bold.R;
import it.liceoarzignano.bold.Utils;

public class TasksActivity extends AppCompatActivity {


    private static Context fContext;
    private Calendar calendar = Calendar.getInstance();

    private DatabaseConnection databaseConnection;

    private ArrayList<Integer> ids;

    private int selectedDay;

    private Button mButton1;
    private Button mButton2;
    private Button mButton3;
    private Button mButton4;
    private Button mButton5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tasks);

        fContext = this;
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mButton1 = (Button) findViewById(R.id.button1);
        mButton2 = (Button) findViewById(R.id.button2);
        mButton3 = (Button) findViewById(R.id.button3);
        mButton4 = (Button) findViewById(R.id.button4);
        mButton5 = (Button) findViewById(R.id.button5);

        databaseConnection = DatabaseConnection.getInstance(fContext);

        ids = new ArrayList<>();

        ids.add(0, 0);
        ids.add(1, 0);
        ids.add(2, 0);
        ids.add(3, 0);
        ids.add(4, 0);

        mButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateTask(1);
            }
        });

        mButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateTask(2);
            }
        });

        mButton3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateTask(3);
            }
        });

        mButton4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateTask(4);
            }
        });

        mButton5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateTask(5);
            }
        });

        reloadUI();

    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.tasks, menu);

        selectedDay = calendar.get(Calendar.DAY_OF_WEEK) - 2;
        menu.getItem(selectedDay).setChecked(true);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.day_1:
                selectedDay = 1;
                break;
            case R.id.day_2:
                selectedDay = 2;
                break;
            case R.id.day_3:
                selectedDay = 3;
                break;
            case R.id.day_4:
                selectedDay = 4;
                break;
            case R.id.day_5:
                selectedDay = 5;
                break;
            case R.id.day_6:
                selectedDay = 6;
                break;
        }

        item.setChecked(!item.isChecked());

        reloadUI();

        return super.onOptionsItemSelected(item);
    }


    private void updateTask(final int position) {
        new MaterialDialog.Builder(fContext)
                .title(getString(R.string.tasks_update_title_dialog))
                .content(getString(R.string.tasks_update_content_dialog))
                .input(getString(Utils.isTeacher(fContext) ?
                                R.string.tasks_update_hint_teacher :
                                R.string.tasks_update_hint_student),
                        "", new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                                if (input != null) {
                                    saveTask(input, position);
                                }
                            }
                        })
                .show();
    }

    private void saveTask(CharSequence input, int position) {

        DatabaseConnection databaseConnection =
                DatabaseConnection.getInstance(fContext);

        String msg;

        switch (position) {
            case 1:
                msg = mButton1.getText().toString();
                break;
            case 2:
                msg = mButton2.getText().toString();
                break;
            case 3:
                msg = mButton3.getText().toString();
                break;
            case 4:
                msg = mButton4.getText().toString();
                break;
            case 5:
                msg = mButton5.getText().toString();
                break;
            default:
                msg = "";
                break; // Should never be used
        }
        Task task;

        if (msg.equals(getString(R.string.tasks_nothing))) {
            task = new Task(input.toString(),
                    selectedDay, position);
            databaseConnection.addTask(task);
        } else {
            task = new Task(ids.get(position - 1), input.toString(),
                    selectedDay, position);
            databaseConnection.updateTask(task);
        }

        switch (position) {
            case 1:
                mButton1.setText(input.toString());
                break;
            case 2:
                mButton2.setText(input.toString());
                break;
            case 3:
                mButton3.setText(input.toString());
                break;
            case 4:
                mButton4.setText(input.toString());
                break;
            case 5:
                mButton5.setText(input.toString());
                break;
        }

        reloadUI();
    }

    private void reloadUI() {

        mButton1.setText(getString(R.string.tasks_nothing));
        mButton2.setText(getString(R.string.tasks_nothing));
        mButton3.setText(getString(R.string.tasks_nothing));
        mButton4.setText(getString(R.string.tasks_nothing));
        mButton5.setText(getString(R.string.tasks_nothing));

        List<Task> taskList = new DatabaseConnection(fContext).getFilteredTasks(selectedDay);

        for (Task taskInList : taskList) {
            switch (taskInList.getStage()) {
                case 1:
                    mButton1.setText(taskInList.getTitle());
                    ids.set(0, taskInList.getId());
                    break;
                case 2:
                    mButton2.setText(taskInList.getTitle());
                    ids.set(1, taskInList.getId());
                    break;
                case 3:
                    mButton3.setText(taskInList.getTitle());
                    ids.set(2, taskInList.getId());
                    break;
                case 4:
                    mButton4.setText(taskInList.getTitle());
                    ids.set(3, taskInList.getId());
                    break;
                case 5:
                    mButton5.setText(taskInList.getTitle());
                    ids.set(4, taskInList.getId());
                    break;
            }
        }
    }
}
