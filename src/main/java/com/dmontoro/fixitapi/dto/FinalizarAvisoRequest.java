package com.dmontoro.fixitapi.dto;

import java.util.List;

public class FinalizarAvisoRequest {
    private String estado;
    private String observaciones;
    private String fotoBase64;
    private String firmaBase64;
    private Integer valoracionCliente;
    private List<MaterialGastado> materialesUsados;

    public FinalizarAvisoRequest() {
    }

    // --- MÉTODOS SET ---
    public void setEstado(String estado) {
        this.estado = estado;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public void setFotoBase64(String fotoBase64) {
        this.fotoBase64 = fotoBase64;
    }

    public void setFirmaBase64(String firmaBase64) {
        this.firmaBase64 = firmaBase64;
    }

    public void setValoracionCliente(Integer valoracionCliente) {
        this.valoracionCliente = valoracionCliente;
    }

    public void setMaterialesUsados(List<MaterialGastado> materialesUsados) {
        this.materialesUsados = materialesUsados;
    }

    // --- MÉTODOS GET ---
    public String getEstado() {
        return estado;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public String getFotoBase64() {
        return fotoBase64;
    }

    public String getFirmaBase64() {
        return firmaBase64;
    }

    public Integer getValoracionCliente() {
        return valoracionCliente;
    }

    public List<MaterialGastado> getMaterialesUsados() {
        return materialesUsados;
    }
}