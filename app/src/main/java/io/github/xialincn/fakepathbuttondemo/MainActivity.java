package io.github.xialincn.fakepathbuttondemo;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import io.github.xialincn.fakepathbutton.FakePathButton;


public class MainActivity extends ActionBarActivity {
    private final String TAG = "MainActivity";
    RelativeLayout rootLayout;
    FakePathButton button;
    ImageButton resetButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rootLayout = (RelativeLayout) findViewById(R.id.root_layout);
        button = (FakePathButton) findViewById(R.id.float_action_button);
        rootLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                button.gather();
            }
        });

        resetButton = (ImageButton) findViewById(R.id.reset_button);
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                button.reset();
            }
        });
    }
}
