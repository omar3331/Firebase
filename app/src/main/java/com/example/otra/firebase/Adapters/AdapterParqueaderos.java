package com.example.otra.firebase.Adapters;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.otra.firebase.R;
import com.example.otra.firebase.modelo.Parqueaderos;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class AdapterParqueaderos extends RecyclerView.Adapter<AdapterParqueaderos.ParqueaderosViewHolder> {

    private ArrayList<Parqueaderos> parqueaderosList;
    private int resource;
    private Activity activity;

    public AdapterParqueaderos (ArrayList<Parqueaderos> parqueaderosList){
        this.parqueaderosList = parqueaderosList;
    }

    public AdapterParqueaderos(ArrayList<Parqueaderos> parqueaderosList, int resource, Activity activity) {
        this.parqueaderosList = parqueaderosList;
        this.resource = resource;
        this.activity = activity;
    }

    @Override
    public ParqueaderosViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext()).inflate(resource, parent, false);

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(activity, "Abre actividad con detalle", Toast.LENGTH_SHORT).show();
            }
        });

        return new ParqueaderosViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ParqueaderosViewHolder holder, int position) {
        Parqueaderos parqueadero = parqueaderosList.get(position);
        holder.bindParqueadero(parqueadero, activity);
    }

    @Override
    public int getItemCount() {

        return parqueaderosList.size();
    }


    public class ParqueaderosViewHolder extends RecyclerView.ViewHolder {

        private TextView tNombre, tDireccion, tId;
        private CircleImageView iFoto;

        public ParqueaderosViewHolder(View itemView) {
            super(itemView);
            tNombre = itemView.findViewById(R.id.tNombre);
            tDireccion = itemView.findViewById(R.id.tDireccion);
            tId = itemView.findViewById(R.id.tId);
            iFoto = itemView.findViewById(R.id.iFoto);

        }

        public void bindParqueadero(Parqueaderos parqueadero, Activity activity) {
            tNombre.setText(parqueadero.getNombre());
            tDireccion.setText(parqueadero.getDireccion());
            tId.setText(parqueadero.getId());
            Picasso.get().load(parqueadero.getFoto()).into(iFoto);
        }
    }
}
