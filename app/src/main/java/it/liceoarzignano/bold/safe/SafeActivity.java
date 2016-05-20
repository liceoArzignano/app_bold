package it.liceoarzignano.bold.safe;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import it.liceoarzignano.bold.R;
import it.liceoarzignano.bold.Utils;

public class SafeActivity extends AppCompatActivity {

    private static final String TAG = "Bold_safe";
    private static final String SAFE_PREFS = "SafePrefs";
    private static final String XPOSED_INSTALLER_PACAKGE = "de.robv.android.xposed.installer";
    private static final String accessKey = "access_pwd";
    private static final String userKey = "user_name";
    private static final String regPwdKey = "reg_pwd";
    private static final String pcPwdKey = "pd_pwd";
    private static final String internetPwdKey = "internet_pwd";
    private static final String keyStr
            = "eezrWvsJWmaic7/DD2dt5g==:xuf8gXH87fUzeGBIMmn+PveCgx4gXcl610GuYOMFSjo=";
    private static final String salt
            = "3oUjZk/hB6b9K/1Zf6pfgPy/wfBpSPffG8AXwjlqouWFECxbKjJH95tVgFD6ZYuG4odBNjYCh+PquKvKPuz/00KXzqAon2/frtw783/Nmmb1w7GgW0o73BoJtRP6p3g9AzDAwMkgGZXUpYHi7t9fYCihxhY3siVsay+Tzos0i0k=";
    private static SharedPreferences prefs;
    private static SharedPreferences.Editor editor;
    private static String AccessPassword;
    private Encryption encryption;
    private String crUserName;
    private String crReg;
    private String crPc;
    private String crInternet;

    private Context context;

    private Menu safeMenu;

    private LinearLayout mLoadingLayout;
    private LinearLayout mContentLayout;
    private TextView mLoadingText;
    private EditText mUserEdit;
    private EditText mRegEdit;
    private EditText mPcEdit;
    private EditText mInternetEdit;
    private ImageView mImage;
    private ImageButton mShow1;
    private ImageButton mShow2;
    private ImageButton mShow3;
    private ImageButton mShow4;
    private boolean show1Active = true;
    private boolean show2Active = true;
    private boolean show3Active = true;
    private boolean show4Active = true;
    private FloatingActionButton mFab;

