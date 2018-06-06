package com.example.ouhvs.seminarapplication.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.example.ouhvs.seminarapplication.R;

public class BaseClass extends AppCompatActivity {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.menu,menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId())
        {
            case R.id.home:
                Intent intent=new Intent(this,Home.class);
                startActivity(intent);
                finish();
                return true;
            case R.id.imageCompress:
                Intent intent1=new Intent(this,ImageCompress.class);
                startActivity(intent1);
                finish();
                return true;
            case R.id.logout:
                GlobalClass.editor.clear();
                GlobalClass.editor.commit();
                Intent intent2=new Intent(this,MainActivity.class);
                startActivity(intent2);
                finish();
                default:
                    return super.onOptionsItemSelected(item);
        }
    }
}
