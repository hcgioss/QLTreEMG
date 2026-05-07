package com.example.qltreem;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class KidMainActivity extends AppCompatActivity implements SensorEventListener {

    private TextView tvKidStars;
    private LinearLayout layoutKidTasks;
    private Button btnKidBack, btnGoToStore;

    private DatabaseReference mDatabase, mTasksDatabase, mSessionRef;

    // Sensor
    private SensorManager sensorManager;
    private Sensor lightSensor;
    private Sensor proximitySensor;

    // Timer
    private CountDownTimer sessionTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kid_main);

        // Ánh xạ view
        tvKidStars = findViewById(R.id.tvKidStars);
        layoutKidTasks = findViewById(R.id.layoutKidTasks);
        btnKidBack = findViewById(R.id.btnKidBack);
        btnGoToStore = findViewById(R.id.btnGoToStore);

        // Firebase
        mDatabase = FirebaseDatabase.getInstance()
                .getReference()
                .child("TotalStars");

        mTasksDatabase = FirebaseDatabase.getInstance()
                .getReference()
                .child("Tasks");

        mSessionRef = FirebaseDatabase.getInstance()
                .getReference()
                .child("SessionTimeLimit");

        // ====================================================
        // LẤY THỜI GIAN GIỚI HẠN TỪ FIREBASE
        // ====================================================

        mSessionRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                long timeInMillis = 15 * 60 * 1000L;

                if (snapshot.exists()) {

                    Integer minutes = snapshot.getValue(Integer.class);

                    if (minutes != null) {
                        timeInMillis = minutes * 60 * 1000L;
                    }
                }

                startSessionTimer(timeInMillis);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

                startSessionTimer(15 * 60 * 1000L);
            }
        });

        // ====================================================
        // LOAD SAO
        // ====================================================

        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.exists()) {

                    Integer stars = snapshot.getValue(Integer.class);

                    if (stars != null) {
                        tvKidStars.setText("⭐ " + stars);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        // ====================================================
        // LOAD NHIỆM VỤ
        // ====================================================

        mTasksDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                layoutKidTasks.removeAllViews();

                // =====================================
                // NÚT HỌC BÀI CỐ ĐỊNH
                // =====================================

                Button btnStudy = new Button(KidMainActivity.this);

                btnStudy.setText("📚 HỌC BÀI 90 PHÚT (+50 ⭐)");
                btnStudy.setTextSize(18);

                btnStudy.setPadding(20, 20, 20, 20);

                btnStudy.setBackgroundTintList(
                        ColorStateList.valueOf(Color.parseColor("#3498DB"))
                );

                btnStudy.setTextColor(Color.WHITE);

                btnStudy.setOnClickListener(v -> {

                    Intent intent = new Intent(
                            KidMainActivity.this,
                            StudyModeActivity.class
                    );

                    startActivity(intent);
                });

                layoutKidTasks.addView(btnStudy);

                // =====================================
                // LOAD TASK TỪ FIREBASE
                // =====================================

                for (DataSnapshot taskSnap : snapshot.getChildren()) {

                    try {

                        String status = String.valueOf(
                                taskSnap.child("status").getValue()
                        );

                        if ("assigned".equals(status)
                                || "pending".equals(status)) {

                            updateTaskListUI(taskSnap, status);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        // ====================================================
        // SENSOR
        // ====================================================

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        if (sensorManager != null) {

            lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

            proximitySensor = sensorManager.getDefaultSensor(
                    Sensor.TYPE_PROXIMITY
            );
        }

        if (lightSensor == null || proximitySensor == null) {

            Toast.makeText(
                    this,
                    "Máy thiếu cảm biến!",
                    Toast.LENGTH_SHORT
            ).show();
        }

        // ====================================================
        // NÚT CHUYỂN TRANG
        // ====================================================

        btnGoToStore.setOnClickListener(v -> {

            startActivity(
                    new Intent(this, VideoStoreActivity.class)
            );
        });

        btnKidBack.setOnClickListener(v -> finish());
    }

    // ====================================================
    // TIMER GIỚI HẠN THỜI GIAN
    // ====================================================

    private void startSessionTimer(long durationInMillis) {

        long minutes = durationInMillis / (60 * 1000);

        Toast.makeText(
                this,
                "Con có " + minutes + " phút sử dụng app",
                Toast.LENGTH_LONG
        ).show();

        sessionTimer = new CountDownTimer(durationInMillis, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {

                new AlertDialog.Builder(KidMainActivity.this)
                        .setTitle("⏰ Hết giờ rồi!")
                        .setMessage("Con hãy nghỉ ngơi nhé!")
                        .setCancelable(false)
                        .setPositiveButton("Dạ", (dialog, which) -> {

                            finish();
                        })
                        .show();
            }
        }.start();
    }

    // ====================================================
    // SENSOR CHANGED
    // ====================================================

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType() == Sensor.TYPE_LIGHT) {

            float lightLevel = event.values[0];

            if (lightLevel < 10) {

                Toast.makeText(
                        this,
                        "🌙 Bật đèn lên nhé!",
                        Toast.LENGTH_SHORT
                ).show();
            }
        }

        if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {

            float distance = event.values[0];

            if (proximitySensor != null &&
                    distance < proximitySensor.getMaximumRange()) {

                Toast.makeText(
                        this,
                        "📱 Đừng để quá gần mắt!",
                        Toast.LENGTH_SHORT
                ).show();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onResume() {
        super.onResume();

        if (lightSensor != null) {

            sensorManager.registerListener(
                    this,
                    lightSensor,
                    SensorManager.SENSOR_DELAY_NORMAL
            );
        }

        if (proximitySensor != null) {

            sensorManager.registerListener(
                    this,
                    proximitySensor,
                    SensorManager.SENSOR_DELAY_NORMAL
            );
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (sessionTimer != null) {
            sessionTimer.cancel();
        }
    }

    // ====================================================
    // HIỂN THỊ TASK
    // ====================================================

    private void updateTaskListUI(DataSnapshot taskSnap, String status) {

        String name = String.valueOf(
                taskSnap.child("name").getValue()
        );

        String reward = String.valueOf(
                taskSnap.child("reward").getValue()
        );

        View taskView = LayoutInflater.from(this)
                .inflate(R.layout.item_task_kid, null);

        TextView tvName = taskView.findViewById(R.id.tvKidTaskName);

        TextView tvReward = taskView.findViewById(R.id.tvKidTaskReward);

        Button btnDone = taskView.findViewById(R.id.btnDone);

        tvName.setText(name);

        tvReward.setText("Thưởng: " + reward + " Sao");

        if ("assigned".equals(status)) {

            btnDone.setText("XONG!");

            btnDone.setEnabled(true);

            btnDone.setBackgroundTintList(
                    ColorStateList.valueOf(
                            Color.parseColor("#4CAF50")
                    )
            );

            btnDone.setOnClickListener(v -> {

                taskSnap.getRef()
                        .child("status")
                        .setValue("pending");

                Toast.makeText(
                        this,
                        "Đã gửi chờ duyệt!",
                        Toast.LENGTH_SHORT
                ).show();
            });

        } else {

            btnDone.setText("⏳ CHỜ DUYỆT");

            btnDone.setEnabled(false);

            btnDone.setBackgroundTintList(
                    ColorStateList.valueOf(
                            Color.parseColor("#95A5A6")
                    )
            );
        }

        layoutKidTasks.addView(taskView);
    }
}