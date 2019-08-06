package org.liran.noam.ykpassdroid;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.github.fzakaria.ascii85.Ascii85;
import com.google.common.io.BaseEncoding;

import java.util.Formatter;

public class MainActivity extends Activity {
    private static final int CHALLENGE_RESPONSE_REQUEST_CODE = 12345;

    private EditText etSalt;
    private EditText etPasswordId;
    private EditText etResult;
    private Button btnGenerate;
    private Button btnClear;
    private ImageButton btnCopy;
    private RadioGroup rgMethod;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etSalt = findViewById(R.id.etSalt);
        etPasswordId = findViewById(R.id.etPasswordId);
        etResult = findViewById(R.id.etResult);
        btnGenerate = findViewById(R.id.btnGenerate);
        btnCopy = findViewById(R.id.btnCopy);
        btnClear = findViewById(R.id.btnClear);
        rgMethod = findViewById(R.id.rgMethod);

        btnGenerate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent intent = new Intent("net.pp3345.ykdroid.intent.action.CHALLENGE_RESPONSE");
                intent.putExtra("challenge", String.format("%s,%s", etSalt.getText(), etPasswordId.getText()).getBytes());

                startActivityForResult(intent, CHALLENGE_RESPONSE_REQUEST_CODE);
            }
        });

        btnCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("ykpassDroid", etResult.getText());
                clipboard.setPrimaryClip(clip);

                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "Copied", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etPasswordId.setText("");
                etResult.setText("");
            }
        });



    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CHALLENGE_RESPONSE_REQUEST_CODE && resultCode == RESULT_OK) {
            final byte[] response = data.getByteArrayExtra("response");

            String processedResponse = processResponse(response, rgMethod.getCheckedRadioButtonId());
            etResult.setText(new String(processedResponse));
        }
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
