package com.gathering.friends.viewmodels;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.gathering.friends.models.LoginRequest;
import com.gathering.friends.util.Constants;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class AuthViewModel extends ViewModel {
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();

    public LiveData<String> registerUser(final LoginRequest loginRequest) {
        final MutableLiveData<String> mutableLiveData = new MutableLiveData<>();

        mAuth.createUserWithEmailAndPassword(loginRequest.getEmail(), loginRequest.getPassword()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    mAuth.getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            mAuth.signOut();
                            if (task.isSuccessful()) {
                                mutableLiveData.setValue(Constants.SUCCESS);
                            } else
                                mutableLiveData.setValue(task.getException().getMessage());
                        }
                    });

                } else
                    mutableLiveData.setValue(task.getException().getMessage());
            }
        });

        return mutableLiveData;
    }

    public LiveData<String> isCorrectUser(final LoginRequest loginRequest) {
        final MutableLiveData<String> mutableLiveData = new MutableLiveData<>();

        mAuth.signInWithEmailAndPassword(loginRequest.getEmail(), loginRequest.getPassword()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    if (mAuth.getCurrentUser().isEmailVerified()) {
                        mutableLiveData.setValue(Constants.SUCCESS);
                    } else {
                        mutableLiveData.setValue("User not Verified\n  check Email");
                    }
                } else {
                    mutableLiveData.setValue(task.getException().getMessage());
                }
            }
        });

        return mutableLiveData;
    }

}
