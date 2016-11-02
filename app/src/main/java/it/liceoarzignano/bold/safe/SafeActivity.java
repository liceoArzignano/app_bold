package it.liceoarzignano.bold.safe;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.ScrollView;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;

import it.liceoarzignano.bold.BuildConfig;
import it.liceoarzignano.bold.R;
import it.liceoarzignano.bold.Utils;

public class SafeActivity extends AppCompatActivity {

    private static final String SAFE_PREFS = "SafePrefs";
    private static final String XPOSED_INSTALLER_PACAKGE = "de.robv.android.xposed.installer";
    private static final String accessKey = "access_pwd";
    private static final String userKey = "user_name";
    private static final String regPwdKey = "reg_pwd";
    private static final String pcPwdKey = "pd_pwd";
    private static final String internetPwdKey = "internet_pwd";
    private static final String hasSharedKey = "has_shared";

    private static SharedPreferences sPrefs;
    private static SharedPreferences.Editor sEditor;
    private static String sAccessPassword;
    private Encryption.SecretKeys mSecretKeys = null;
    private String mCrUserName;
    private String mCrReg;
    private String mCrPc;
    private String mCrInternet;
    private boolean hasCompletedSetup;
    private boolean isWorking = true;

    private Context mContext;

    private Menu mMenu;

    private LinearLayout mLoadingLayout;
    private ScrollView mContentLayout;
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

