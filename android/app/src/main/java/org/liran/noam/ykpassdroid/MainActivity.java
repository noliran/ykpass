package org.liran.noam.ykpassdroid;

import android.app.Activity;
import android.app.assist.AssistStructure;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.service.autofill.Dataset;
import android.service.autofill.FillResponse;
import android.service.autofill.UserData;
import android.util.Base64;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.autofill.AutofillId;
import android.view.autofill.AutofillValue;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import com.github.fzakaria.ascii85.Ascii85;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.io.BaseEncoding;

import java.util.Formatter;
import java.util.stream.IntStream;

import static android.view.autofill.AutofillManager.EXTRA_ASSIST_STRUCTURE;
import static android.view.autofill.AutofillManager.EXTRA_AUTHENTICATION_RESULT;

public class MainActivity extends Activity {
    private static final int CHALLENGE_RESPONSE_REQUEST_CODE = 12345;

    private LinearLayout llStandalone;
    private LinearLayout llMethod;
    private EditText etSalt;
    private EditText etPasswordId;
    private EditText etResult;
    private Button btnGenerate;
    private Button btnClear;
    private ImageButton btnCopy;
    private RadioGroup rgMethod;
    private boolean autofill;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        llStandalone = findViewById(R.id.llStandalone);
        llMethod = findViewById(R.id.llMethod);
        etSalt = findViewById(R.id.etSalt);
        etPasswordId = findViewById(R.id.etPasswordId);
        etResult = findViewById(R.id.etResult);
        btnGenerate = findViewById(R.id.btnGenerate);
        btnCopy = findViewById(R.id.btnCopy);
        btnClear = findViewById(R.id.btnClear);
        rgMethod = findViewById(R.id.rgMethod);

        btnGenerate.setOnClickListener(v -> {
            final Intent intent = new Intent("net.pp3345.ykdroid.intent.action.CHALLENGE_RESPONSE");
            intent.putExtra("challenge", String.format("%s,%s", etSalt.getText(), etPasswordId.getText()).getBytes());

            startActivityForResult(intent, CHALLENGE_RESPONSE_REQUEST_CODE);
        });

        btnCopy.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("ykpassDroid", etResult.getText());
            clipboard.setPrimaryClip(clip);

            MainActivity.this.runOnUiThread(() -> Toast.makeText(MainActivity.this, "Copied", Toast.LENGTH_SHORT).show());
        });

        btnClear.setOnClickListener(v -> {
            etPasswordId.setText("");
            etResult.setText("");
        });

        etPasswordId.setOnEditorActionListener((v, actionId, event) -> {
            if ((event == null) && ((actionId == EditorInfo.IME_ACTION_DONE) || (actionId ==EditorInfo.IME_ACTION_NEXT))) {
                btnGenerate.performClick();
                return true;
            }
            return false;
        });

        autofill = getIntent().getBooleanExtra("autofill", false);
        llStandalone.setVisibility(autofill ? View.GONE : View.VISIBLE);
        llMethod.setVisibility(autofill ? View.GONE : View.VISIBLE);

        String passwordId = getIntent().getStringExtra("passwordId");
        etPasswordId.setText(passwordId);

        etSalt.requestFocus();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CHALLENGE_RESPONSE_REQUEST_CODE && resultCode == RESULT_OK) {
            final byte[] response = data.getByteArrayExtra("response");

            if (autofill) {
                handleAutofillResult(response);
            } else {
                String processedResponse = processResponse(response, rgMethod.getCheckedRadioButtonId());
                //noinspection RedundantStringConstructorCall
                etResult.setText(new String(processedResponse));
            }
        }
    }

    private void handleAutofillResult(byte[] response) {
        Intent intent = getIntent();

        AutofillId autofillPasswordId = intent.getParcelableExtra("autofillId");

        FillResponse.Builder fillResponseBuilder = new FillResponse.Builder();

        IntStream.range(0, rgMethod.getChildCount())
                .mapToObj(rgMethod::getChildAt)
                .forEach(v -> {
                    RadioButton rb = (RadioButton) v;

                    RemoteViews label = new RemoteViews(getPackageName(), android.R.layout.simple_list_item_1);
                    label.setTextViewText(android.R.id.text1, String.format("%s (%s)", rb.getText(), rb.getTooltipText()));

                    fillResponseBuilder.addDataset(
                            new Dataset.Builder()
                                    .setValue(autofillPasswordId, AutofillValue.forText(processResponse(response, v.getId())), label)
                                    .build());
                });

        Intent replyIntent = new Intent();

        // Send the data back to the service.
        replyIntent.putExtra(EXTRA_AUTHENTICATION_RESULT, fillResponseBuilder.build());

        setResult(RESULT_OK, replyIntent);
        finish();
    }

    private String processResponse(byte[] input, int method) {
        switch (method) {
            case R.id.rb1:
                return Ascii85.encode(input).substring(0, 16);
            case R.id.rb2:
                return BaseEncoding.base64().encode(input).substring(0, 16);
            case R.id.rb3:
                return BaseEncoding.base32().encode(input).substring(0, 12);
            case R.id.rb4:
                return BaseEncoding.base32().encode(input).substring(0, 8);
            case R.id.rb5:
                String s = BaseEncoding.base32().encode(input).substring(0, 8);
                return s.substring(0, 4) + s.substring(4, 8).toLowerCase();
            default:
                throw new RuntimeException(String.format("Unknown method %d", method));
        }
    }

    private static String toHexString(byte[] response) {
        Formatter formatter = new Formatter();

        for (byte b : response) {
            formatter.format("%02x", b);
        }

        return formatter.toString();
    }
}
