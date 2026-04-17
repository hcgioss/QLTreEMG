package com.example.qltreem;


import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

// IMPORT THÊM THƯ VIỆN FIREBASE VÀ HASHMAP
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;

public class AddTaskActivity extends AppCompatActivity {

    private EditText edtTaskName, edtTaskReward;
    private Button btnSaveTask, btnCancelTask;

    // Khai báo biến Firebase
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        // Ánh xạ View
        edtTaskName = findViewById(R.id.edtTaskName);
        edtTaskReward = findViewById(R.id.edtTaskReward);
        btnSaveTask = findViewById(R.id.btnSaveTask);
        btnCancelTask = findViewById(R.id.btnCancelTask);

        // KẾT NỐI FIREBASE - Trỏ vào thư mục chứa danh sách Nhiệm vụ
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Tasks");

        // Sự kiện: Bấm nút Hủy
        btnCancelTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Đóng trang này lại, quay về trang trước đó
            }
        });

        // Sự kiện: Bấm nút Giao việc
        btnSaveTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Lấy chữ mà người dùng vừa gõ
                String taskName = edtTaskName.getText().toString().trim();
                String taskRewardStr = edtTaskReward.getText().toString().trim();

                // Kiểm tra dữ liệu trống
                if (taskName.isEmpty()) {
                    Toast.makeText(AddTaskActivity.this, "Vui lòng nhập tên công việc!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (taskRewardStr.isEmpty()) {
                    Toast.makeText(AddTaskActivity.this, "Vui lòng nhập số sao thưởng!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Chuyển chữ thành số
                int reward = Integer.parseInt(taskRewardStr);
                if (reward <= 0) {
                    Toast.makeText(AddTaskActivity.this, "Số sao thưởng phải lớn hơn 0!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // BẮT ĐẦU GÓI DỮ LIỆU ĐỂ ĐẨY LÊN FIREBASE
                // Dùng HashMap giống như một cái hộp chứa nhiều ngăn (Tên việc, Số sao, Trạng thái)
                HashMap<String, Object> taskMap = new HashMap<>();
                taskMap.put("name", taskName);
                taskMap.put("reward", reward);
                taskMap.put("status", "assigned"); // Trạng thái: "assigned" (Mới giao, chưa làm)

                // Đẩy cái hộp đó lên mây bằng lệnh push() để tự tạo ID duy nhất
                mDatabase.push().setValue(taskMap);

                // Báo thành công và đóng form
                Toast.makeText(AddTaskActivity.this, "Đã giao việc lên hệ thống: " + taskName, Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }
}
