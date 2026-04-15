package com.example.qltreem;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private EditText edtEmail, edtPassword;
    private Button btnLogin;
    private TextView tvRegister; // ĐÃ THÊM: Biến cho dòng chữ Đăng ký

    // Khai báo biến xác thực của Firebase
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Ánh xạ View
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister); // ĐÃ THÊM: Ánh xạ dòng chữ

        // Khởi tạo Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // 1. SỰ KIỆN: Bấm vào dòng chữ "Đăng ký ngay"
        tvRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Mở trang tạo tài khoản mới
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        // 2. SỰ KIỆN: Xử lý khi bấm nút Đăng Nhập
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = edtEmail.getText().toString().trim();
                String password = edtPassword.getText().toString().trim();

                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Vui lòng nhập đủ Email và Mật khẩu!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Tạm thời đổi chữ trên nút để báo đang xử lý
                btnLogin.setText("ĐANG KIỂM TRA...");
                btnLogin.setEnabled(false);

                // PHÉP MÀU: Nhờ Firebase kiểm tra tài khoản
                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // ĐĂNG NHẬP ĐÚNG -> Chuyển sang màn hình chọn Quyền (MainActivity)
                                    Toast.makeText(LoginActivity.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                    startActivity(intent);
                                    finish(); // Đóng trang Login lại
                                } else {
                                    // ĐĂNG NHẬP SAI -> Báo lỗi
                                    Toast.makeText(LoginActivity.this, "Sai Email hoặc Mật khẩu!", Toast.LENGTH_LONG).show();
                                    // Khôi phục lại nút
                                    btnLogin.setText("Đăng nhập");
                                    btnLogin.setEnabled(true);
                                }
                            }
                        });
            }
        });
    }
}