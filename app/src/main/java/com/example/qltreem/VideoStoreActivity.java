package com.example.qltreem;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class VideoStoreActivity extends AppCompatActivity {

    private TextView tvStoreStars;
    private CardView cardVideo1, cardVideo2;

    // Khai báo biến Firebase
    private DatabaseReference mDatabase;
    private int currentStars = 0; // Số sao sẽ được tự động tải từ mạng về thay vì gán cứng 150

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_store);

        tvStoreStars = findViewById(R.id.tvStoreStars);
        cardVideo1 = findViewById(R.id.cardVideo1);
        cardVideo2 = findViewById(R.id.cardVideo2);

        // KẾT NỐI FIREBASE - Trỏ thẳng vào Ví tiền chung
        mDatabase = FirebaseDatabase.getInstance().getReference().child("TotalStars");

        // 1. LẮNG NGHE SỐ DƯ TỪ MÂY
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Lấy số dư hiện tại trên mạng về và hiển thị
                    currentStars = snapshot.getValue(Integer.class);
                    tvStoreStars.setText("⭐ " + currentStars);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(VideoStoreActivity.this, "Lỗi mạng!", Toast.LENGTH_SHORT).show();
            }
        });

        // 2. Xử lý khi bé bấm vào Video 1 (Giá 50 Sao)
        cardVideo1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAndBuyVideo("Hoạt hình Gấu", 50);
            }
        });

        // 3. Xử lý khi bé bấm vào Video 2 (Giá 200 Sao)
        cardVideo2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAndBuyVideo("Siêu nhân nhện", 200);
            }
        });
    }

    // Hàm kiểm tra tiền và hỏi xác nhận mua
    private void checkAndBuyVideo(String videoName, int price) {
        if (currentStars >= price) {
            // ĐỦ TIỀN: Hiện hộp thoại hỏi bé có chắc chắn mua không
            new AlertDialog.Builder(this)
                    .setTitle("Mở khóa Video")
                    .setMessage("Bé có muốn dùng " + price + " Sao để xem phim '" + videoName + "' không?")
                    .setPositiveButton("Đồng ý", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            // PHÉP MÀU: Tính số sao mới và ghi thẳng lên Firebase
                            int newStars = currentStars - price;
                            mDatabase.setValue(newStars);

                            Toast.makeText(VideoStoreActivity.this, "Đang mở phim! Chúc bé xem vui vẻ", Toast.LENGTH_LONG).show();
                        }
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        } else {
            // KHÔNG ĐỦ TIỀN: Báo lỗi
            Toast.makeText(this, "Bé chưa đủ Sao rồi! Hãy làm thêm nhiệm vụ nhé.", Toast.LENGTH_LONG).show();
        }
    }
}