    @SuppressLint("CommitPrefEdits")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_safe);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        context = this;

        prefs = getSharedPreferences(SAFE_PREFS, MODE_PRIVATE);
        editor = getSharedPreferences(SAFE_PREFS, MODE_PRIVATE).edit();

        mLoadingLayout = (LinearLayout) findViewById(R.id.safe_loading);
        mContentLayout = (LinearLayout) findViewById(R.id.safe_content);
        mLoadingText = (TextView) findViewById(R.id.safe_loading_text);
        mUserEdit = (EditText) findViewById(R.id.userNamePwd);
        mRegEdit = (EditText) findViewById(R.id.regPwd);
        mPcEdit = (EditText) findViewById(R.id.pcPwd);
        mInternetEdit = (EditText) findViewById(R.id.internetPwd);
        mImage = (ImageView) findViewById(R.id.safe_image);
        mShow1 = (ImageButton) findViewById(R.id.showHide1);
        mShow2 = (ImageButton) findViewById(R.id.showHide2);
        mShow3 = (ImageButton) findViewById(R.id.showHide3);
        mShow4 = (ImageButton) findViewById(R.id.showHide4);
        mFab = (FloatingActionButton) findViewById(R.id.fab);

        mLoadingLayout.setVisibility(View.VISIBLE);

        if (tellMeTheresNoXposed()) {
            setupEncryption();
            showPasswordDialog();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        safeMenu = menu;
        getMenuInflater().inflate(R.menu.safe, menu);
        safeMenu.findItem(R.id.action_info).setVisible(false);
        safeMenu.findItem(R.id.action_reset).setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_reset:
                safeReset();
                break;
            case R.id.action_info:
                new MaterialDialog.Builder(context)
                        .title(getString(R.string.safe_info_title))
                        .content(getString(R.string.safe_info_content))
                        .neutralText(getString(android.R.string.ok))
                        .show();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Xposed can inject code by hacking the runtime, if xposed is installed
     * do not allow user to open this activity for security reasons
     *
     * @return false if Xposed is installed, true if the device is safe
     */
    private boolean tellMeTheresNoXposed() {
        try {
            PackageInfo pi = getApplicationContext().getPackageManager().getPackageInfo(XPOSED_INSTALLER_PACAKGE, 0);
            if (pi.applicationInfo.enabled) {
                mLoadingText.setText(getString(R.string.safe_security_issue_xposed));
                Log.e(TAG, "Shit! There\'s xposed in this device");
                return false;
            } else {
                return true;
            }
        } catch (PackageManager.NameNotFoundException e) {
            // Gotta catch 'em all
            return true;
        }
    }

    /**
     * Ask password as soon as the activity starts.
     * It's used both for first time setup and
     * normal access
     */
    private void showPasswordDialog() {
        final boolean doneSetup = prefs.getBoolean("doneSetup", false);
        String title;
        String msg;

        if (!doneSetup) {
            title = getString(R.string.safe_dialog_first_title);
            msg = getString(R.string.safe_dialog_first_content);
        } else {
            title = getString(R.string.safe_dialog_title);
            msg = getString(R.string.safe_dialog_content);
        }

        new MaterialDialog.Builder(context)
                .title(title)
                .content(msg)
                .canceledOnTouchOutside(false)
                .inputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)
                .input(getString(R.string.safe_dialog_password_input_hint),
                        "", new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                                AccessPassword = input.toString();
                                mLoadingText.setVisibility(View.VISIBLE);
                                if (!AccessPassword.isEmpty()) {
                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (!doneSetup) {
                                                mLoadingText.setText(getString(R.string.safe_first_load));
                                                String encrypted = encrypt(AccessPassword);
                                                editor.putString(accessKey, encrypted).apply();
                                                editor.putBoolean("doneSetup", true).apply();
                                                onCreateContinue();
                                            } else {
                                                validateLogin();
                                            }
                                        }
                                    }, 200);
                                } else {
                                    mLoadingText.setText(getString(R.string.safe_nomatch));
                                }
                            }
                        })
                .positiveText(android.R.string.ok)
                .show();
    }

    /**
     * Fire encryption stuffs
     */
    private void setupEncryption() {
        encryption = Encryption.getDefault(keyStr, salt, new byte[16]);
    }

    /**
     * Encrypt a given string
     *
     * @param s: string to be encrypted
     * @return encrypted string
     */
    private String encrypt(String s) {
        return encryption.encryptOrNull(s);
    }

    /**
     * Decrypt a string
     *
     * @param s: string to be decrypted
     * @return decrypted string
     */
    private String decrypt(String s) {
        return encryption.decryptOrNull(s);
    }

    /**
     
     * Check if password is right and update UI
     */
    private void validateLogin() {
        final String decrypted = decrypt(prefs.getString(accessKey, "ERROR"));
        mLoadingText.setText(getString(R.string.safe_decrypting));
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (AccessPassword.equals(decrypted)) {
                    onCreateContinue();
                } else {
                    mLoadingText.setText(getString(R.string.safe_nomatch));
                }
            }
        }, 1000);
    }

    /**
     * Password is fine, let's decrypt other
     * data and setup up the UI to be show and edit stored
     * data
     */
    private void onCreateContinue() {
        crUserName = decrypt(prefs.getString(userKey, ""));
        crReg = decrypt(prefs.getString(regPwdKey, ""));
        crPc = decrypt(prefs.getString(pcPwdKey, ""));
        crInternet = decrypt(prefs.getString(internetPwdKey, ""));

        safeMenu.findItem(R.id.action_reset).setVisible(true);
        safeMenu.findItem(R.id.action_info).setVisible(true);
        mLoadingLayout.setVisibility(View.GONE);
        mContentLayout.setVisibility(View.VISIBLE);
        mUserEdit.setText(crUserName);
        mRegEdit.setText(crReg);
        mPcEdit.setText(crPc);
        mInternetEdit.setText(crInternet);
        mShow1.setBackgroundResource(R.drawable.ic_show);
        mShow2.setBackgroundResource(R.drawable.ic_show);
        mShow3.setBackgroundResource(R.drawable.ic_show);
        mShow4.setBackgroundResource(R.drawable.ic_show);

        mShow1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mShow1.setBackgroundResource(show1Active ? R.drawable.ic_hide : R.drawable.ic_show);
                mUserEdit.setTransformationMethod(show1Active ? null : new PasswordTransformationMethod());
                show1Active = !show1Active;
            }
        });
        mShow2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mShow2.setBackgroundResource(show2Active ? R.drawable.ic_hide : R.drawable.ic_show);
                mRegEdit.setTransformationMethod(show2Active ? null : new PasswordTransformationMethod());
                show2Active = !show2Active;
            }
        });
        mShow3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mShow3.setBackgroundResource(show3Active ? R.drawable.ic_hide : R.drawable.ic_show);
                mPcEdit.setTransformationMethod(show3Active ? null : new PasswordTransformationMethod());
                show3Active = !show3Active;
            }
        });
        mShow4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mShow4.setBackgroundResource(show4Active ? R.drawable.ic_hide : R.drawable.ic_show);
                mInternetEdit.setTransformationMethod(show4Active ? null : new PasswordTransformationMethod());
                show4Active = !show4Active;
            }
        });

        Utils.animFabIntro(this, mFab,
                getString(R.string.intro_fab_save_safe), getString(R.string.intro_gotit), "safeKey");

        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                safeMenu.findItem(R.id.action_reset).setVisible(false);
                safeMenu.findItem(R.id.action_info).setVisible(false);
                mFab.hide();
                mContentLayout.setVisibility(View.GONE);
                mLoadingText.setText(R.string.safe_encrypting);
                mLoadingLayout.setVisibility(View.VISIBLE);
                mImage.setBackgroundResource(R.drawable.safe_encrypt);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        crUserName = encrypt(mUserEdit.getText().toString());
                        crReg = encrypt(mRegEdit.getText().toString());
                        crPc = encrypt(mPcEdit.getText().toString());
                        crInternet = encrypt(mInternetEdit.getText().toString());

                        editor.putString(userKey, crUserName).apply();
                        editor.putString(regPwdKey, crReg).apply();
                        editor.putString(pcPwdKey, crPc).apply();
                        editor.putString(internetPwdKey, crInternet).apply();
                        finish();
                    }
                }, 800);
            }
        });
    }

    /**
     * Reset the safe data when user wants to change the password
     */
    private void safeReset() {
        new MaterialDialog.Builder(context)
                .title(getString(R.string.safe_reset_title))
                .content(getString(R.string.safe_reset_content))
                .negativeText(getString(android.R.string.no))
                .positiveText(getString(android.R.string.yes))
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        editor.remove(accessKey).apply();
                        editor.remove(userKey).apply();
                        editor.remove(regPwdKey).apply();
                        editor.remove(pcPwdKey).apply();
                        editor.remove(internetPwdKey).apply();
                        editor.remove("doneSetup").apply();

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Intent i = new Intent(context, SafeActivity.class);
                                startActivity(i);
                            }
                        }, 700);
                    }
                })
                .show();
    }
}
