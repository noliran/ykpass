package org.liran.noam.ykpassdroid;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.service.autofill.Dataset;
import android.service.autofill.FillResponse;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.google.common.io.BaseEncoding;

import org.apache.commons.lang3.StringUtils;

import java.util.Formatter;
import java.util.stream.IntStream;
import java.util.zip.CRC32;

import static android.view.autofill.AutofillManager.EXTRA_AUTHENTICATION_RESULT;

public class MainActivity extends Activity {
    private static final String[] FONT_AWESOME_ICONS = StringUtils.split("\uf000,\uf001,\uf002,\uf004,\uf005,\uf007,\uf008,\uf009,\uf00a,\uf00b,\uf00c,\uf00d,\uf00e,\uf010,\uf011,\uf012,\uf013,\uf015,\uf017,\uf018,\uf019,\uf01c,\uf01e,\uf021,\uf022,\uf023,\uf024,\uf025,\uf026,\uf027,\uf028,\uf029,\uf02a,\uf02b,\uf02c,\uf02d,\uf02e,\uf02f,\uf030,\uf031,\uf032,\uf033,\uf034,\uf035,\uf036,\uf037,\uf038,\uf039,\uf03a,\uf03b,\uf03c,\uf03d,\uf03e,\uf041,\uf042,\uf043,\uf044,\uf048,\uf049,\uf04a,\uf04b,\uf04c,\uf04d,\uf04e,\uf050,\uf051,\uf052,\uf053,\uf054,\uf055,\uf056,\uf057,\uf058,\uf059,\uf05a,\uf05b,\uf05e,\uf060,\uf061,\uf062,\uf063,\uf064,\uf065,\uf066,\uf067,\uf068,\uf069,\uf06a,\uf06b,\uf06c,\uf06d,\uf06e,\uf070,\uf071,\uf072,\uf073,\uf074,\uf075,\uf076,\uf077,\uf078,\uf079,\uf07a,\uf07b,\uf07c,\uf080,\uf083,\uf084,\uf085,\uf086,\uf089,\uf08d,\uf091,\uf093,\uf094,\uf095,\uf098,\uf09c,\uf09d,\uf09e,\uf0a0,\uf0a1,\uf0a3,\uf0a4,\uf0a5,\uf0a6,\uf0a7,\uf0a8,\uf0a9,\uf0aa,\uf0ab,\uf0ac,\uf0ad,\uf0ae,\uf0b0,\uf0b1,\uf0b2,\uf0c0,\uf0c1,\uf0c2,\uf0c3,\uf0c4,\uf0c5,\uf0c6,\uf0c7,\uf0c8,\uf0c9,\uf0ca,\uf0cb,\uf0cc,\uf0cd,\uf0ce,\uf0d0,\uf0d1,\uf0d6,\uf0d7,\uf0d8,\uf0d9,\uf0da,\uf0db,\uf0dc,\uf0dd,\uf0de,\uf0e0,\uf0e2,\uf0e3,\uf0e7,\uf0e8,\uf0e9,\uf0ea,\uf0eb,\uf0f0,\uf0f1,\uf0f2,\uf0f3,\uf0f4,\uf0f8,\uf0f9,\uf0fa,\uf0fb,\uf0fc,\uf0fd,\uf0fe,\uf100,\uf101,\uf102,\uf103,\uf104,\uf105,\uf106,\uf107,\uf108,\uf109,\uf10a,\uf10b,\uf10d,\uf10e,\uf110,\uf111,\uf118,\uf119,\uf11a,\uf11b,\uf11c,\uf11e,\uf120,\uf121,\uf122,\uf124,\uf125,\uf126,\uf127,\uf128,\uf129,\uf12a,\uf12b,\uf12c,\uf12d,\uf12e,\uf130,\uf131,\uf133,\uf134,\uf135,\uf137,\uf138,\uf139,\uf13a,\uf13d,\uf13e,\uf140,\uf141,\uf142,\uf143,\uf144,\uf146,\uf14a,\uf14b,\uf14d,\uf14e,\uf150,\uf151,\uf152,\uf153,\uf154,\uf155,\uf156,\uf157,\uf158,\uf159,\uf15b,\uf15c,\uf15d,\uf15e,\uf160,\uf161,\uf162,\uf163,\uf164,\uf165,\uf182,\uf183,\uf185,\uf186,\uf187,\uf188,\uf191,\uf192,\uf193,\uf195,\uf197,\uf199,\uf19c,\uf19d,\uf1ab,\uf1ac,\uf1ad,\uf1ae,\uf1b0,\uf1b2,\uf1b3,\uf1b8,\uf1b9,\uf1ba,\uf1bb,\uf1c0,\uf1c1,\uf1c2,\uf1c3,\uf1c4,\uf1c5,\uf1c6,\uf1c7,\uf1c8,\uf1c9,\uf1cd,\uf1ce,\uf1d8,\uf1da,\uf1dc,\uf1dd,\uf1de,\uf1e0,\uf1e1,\uf1e2,\uf1e3,\uf1e4,\uf1e5,\uf1e6,\uf1ea,\uf1eb,\uf1ec,\uf1f6,\uf1f8,\uf1f9,\uf1fa,\uf1fb,\uf1fc,\uf1fd,\uf1fe,\uf200,\uf201,\uf204,\uf205,\uf206,\uf207,\uf20a,\uf20b,\uf217,\uf218,\uf21a,\uf21b,\uf21c,\uf21d,\uf21e,\uf221,\uf222,\uf223,\uf224,\uf225,\uf226,\uf227,\uf228,\uf229,\uf22a,\uf22b,\uf22c,\uf22d,\uf233,\uf234,\uf235,\uf236,\uf238,\uf239,\uf240,\uf241,\uf242,\uf243,\uf244,\uf245,\uf246,\uf247,\uf248,\uf249,\uf24d,\uf24e,\uf251,\uf252,\uf253,\uf254,\uf255,\uf256,\uf257,\uf258,\uf259,\uf25a,\uf25b,\uf25c,\uf25d,\uf26c,\uf271,\uf272,\uf273,\uf274,\uf275,\uf276,\uf277,\uf279,\uf27a,\uf28b,\uf28d,\uf290,\uf291,\uf292,\uf295,\uf29a,\uf29d,\uf29e,\uf2a0,\uf2a1,\uf2a2,\uf2a3,\uf2a4,\uf2a7,\uf2a8,\uf2b5,\uf2b6,\uf2b9,\uf2bb,\uf2bd,\uf2c1,\uf2c2,\uf2c7,\uf2c8,\uf2c9,\uf2ca,\uf2cb,\uf2cc,\uf2cd,\uf2ce,\uf2d0,\uf2d1,\uf2d2,\uf2db,\uf2dc,\uf2e5,\uf2e7,\uf2ea,\uf2ed,\uf2f1,\uf2f2,\uf2f5,\uf2f6,\uf2f9,\uf2fe,\uf302,\uf303,\uf304,\uf305,\uf309,\uf30a,\uf30b,\uf30c,\uf31e,\uf328,\uf337,\uf338,\uf358,\uf359,\uf35a,\uf35b,\uf35d,\uf360,\uf362,\uf381,\uf382,\uf3a5,\uf3be,\uf3bf,\uf3c1,\uf3c5,\uf3c9,\uf3cd,\uf3d1,\uf3dd,\uf3e0,\uf3e5,\uf3ed,\uf3fa,\uf3fd,\uf3ff,\uf406,\uf410,\uf433,\uf434,\uf436,\uf439,\uf43a,\uf43c,\uf43f,\uf441,\uf443,\uf445,\uf447,\uf44b,\uf44e,\uf450,\uf453,\uf458,\uf45c,\uf45d,\uf45f,\uf461,\uf462,\uf466,\uf468,\uf469,\uf46a,\uf46b,\uf46c,\uf46d,\uf470,\uf471,\uf472,\uf474,\uf477,\uf478,\uf479,\uf47d,\uf47e,\uf47f,\uf481,\uf482,\uf484,\uf485,\uf486,\uf487,\uf48b,\uf48d,\uf48e,\uf490,\uf491,\uf492,\uf493,\uf494,\uf496,\uf497,\uf49e,\uf4ad,\uf4b3,\uf4b8,\uf4b9,\uf4ba,\uf4bd,\uf4be,\uf4c0,\uf4c2,\uf4c4,\uf4cd,\uf4ce,\uf4d3,\uf4d6,\uf4d7,\uf4d8,\uf4d9,\uf4da,\uf4db,\uf4de,\uf4df,\uf4e2,\uf4e3,\uf4fa,\uf4fb,\uf4fc,\uf4fd,\uf4fe,\uf4ff,\uf500,\uf501,\uf502,\uf503,\uf504,\uf505,\uf506,\uf507,\uf508,\uf509,\uf515,\uf516,\uf517,\uf518,\uf519,\uf51a,\uf51b,\uf51c,\uf51d,\uf51e,\uf51f,\uf520,\uf521,\uf522,\uf523,\uf524,\uf525,\uf526,\uf527,\uf528,\uf529,\uf52a,\uf52b,\uf52c,\uf52d,\uf52e,\uf52f,\uf530,\uf531,\uf532,\uf533,\uf534,\uf535,\uf536,\uf537,\uf538,\uf539,\uf53a,\uf53b,\uf53c,\uf53d,\uf53e,\uf53f,\uf540,\uf541,\uf542,\uf543,\uf544,\uf545,\uf546,\uf547,\uf548,\uf549,\uf54a,\uf54b,\uf54c,\uf54d,\uf54e,\uf54f,\uf550,\uf551,\uf552,\uf553,\uf554,\uf555,\uf556,\uf557,\uf558,\uf559,\uf55a,\uf55b,\uf55c,\uf55d,\uf55e,\uf55f,\uf560,\uf561,\uf562,\uf563,\uf564,\uf565,\uf566,\uf567,\uf568,\uf569,\uf56a,\uf56b,\uf56c,\uf56d,\uf56e,\uf56f,\uf570,\uf571,\uf572,\uf573,\uf574,\uf575,\uf576,\uf577,\uf578,\uf579,\uf57a,\uf57b,\uf57c,\uf57d,\uf57e,\uf57f,\uf580,\uf581,\uf582,\uf583,\uf584,\uf585,\uf586,\uf587,\uf588,\uf589,\uf58a,\uf58b,\uf58c,\uf58d,\uf58e,\uf58f,\uf590,\uf591,\uf593,\uf594,\uf595,\uf596,\uf597,\uf598,\uf599,\uf59a,\uf59b,\uf59c,\uf59d,\uf59f,\uf5a0,\uf5a1,\uf5a2,\uf5a4,\uf5a5,\uf5a6,\uf5a7,\uf5aa,\uf5ab,\uf5ac,\uf5ad,\uf5ae,\uf5af,\uf5b0,\uf5b1,\uf5b3,\uf5b4,\uf5b6,\uf5b7,\uf5b8,\uf5ba,\uf5bb,\uf5bc,\uf5bd,\uf5bf,\uf5c0,\uf5c1,\uf5c2,\uf5c3,\uf5c4,\uf5c5,\uf5c7,\uf5c8,\uf5c9,\uf5ca,\uf5cb,\uf5cd,\uf5ce,\uf5d0,\uf5d1,\uf5d2,\uf5d7,\uf5da,\uf5dc,\uf5de,\uf5df,\uf5e1,\uf5e4,\uf5e7,\uf5eb,\uf5ee,\uf5fc,\uf5fd,\uf610,\uf613,\uf619,\uf61f,\uf621,\uf62e,\uf62f,\uf630,\uf637,\uf63b,\uf63c,\uf641,\uf644,\uf647,\uf64a,\uf64f,\uf651,\uf653,\uf654,\uf655,\uf658,\uf65d,\uf65e,\uf662,\uf664,\uf665,\uf666,\uf669,\uf66a,\uf66b,\uf66d,\uf66f,\uf674,\uf676,\uf678,\uf679,\uf67b,\uf67c,\uf67f,\uf681,\uf682,\uf683,\uf684,\uf687,\uf688,\uf689,\uf696,\uf698,\uf699,\uf69a,\uf69b,\uf6a0,\uf6a1,\uf6a7,\uf6a9,\uf6ad,\uf6b6,\uf6b7,\uf6bb,\uf6be,\uf6c0,\uf6c3,\uf6c4,\uf6cf,\uf6d1,\uf6d3,\uf6d5,\uf6d7,\uf6d9,\uf6dd,\uf6de,\uf6e2,\uf6e3,\uf6e6,\uf6e8,\uf6ec,\uf6ed,\uf6f0,\uf6f1,\uf6f2,\uf6fa,\uf6fc,\uf6ff,\uf700,\uf70b,\uf70c,\uf70e,\uf714,\uf715,\uf717,\uf71e,\uf722,\uf728,\uf729,\uf72e,\uf72f,\uf73b,\uf73c,\uf73d,\uf740,\uf743,\uf747,\uf74d,\uf753,\uf756,\uf75a,\uf75b,\uf75e,\uf75f,\uf769,\uf76b,\uf772,\uf773,\uf77c,\uf77d,\uf780,\uf781,\uf783,\uf784,\uf786,\uf787,\uf788,\uf78c,\uf793,\uf794,\uf796,\uf79c,\uf79f,\uf7a0,\uf7a2,\uf7a4,\uf7a5,\uf7a6,\uf7a9,\uf7aa,\uf7ab,\uf7ad,\uf7ae,\uf7b5,\uf7b6,\uf7b9,\uf7ba,\uf7bd,\uf7bf,\uf7c0,\uf7c2,\uf7c4,\uf7c5,\uf7c9,\uf7ca,\uf7cc,\uf7cd,\uf7ce,\uf7d0,\uf7d2,\uf7d7,\uf7d8,\uf7d9,\uf7da,\uf7e4,\uf7e5,\uf7e6,\uf7ec,\uf7ef,\uf7f2,\uf7f5,\uf7f7,\uf7fb,\uf805,\uf806,\uf807,\uf80f,\uf810,\uf812,\uf815,\uf816,\uf818,\uf829,\uf82a,\uf82f,\uf83e,\uf84a,\uf84c,\uf850,\uf853,\uf863,\uf86d,\uf879,\uf87b,\uf87c,\uf87d,\uf881,\uf882,\uf884,\uf885,\uf886,\uf887,\uf891,\uf897", ",");
    private static final int CHALLENGE_RESPONSE_REQUEST_CODE = 12345;

