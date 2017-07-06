package com.sth.opengldemo.Acitivty;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.nbsp.materialfilepicker.ui.FilePickerActivity;
import com.sth.opengldemo.R;

import java.util.regex.Pattern;

public class SelectVideoFileActivity extends Activity {
    private static final String TAG = "TestActivity";
    private String filePath="~(～￣▽￣)～";

    private Button btn_pick;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select__video_file);
        btn_pick=(Button)findViewById(R.id.pick_video_btn);
        btn_pick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(SelectVideoFileActivity.this, FilePickerActivity.class);
                intent.putExtra(FilePickerActivity.ARG_FILTER, Pattern.compile("(.*\\.mp4$)||(.*\\.avi$)||(.*\\.wmv$)"));
                startActivityForResult(intent, 1);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            filePath = data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH);
            Intent intent=new Intent(this,MainActivity.class);
            intent.putExtra("filePath",filePath);
            startActivity(intent);
        }
    }

}
