package com.example.otra.firebase;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private GoogleApiClient googleApiClient;

    private EditText eCorreo, eContrasena;
    private SignInButton btnSignInGoogle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        eCorreo = findViewById(R.id.eCorreo);
        eContrasena = findViewById(R.id.eContrasena);
        btnSignInGoogle = findViewById(R.id.btnSignInGoogle);

        btnSignInGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
                startActivityForResult(i,1); // 1 para google
            }
        });

        inicializar(); //siempre


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1){
            GoogleSignInResult googleSignInResult = Auth.GoogleSignInApi.
                    getSignInResultFromIntent(data);
            signInGoogle(googleSignInResult);
        }
    }

    private void signInGoogle(GoogleSignInResult googleSignInResult) {
        if (googleSignInResult.isSuccess()){
            AuthCredential authCredential = GoogleAuthProvider.getCredential(
                    googleSignInResult.getSignInAccount().getIdToken(),null);
        }
    }

    private void inicializar(){
        firebaseAuth = FirebaseAuth.getInstance();
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                if (firebaseUser != null){
                    Log.d("usuario logueado: ", firebaseUser.getEmail());
                }else {
                    Log.d("usuario logueado", "no hay");
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

    public void crearCuentaClicked(View view){
        //validaciones ya realizadas
        crearCuentaFirebase(eCorreo.getText().toString(),eContrasena.getText().toString());
    }

    public void crearCuentaFirebase(String correo, String contrasena){
        firebaseAuth.createUserWithEmailAndPassword(correo,contrasena).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    Toast.makeText(LoginActivity.this, "cuenta creada", Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(LoginActivity.this, "error al crear cuenta", Toast.LENGTH_SHORT).show();
                    Log.d("error",""+task.getException());
                }
            }
        });
    }

    public void iniciarSesionClicked(View view) {
        iniciarSesionFirebase(eCorreo.getText().toString(),eContrasena.getText().toString());
    }

    private void iniciarSesionFirebase(String correo, String contrasena) {
        firebaseAuth.signInWithEmailAndPassword(correo,contrasena).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    Intent i = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(i);

                }else {
                    Toast.makeText(LoginActivity.this, "error al iniciar sesion", Toast.LENGTH_SHORT).show();

                }
            }
        });
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
