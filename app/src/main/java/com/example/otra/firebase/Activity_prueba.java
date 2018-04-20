package com.example.otra.firebase;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.renderscript.ScriptGroup;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.otra.firebase.modelo.Usuarios;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class Activity_prueba extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private GoogleApiClient googleApiClient;

    private EditText eNombre, eEdad, eTelefono;
    private ListView listView;
    private ImageView iFoto;
    private ArrayAdapter listAdapter;
    private ArrayList<String> listNombres;
    private ArrayList<Usuarios> listUsuarios;

    private DatabaseReference databaseReference;

    private Bitmap bitmap;
    private String urlFoto;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prueba);

        FirebaseDatabase.getInstance(); // para que actualice la informacion cuando se conecte a internet
        databaseReference = FirebaseDatabase.getInstance().getReference(); // estoy aqui fir-2c7fa
        eNombre = findViewById(R.id.eNombre);
        eEdad = findViewById(R.id.eEdad);
        eTelefono = findViewById(R.id.eTelefono);
        listView = findViewById(R.id.listView);
        iFoto = findViewById(R.id.iFoto);

        listNombres = new ArrayList<>();
        listUsuarios = new ArrayList<>();
        inicializar();
        final UsuarioAdapter usuarioAdapter = new UsuarioAdapter(this, listUsuarios);

      /*  listAdapter = new ArrayAdapter(this,
                android.R.layout.simple_list_item_1,
                listNombres);

        listView.setAdapter(listAdapter);*/

        listView.setAdapter(usuarioAdapter);

        databaseReference.child("usuarios").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                listNombres.clear();
                listUsuarios.clear();
                if (dataSnapshot.exists()){
                    for (DataSnapshot snapshot:dataSnapshot.getChildren()){
                        Log.d("data",snapshot.toString());
                        Usuarios usuarios = snapshot.getValue(Usuarios.class);
                        listNombres.add(usuarios.getNombre());
                        listUsuarios.add(usuarios);
                    }
                }
                //listAdapter.notifyDataSetChanged();
                usuarioAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long l) { //elimina usuarios
                String uid = listUsuarios.get(position).getId();
                databaseReference.child("usuarios").child(uid).removeValue();
                listNombres.remove(position);
                listUsuarios.remove(position);
                return false;
            }
        });
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

    public void onClickButtonExit(View view) {
        firebaseAuth.signOut();
        if (Auth.GoogleSignInApi != null) {
            Auth.GoogleSignInApi.signOut(googleApiClient).setResultCallback(new ResultCallback<Status>() {
                @Override
                public void onResult(@NonNull Status status) {
                    if (status.isSuccess()) {
                        Toast.makeText(Activity_prueba.this, "El ususario ha cerrado sesión2", Toast.LENGTH_SHORT).show();
                        goLogin();
                    } else {
                        Toast.makeText(Activity_prueba.this, "error cerrando sesion con google", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        if (LoginManager.getInstance() != null){
            LoginManager.getInstance().logOut();
        }
    }

    private void goLogin() {
        Intent i = new Intent(Activity_prueba.this, LoginActivity.class);
        startActivity(i);
        finish();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    public void fotoClicked(View view){
        Intent fotoIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        fotoIntent.setType("image/*");
        startActivityForResult(fotoIntent,1234);
     }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1234 && resultCode == RESULT_OK){
            if (data == null){
                Toast.makeText(this, "ERROR CARGANDO FOTO", Toast.LENGTH_SHORT).show();
            }else {
                Uri imagen = data.getData();

                try {
                    InputStream is = getContentResolver().openInputStream(imagen);
                    BufferedInputStream bis = new BufferedInputStream(is);
                    bitmap = BitmapFactory.decodeStream(bis);

                    iFoto.setImageBitmap(bitmap);
                }catch (FileNotFoundException e){
                    e.printStackTrace();
                }
            }
        }
    }

    class UsuarioAdapter extends ArrayAdapter<Usuarios>{

        public UsuarioAdapter(@NonNull Context context, ArrayList<Usuarios> data) {
            super(context, R.layout.list_item, data);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

            Usuarios usuarios = getItem(position);

            LayoutInflater inflater = LayoutInflater.from(getContext());
            View item = inflater.inflate(R.layout.list_item, null);

            TextView nombre = item.findViewById(R.id.tNombre);
            TextView telefono = item.findViewById(R.id.tTelefono);
            TextView edad = item.findViewById(R.id.tEdad);

            nombre.setText(usuarios.getNombre());
            telefono.setText(usuarios.getTelefono());
            edad.setText(String.valueOf(usuarios.getEdad()));

            CircleImageView iFoto = item.findViewById(R.id.iFoto);
            Picasso.get().load(usuarios.getFoto()).into(iFoto);

            return item;
        }
    }

    public void onClickButton(View view) {
     /*    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        Usuarios usuarios = new Usuarios(firebaseUser.getUid(),
                firebaseUser.getDisplayName(),
                firebaseUser.getPhoneNumber(),
                0); */


        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
        StorageReference storageReference = firebaseStorage.getReference();

        ByteArrayOutputStream baos = new ByteArrayOutputStream(); // comprimir foto
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,baos);
        byte[] data = baos.toByteArray();

        storageReference.child("usuariosFotos").child(databaseReference.push().getKey())
        .putBytes(data).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                urlFoto = taskSnapshot.getDownloadUrl().toString();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("error",e.getMessage().toString());
            }
        });

        Usuarios usuarios = new Usuarios(databaseReference.push().getKey(),
                eNombre.getText().toString(),
                eTelefono.getText().toString(),
                urlFoto,
                Integer.valueOf(eEdad.getText().toString()));

        databaseReference.child("usuarios").child(usuarios.getId()).setValue(usuarios).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    Log.d("entre1","ok");
                }else {
                    Log.d("entre2","ok");
                    Log.d("save", task.getException().toString());
                }
            }
        });
        Toast.makeText(Activity_prueba.this,"almacenar",Toast.LENGTH_SHORT).show();
    }
}
