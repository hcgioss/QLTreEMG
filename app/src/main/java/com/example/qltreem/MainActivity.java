package com.example.qltreem;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView; // Thêm CardView
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

// Thư viện Firebase Auth để đăng xuất
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private CardView cardParent, cardKid;
    private Button btnLogout;
    private final String CORRECT_PIN = "1234";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Ánh xạ View mới
        cardParent = findViewById(R.id.cardParent);
        cardKid = findViewById(R.id.cardKid);
        btnLogout = findViewById(R.id.btnLogout);

        // Sự kiện: Bấm vào thẻ Bố Mẹ (Vẫn bắt nhập PIN)
        cardParent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPinDialog();
            }
        });

        // Sự kiện: Bấm vào thẻ của Bé
        cardKid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, KidMainActivity.class);
                startActivity(intent);
            }
        });

        // Sự kiện: Bấm ĐĂNG XUẤT
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Yêu cầu Firebase xóa phiên đăng nhập hiện tại
                FirebaseAuth.getInstance().signOut();
                Toast.makeText(MainActivity.this, "Đã đăng xuất!", Toast.LENGTH_SHORT).show();

                // Đẩy người dùng về lại trang Login
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                // Xóa lịch sử trang để người dùng không bấm nút Back (quay lại) về trang này được nữa
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });
    }

    // Hàm hiển thị Popup nhập mã PIN (Giữ nguyên)
    private void showPinDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Xác thực bảo mật");
        builder.setMessage("Vui lòng nhập mã PIN của phụ huynh:");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        builder.setView(input);

        builder.setPositiveButton("Xác nhận", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String enteredPin = input.getText().toString();
                if (enteredPin.equals(CORRECT_PIN)) {
                    Intent intent = new Intent(MainActivity.this, ParentMainActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(MainActivity.this, "Mã PIN không chính xác!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Hủy", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }
}