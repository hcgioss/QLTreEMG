package com.example.qltreem;

import android.os.Bundle;
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

public class VideoStoreActivity extends AppCompatActivity {

    private TextView tvStoreStars;
    private LinearLayout layoutRewardsContainer;
    private Button btnStoreBack;

    private DatabaseReference mStarsDatabase;
    private int currentTotalStars = 0; // Biến lưu Ví tiền của bé

    // TẠO MENU QUÀ TẶNG (Tên quà và Giá tiền)
    private String[] rewardNames = {"Xem 1 tập phim Hoạt hình", "Chơi iPad 30 phút", "Đi chơi Công viên", "Ăn gà rán KFC"};
    private int[] rewardPrices = {30, 50, 100, 150};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_store);

        tvStoreStars = findViewById(R.id.tvStoreStars);
        layoutRewardsContainer = findViewById(R.id.layoutRewardsContainer);
        btnStoreBack = findViewById(R.id.btnStoreBack);

        // KẾT NỐI VÀO VÍ TIỀN TRÊN FIREBASE
        mStarsDatabase = FirebaseDatabase.getInstance().getReference().child("TotalStars");

        // 1. LUÔN CẬP NHẬT SỐ DƯ
        mStarsDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    currentTotalStars = snapshot.getValue(Integer.class);
                    tvStoreStars.setText("⭐ " + currentTotalStars);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        // 2. BÀY BÁN CÁC MÓN QUÀ LÊN KỆ
        for (int i = 0; i < rewardNames.length; i++) {
            final String name = rewardNames[i];
            final int price = rewardPrices[i];

            // Bơm khuôn thẻ vào
            View rewardView = LayoutInflater.from(this).inflate(R.layout.item_reward_card, null);

            TextView tvName = rewardView.findViewById(R.id.tvRewardName);
            TextView tvPrice = rewardView.findViewById(R.id.tvRewardPrice);
            Button btnBuy = rewardView.findViewById(R.id.btnBuyReward);

            tvName.setText(name);
            tvPrice.setText("Giá: " + price + " Sao");

            // SỰ KIỆN: KHI BÉ BẤM NÚT "ĐỔI QUÀ"
            btnBuy.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Kiểm tra xem ví có đủ tiền không?
                    if (currentTotalStars >= price) {
                        // Hiện Popup hỏi lại cho chắc chắn
                        new AlertDialog.Builder(VideoStoreActivity.this)
                                .setTitle("Xác nhận đổi quà")
                                .setMessage("Con có muốn dùng " + price + " Sao để đổi lấy: " + name + " không?")
                                .setPositiveButton("Đổi luôn!", (dialog, which) -> {

                                    // BƯỚC 1: Trừ tiền ví của bé và cập nhật lên mạng
                                    int newTotal = currentTotalStars - price;
                                    mStarsDatabase.setValue(newTotal);

                                    // BƯỚC 2: TẠO "HÓA ĐƠN" LƯU LỊCH SỬ ĐỔI QUÀ ĐỂ BỐ MẸ KIỂM TRA
                                    DatabaseReference mRewardHistory = FirebaseDatabase.getInstance().getReference().child("RewardRequests");
                                    String requestId = mRewardHistory.push().getKey(); // Tạo mã hóa đơn ngẫu nhiên

                                    if (requestId != null) {
                                        mRewardHistory.child(requestId).child("name").setValue(name);
                                        mRewardHistory.child(requestId).child("price").setValue(price);
                                        mRewardHistory.child(requestId).child("status").setValue("Chờ bố mẹ trao quà");

                                        // DÒNG ĐƯỢC THÊM: Lưu thời gian đổi quà của bé
                                        String currentTime = new java.text.SimpleDateFormat("HH:mm - dd/MM/yyyy", java.util.Locale.getDefault()).format(new java.util.Date());
                                        mRewardHistory.child(requestId).child("time").setValue(currentTime);
                                    }

                                    Toast.makeText(VideoStoreActivity.this, "🎉 Chúc mừng! Con hãy báo Bố mẹ để nhận thưởng nhé!", Toast.LENGTH_LONG).show();
                                })
                                .setNegativeButton("Thôi", null)
                                .show();
                    } else {
                        // Thiếu tiền -> Báo lỗi
                        Toast.makeText(VideoStoreActivity.this, "😢 Con chưa đủ Sao rồi. Cố gắng làm thêm nhiệm vụ nhé!", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            // Nhét thẻ lên màn hình
            layoutRewardsContainer.addView(rewardView);
        }

        // Nút Quay lại
        btnStoreBack.setOnClickListener(v -> finish());
    }
}
