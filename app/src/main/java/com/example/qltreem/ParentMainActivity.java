package com.example.qltreem;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ParentMainActivity extends AppCompatActivity {

    private TextView tvTotalStars, tvTaskCount; // Biến đếm
    private Button btnGoToAddTask, btnViewTasks, btnBack;

    private DatabaseReference mDatabase;
    private DatabaseReference mTasksDatabase; // Biến trỏ vào danh sách việc

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_main);

        // Ánh xạ View
        tvTotalStars = findViewById(R.id.tvTotalStars);
        tvTaskCount = findViewById(R.id.tvTaskCount); // Ánh xạ biến đếm
        btnGoToAddTask = findViewById(R.id.btnGoToAddTask);
        btnViewTasks = findViewById(R.id.btnViewTasks);
        btnBack = findViewById(R.id.btnBack);

        // KẾT NỐI FIREBASE 1 - Lấy điểm thực tế
        mDatabase = FirebaseDatabase.getInstance().getReference().child("TotalStars");
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

        // KẾT NỐI FIREBASE 2 - Đếm số lượng nhiệm vụ đang có
        mTasksDatabase = FirebaseDatabase.getInstance().getReference().child("Tasks");
        mTasksDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Đếm tổng số con trong thư mục Tasks
                long count = snapshot.getChildrenCount();
                tvTaskCount.setText(String.valueOf(count));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        // SỰ KIỆN: Mở trang Giao việc
        btnGoToAddTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ParentMainActivity.this, AddTaskActivity.class);
                startActivity(intent);
            }
        });

        // ĐÃ SỬA CHỖ NÀY: Mở trang Danh sách nhiệm vụ (TaskListActivity)
        btnViewTasks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ParentMainActivity.this, TaskListActivity.class);
                startActivity(intent);
            }
        });

        // SỰ KIỆN: Nút Quay lại
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Đóng trang quản lý, trở về màn hình chọn Bố mẹ/Bé
            }
        });
    }
}