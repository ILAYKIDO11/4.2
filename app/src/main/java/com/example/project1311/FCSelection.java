package com.example.project1311;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class FCSelection extends AppCompatActivity implements View.OnClickListener
{
    Button btnRET,btnAll,btnFamily,btnNumbers;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fcselection);
        btnRET=findViewById(R.id.btnRET);
        btnRET.setOnClickListener(this);
        btnAll=findViewById(R.id.btnAll);
        btnAll.setOnClickListener(this);
        btnFamily=findViewById(R.id.btnFamily);
        btnFamily.setOnClickListener(this);
        btnNumbers=findViewById(R.id.btnNumb);
        btnNumbers.setOnClickListener(this);

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
            Intent intent=new Intent(this,FlashCardsActivity.class);
            startActivity(intent);

        }
        if(v==btnFamily)
        {
            Intent intent=new Intent(this,FamilyFcActivity.class);
            startActivity(intent);


        }
        if(v==btnNumbers)
        {
            Intent intent=new Intent(this,NumbersFcActivity.class);
            startActivity(intent);

        }

    }
}