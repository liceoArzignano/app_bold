package it.liceoarzignano.bold.feedback;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.IssueService;

import java.io.IOException;

import it.liceoarzignano.bold.R;
import it.liceoarzignano.bold.safe.mod.Encryption;

public class FeedbackActivity extends AppCompatActivity {

    private static final String TAG = FeedbackActivity.class.getSimpleName();
    private static final String USERNAME = "liceoArzignano";
    private static final String REPO = "bold_feedback";

    private CoordinatorLayout mCoordinator;
    private EditText mTitleText;
    private EditText mDescriptionText;
    private EditText mEmailText;

    @Override
    protected void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);

        setContentView(R.layout.activity_feedback);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_toolbar_back);
        toolbar.setNavigationOnClickListener(v -> finish());

        mCoordinator = (CoordinatorLayout) findViewById(R.id.coordinator_layout);
        mTitleText = (EditText) findViewById(R.id.feedback_title);
        mDescriptionText = (EditText) findViewById(R.id.feedback_description);
        mEmailText = (EditText) findViewById(R.id.feedback_email);
        mEmailText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                mEmailText.setError(isValidEmail(s.toString()) ? null :
                        getString(R.string.feedback_error_email));
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(v -> sendFeedback());
        TextView info = (TextView) findViewById(R.id.feedback_info);
        info.setMovementMethod(new LinkMovementMethod());
    }

    private void sendFeedback() {
        String title = mTitleText.getText().toString();
        String description = mDescriptionText.getText().toString();
        String email = mEmailText.getText().toString();

        if (title.isEmpty() || description.isEmpty() || !isValidEmail(email)) {
            Snackbar.make(mCoordinator, getString(R.string.feedback_error_check),
                    Snackbar.LENGTH_LONG).show();
            return;
        }

        final String message = description + FeedbackInfo.getInfo(this);

        MaterialDialog progressDialog = new MaterialDialog.Builder(this)
                .title(R.string.feedback_progress_title)
                .content(R.string.feedback_progress_message)
                .progress(true, 100)
                .progressIndeterminateStyle(true)
                .canceledOnTouchOutside(false)
                .build();

        Context context = this;
        AsyncTask<Void, Void, String> task = new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                GitHubClient client = new GitHubClient().setOAuth2Token(Encryption.getOauth());
                Issue issue = new Issue().setTitle(title).setBody(message);
                try {
                    return new IssueService(client).createIssue(USERNAME, REPO, issue).getHtmlUrl();
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage());
                }

                return null;
            }

            @Override
            protected void onPostExecute(String result) {
                progressDialog.dismiss();

                MaterialDialog.Builder builder = new MaterialDialog.Builder(context)
                        .title(R.string.feedback_progress_title)
                        .content(result == null ? R.string.feedback_progress_failure :
                                R.string.feedback_progress_success)
                        .positiveText(android.R.string.ok);

                if (result != null) {
                    builder.neutralText(R.string.feedback_progress_view)
                            .onNeutral((dialog, which) -> context.startActivity(
                                    new Intent(Intent.ACTION_VIEW).setData(Uri.parse(result))))
                            .dismissListener(dialog -> {
                                mTitleText.setText("");
                                mDescriptionText.setText("");
                                mEmailText.setText("");
                            });
                }

                builder.show();
            }
        };

        progressDialog.show();
        task.execute();
    }

    private boolean isValidEmail(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
}
