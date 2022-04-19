package com.example.nfc;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private Test lpcd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lpcd = new Test();
    }

    public void onClick(View view) {
        Toast.makeText(this, "按钮被点击啦", Toast.LENGTH_LONG).show();
        lpcd.onLPCDEvent("lpcd");
    }
}