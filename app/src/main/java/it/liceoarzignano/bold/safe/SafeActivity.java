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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;

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
    private static final String hasSharedKey = "has_shared";
    private static SharedPreferences prefs;
    private static SharedPreferences.Editor editor;
    private static String accessPassword;
    private Encryption.SecretKeys secretKeys = null;
    private String primKey;
    private String salt;
    private String crUserName;
    private String crReg;
    private String crPc;
    private String crInternet;
    private boolean doneSetup;
    private boolean isWorking = true;

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
    private FloatingActionButton mFab;

    // Safe jni addons
    static {
        System.loadLibrary("safe-addon-jni");
    }
    public native String getKey();
    public native String getSalt();


    @SuppressLint("CommitPrefEdits")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_safe);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

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
        mFab = (FloatingActionButton) findViewById(R.id.fab);

        mLoadingLayout.setVisibility(View.VISIBLE);
        isWorking = true;

        primKey = getKey();
        salt = getSalt();

        if (tellMeTheresNoXposed()) {
            mLoadingText.setText(R.string.safe_first_load);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    setupEncryption();
                    showPasswordDialog();
                }
            }, 100);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // Reload everything since it everything was destroyed onPause();
        if (!isWorking) {
            showPasswordDialog();
        }
    }

    @Override
    public void onPause() {
        if (!isWorking) {
            // Remove all the private data from memory
            crUserName = null;
            crInternet = null;
            crPc = null;
            crReg = null;
            mUserEdit.setText("");
            mInternetEdit.setText("");
            mPcEdit.setText("");
            mRegEdit.setText("");
            mFab.setVisibility(View.GONE);
            mContentLayout.setVisibility(View.GONE);
            mLoadingLayout.setVisibility(View.VISIBLE);
            safeMenu.findItem(R.id.action_reset).setVisible(false);
            safeMenu.findItem(R.id.action_info).setVisible(false);
        }

        super.onPause();
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

    @Override
    public void onBackPressed() {
        if (isWorking) {
            super.onBackPressed();
        } else {
            new MaterialDialog.Builder(context)
                    .title(R.string.safe_back_title)
                    .content(R.string.safe_back_message)
                    .positiveText(android.R.string.ok)
                    .negativeText(android.R.string.cancel)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog,
                                            @NonNull DialogAction which) {
                            finish();
                        }
                    })
                    .show();
        }
    }

    /**
     * Xposed can inject code by hacking the runtime, if xposed is installed
     * do not allow user to open this activity for security reasons
     *
     * @return false if Xposed is installed, true if the device is safe
     */
    private boolean tellMeTheresNoXposed() {
        try {
            PackageInfo pi = getApplicationContext().getPackageManager()
                    .getPackageInfo(XPOSED_INSTALLER_PACAKGE, 0);
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
        doneSetup = prefs.getBoolean("doneSetup", false);
        String title;
        String msg;

        if (doneSetup) {
            title = getString(R.string.safe_dialog_title);
            msg = getString(R.string.safe_dialog_content);
        } else {
            title = getString(R.string.safe_dialog_first_title);
            msg = getString(R.string.safe_dialog_first_content);
        }

        new MaterialDialog.Builder(context)
                .title(title)
                .content(msg)
                .canceledOnTouchOutside(false)
                .inputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)
                .input(getString(R.string.safe_dialog_password_input_hint),
                        "", new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(@NonNull MaterialDialog dialog,
                                                CharSequence input) {
                                accessPassword = input.toString();
                                mLoadingText.setVisibility(View.VISIBLE);
                                if (!accessPassword.isEmpty() && accessPassword != null) {
                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            mLoadingText.setText(getString(doneSetup ?
                                                    R.string.safe_decrypting :
                                                    R.string.safe_first_load));
                                            if (doneSetup) {
                                                validateLogin();
                                            } else {
                                                String encrypted = encrypt(accessPassword);
                                                editor.putString(accessKey, encrypted).apply();
                                                editor.putBoolean("doneSetup", true).apply();
                                                onCreateContinue();
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
        //encryption = Encryption.getDefault(getKey(), getSalt(), new byte[16]);
        try {
            secretKeys = Encryption.generateKeyFromPassword(primKey, salt.getBytes());
        } catch (GeneralSecurityException e) {
            Log.e("Safe", e.getMessage(), e);
        }
    }

    /**
     * Encrypt a given string
     *
     * @param s: string to be encrypted
     * @return encrypted string
     */
    private String encrypt(String s) {
        //return encryption.encryptOrNull(s);
        try {
            return Encryption.encrypt(s, secretKeys).toString();
        } catch (UnsupportedEncodingException | GeneralSecurityException e) {
            Log.e("Safe", e.getMessage(), e);
            return "";
        }
    }

    /**
     * Decrypt a string
     *
     * @param s: string to be decrypted
     * @return decrypted string
     */
    private String decrypt(String s) {
        //return encryption.decryptOrNull(s);
        try {
            return Encryption.decryptString(
                    new Encryption.CipherTextIvMac(s), secretKeys);
        } catch (UnsupportedEncodingException | GeneralSecurityException e) {
            Log.e("Safe", e.getMessage(), e);
            return "";
        }
    }

    /**
     * Check if password is right and update UI
     */
    private void validateLogin() {
        final String decrypted = decrypt(prefs.getString(accessKey, "ERROR"));
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (accessPassword.equals(decrypted)) {
                    onCreateContinue();
                } else {
                    mLoadingText.setText(getString(R.string.safe_nomatch));
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            finish();
                        }
                    }, 3000);
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
        isWorking = false;

        // Always check if there's sth to decrypt, if not, pass
        // away to speed up this process
        String obj = prefs.getString(userKey, null);
        if (obj != null) {
            crUserName = decrypt(obj);
        }
        obj = prefs.getString(regPwdKey, null);
        if (obj != null) {
            crReg = decrypt(obj);
        }
        obj = prefs.getString(pcPwdKey, null);
        if (obj != null) {
            crPc = decrypt(obj);
        }
        obj = prefs.getString(internetPwdKey, null);
        if (obj != null) {
            crInternet = decrypt(obj);
        }

        safeMenu.findItem(R.id.action_reset).setVisible(true);
        safeMenu.findItem(R.id.action_info).setVisible(true);
        mLoadingLayout.setVisibility(View.GONE);
        mContentLayout.setVisibility(View.VISIBLE);
        mUserEdit.setText(crUserName);
        mRegEdit.setText(crReg);
        mPcEdit.setText(crPc);
        mInternetEdit.setText(crInternet);

        Utils.animFabIntro(this, mFab, getString(R.string.intro_fab_save_safe_title),
                getString(R.string.intro_fab_save_safe), "safeKey");

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
                        String text = mUserEdit.getText().toString();
                        if (!text.isEmpty()) {
                            editor.putString(userKey, encrypt(text)).apply();
                        }
                        text = mRegEdit.getText().toString();
                        if (!text.isEmpty()) {
                            editor.putString(regPwdKey, encrypt(text)).apply();
                        }
                        text = mPcEdit.getText().toString();
                        if (!text.isEmpty()) {
                            editor.putString(pcPwdKey, encrypt(text)).apply();
                        }
                        text = mInternetEdit.getText().toString();
                        if (!text.isEmpty()) {
                            editor.putString(internetPwdKey, encrypt(text)).apply();
                        }
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
                    public void onClick(@NonNull MaterialDialog dialog,
                                        @NonNull DialogAction which) {
                        editor.remove(accessKey).apply();
                        editor.remove(userKey).apply();
                        editor.remove(regPwdKey).apply();
                        editor.remove(pcPwdKey).apply();
                        editor.remove(internetPwdKey).apply();
                        editor.remove(hasSharedKey).apply();
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

    /**
     * Encrypted password getter
     *
     * @param context used to access sharedPreferences
     * @return encrypted password from sharedPreferences
     */
    public static String getEncryptedPassword(Context context) {
        return context.getSharedPreferences(SAFE_PREFS, MODE_PRIVATE).getString(accessKey, "");
    }

    /**
     * Public getter for hasShared password, used to prevent
     * encrypted password to be shared too many times from secret menu
     *
     * @param context used to access to sharedPreferences
     * @return true if user has already shared the password
     */
    public static boolean hasSharedPassword(Context context) {
        return context.getSharedPreferences(SAFE_PREFS, MODE_PRIVATE)
                .getBoolean(hasSharedKey, false);
    }

    /**
     * Public setter for hasShared password, used to prevent
     * encrypted password to be shared too many times from secret menu
     *
     * @param context used to access SharedPreferences
     */
    public static void setSharedPassword(Context context) {
        context.getSharedPreferences(SAFE_PREFS, MODE_PRIVATE).edit()
                .putBoolean(hasSharedKey, true).apply();
    }
}