        if (!BuildConfig.DEBUG) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                    WindowManager.LayoutParams.FLAG_SECURE);
        }

        setContentView(R.layout.activity_safe);
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mContext = this;

        sPrefs = getSharedPreferences(SAFE_PREFS, MODE_PRIVATE);
        sEditor = getSharedPreferences(SAFE_PREFS, MODE_PRIVATE).edit();

        mLoadingLayout = (LinearLayout) findViewById(R.id.safe_loading_layout);
        mContentLayout = (ScrollView) findViewById(R.id.safe_layout_content);
        mLoadingText = (TextView) findViewById(R.id.safe_loading_text);
        mUserEdit = (EditText) findViewById(R.id.safe_username);
        mRegEdit = (EditText) findViewById(R.id.safe_register);
        mPcEdit = (EditText) findViewById(R.id.safe_pc);
        mInternetEdit = (EditText) findViewById(R.id.safe_internet);
        mImage = (ImageView) findViewById(R.id.safe_loading_image);
        mFab = (FloatingActionButton) findViewById(R.id.fab);

        mLoadingLayout.setVisibility(View.VISIBLE);
        isWorking = true;

        // Xposed can inject code by doing shitty stuffs in the runtime.
        // If xposed is installed, do not allow user to open this activity for security reasons
        if (Utils.hasPackage(mContext, XPOSED_INSTALLER_PACAKGE)) {
            mLoadingText.setText(getString(R.string.safe_security_issue_xposed));
        } else {
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
            mCrUserName = null;
            mCrInternet = null;
            mCrPc = null;
            mCrReg = null;
            mUserEdit.setText("");
            mInternetEdit.setText("");
            mPcEdit.setText("");
            mRegEdit.setText("");
            mFab.setVisibility(View.GONE);
            mContentLayout.setVisibility(View.GONE);
            mLoadingLayout.setVisibility(View.VISIBLE);
            mMenu.findItem(R.id.action_reset).setVisible(false);
            mMenu.findItem(R.id.action_info).setVisible(false);
        }

        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu mMenu) {
        this.mMenu = mMenu;
        getMenuInflater().inflate(R.menu.safe, mMenu);
        this.mMenu.findItem(R.id.action_info).setVisible(false);
        this.mMenu.findItem(R.id.action_reset).setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem mItem) {
        int mId = mItem.getItemId();
        switch (mId) {
            case R.id.action_reset:
                safeReset();
                break;
            case R.id.action_info:
                new MaterialDialog.Builder(mContext)
                        .title(getString(R.string.safe_info_title))
                        .content(getString(R.string.safe_info_content))
                        .neutralText(getString(android.R.string.ok))
                        .show();
                break;
        }

        return super.onOptionsItemSelected(mItem);
    }

    @Override
    public void onBackPressed() {
        if (isWorking) {
            super.onBackPressed();
        } else {
            new MaterialDialog.Builder(mContext)
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
     * Ask password as soon as the activity starts.
     * It's used both for first time setup and
     * normal access
     */
    private void showPasswordDialog() {
        hasCompletedSetup = sPrefs.getBoolean("hasCompletedSetup", false);
        String mTitle;
        String mMessage;

        if (hasCompletedSetup) {
            mTitle = getString(R.string.safe_dialog_title);
            mMessage = getString(R.string.safe_dialog_content);
        } else {
            mTitle = getString(R.string.safe_dialog_first_title);
            mMessage = getString(R.string.safe_dialog_first_content);
        }

        new MaterialDialog.Builder(mContext)
                .title(mTitle)
                .content(mMessage)
                .canceledOnTouchOutside(false)
                .inputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)
                .input(getString(R.string.safe_dialog_password_input_hint),
                        "", new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(@NonNull MaterialDialog dialog,
                                                CharSequence input) {
                                sAccessPassword = input.toString();
                                mLoadingText.setVisibility(View.VISIBLE);
                                if (!sAccessPassword.isEmpty() && sAccessPassword != null) {
                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            mLoadingText.setText(getString(hasCompletedSetup ?
                                                    R.string.safe_decrypting :
                                                    R.string.safe_first_load));
                                            if (hasCompletedSetup) {
                                                validateLogin();
                                            } else {
                                                String encrypted = encrypt(sAccessPassword);
                                                sEditor.putString(accessKey, encrypted).apply();
                                                sEditor.putBoolean("hasCompletedSetup",
                                                        true).apply();
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
        try {
            mSecretKeys = Encryption.generateKeyFromPassword(getKey(), getSalt().getBytes());
        } catch (GeneralSecurityException e) {
            Log.e("Safe", e.getMessage(), e);
        }
    }

    /**
     * Encrypt a given string
     *
     * @param mString: string to be encrypted
     * @return encrypted string
     */
    private String encrypt(String mString) {
        try {
            return Encryption.encrypt(mString, mSecretKeys).toString();
        } catch (UnsupportedEncodingException | GeneralSecurityException e) {
            Log.e("Safe", e.getMessage(), e);
            return "";
        }
    }

    /**
     * Decrypt a string
     *
     * @param mString: string to be decrypted
     * @return decrypted string
     */
    private String decrypt(String mString) {
        try {
            return Encryption.decryptString(
                    new Encryption.CipherTextIvMac(mString), mSecretKeys);
        } catch (UnsupportedEncodingException | GeneralSecurityException e) {
            Log.e("Safe", e.getMessage(), e);
            return "";
        }
    }

    /**
     * Check if password is right and update UI
     */
    private void validateLogin() {
        final String mDecrypted = decrypt(sPrefs.getString(accessKey, "ERROR"));
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (sAccessPassword.equals(mDecrypted)) {
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
        String mObj = sPrefs.getString(userKey, null);
        if (mObj != null) {
            mCrUserName = decrypt(mObj);
        }
        mObj = sPrefs.getString(regPwdKey, null);
        if (mObj != null) {
            mCrReg = decrypt(mObj);
        }
        mObj = sPrefs.getString(pcPwdKey, null);
        if (mObj != null) {
            mCrPc = decrypt(mObj);
        }
        mObj = sPrefs.getString(internetPwdKey, null);
        if (mObj != null) {
            mCrInternet = decrypt(mObj);
        }

        mMenu.findItem(R.id.action_reset).setVisible(true);
        mMenu.findItem(R.id.action_info).setVisible(true);
        mLoadingLayout.setVisibility(View.GONE);
        mContentLayout.setVisibility(View.VISIBLE);
        mUserEdit.setText(mCrUserName);
        mRegEdit.setText(mCrReg);
        mPcEdit.setText(mCrPc);
        mInternetEdit.setText(mCrInternet);

        Utils.animFabIntro(this, mFab, getString(R.string.intro_fab_save_safe_title),
                getString(R.string.intro_fab_save_safe), "safeKey");

        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMenu.findItem(R.id.action_reset).setVisible(false);
                mMenu.findItem(R.id.action_info).setVisible(false);
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
                            sEditor.putString(userKey, encrypt(text)).apply();
                        }
                        text = mRegEdit.getText().toString();
                        if (!text.isEmpty()) {
                            sEditor.putString(regPwdKey, encrypt(text)).apply();
                        }
                        text = mPcEdit.getText().toString();
                        if (!text.isEmpty()) {
                            sEditor.putString(pcPwdKey, encrypt(text)).apply();
                        }
                        text = mInternetEdit.getText().toString();
                        if (!text.isEmpty()) {
                            sEditor.putString(internetPwdKey, encrypt(text)).apply();
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
        new MaterialDialog.Builder(mContext)
                .title(getString(R.string.safe_reset_title))
                .content(getString(R.string.safe_reset_content))
                .negativeText(getString(android.R.string.no))
                .positiveText(getString(android.R.string.yes))
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog,
                                        @NonNull DialogAction which) {
                        sEditor.remove(accessKey).apply();
                        sEditor.remove(userKey).apply();
                        sEditor.remove(regPwdKey).apply();
                        sEditor.remove(pcPwdKey).apply();
                        sEditor.remove(internetPwdKey).apply();
                        sEditor.remove(hasSharedKey).apply();
                        sEditor.remove("hasCompletedSetup").apply();

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Intent i = new Intent(mContext, SafeActivity.class);
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
     * @param mContext used to access sharedPreferences
     * @return encrypted password from sharedPreferences
     */
    public static String getEncryptedPassword(Context mContext) {
        return mContext.getSharedPreferences(SAFE_PREFS, MODE_PRIVATE).getString(accessKey, "");
    }

    /**
     * Public getter for hasShared password, used to prevent
     * encrypted password to be shared too many times from secret menu
     *
     * @param mContext used to access to sharedPreferences
     * @return true if user has already shared the password
     */
    public static boolean hasSharedPassword(Context mContext) {
        return mContext.getSharedPreferences(SAFE_PREFS, MODE_PRIVATE)
                .getBoolean(hasSharedKey, false);
    }

    /**
     * Public setter for hasShared password, used to prevent
     * encrypted password to be shared too many times from secret menu
     *
     * @param mContext used to access SharedPreferences
     */
    public static void setSharedPassword(Context mContext) {
        mContext.getSharedPreferences(SAFE_PREFS, MODE_PRIVATE).edit()
                .putBoolean(hasSharedKey, true).apply();
    }
}
