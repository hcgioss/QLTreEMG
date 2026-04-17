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
    private DatabaseReference mStarsDatabase;

    private int currentTotalStars = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_list);

        layoutTaskContainer = findViewById(R.id.layoutTaskContainer);
        btnBack = findViewById(R.id.btnBack);

        mTasksDatabase = FirebaseDatabase.getInstance().getReference().child("Tasks");
        mStarsDatabase = FirebaseDatabase.getInstance().getReference().child("TotalStars");

        mStarsDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    currentTotalStars = snapshot.getValue(Integer.class);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        mTasksDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                layoutTaskContainer.removeAllViews();

                for (DataSnapshot taskSnapshot : snapshot.getChildren()) {
                    try {
                        String name = String.valueOf(taskSnapshot.child("name").getValue());
                        String rewardStr = String.valueOf(taskSnapshot.child("reward").getValue());
                        String status = String.valueOf(taskSnapshot.child("status").getValue());

                        View taskView = LayoutInflater.from(TaskListActivity.this).inflate(R.layout.item_task_card, null);

                        TextView tvName = taskView.findViewById(R.id.tvTaskName);
                        TextView tvReward = taskView.findViewById(R.id.tvTaskReward);
                        TextView tvStatus = taskView.findViewById(R.id.tvTaskStatus);

                        // Ánh xạ cụm nút
                        LinearLayout layoutActionButtons = taskView.findViewById(R.id.layoutActionButtons);
                        Button btnApprove = taskView.findViewById(R.id.btnApprove);
                        Button btnReject = taskView.findViewById(R.id.btnReject);

                        tvName.setText(name);
                        tvReward.setText("⭐ " + rewardStr);

                        // XỬ LÝ HIỂN THỊ
                        if ("assigned".equals(status)) {
                            tvStatus.setText("Trạng thái: Đang chờ bé làm");
                            tvStatus.setTextColor(Color.parseColor("#E67E22"));
                            layoutActionButtons.setVisibility(View.GONE); // Ẩn nút

                        } else if ("pending".equals(status)) {
                            tvStatus.setText("Trạng thái: Bé báo xong. Hãy kiểm tra!");
                            tvStatus.setTextColor(Color.parseColor("#E74C3C"));
                            layoutActionButtons.setVisibility(View.VISIBLE); // HIỆN CỤM 2 NÚT

                        } else if ("completed".equals(status)) {
                            tvStatus.setText("Trạng thái: Đã duyệt & Cộng sao");
                            tvStatus.setTextColor(Color.parseColor("#27AE60"));
                            layoutActionButtons.setVisibility(View.GONE); // Ẩn nút
                        }

                        // SỰ KIỆN 1: BẤM DUYỆT (TẶNG SAO)
                        btnApprove.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                int rewardValue = Integer.parseInt(rewardStr);
                                int newTotal = currentTotalStars + rewardValue;
                                mStarsDatabase.setValue(newTotal);
                                taskSnapshot.getRef().child("status").setValue("completed");
                                Toast.makeText(TaskListActivity.this, "Đã cộng " + rewardValue + " Sao cho bé!", Toast.LENGTH_SHORT).show();
                            }
                        });

                        // SỰ KIỆN 2: BẤM TỪ CHỐI (BẮT LÀM LẠI)
                        btnReject.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                // Đẩy trạng thái về lại ban đầu
                                taskSnapshot.getRef().child("status").setValue("assigned");
                                Toast.makeText(TaskListActivity.this, "Đã yêu cầu bé làm lại!", Toast.LENGTH_SHORT).show();
                            }
                        });

                        layoutTaskContainer.addView(taskView);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        btnBack.setOnClickListener(v -> finish());
    }
}
