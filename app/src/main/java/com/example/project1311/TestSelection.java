package com.example.project1311;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class TestSelection extends AppCompatActivity implements View.OnClickListener
{
    Button btnRET,btnAll,btnFamily,btnNumbers;
    private ActivityResultLauncher<Intent> activityResultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_selection);
        btnRET=findViewById(R.id.btnRET);
        btnRET.setOnClickListener(this);
        btnAll=findViewById(R.id.btnAll);
        btnAll.setOnClickListener(this);
        btnFamily=findViewById(R.id.btnFamily);
        btnFamily.setOnClickListener(this);
        btnNumbers=findViewById(R.id.btnNumb);
        btnNumbers.setOnClickListener(this);
        activityResultLauncher = registerForActivityResult(//משתנה שאיתו עוברים בין האקטיביטי
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {//המתודה מטפלת בהחזרת תוצאה מפעילות אחרת, שומרת הזמנה ב-Firebase ומציגה הודעת Toast עם הפרטים.
                        if (result.getResultCode() == RESULT_OK) {
                            Intent data = result.getData();
                            String str = data.getStringExtra("score");
                            Toast.makeText(TestSelection.this, ""+str, Toast.LENGTH_SHORT).show();
                        }

                    }
                });

    }

    @Override
    public void onClick(View v)
    {
        if(v==btnRET)
        {
            finish();
        }
        if(v==btnAll)
        {
            Intent i = new Intent(this, TestActivity.class);
            activityResultLauncher.launch(i);

        }
        if(v==btnFamily)
        {
            Intent i = new Intent(this, TestFamily.class);
            activityResultLauncher.launch(i);



        }
        if(v==btnNumbers)
        {
            Intent i = new Intent(this, TestNumbers.class);
            activityResultLauncher.launch(i);



        }

    }
}