package me.arthurc.find_sub_strings;

import android.content.Context;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.Min;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, Validator.ValidationListener {

    // UI
    @NotEmpty(message = "這個欄位是必填的")
    private EditText etStartWith;

    @NotEmpty(message = "這個欄位是必填的")
    private EditText etEndWith;

    @NotEmpty(message = "這個欄位是必填的")
    @Min(value = 0, message = "此欄位的最小值為 0")
    private EditText etInterval;

    @NotEmpty(message = "這個欄位是必填的")
    private EditText etTarget;

    private TextView tvResult;

    // Var
    private Validator validator;
    private boolean validated = false;
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

        validator = new Validator(this);
        validator.setValidationListener(this);

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

    private void doSubmit() {
        String interval = etInterval.getText().toString();
        String startWith = etStartWith.getText().toString();
        String endWith = etEndWith.getText().toString();

        String s = etTarget.getText().toString();
        String p = String.format(Locale.getDefault(), "%s.{%s,}%s", startWith, interval, endWith);

        findResult(s, p);

        if (result.isEmpty()) {
            Toast.makeText(this, "找不到任何結果！", Toast.LENGTH_SHORT).show();
            return;
        }

        closeKeyboard();

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

        String toastMessage =  String.format(Locale.getDefault(), "找到 %d 個結果", result.size());
        Toast.makeText(this, toastMessage, Toast.LENGTH_SHORT).show();

        // Clean up
        result.clear();
    }

    private void closeKeyboard() {
        if (getCurrentFocus() ==  null) {
            return;
        }

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    @Override
    public void onValidationSucceeded() {
        validated = true;
    }

    @Override
    public void onValidationFailed(List<ValidationError> errors) {
        validated = false;

        for (ValidationError error : errors) {
            View view = error.getView();
            String message = error.getCollatedErrorMessage(this);

            if (view instanceof EditText) {
                ((EditText) view).setError(message);
            } else {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onClick(View view) {
        validator.validate();

        switch (view.getId()) {
            case R.id.btn_submit:
                tvResult.setText("");

                if (validated) {
                    doSubmit();
                }

                break;
        }
    }
}
