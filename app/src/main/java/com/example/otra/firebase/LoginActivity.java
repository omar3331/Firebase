package com.example.otra.firebase;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.otra.firebase.modelo.Usuarios;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
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
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class LoginActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private GoogleApiClient googleApiClient;

    private EditText eCorreo, eContrasena;
    private SignInButton btnSignInGoogle;

    private CallbackManager callbackManager;
    private LoginButton loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        eCorreo = findViewById(R.id.eCorreo);
        eContrasena = findViewById(R.id.eContrasena);
        btnSignInGoogle = findViewById(R.id.btnSignInGoogle);
        loginButton = findViewById(R.id.login_button);
        loginButton.setReadPermissions("email","public_profile");


        callbackManager = CallbackManager.Factory.create();
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {

                Log.d("Login Facebook","OK");
                signInFacebook(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d("Login Facebook","Cancelado");
            }

            @Override
            public void onError(FacebookException error) {
                Log.d("Login Facebook","ERROR");
                error.printStackTrace();
            }
        });

        btnSignInGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
                startActivityForResult(i,1); // 1 para google
            }
        });

        getKeyHash();
        inicializar(); //siempre


    }

    private void signInFacebook(AccessToken accessToken) {
        AuthCredential authCredential = FacebookAuthProvider.getCredential(accessToken.getToken());
        firebaseAuth.signInWithCredential(authCredential).addOnCompleteListener(this,
                new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            goMainActivity();
                        }else {
                            Toast.makeText(LoginActivity.this,"Fallo autenticacion con face",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
 //************************* Generar key hash***********************************
    private void getKeyHash() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    "com.example.otra.firebase",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {

        } catch (NoSuchAlgorithmException e) {

        }

    }

    //*********************************************************************************************

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1){
            GoogleSignInResult googleSignInResult = Auth.GoogleSignInApi.
                    getSignInResultFromIntent(data);
            signInGoogle(googleSignInResult);
        } else {
            callbackManager.onActivityResult(requestCode,resultCode,data);
        }
    }

    private void signInGoogle(GoogleSignInResult googleSignInResult) {
        if (googleSignInResult.isSuccess()){
            AuthCredential authCredential = GoogleAuthProvider.getCredential(
                    googleSignInResult.getSignInAccount().getIdToken(),null);

            firebaseAuth.signInWithCredential(authCredential).
                    addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    eCorreo.setText(firebaseAuth.getInstance().toString());
                    if (task.isSuccessful()){
                        goMainActivity();
                    }else {
                        Toast.makeText(LoginActivity.this, "error inicio de sesion",Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void inicializar(){
        firebaseAuth = FirebaseAuth.getInstance();
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                if (firebaseUser != null){
                    Log.d("FirebaseUser","usuario logueado: "+ firebaseUser.getEmail());
                    Toast.makeText(LoginActivity.this, "usuario logueado1", Toast.LENGTH_SHORT).show();
                }else {
                    Log.d("FirebaseUser", "El ususario ha cerrado sesión");
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
        firebaseAuth.createUserWithEmailAndPassword(correo,contrasena).
                addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
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
        firebaseAuth.signInWithEmailAndPassword(correo,contrasena).
                addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    goMainActivity();
                }else {
                    Toast.makeText(LoginActivity.this, "error al iniciar sesion", Toast.LENGTH_SHORT).show();

                }
            }
        });
    }

    private void goMainActivity(){
        createCuenta();
        Intent i = new Intent(LoginActivity.this, ParqueaderosActivity.class);
        startActivity(i);
        finish();
    }

    private void createCuenta() {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        final FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        FirebaseDatabase.getInstance(); // para que actualice la informacion cuando se conecte a internet
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(); // estoy aqui fir-2c7fa

        databaseReference.child("usuarios").child(firebaseUser.getUid()).
                addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    Log.d("usuario","ok");
                }else {
                    Log.d("usuario","NO");
                    Usuarios usuarios = new Usuarios(firebaseUser.getUid(),
                            firebaseUser.getDisplayName(),
                            firebaseUser.getPhoneNumber(),
                            0);

                    databaseReference.child("usuarios").child(usuarios.getId()).setValue(usuarios);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


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
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
