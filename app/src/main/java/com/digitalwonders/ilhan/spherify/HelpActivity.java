package com.digitalwonders.ilhan.spherify;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;


public class HelpActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        findViewById(R.id.imageButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(getResources().getString(R.string.video_link))));
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_help, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if(id == R.id.menu_item_new) {
            //Log.i("Spherify", "share clicked");

            selectAnImage();
        }

        return super.onOptionsItemSelected(item);
    }

    private void selectAnImage(){
        Intent pickIntent = new Intent();
        pickIntent.setType("image/*");
        pickIntent.setAction(Intent.ACTION_PICK);

        startActivityForResult(Intent.createChooser(pickIntent, "Select Picture"), AppConstant.SELECT_PICTURE);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if(resultCode == RESULT_OK){

            final Uri imageUri = data.getData();

            Intent intent = new Intent(HelpActivity.this, EditActivity.class);
            intent.putExtra(AppConstant.SPHERIFY_IMAGE_PATH, imageUri.toString());
            //Log.i("Spherify", imageUri.toString());

            startActivity(intent);

        }
    }
}
