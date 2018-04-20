package com.example.otra.firebase;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.login.Login;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private TextView tUsuario;
    private ImageView iFoto;

    private GoogleApiClient googleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tUsuario = findViewById(R.id.tUsuario);
        iFoto = findViewById(R.id.iFoto);
        inicializar();
    }

    private void inicializar(){
        firebaseAuth = FirebaseAuth.getInstance();
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                if (firebaseUser != null){
                    Log.d("usuario logueado: ", firebaseUser.getEmail());
                    Log.d("FirebaseUser","Usuario Logueado: "+firebaseUser.getDisplayName());
                    tUsuario.setText("Correo Usuario: "+firebaseUser.getEmail());
                    Picasso.get().load(firebaseUser.getPhotoUrl()).into(iFoto);

                    goSwipeTabActivity();
                }else {
                    Log.d("FirebaseUser", "El usuario ha cerrado sesion");
                    //Toast.makeText(MainActivity.this, "El ususario ha cerrado sesión2", Toast.LENGTH_SHORT).show();
                    goLogin();
                }
            }
        };
        GoogleSignInOptions gso = new GoogleSignInOptions.
                Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).
                requestIdToken(getString(R.string.default_web_client_id)).
                requestEmail().
                build();

        googleApiClient = new GoogleApiClient.Builder(this).
                enableAutoManage(this,this).
                addApi(Auth.GOOGLE_SIGN_IN_API,gso).
                build();
    }

    private void goSwipeTabActivity() {
        Intent i = new Intent(MainActivity.this, ParqueaderosActivity.class);
        startActivity(i);

    }

    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        firebaseAuth.removeAuthStateListener(authStateListener);
        googleApiClient.disconnect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        googleApiClient.stopAutoManage(this);
        googleApiClient.disconnect();
    }

    @Override
    protected void onResume() {
        super.onResume();
        googleApiClient.connect();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        googleApiClient.stopAutoManage(this);
        googleApiClient.disconnect();
    }

    public void cerrarSesionClicked(View view) {
        firebaseAuth.signOut();
        if (Auth.GoogleSignInApi != null) {
            Auth.GoogleSignInApi.signOut(googleApiClient).setResultCallback(new ResultCallback<Status>() {
                @Override
                public void onResult(@NonNull Status status) {
                    if (status.isSuccess()) {
                        Toast.makeText(MainActivity.this, "El ususario ha cerrado sesión2", Toast.LENGTH_SHORT).show();
                        goLogin();
                    } else {
                        Toast.makeText(MainActivity.this, "error cerrando sesion con google", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        if (LoginManager.getInstance() != null){
            LoginManager.getInstance().logOut();
        }
    }

    private void goLogin(){
        Intent i = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(i);
        finish();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
