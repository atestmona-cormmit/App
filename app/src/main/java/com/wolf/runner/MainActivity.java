package com.wolf.runner;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {
    private static final int PICK_ZIP_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Button btnSelectZip = new Button(this);
        btnSelectZip.setText("اختر ملف البوت (ZIP) للبدء");
        setContentView(btnSelectZip);

        // طلب صلاحية قراءة الملفات (مهم جداً لأندرويد 11+)
        if (!Environment.isExternalStorageManager()) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivity(intent);
        }

        btnSelectZip.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("application/zip");
            startActivityForResult(intent, PICK_ZIP_REQUEST);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_ZIP_REQUEST && resultCode == Activity.RESULT_OK) {
            if (data != null && data.getData() != null) {
                Uri zipUri = data.getData();
                // نسخ الملف إلى مساحة التطبيق الخاصة لتسهيل قراءته
                File destFile = new File(getFilesDir(), "bot.zip");
                try (InputStream is = getContentResolver().openInputStream(zipUri);
                     FileOutputStream os = new FileOutputStream(destFile)) {
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = is.read(buffer)) > 0) {
                        os.write(buffer, 0, length);
                    }
                    
                    // بدء الخدمة وتمرير مسار الملف
                    Intent serviceIntent = new Intent(this, NodeService.class);
                    serviceIntent.putExtra("ZIP_PATH", destFile.getAbsolutePath());
                    // مسار ملف التوكن (في مجلد التنزيلات)
                    serviceIntent.putExtra("TOKENS_PATH", "/storage/emulated/0/Download/wolf_tokens.json");
                    startForegroundService(serviceIntent);
                    Toast.makeText(this, "تم تجهيز الملف، جاري التشغيل...", Toast.LENGTH_LONG).show();

                } catch (Exception e) {
                    Toast.makeText(this, "خطأ في قراءة الملف", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
