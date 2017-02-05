package org.rc.inventory.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import org.rc.inventory.R;
import org.rc.inventory.excel.ExcelParser;
import org.rc.inventory.model.InventoryModel;
import org.rc.inventory.util.InventoryUtil;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;

    private View mLoadingView;
    private View mButtonView;
    private View mErrorView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initUI();
    }


    private void initUI() {
        mLoadingView = findViewById(R.id.main_activity_loading_layout);
        mButtonView = findViewById(R.id.main_activity_button_layout);
        mErrorView = findViewById(R.id.main_activity_error_layout);
    }


    @Override
    protected void onResume() {
        super.onResume();

        mLoadingView.setVisibility(View.VISIBLE);
        mButtonView.setVisibility(View.INVISIBLE);
        mErrorView.setVisibility(View.INVISIBLE);

        loadExcelDataAsync();
        requestPermission();
    }


    private void loadExcelDataAsync() {
        Thread thread = new Thread(mLoadExcelFileRunnable);
        thread.setDaemon(true);
        thread.start();
    }


    public void OnStartInventoryButtonClick(View v) {
        Intent selectCarLocationIntent = new Intent(this, InventoryLocationMainActivity.class);

        InventoryModel item = new InventoryModel();
        selectCarLocationIntent.putExtra(InventoryUtil.EXTRA_INVENTORY_ITEM, item);

        startActivity(selectCarLocationIntent);
    }


    private Runnable mLoadExcelFileRunnable = new Runnable() {
        @Override
        public void run() {

            // load excel workbook
            if (!ExcelParser.getInstance().isValid()) {
                ExcelParser.getInstance().loadLocationStructureAsync();
            }

            // wait until the the workbook was loaded
            while(!ExcelParser.getInstance().isDoneLoading()) {
                try {
                    Thread.sleep(10);
                } catch (Exception e) {}
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mLoadingView.setVisibility(View.INVISIBLE);

                    if(ExcelParser.getInstance().isValid()) {
                        mButtonView.setVisibility(View.VISIBLE);
                        mErrorView.setVisibility(View.INVISIBLE);

                    } else {
                        mErrorView.setVisibility(View.VISIBLE);
                        mButtonView.setVisibility(View.INVISIBLE);
                    }
                }
            });
        }
    };


    private void requestPermission() {
        final int permissionCheck = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);

        // permission not granted yet
        if(permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.length <= 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {

                    // permission was not granted
                    new AlertDialog.Builder(this)
                            .setTitle(R.string.alert_dialog_permission_denied_title)
                            .setMessage(R.string.alert_dialog_permission_denied_msg)
                            .setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    finish();
                                }
                            }).show();
                }
            }
        }
    }
}
