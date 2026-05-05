package com.example.qltreem;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType; // IMPORT ĐỂ DÙNG BÀN PHÍM SỐ
import android.view.View;
import android.widget.Button;
import android.widget.EditText; // IMPORT ĐỂ NHẬP LIỆU
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

// IMPORT THƯ VIỆN FIREBASE
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ParentMainActivity extends AppCompatActivity {

    // THÊM BIẾN btnSetTimeLimit
    private TextView tvTotalStars, tvTaskCount;
    private Button btnGoToAddTask, btnViewTasks, btnViewRewards, btnSetTimeLimit, btnBack;

    private DatabaseReference mDatabase;
    private DatabaseReference mTasksDatabase;
    private DatabaseReference mRewardRequests;
    private DatabaseReference mSessionRef; // BIẾN FIREBASE LƯU THỜI GIAN

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_main);

        // Ánh xạ View
        tvTotalStars = findViewById(R.id.tvTotalStars);
        tvTaskCount = findViewById(R.id.tvTaskCount);
        btnGoToAddTask = findViewById(R.id.btnGoToAddTask);
        btnViewTasks = findViewById(R.id.btnViewTasks);
        btnViewRewards = findViewById(R.id.btnViewRewards);
        btnSetTimeLimit = findViewById(R.id.btnSetTimeLimit); // ÁNH XẠ NÚT MỚI
        btnBack = findViewById(R.id.btnBack);

        // KẾT NỐI FIREBASE
        mDatabase = FirebaseDatabase.getInstance().getReference().child("TotalStars");
        mTasksDatabase = FirebaseDatabase.getInstance().getReference().child("Tasks");
        mRewardRequests = FirebaseDatabase.getInstance().getReference().child("RewardRequests");
        mSessionRef = FirebaseDatabase.getInstance().getReference().child("SessionTimeLimit"); // KẾT NỐI NHÁNH THỜI GIAN

        // 1 - Lấy điểm thực tế
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Integer stars = snapshot.getValue(Integer.class);
                    tvTotalStars.setText("⭐ " + stars);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ParentMainActivity.this, "Lỗi mạng!", Toast.LENGTH_SHORT).show();
            }
        });

        // 2 - Đếm số lượng nhiệm vụ đang có
        mTasksDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long count = snapshot.getChildrenCount();
                tvTaskCount.setText(String.valueOf(count));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        // =====================================================================
        // SỰ KIỆN MỚI: CÀI ĐẶT THỜI GIAN SỬ DỤNG CHO BÉ
        // =====================================================================
        btnSetTimeLimit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Tạo một EditText để Bố mẹ nhập số phút
                final EditText input = new EditText(ParentMainActivity.this);
                input.setInputType(InputType.TYPE_CLASS_NUMBER); // Chỉ cho phép nhập số
                input.setHint("Nhập số phút (Ví dụ: 15)");

                new AlertDialog.Builder(ParentMainActivity.this)
                        .setTitle("Cài đặt giới hạn thời gian")
                        .setMessage("Khi hết số phút này, ứng dụng của con sẽ tự động khóa.")
                        .setView(input) // Đưa EditText vào Popup
                        .setPositiveButton("Lưu cài đặt", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String timeStr = input.getText().toString();
                                if (!timeStr.isEmpty()) {
                                    int minutes = Integer.parseInt(timeStr);
                                    // Đẩy số phút lên Firebase
                                    mSessionRef.setValue(minutes);
                                    Toast.makeText(ParentMainActivity.this, "Đã lưu thời gian: " + minutes + " phút", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(ParentMainActivity.this, "Vui lòng nhập số phút!", Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                        .setNegativeButton("Hủy", null)
                        .show();
            }
        });

        // SỰ KIỆN: Xem Lịch sử đổi quà
        if (btnViewRewards != null) {
            btnViewRewards.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mRewardRequests.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            StringBuilder history = new StringBuilder();

                            // Quét tất cả các món quà con đã đổi
                            for (DataSnapshot request : snapshot.getChildren()) {
                                String name = request.child("name").getValue(String.class);
                                String price = String.valueOf(request.child("price").getValue());
                                String time = request.child("time").getValue(String.class);

                                if (time != null) {
                                    history.append("⏰ ").append(time).append("\n");
                                }
                                history.append("🎁 ").append(name).append(" (-").append(price).append(" Sao)\n\n");
                            }

                            if (history.length() == 0) {
                                history.append("Hiện con chưa đổi món quà nào.");
                            }

                            // Hiển thị Popup
                            new AlertDialog.Builder(ParentMainActivity.this)
                                    .setTitle("Lịch sử con đổi quà")
                                    .setMessage(history.toString())
                                    .setPositiveButton("Đã hiểu", null)
                                    .setNeutralButton("Đã trao & Xóa lịch sử", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            mRewardRequests.removeValue(); // Xóa sạch dữ liệu trên Firebase
                                            Toast.makeText(ParentMainActivity.this, "Đã dọn dẹp lịch sử!", Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .show();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {}
                    });
                }
            });
        }

        // Mở trang Giao việc
        btnGoToAddTask.setOnClickListener(v -> {
            Intent intent = new Intent(ParentMainActivity.this, AddTaskActivity.class);
            startActivity(intent);
        });

        // Mở trang Danh sách nhiệm vụ
        btnViewTasks.setOnClickListener(v -> {
            Intent intent = new Intent(ParentMainActivity.this, TaskListActivity.class);
            startActivity(intent);
        });

        // Quay lại Menu Chọn quyền
        btnBack.setOnClickListener(v -> finish());
    }
}