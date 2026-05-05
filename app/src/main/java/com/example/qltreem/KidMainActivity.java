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
import android.os.CountDownTimer; // IMPORT THÊM CHO ĐẾM NGƯỢC
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog; // IMPORT THÊM CHO HỘP THOẠI
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

    // THÊM BIẾN mSessionRef ĐỂ LẤY THỜI GIAN TỪ FIREBASE
    private DatabaseReference mDatabase, mTasksDatabase, mSessionRef;

    // KHAI BÁO BIẾN CHO SENSORS
    private SensorManager sensorManager;
    private Sensor lightSensor;
    private Sensor proximitySensor;

    // KHAI BÁO BIẾN CHO TÍNH NĂNG GIỚI HẠN THỜI GIAN
    private CountDownTimer sessionTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kid_main);

        // Ánh xạ View
        tvKidStars = findViewById(R.id.tvKidStars);
        layoutKidTasks = findViewById(R.id.layoutKidTasks);
        btnKidBack = findViewById(R.id.btnKidBack);
        btnGoToStore = findViewById(R.id.btnGoToStore);

        // 1. KẾT NỐI FIREBASE
        mDatabase = FirebaseDatabase.getInstance().getReference().child("TotalStars");
        mTasksDatabase = FirebaseDatabase.getInstance().getReference().child("Tasks");
        // KẾT NỐI NHÁNH CẤU HÌNH THỜI GIAN
        mSessionRef = FirebaseDatabase.getInstance().getReference().child("SessionTimeLimit");

        // =========================================================================
        // TÍNH NĂNG MỚI: LẤY THỜI GIAN PHỤ HUYNH CÀI ĐẶT TỪ FIREBASE (TÍNH BẰNG PHÚT)
        // =========================================================================
        mSessionRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long timeInMillis = 15 * 1000; // Mặc định là 15 phút nếu Bố mẹ chưa cài
                if (snapshot.exists()) {
                    Integer minutes = snapshot.getValue(Integer.class);
                    if (minutes != null) {
                        timeInMillis = minutes* 1000L; // Đổi từ phút sang mili-giây
                    }
                }
                startSessionTimer(timeInMillis); // Gọi hàm bắt đầu đếm ngược
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Lỗi mạng thì cho mặc định 15 phút
                startSessionTimer(15 * 60 * 1000);
            }
        });

        // 2. LOAD SỐ SAO (Giữ nguyên)
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

        // 3. LOAD DANH SÁCH VIỆC (Giữ nguyên)
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

        // 4. THIẾT LẬP CẢM BIẾN (Giữ nguyên)
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
            proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        }

        // Báo lỗi nếu máy không có cảm biến
        if (lightSensor == null || proximitySensor == null) {
            Toast.makeText(this, "Cảnh báo: Máy này thiếu cảm biến phần cứng!", Toast.LENGTH_SHORT).show();
        }

        // Các nút chuyển trang
        btnGoToStore.setOnClickListener(v -> startActivity(new Intent(this, VideoStoreActivity.class)));
        btnKidBack.setOnClickListener(v -> finish());
    }

    // =========================================================================
    // HÀM XỬ LÝ ĐẾM NGƯỢC GIỚI HẠN THỜI GIAN
    // =========================================================================
    private void startSessionTimer(long durationInMillis) {
        long minutes = durationInMillis / (60 * 1000);
        Toast.makeText(this, "Phiên sử dụng của con là " + minutes + " phút nhé!", Toast.LENGTH_LONG).show();

        sessionTimer = new CountDownTimer(durationInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                // Không cần làm gì ở đây
            }

            @Override
            public void onFinish() {
                // Khi hết giờ -> Khóa ứng dụng, bung Dialog báo lỗi
                new AlertDialog.Builder(KidMainActivity.this)
                        .setTitle("Hết giờ rồi con ơi! ⏰")
                        .setMessage("Phiên sử dụng đã kết thúc để bảo vệ mắt cho con. Hãy nhờ Bố mẹ thiết lập lại thời gian nếu muốn dùng tiếp nhé!")
                        .setCancelable(false) // Không cho bấm ra ngoài để thoát Popup
                        .setPositiveButton("Dạ vâng", (dialog, which) -> {
                            finish(); // Đá văng ra màn hình chọn vai trò
                        })
                        .show();
            }
        }.start();
    }

    // NHỚ HỦY ĐẾM NGƯỢC KHI THOÁT APP ĐỂ KHÔNG LỖI BỘ NHỚ
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (sessionTimer != null) {
            sessionTimer.cancel();
        }
    }
    // =========================================================================

    // HÀM XỬ LÝ KHI DỮ LIỆU CẢM BIẾN THAY ĐỔI (Giữ nguyên)
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
            float lightLevel = event.values[0];
            if (lightLevel < 10) {
                Toast.makeText(this, "🌙 Trời tối quá! Con hãy bật đèn để bảo vệ mắt nhé!", Toast.LENGTH_SHORT).show();
            }
        }

        if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
            float distance = event.values[0];
            if (distance < proximitySensor.getMaximumRange()) {
                Toast.makeText(this, "🚫 Con đang để máy quá gần mắt! Hãy để xa ra nào!", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

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

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    // Hàm phụ trợ cập nhật danh sách việc (Giữ nguyên)
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