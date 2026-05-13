package com.dmontoro.fixitapi.dto;

public class MaterialGastado {
    private Long idMaterial;
    private double cantidad;

    public MaterialGastado() {} // Constructor vacío

    // GETTERS Y SETTERS
    public Long getIdMaterial() { return idMaterial; }
    public void setIdMaterial(Long idMaterial) { this.idMaterial = idMaterial; }
    public double getCantidad() { return cantidad; }
    public void setCantidad(double cantidad) { this.cantidad = cantidad; }
}