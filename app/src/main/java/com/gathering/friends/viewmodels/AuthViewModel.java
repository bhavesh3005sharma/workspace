package com.gathering.friends.viewmodels;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.gathering.friends.database.Prefs;
import com.gathering.friends.models.LoginRequest;
import com.gathering.friends.models.User;
import com.gathering.friends.util.Constants;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

public class AuthViewModel extends ViewModel {
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();

    public LiveData<String> registerUser(final User user, String password) {
        final MutableLiveData<String> mutableLiveData = new MutableLiveData<>();

        // check the uniqueness of the username
        Query query = FirebaseDatabase.getInstance().getReference().child("users").orderByChild("username").equalTo(user.getUsername());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // user with this username already exists
                    mutableLiveData.setValue(Constants.DUPLICATE);
                } else {
                    mAuth.createUserWithEmailAndPassword(user.getEmail(), password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // set uid of the user
                                user.setUid(mAuth.getCurrentUser().getUid());

                                // send verification email
                                mAuth.getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        mAuth.signOut();
                                        if (task.isSuccessful())
                                            mutableLiveData.setValue(Constants.SUCCESS);
                                        else
                                            mutableLiveData.setValue(task.getException().getMessage());
                                    }
                                });

                                // register user to our db
                                registerUserToDb(user);
                            } else
                                mutableLiveData.setValue(task.getException().getMessage());
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                mutableLiveData.setValue(error.toString());
            }
        });

        return mutableLiveData;
    }

    private void registerUserToDb(User user) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("users");
        databaseReference.child(user.getUsername()).setValue(user);
    }

    public LiveData<String> isCorrectUser(Context context, final LoginRequest loginRequest) {
        final MutableLiveData<String> mutableLiveData = new MutableLiveData<>();

        mAuth.signInWithEmailAndPassword(loginRequest.getEmail(), loginRequest.getPassword()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    if (mAuth.getCurrentUser().isEmailVerified()) {
                        mutableLiveData.setValue(Constants.SUCCESS);
                        loadAndSaveUserData(context, loginRequest.getEmail());
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

    private void loadAndSaveUserData(Context context, String email) {
        Query query = FirebaseDatabase.getInstance().getReference().child("users").orderByChild("email").equalTo(email);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    User user = getUserModelFromDS(ds);
                    // save user to Prefs
                    Prefs.setUserLoggedIn(context, true);
                    Prefs.setUserData(context, user);
                    Log.d("TAG", Prefs.isUserLoggedIn(context) + " " + Prefs.getUser(context).getEmail() + " logged in");

                    // get FCM token and save to remote database to send push notifications to user.
                    getFCMToken(context, user.getUsername());
                    break;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private User getUserModelFromDS(DataSnapshot ds) {
        return new User((String) ds.child("username").getValue(), (String) ds.child("email").getValue(),
                (String) ds.child("displayName").getValue(), (String) ds.child("profileUri").getValue(), (String) ds.child("uid").getValue());
    }

    private void getFCMToken(Context context, String username) {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.w("FCM_TOKEN Task : ", "Fetching FCM registration token failed", task.getException());
                            return;
                        }

                        // Get new FCM registration token
                        String token = task.getResult();

                        // Log and send FCM Token to server
                        Log.d("FCM_TOKEN", token);
                        saveTokenToServer(context, token, username);
                    }
                });
    }

    private void saveTokenToServer(Context context, String token, String username) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users");
        databaseReference.child(username).child("fcm_token").setValue(token).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    // successfully saved
                    Prefs.getUser(context).setFcm_token(token);
                } else {
                    // error
                }
            }
        });
    }
}
