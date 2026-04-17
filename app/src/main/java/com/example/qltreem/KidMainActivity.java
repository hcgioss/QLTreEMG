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
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

// IMPLEMENT THÊM SensorEventListener ĐỂ SỬ DỤNG CẢM BIẾN
public class KidMainActivity extends AppCompatActivity implements SensorEventListener {

    private TextView tvKidStars;
    private LinearLayout layoutKidTasks;
    private Button btnKidBack, btnGoToStore;

    private DatabaseReference mDatabase, mTasksDatabase;

    // KHAI BÁO BIẾN CHO SENSORS
    private SensorManager sensorManager;
    private Sensor lightSensor;     // Cảm biến ánh sáng
    private Sensor proximitySensor; // Cảm biến tiệm cận

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kid_main);

        // Ánh xạ View
        tvKidStars = findViewById(R.id.tvKidStars);
        layoutKidTasks = findViewById(R.id.layoutKidTasks);
        btnKidBack = findViewById(R.id.btnKidBack);
        btnGoToStore = findViewById(R.id.btnGoToStore);

        // 1. KẾT NỐI FIREBASE (Giữ nguyên luồng cũ)
        mDatabase = FirebaseDatabase.getInstance().getReference().child("TotalStars");
        mTasksDatabase = FirebaseDatabase.getInstance().getReference().child("Tasks");

        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    tvKidStars.setText("⭐ " + snapshot.getValue(Integer.class));
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        mTasksDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                layoutKidTasks.removeAllViews();
                for (DataSnapshot taskSnap : snapshot.getChildren()) {
                    try {
                        String status = String.valueOf(taskSnap.child("status").getValue());
                        if ("assigned".equals(status) || "pending".equals(status)) {
                            updateTaskListUI(taskSnap, status);
                        }
                    } catch (Exception e) { e.printStackTrace(); }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        // 2. THIẾT LẬP CẢM BIẾN (SENSORS)
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
            proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        }

        // Báo lỗi nếu máy không có cảm biến (thường máy ảo sẽ thiếu)
        if (lightSensor == null || proximitySensor == null) {
            Toast.makeText(this, "Cảnh báo: Máy này thiếu cảm biến phần cứng!", Toast.LENGTH_SHORT).show();
        }

        // Các nút chuyển trang
        btnGoToStore.setOnClickListener(v -> startActivity(new Intent(this, VideoStoreActivity.class)));
        btnKidBack.setOnClickListener(v -> finish());
    }

    // HÀM XỬ LÝ KHI DỮ LIỆU CẢM BIẾN THAY ĐỔI
    @Override
    public void onSensorChanged(SensorEvent event) {
        // XỬ LÝ CẢM BIẾN ÁNH SÁNG (Light Sensor)
        if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
            float lightLevel = event.values[0];
            // Nếu ánh sáng dưới 10 lux (quá tối)
            if (lightLevel < 10) {
                Toast.makeText(this, "🌙 Trời tối quá! Con hãy bật đèn để bảo vệ mắt nhé!", Toast.LENGTH_SHORT).show();
            }
        }

        // XỬ LÝ CẢM BIẾN TIỆM CẬN (Proximity Sensor)
        if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
            float distance = event.values[0];
            // Nếu khoảng cách gần bằng 0 (áp sát mặt vào màn hình)
            if (distance < proximitySensor.getMaximumRange()) {
                Toast.makeText(this, "🚫 Con đang để máy quá gần mắt! Hãy để xa ra nào!", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Không cần xử lý phần này
    }

    // ĐĂNG KÝ CẢM BIẾN KHI VÀO APP
    @Override
    protected void onResume() {
        super.onResume();
        if (lightSensor != null) {
            sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
        if (proximitySensor != null) {
            sensorManager.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    // HỦY ĐĂNG KÝ KHI THOÁT APP ĐỂ TIẾT KIỆM PIN
    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    // Hàm phụ trợ cập nhật danh sách việc (tách ra cho code gọn hơn)
    private void updateTaskListUI(DataSnapshot taskSnap, String status) {
        String name = String.valueOf(taskSnap.child("name").getValue());
        String reward = String.valueOf(taskSnap.child("reward").getValue());

        View taskView = LayoutInflater.from(KidMainActivity.this).inflate(R.layout.item_task_kid, null);
        TextView tvName = taskView.findViewById(R.id.tvKidTaskName);
        TextView tvReward = taskView.findViewById(R.id.tvKidTaskReward);
        Button btnDone = taskView.findViewById(R.id.btnDone);

        tvName.setText(name);
        tvReward.setText("Thưởng: " + reward + " Sao");

        if ("assigned".equals(status)) {
            btnDone.setText("XONG!");
            btnDone.setEnabled(true);
            btnDone.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#4CAF50")));
            btnDone.setOnClickListener(v -> {
                taskSnap.getRef().child("status").setValue("pending");
                Toast.makeText(KidMainActivity.this, "Đã gửi! Chờ bố mẹ duyệt nhé!", Toast.LENGTH_SHORT).show();
            });
        } else {
            btnDone.setText("⏳ CHỜ DUYỆT");
            btnDone.setEnabled(false);
            btnDone.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#95A5A6")));
        }
        layoutKidTasks.addView(taskView);
    }
}
