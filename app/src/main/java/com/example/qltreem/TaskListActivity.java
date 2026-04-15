package com.example.qltreem;

import android.graphics.Color;
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

public class TaskListActivity extends AppCompatActivity {

    private LinearLayout layoutTaskContainer;
    private Button btnBack;
    private DatabaseReference mTasksDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_list);

        layoutTaskContainer = findViewById(R.id.layoutTaskContainer);
        btnBack = findViewById(R.id.btnBack);

        // Kết nối vào thư mục Tasks trên Firebase
        mTasksDatabase = FirebaseDatabase.getInstance().getReference().child("Tasks");

        // Lắng nghe dữ liệu tải về
        mTasksDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Xóa danh sách cũ đi để vẽ lại
                layoutTaskContainer.removeAllViews();

                for (DataSnapshot taskSnapshot : snapshot.getChildren()) {
                    try {
                        // 1. LẤY DỮ LIỆU SIÊU AN TOÀN (Bất chấp là Số hay Chữ)
                        String name = "Nhiệm vụ không tên";
                        if (taskSnapshot.child("name").getValue() != null) {
                            name = String.valueOf(taskSnapshot.child("name").getValue());
                        }

                        String reward = "0";
                        if (taskSnapshot.child("reward").getValue() != null) {
                            reward = String.valueOf(taskSnapshot.child("reward").getValue());
                        }

                        String status = "assigned";
                        if (taskSnapshot.child("status").getValue() != null) {
                            status = String.valueOf(taskSnapshot.child("status").getValue());
                        }

                        // 2. Bơm khuôn giao diện thẻ vào
                        View taskView = LayoutInflater.from(TaskListActivity.this).inflate(R.layout.item_task_card, null);

                        // 3. Ánh xạ các chữ bên trong thẻ
                        TextView tvName = taskView.findViewById(R.id.tvTaskName);
                        TextView tvReward = taskView.findViewById(R.id.tvTaskReward);
                        TextView tvStatus = taskView.findViewById(R.id.tvTaskStatus);

                        // 4. Đổ dữ liệu vào thẻ
                        tvName.setText(name);
                        tvReward.setText("⭐ " + reward);

                        if ("assigned".equals(status)) {
                            tvStatus.setText("Trạng thái: Chưa làm");
                            tvStatus.setTextColor(Color.parseColor("#E67E22")); // Màu cam
                        } else {
                            tvStatus.setText("Trạng thái: Đã hoàn thành");
                            tvStatus.setTextColor(Color.parseColor("#27AE60")); // Màu xanh lá
                        }

                        // 5. Nhét thẻ vào màn hình
                        layoutTaskContainer.addView(taskView);

                    } catch (Exception e) {
                        // Nếu có lỗi ở 1 nhiệm vụ nào đó, bỏ qua để KHÔNG BỊ CRASH APP
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(TaskListActivity.this, "Lỗi mạng!", Toast.LENGTH_SHORT).show();
            }
        });

        // Nút Đóng
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}