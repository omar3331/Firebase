package com.example.otra.firebase.modelo;

public class Parqueaderos {
    String nombre, foto, direccion, id;


    public Parqueaderos(String nombre, String foto, String direccion, String id) {
        this.nombre = nombre;
        this.foto = foto;
        this.direccion = direccion;
        this.id = id;
    }

    public Parqueaderos() {
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getFoto() {
        return foto;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
