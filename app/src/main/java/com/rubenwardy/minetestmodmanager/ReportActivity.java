package com.rubenwardy.minetestmodmanager;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.rubenwardy.minetestmodmanager.manager.ModManager;

public class ReportActivity extends AppCompatActivity {
    public static final String EXTRA_LIST = "list";
    public static final String EXTRA_MOD_NAME = "modname";
    public static final String EXTRA_AUTHOR = "author";
    public static final String EXTRA_LINK = "link";
    private String selected = "";

    private @NonNull String str_make_nonnull(@Nullable String str) {
        if (str == null) {
            return "";
        } else {
            return str;
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }


        final String listname = str_make_nonnull(getIntent().getStringExtra(EXTRA_LIST));
        final String link     = str_make_nonnull(getIntent().getStringExtra(EXTRA_LINK));
        final String author   = str_make_nonnull(getIntent().getStringExtra(EXTRA_AUTHOR));
        final String modname  = str_make_nonnull(getIntent().getStringExtra(EXTRA_MOD_NAME));

        Resources res = getResources();

        TextView tv = (TextView) findViewById(R.id.mod_details);
        String details = String.format(res.getString(R.string.x_by_y), modname, author);
        details += "\n" + link;
        details += "\n" + listname;
        tv.setText(details);

        final Spinner spinner = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.report_reasons, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selected = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        Button btn_report = (Button) findViewById(R.id.submit);
        btn_report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(@NonNull View view) {
                EditText textbox = (EditText)findViewById(R.id.editText);
                String info = "Reason: " + str_make_nonnull(selected) + "\n";
                info += "Msg: " + str_make_nonnull(textbox.getText().toString());

                ModManager modman = new ModManager();
                modman.reportModAsync(view.getContext(), modname, author, listname, link, info);
                finish();
            }
        });
    }

}
