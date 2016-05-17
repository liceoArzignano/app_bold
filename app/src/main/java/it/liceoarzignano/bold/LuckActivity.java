package it.liceoarzignano.bold;

import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class LuckActivity extends AppCompatActivity {

    private TextView mLeftTextView;
    private TextView mRightTextView;

    private Spinner mNumSpinner;
    private Spinner mMaxSpinner;
    private CardView mCard;

    private int numValues;
    private int max;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_luck);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mLeftTextView = (TextView) findViewById(R.id.luck_text_left);
        mRightTextView = (TextView) findViewById(R.id.luck_text_right);

        mMaxSpinner = (Spinner) findViewById(R.id.luck_max_spinner);
        mNumSpinner = (Spinner) findViewById(R.id.luck_num_spinner);

        mCard = (CardView) findViewById(R.id.luck_card);

        List<Integer> values = new ArrayList<>();

        for (int i = 1; i < 36; i++) {
            values.add(i);
        }

        ArrayAdapter<Integer> dataAdapter = new ArrayAdapter<>(
                this, R.layout.dropdown_item_number, values);

        mMaxSpinner.setAdapter(dataAdapter);
        mNumSpinner.setAdapter(dataAdapter);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        assert fab != null;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickRandom();
            }
        });

        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    /**
     * Description:
     * Select a brunch of random numbers and display them in 2 columns using 2 textViews
     */
    private void pickRandom() {
        max = mMaxSpinner.getSelectedItemPosition() + 1;
        numValues = mNumSpinner.getSelectedItemPosition() + 1;

        final Random random = new Random();


        final List<Integer> randoms = new ArrayList<>(numValues);

        for (int i = 0; i < numValues; i++) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    randoms.add(random.nextInt(max) + 1);
                }
            }, 5);
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                String msgLeft = "";
                String msgRight = "";

                for (int i = 0; i < numValues; i++) {
                    if (i % 2 != 0) {
                        msgRight = msgRight + randoms.get(i);
                        if (randoms.get(i) < 10) {
                            msgRight += " ";
                        }
                        msgRight += '\n';
                    } else {
                        if (randoms.get(i) < 10) {
                            msgLeft += " ";
                        }
                        msgLeft = msgLeft + randoms.get(i) + '\n';
                    }
                }

                if (randoms.size() == 1) {
                    mLeftTextView.setGravity(Gravity.CENTER_HORIZONTAL);
                    mRightTextView.setVisibility(View.GONE);
                } else {
                    mLeftTextView.setGravity(Gravity.NO_GRAVITY);
                    mRightTextView.setVisibility(View.VISIBLE);
                }

                mLeftTextView.setText(msgLeft);
                mRightTextView.setText(msgRight);

                mCard.setVisibility(View.VISIBLE);
            }
        }, (randoms.size() * 5) + 5);
    }
}
