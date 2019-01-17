package com.example.dnt.chatapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class PhoneLoginActivity extends AppCompatActivity {
    private Button sendCodeButton, verifyButton;
    private EditText phoneNumber, verifyCode;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private FirebaseAuth mAuth;
    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);
        InitFields();
        callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                loadingBar.dismiss();
                Toast.makeText(PhoneLoginActivity.this, "Invalid phone number, please try it again", Toast.LENGTH_SHORT).show();
                verifyButton.setVisibility(View.INVISIBLE);
                verifyCode.setVisibility(View.INVISIBLE);
                phoneNumber.setVisibility(View.VISIBLE);
                sendCodeButton.setVisibility(View.VISIBLE);
            }

            @Override
            public void onCodeSent(String verificationId,
                                   PhoneAuthProvider.ForceResendingToken token) {
                mVerificationId = verificationId;
                mResendToken = token;
                loadingBar.dismiss();
                Toast.makeText(PhoneLoginActivity.this, "Code has been sent", Toast.LENGTH_SHORT).show();
                verifyButton.setVisibility(View.VISIBLE);
                verifyCode.setVisibility(View.VISIBLE);
                phoneNumber.setVisibility(View.INVISIBLE);
                sendCodeButton.setVisibility(View.INVISIBLE);
            }
        };
        sendCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String inputNumber = phoneNumber.getText().toString();
                if (TextUtils.isEmpty(inputNumber)) {
                    Toast.makeText(PhoneLoginActivity.this, "Please enter your phone number", Toast.LENGTH_SHORT).show();
                } else {
                    loadingBar.setTitle("Phone verification");
                    loadingBar.setMessage("Please wait a moment");
                    loadingBar.setCanceledOnTouchOutside(false);
                    loadingBar.show();
                    PhoneAuthProvider.getInstance().verifyPhoneNumber(
                            inputNumber,        // Phone number to verify
                            60,                 // Timeout duration
                            TimeUnit.SECONDS,   // Unit of timeout
                            PhoneLoginActivity.this,               // Activity (for callback binding)
                            callbacks);        // OnVerificationStateChangedCallbacks
                }
            }
        });

        verifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                phoneNumber.setVisibility(View.INVISIBLE);
                sendCodeButton.setVisibility(View.INVISIBLE);
                String inputCode = verifyCode.getText().toString();
                if (TextUtils.isEmpty(inputCode)) {
                    Toast.makeText(PhoneLoginActivity.this, "Enter verify code please", Toast.LENGTH_SHORT).show();
                } else {
                    loadingBar.setMessage("Please wait a moment");
                    loadingBar.setCanceledOnTouchOutside(false);
                    loadingBar.show();
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, inputCode);
                    signInWithPhoneAuthCredential(credential);
                }
            }
        });
    }

    private void InitFields() {
        sendCodeButton = findViewById(R.id.send_code_button);
        verifyButton = findViewById(R.id.verify_button);
        phoneNumber = findViewById(R.id.number_input);
        verifyCode = findViewById(R.id.verification_code_input);
        mAuth = FirebaseAuth.getInstance();
        loadingBar = new ProgressDialog(this);

    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            loadingBar.dismiss();
                            SendUserToMainActivity();
                        } else {
                            String msg = task.getException().toString();
                            Toast.makeText(PhoneLoginActivity.this, msg, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(this, MainActivity.class);
        startActivity(mainIntent);
    }
}
