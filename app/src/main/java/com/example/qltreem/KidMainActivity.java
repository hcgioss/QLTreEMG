package com.example.qltreem;

import android.content.Intent;
import android.graphics.Color;
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

public class KidMainActivity extends AppCompatActivity {

    private Button btnTask1Done, btnOpenStore;
    private TextView tvKidStars; // Thêm biến để ánh xạ cái Ví của Bé

    private DatabaseReference mDatabase;
    private int currentStars = 0; // Biến lưu số sao hiện tại lấy từ mạng về

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kid_main);

        // Ánh xạ View
        btnTask1Done = findViewById(R.id.btnTask1Done);
        btnOpenStore = findViewById(R.id.btnOpenStore);
        tvKidStars = findViewById(R.id.tvKidStars); // Nhớ phải có dòng này để app không bị crash

        // KẾT NỐI FIREBASE - Trỏ thẳng vào "TotalStars"
        mDatabase = FirebaseDatabase.getInstance().getReference().child("TotalStars");

        // 1. LẮNG NGHE SỐ DƯ TỪ MÂY (Để hiển thị lên Ví của Bé)
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    currentStars = snapshot.getValue(Integer.class);
                    tvKidStars.setText("⭐ " + currentStars);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(KidMainActivity.this, "Lỗi kết nối mạng!", Toast.LENGTH_SHORT).show();
            }
        });

        // 2. SỰ KIỆN: BÉ BẤM NÚT HOÀN THÀNH NHIỆM VỤ (+20 SAO)
        btnTask1Done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Đổi giao diện để khóa nút lại
                btnTask1Done.setText("⏳ ĐANG CHỜ BỐ MẸ DUYỆT...");
                btnTask1Done.setBackgroundColor(Color.parseColor("#95A5A6"));
                btnTask1Done.setEnabled(false);

                // PHÉP MÀU: Cộng 20 sao và đẩy lên Firebase
                int newStars = currentStars + 20;
                mDatabase.setValue(newStars);

                Toast.makeText(KidMainActivity.this, "Giỏi quá! Bé đã được cộng 20 Sao!", Toast.LENGTH_LONG).show();
            }
        });

        // 3. SỰ KIỆN: BÉ BẤM VÀO CỬA HÀNG VIDEO
        btnOpenStore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(KidMainActivity.this, VideoStoreActivity.class);
                startActivity(intent);
            }
        });
    }
}