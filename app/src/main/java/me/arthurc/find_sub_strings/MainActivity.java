package me.arthurc.find_sub_strings;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText etStartWith;
    private EditText etEndWith;
    private EditText etInterval;
    private EditText etTarget;
    private TextView tvResult;

    private ArrayList<String> result = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etStartWith = findViewById(R.id.et_start_with);
        etEndWith = findViewById(R.id.et_end_with);
        etInterval = findViewById(R.id.et_interval);
        etTarget = findViewById(R.id.et_target);
        tvResult = findViewById(R.id.tv_result);

        findViewById(R.id.btn_submit).setOnClickListener(this);
    }

    private void findResult(String s, String p) {
        Pattern pattern = Pattern.compile(p);
        Matcher matcher = pattern.matcher(s);

        if (matcher.find()) {
            String matchString = matcher.group();

            if (result.contains(matchString)) {
                return;
            }

            result.add(matchString);

            findResult(matchString.substring(1), p);
            findResult(matchString.substring(0, matchString.length() - 1), p);
        }
    }

    private boolean validEditText() {
        ArrayList<EditText> editTexts = new ArrayList<>();

        editTexts.add(etStartWith);
        editTexts.add(etEndWith);
        editTexts.add(etInterval);
        editTexts.add(etTarget);

        boolean hasError = false;

        for (EditText editText: editTexts) {
            if (editText.getText().toString().isEmpty()) {
                editText.setError("此欄位不得空白");
                hasError = true;
            }
        }

        return !hasError;
    }

    private void doSubmit() {
        if (!validEditText()) return;

        String interval = etInterval.getText().toString();
        String startWith = etStartWith.getText().toString();
        String endWith = etEndWith.getText().toString();

        String s = etTarget.getText().toString();
        String p = String.format(Locale.getDefault(), "%s.{%s,}%s", startWith, interval, endWith);

        findResult(s, p);

        // Set output
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < result.size(); i++) {
            if (i > 0) {
                stringBuilder.append("\n");
            }

            stringBuilder.append(i + 1);
            stringBuilder.append(". ");
            stringBuilder.append(result.get(i));
        }
        tvResult.setText(stringBuilder.toString());

        // Clean up
        result.clear();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_submit:
                doSubmit();
                break;
        }
    }
}
