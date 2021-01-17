package com.example.excercisetracker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private Button recordButton;
    boolean recordmode=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recordButton=(Button) findViewById(R.id.RecordButton);
        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!recordmode){
                    recordButton.setText("Stop Recording");
                    recordmode=true;
                    toastMessage("Recording Started!");

                }else{
                    recordButton.setText("Start Recording");
                    recordmode=false;
                    toastMessage("Recording Finished!");
                    Intent intent = new Intent(MainActivity.this, ReportActivity.class);
                    startActivity(intent);

                }

            }
        });
    }
    private void toastMessage(String message){
        Toast.makeText(this,message, Toast.LENGTH_SHORT).show();
    }
}