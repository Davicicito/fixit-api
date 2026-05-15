package com.dmontoro.fixitapi.dto;

/**
 * Clase de apoyo que representa un material consumido durante una reparacion.
 * Se utiliza para enviar desde la aplicacion movil que pieza se ha usado y que cantidad exacta
 * se debe restar del inventario general del almacen.
 */
public class MaterialGastado {
    private Long idMaterial;
    private double cantidad;

    /**
     * Constructor sin parametros necesario para que el sistema pueda procesar los datos
     * que llegan a traves de la red desde el telefono movil.
     */
    public MaterialGastado() {} // Constructor vacío

    // GETTERS Y SETTERS
    /**
     * Recupera el numero identificador del material que se ha gastado.
     * @return El numero de identificacion unico del producto.
     */
    public Long getIdMaterial() { return idMaterial; }

    /**
     * Define que material es el que se ha utilizado en el trabajo.
     * @param idMaterial El numero identificador sacado de la base de datos.
     */
    public void setIdMaterial(Long idMaterial) { this.idMaterial = idMaterial; }

    /**
     * Recupera el numero de unidades o la medida gastada del material.
     * @return La cifra con la cantidad consumida.
     */
    public double getCantidad() { return cantidad; }

    /**
     * Define cuanta cantidad de este producto ha gastado el tecnico.
     * @param cantidad El numero de unidades o medida utilizada.
     */
    public void setCantidad(double cantidad) { this.cantidad = cantidad; }
}