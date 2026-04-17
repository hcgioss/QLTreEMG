package com.example.qltreem;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity {

    private EditText edtRegEmail, edtRegPassword, edtRegConfirm;
    private Button btnRegister;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        edtRegEmail = findViewById(R.id.edtRegEmail);
        edtRegPassword = findViewById(R.id.edtRegPassword);
        edtRegConfirm = findViewById(R.id.edtRegConfirm);
        btnRegister = findViewById(R.id.btnRegister);

        mAuth = FirebaseAuth.getInstance();

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = edtRegEmail.getText().toString().trim();
                String pass = edtRegPassword.getText().toString().trim();
                String confirm = edtRegConfirm.getText().toString().trim();

                // Kiểm tra lỗi cơ bản
                if (email.isEmpty() || pass.isEmpty() || confirm.isEmpty()) {
                    Toast.makeText(RegisterActivity.this, "Vui lòng nhập đủ thông tin!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (pass.length() < 6) {
                    Toast.makeText(RegisterActivity.this, "Mật khẩu phải từ 6 ký tự trở lên!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!pass.equals(confirm)) {
                    Toast.makeText(RegisterActivity.this, "Mật khẩu nhập lại không khớp!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Gửi lệnh lên Firebase để tạo tài khoản
                btnRegister.setText("ĐANG TẠO...");
                btnRegister.setEnabled(false);

                mAuth.createUserWithEmailAndPassword(email, pass)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(RegisterActivity.this, "Đăng ký thành công!", Toast.LENGTH_LONG).show();
                                    finish(); // Tự động đóng trang này, quay về màn hình Đăng nhập
                                } else {
                                    Toast.makeText(RegisterActivity.this, "Lỗi: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                    btnRegister.setText("ĐĂNG KÝ");
                                    btnRegister.setEnabled(true);
                                }
                            }
                        });
            }
        });
    }
}
