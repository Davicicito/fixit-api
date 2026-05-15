package com.dmontoro.fixitapi.dto;

import java.util.List;

/**
 * Clase de transferencia de datos utilizada para cerrar un trabajo desde la aplicacion movil.
 * Este paquete agrupa toda la informacion final que el tecnico rellena al terminar una averia,
 * como las fotos, la firma y los materiales que ha gastado en la reparacion.
 */
public class FinalizarAvisoRequest {
    private String estado;
    private String observaciones;
    private String fotoBase64;
    private String firmaBase64;
    private Integer valoracionCliente;
    private List<MaterialGastado> materialesUsados;

    /**
     * Constructor vacio necesario para que el sistema pueda transformar el texto JSON
     * que envia el movil en un objeto Java utilizable por el servidor.
     */
    public FinalizarAvisoRequest() {
    }

    // --- MÉTODOS SET ---
    /**
     * Define el estado final del aviso, que normalmente sera completado.
     * @param estado Texto con el nombre del nuevo estado.
     */
    public void setEstado(String estado) {
        this.estado = estado;
    }

    /**
     * Guarda los comentarios finales escritos por el tecnico sobre el trabajo realizado.
     * @param observaciones Texto con el resumen de la reparacion.
     */
    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    /**
     * Recibe la imagen capturada con la camara del movil en un formato de texto largo.
     * @param fotoBase64 Cadena de texto que representa la foto del desperfecto arreglado.
     */
    public void setFotoBase64(String fotoBase64) {
        this.fotoBase64 = fotoBase64;
    }

    /**
     * Recibe el dibujo de la firma que el cliente ha hecho con el dedo en la pantalla del movil.
     * @param firmaBase64 Cadena de texto que representa la firma tactil.
     */
    public void setFirmaBase64(String firmaBase64) {
        this.firmaBase64 = firmaBase64;
    }

    /**
     * Guarda la nota o puntuacion de estrellas que el cliente le ha puesto al tecnico.
     * @param valoracionCliente Numero entero del uno al cinco.
     */
    public void setValoracionCliente(Integer valoracionCliente) {
        this.valoracionCliente = valoracionCliente;
    }

    /**
     * Guarda la lista de todos los repuestos y piezas que se han consumido durante el arreglo.
     * @param materialesUsados Lista de objetos con el nombre del material y su cantidad.
     */
    public void setMaterialesUsados(List<MaterialGastado> materialesUsados) {
        this.materialesUsados = materialesUsados;
    }

    // --- MÉTODOS GET ---
    /**
     * Recupera el estado final del aviso.
     * @return El nombre del estado.
     */
    public String getEstado() {
        return estado;
    }

    /**
     * Recupera los comentarios del tecnico.
     * @return Las anotaciones del parte de trabajo.
     */
    public String getObservaciones() {
        return observaciones;
    }

    /**
     * Recupera el texto de la fotografia.
     * @return La imagen en formato de texto base64.
     */
    public String getFotoBase64() {
        return fotoBase64;
    }

    /**
     * Recupera el texto de la firma del cliente.
     * @return La firma en formato de texto base64.
     */
    public String getFirmaBase64() {
        return firmaBase64;
    }

    /**
     * Recupera la puntuacion dada por el cliente.
     * @return El numero de estrellas de valoracion.
     */
    public Integer getValoracionCliente() {
        return valoracionCliente;
    }

    /**
     * Recupera el listado de repuestos utilizados.
     * @return La lista de materiales gastados.
     */
    public List<MaterialGastado> getMaterialesUsados() {
        return materialesUsados;
    }
}