    private LinearLayout llStandalone;
    private LinearLayout llMethod;
    private EditText etSalt;
    private TextView tvSaltColor;
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
        tvSaltColor = findViewById(R.id.tvSaltColor);
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

        Typeface font = Typeface.createFromAsset( getAssets(), "fa-solid-900.ttf" );

        etSalt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                updateSaltColor();
            }
        });
        tvSaltColor.setTypeface(font);
        updateSaltColor();


        autofill = getIntent().getBooleanExtra("autofill", false);
        llStandalone.setVisibility(autofill ? View.GONE : View.VISIBLE);
        llMethod.setVisibility(autofill ? View.GONE : View.VISIBLE);

        String passwordId = getIntent().getStringExtra("passwordId");
        etPasswordId.setText(passwordId);

        etSalt.requestFocus();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    private void updateSaltColor() {
        CRC32 crcObj = new CRC32();
        crcObj.update(etSalt.getText().toString().getBytes());
        int crc = Math.abs((int) crcObj.getValue());
        int rgb = crc & 0xFFFFFF;
        int negatedRgb = 0xFFFFFF - rgb;
        tvSaltColor.setBackground(customDrawable(rgb | 0xFF000000, 0xFF000000));
        tvSaltColor.setTextColor(negatedRgb | 0xFF000000);

        tvSaltColor.setText(FONT_AWESOME_ICONS[crc % FONT_AWESOME_ICONS.length]);
    }

    public static Drawable customDrawable(int backgroundColor, int borderColor) {
        GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.RECTANGLE);
        shape.setCornerRadii(new float[] { 8, 8, 8, 8, 8, 8, 8, 8 });
        shape.setColor(backgroundColor);
        shape.setStroke(3, borderColor);
        return shape;
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
