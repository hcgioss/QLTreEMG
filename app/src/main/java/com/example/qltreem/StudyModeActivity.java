package com.example.qltreem;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class StudyModeActivity extends AppCompatActivity {

    TextView tvTimer;

    boolean isFinished = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_study_mode);

        tvTimer = findViewById(R.id.tvTimer);

        // chặn nút back
        getOnBackPressedDispatcher().addCallback(
                this,
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {

                        if (!isFinished) {

                            Toast.makeText(
                                    StudyModeActivity.this,
                                    "Phải học xong mới được thoát!",
                                    Toast.LENGTH_SHORT
                            ).show();

                        } else {

                            finish();
                        }
                    }
                }
        );

        // 90 phút
        new CountDownTimer(1 * 60 * 1000L,  1000) {

            @Override
            public void onTick(long millisUntilFinished) {

                int minutes = (int) (millisUntilFinished / 1000) / 60;
                int seconds = (int) (millisUntilFinished / 1000) % 60;

                tvTimer.setText(
                        String.format("%02d:%02d", minutes, seconds)
                );
            }

            @Override
            public void onFinish() {

                isFinished = true;

                tvTimer.setText("HOÀN THÀNH!");

                // cộng 50 sao
                DatabaseReference ref = FirebaseDatabase
                        .getInstance()
                        .getReference("TotalStars");

                ref.get().addOnSuccessListener(snapshot -> {

                    int currentStars = 0;

                    if (snapshot.exists()) {
                        currentStars = snapshot.getValue(Integer.class);
                    }

                    ref.setValue(currentStars + 50);

                    Toast.makeText(
                            StudyModeActivity.this,
                            "Đã thưởng 50 sao!",
                            Toast.LENGTH_LONG
                    ).show();

                    finish();
                });
            }
        }.start();
    }

    // cảnh báo khi thoát app
    @Override
    protected void onPause() {
        super.onPause();

        if (!isFinished) {

            Toast.makeText(
                    this,
                    "Không được thoát khi đang học!",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }
}