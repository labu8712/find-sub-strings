package me.arthurc.find_sub_strings;

import android.content.Context;
import android.support.annotation.NonNull;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
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

    private Validator validator;
    private boolean validated = false;

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
        findViewById(R.id.btn_set_default).setOnClickListener(this);
        findViewById(R.id.btn_clear).setOnClickListener(this);
    }

    private ArrayList<String> findResult(@NonNull String s, @NonNull String p) {
        Pattern pattern = Pattern.compile(p);
        ArrayList<String> result = new ArrayList<>();
        ArrayList<String> targetList = new ArrayList<>();

        targetList.add(s);

        while (!targetList.isEmpty()) {
            String target = targetList.get(0);
            targetList.remove(0);

            Matcher matcher = pattern.matcher(target);
            if (!matcher.find()) {
                continue;
            }

            String matchString = matcher.group();
            if (result.contains(matchString)) {
                continue;
            }

            result.add(matchString);
            targetList.add(matchString.substring(1));
            targetList.add(matchString.substring(0, matchString.length() - 1));
        }

        return result;
    }

    private HashSet<String> findResultRecursive(@NonNull String s, @NonNull String p) {
        Pattern pattern = Pattern.compile(p);
        Matcher matcher = pattern.matcher(s);
        HashSet<String> result = new HashSet<>();

        if (!matcher.find()) {
            return result;
        }

        String matchString = matcher.group();
        result.add(matchString);
        result.addAll(findResultRecursive(matchString.substring(1), p));
        result.addAll(findResultRecursive(matchString.substring(0, matchString.length() -1), p));

        return result;
    }

    private String resultToString(String title, ArrayList<String> result) {
        if (result.isEmpty()) {
            return "";
        }

        // Sort result
        Collections.sort(result, new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                return Integer.compare(s1.length(), s2.length());
            }
        });

        String count = String.format(Locale.getDefault(), "（共 %d 個）：", result.size());

        // Build result
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(title);
        stringBuilder.append(count);
        stringBuilder.append("\n");

        for (int i = 0; i < result.size(); i++) {
            if (i > 0) {
                stringBuilder.append("\n");
            }

            stringBuilder.append(i + 1);
            stringBuilder.append(". ");
            stringBuilder.append(result.get(i));
        }

        return stringBuilder.toString();
    }

    private void doSubmit() {
        closeKeyboard();

        int interval = Integer.parseInt(etInterval.getText().toString());
        String startWith = etStartWith.getText().toString();
        String endWith = etEndWith.getText().toString();
        String s = etTarget.getText().toString();
        String p = String.format(Locale.getDefault(), "%s.{%s,}%s", startWith, interval, endWith);

        ArrayList<String> result1 = findResult(s, p);
        ArrayList<String> result2 = new ArrayList<>(findResultRecursive(s, p));

        if (result1.isEmpty() && result2.isEmpty()) {
            Toast.makeText(this, "找不到任何結果！", Toast.LENGTH_SHORT).show();
            return;
        }

        String sResult1 = resultToString("遞　回", result1);
        String sResult2 = resultToString("非遞回", result2);
        tvResult.setText(String.format(Locale.getDefault(), "%s\n\n%s", sResult1, sResult2));
    }

    private void closeKeyboard() {
        if (getCurrentFocus() == null) {
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
        switch (view.getId()) {
            case R.id.btn_submit:
                validator.validate();
                tvResult.setText("");

                if (validated) {
                    doSubmit();
                }

                break;

            case R.id.btn_set_default:
                String defaultTarget = "aba51bc2b";
                etStartWith.setText("a");
                etEndWith.setText("b");
                etInterval.setText("1");
                etTarget.setText(defaultTarget);
                break;

            case R.id.btn_clear:
                etStartWith.setText("");
                etEndWith.setText("");
                etInterval.setText("");
                etTarget.setText("");
                tvResult.setText("");
                break;
        }
    }
}
