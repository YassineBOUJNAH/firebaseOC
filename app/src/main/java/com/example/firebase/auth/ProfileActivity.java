package com.example.firebase.auth;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.firebase.api.UserHelper;
import com.example.firebase.base.BaseActivity;
import com.example.firebase.R;
import com.example.firebase.model.User;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;

import butterknife.BindView;
import butterknife.OnClick;

public class ProfileActivity extends BaseActivity {

    // 1 - Adding CheckBox Mentor View

    @BindView(R.id.profile_activity_check_box_is_mentor)
    CheckBox checkBoxIsMentor;



    // Creating identifier to identify REST REQUEST (Update username)

    private static final int UPDATE_USERNAME = 30;

    //FOR DATA
    // 2 - Identify each Http Request
    private static final int SIGN_OUT_TASK = 10;
    private static final int DELETE_USER_TASK = 20;




    @OnClick(R.id.profile_activity_check_box_is_mentor)

    public void onClickCheckBoxIsMentor() { this.updateUserIsMentor(); }
    //FOR DESIGN
    @BindView(R.id.profile_activity_imageview_profile)
    ImageView imageViewProfile;
    @BindView(R.id.profile_activity_edit_text_username)
    TextInputEditText textInputEditTextUsername;
    @BindView(R.id.profile_activity_text_view_email)
    TextView textViewEmail;
    @BindView(R.id.profile_activity_progress_bar)
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.configureToolbar();
        this.updateUIWhenCreating();
    }

    @Override
    public int getFragmentLayout() { return R.layout.activity_profile; }

    // --------------------
    // ACTIONS
    // --------------------
    @OnClick(R.id.profile_activity_button_update)
    public void onClickUpdateButton() { this.updateUsernameInFirebase(); }


    @OnClick(R.id.profile_activity_button_sign_out)
    public void onClickSignOutButton() { this.signOutUserFromFirebase(); }

    @OnClick(R.id.profile_activity_button_delete)
    public void onClickDeleteButton() {
        new AlertDialog.Builder(this)
                .setMessage(R.string.popup_message_confirmation_delete_account)
                .setPositiveButton(R.string.popup_message_choice_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        deleteUserFromFirebase();
                    }
                })
                .setNegativeButton(R.string.popup_message_choice_no, null)
                .show();
    }

    // --------------------
    // REST REQUESTS
    // --------------------
    // 1 - Create http requests (SignOut & Delete)

    private void signOutUserFromFirebase(){
        AuthUI.getInstance()
                .signOut(this)
                .addOnSuccessListener(this, this.updateUIAfterRESTRequestsCompleted(SIGN_OUT_TASK));
    }

    private void deleteUserFromFirebase(){
        if (this.getCurrentUser() != null) {
            UserHelper.deleteUser(this.getCurrentUser().getUid()).addOnFailureListener(this.onFailureListener());
            AuthUI.getInstance()
                    .delete(this)
                    .addOnSuccessListener(this, this.updateUIAfterRESTRequestsCompleted(DELETE_USER_TASK));

        }
    }

    private void updateUsernameInFirebase(){


        this.progressBar.setVisibility(View.VISIBLE);

        String username = this.textInputEditTextUsername.getText().toString();


        if (this.getCurrentUser() != null){

            if (!username.isEmpty() &&  !username.equals(getString(R.string.info_no_username_found))){

                UserHelper.updateUsername(username, this.getCurrentUser().getUid()).addOnFailureListener(this.onFailureListener()).addOnSuccessListener(this.updateUIAfterRESTRequestsCompleted(UPDATE_USERNAME));

            }

        }

    }

    private void updateUserIsMentor(){

        if (this.getCurrentUser() != null) {

            UserHelper.updateIsMentor(this.getCurrentUser().getUid(), this.checkBoxIsMentor.isChecked()).addOnFailureListener(this.onFailureListener());

        }

    }

    // 3 - Create OnCompleteListener called after tasks ended
    private OnSuccessListener<Void> updateUIAfterRESTRequestsCompleted(final int origin){
        return new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                switch (origin){
                    case SIGN_OUT_TASK:
                        finish();
                        break;
                    case DELETE_USER_TASK:
                        finish();
                        break;
                    case UPDATE_USERNAME:
                        progressBar.setVisibility(View.INVISIBLE);
                            break;
                    default:
                        break;
                }
            }
        };
    }
    // 6 - Arranging method that updating UI with Firestore data
    private void updateUIWhenCreating(){

        if (this.getCurrentUser() != null){

            if (this.getCurrentUser().getPhotoUrl() != null) {
                Glide.with(this)
                        .load(this.getCurrentUser().getPhotoUrl())
                        .apply(RequestOptions.circleCropTransform())
                        .into(imageViewProfile);
            }

            String email = TextUtils.isEmpty(this.getCurrentUser().getEmail()) ? getString(R.string.info_no_email_found) : this.getCurrentUser().getEmail();

            this.textViewEmail.setText(email);

            // 7 - Get additional data from Firestore (isMentor & Username)
            UserHelper.getUser(this.getCurrentUser().getUid()).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    User currentUser = documentSnapshot.toObject(User.class);
                    String username = TextUtils.isEmpty(currentUser.getUsername()) ? getString(R.string.info_no_username_found) : currentUser.getUsername();
                    checkBoxIsMentor.setChecked(currentUser.getIsMentor());
                    textInputEditTextUsername.setText(username);
                }
            });
        }
    }

}