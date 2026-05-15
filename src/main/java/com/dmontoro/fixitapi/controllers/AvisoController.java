package com.dmontoro.fixitapi.controllers;

import com.dmontoro.fixitapi.models.Aviso;
import com.dmontoro.fixitapi.models.AvisoMaterial;
import com.dmontoro.fixitapi.services.AvisoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Controlador de las conexiones de la API para los avisos de trabajo.
 * Actua como un puente que recibe las peticiones desde la aplicacion movil o el programa de escritorio
 * y las manda a la base de datos para leer o guardar informacion de las averias.
 */
@RestController
@RequestMapping("/avisos")
@CrossOrigin
public class AvisoController {

    @Autowired
    private AvisoService avisoService;

    // ==========================================
    // 1. CRUD BÁSICO DE AVISOS
    // ==========================================

    /**
     * Pide a la base de datos la lista completa de todos los avisos registrados en la empresa
     * y los devuelve al instante para mostrarlos en la tabla principal.
     *
     * @return Una respuesta con la lista de todos los trabajos guardados.
     */
    @GetMapping
    public ResponseEntity<List<Aviso>> getAllAvisos() {
        return new ResponseEntity<>(avisoService.getAllAvisos(), HttpStatus.OK);
    }

    /**
     * Busca una averia especifica utilizando su numero de identificador unico.
     * Si la encuentra devuelve sus datos completos, y si no existe avisa de que no hay resultados.
     *
     * @param id El numero identificador del aviso que queremos buscar.
     * @return La informacion detallada de ese aviso.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Aviso> getAvisoById(@PathVariable Long id) {
        Optional<Aviso> aviso = avisoService.getAvisoById(id);
        return aviso.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    /**
     * Recoge los datos de un nuevo parte de trabajo que nos envian desde el programa
     * y lo guarda de forma segura en la base de datos.
     *
     * @param aviso El objeto con toda la informacion de la averia a crear.
     * @return El mismo aviso pero ya con su numero de identificador asignado por el sistema.
     */
    @PostMapping
    public ResponseEntity<Aviso> createAviso(@RequestBody Aviso aviso) {
        return ResponseEntity.status(HttpStatus.CREATED).body(avisoService.saveAviso(aviso));
    }

    /**
     * Sobrescribe los datos de un aviso que ya existia previamente.
     * Sirve para cambiar su estado, asignarle un tecnico distinto o modificar su descripcion.
     *
     * @param id El numero identificador de la averia que queremos cambiar.
     * @param avisoDetails Los datos nuevos que van a sustituir a los antiguos.
     * @return El aviso ya modificado y guardado.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Aviso> updateAviso(@PathVariable Long id, @RequestBody Aviso avisoDetails) {
        try {
            return ResponseEntity.ok(avisoService.actualizarAviso(id, avisoDetails));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * Busca un aviso por su numero y lo borra definitivamente de la base de datos.
     * Si intentan borrar uno que no existe el sistema responde con un fallo para evitar errores.
     *
     * @param id El numero del aviso que queremos destruir.
     * @return Una respuesta vacia indicando que el borrado fue un exito.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAviso(@PathVariable Long id) {
        if (avisoService.getAvisoById(id).isPresent()) {
            avisoService.deleteAviso(id);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    // ==========================================
    // 2. CONSULTAS AVANZADAS
    // ==========================================

    /**
     * Filtra la base de datos para devolver unicamente los partes de trabajo
     * que estan asignados a un empleado en concreto. Muy util para que cada trabajador
     * vea solo su tarea en la aplicacion movil.
     *
     * @param tecnicoId El numero de empleado del tecnico.
     * @return La lista de trabajos que tiene asignados esa persona.
     */
    @GetMapping("/tecnico/{tecnicoId}")
    public ResponseEntity<List<Aviso>> getAvisosByTecnico(@PathVariable Long tecnicoId) {
        return ResponseEntity.ok(avisoService.getAvisosPorTecnico(tecnicoId));
    }

    /**
     * Rastrea el historial para encontrar en que averias se ha utilizado un material concreto.
     * Ideal para llevar un buen control de en que casas se han gastado las piezas del almacen.
     *
     * @param material El nombre del producto que queremos investigar.
     * @return Una lista con las averias donde se uso esa pieza.
     */
    @GetMapping("/material/{material}")
    public ResponseEntity<List<Aviso>> getAvisosByMaterial(@PathVariable String material) {
        return ResponseEntity.ok(avisoService.getAvisosPorMaterial(material));
    }

    /**
     * Busca averias que todavia no se han solucionado y las filtra por especialidad.
     * Por ejemplo sirve para ver todos los atascos de fontaneria que siguen pendientes de arreglar.
     *
     * @param categoria La especialidad tecnica a consultar.
     * @return Lista de trabajos sin terminar de esa rama.
     */
    @GetMapping("/pendientes/{categoria}")
    public ResponseEntity<List<Aviso>> getPendientesByCategoria(@PathVariable String categoria) {
        return ResponseEntity.ok(avisoService.getAvisosPendientesPorCategoria(categoria));
    }

    // ==========================================
    // 3. GESTIÓN DE LA TABLA INTERMEDIA (N:M)
    // ==========================================

    /**
     * Conecta una averia con un producto del inventario para indicar que se ha gastado durante la reparacion.
     * Ademas registra la cantidad exacta de material que el tecnico ha utilizado.
     *
     * @param id El numero del aviso de trabajo.
     * @param avisoMaterial La informacion de la pieza y la cantidad consumida.
     * @return El registro exitoso de ese gasto.
     */
    // POST http://localhost:8080/avisos/1/materiales
    // Sirve para añadir un material a un aviso concreto indicando su cantidad
    @PostMapping("/{id}/materiales")
    public ResponseEntity<?> addMaterialAAviso(@PathVariable Long id, @RequestBody AvisoMaterial avisoMaterial) {
        try {
            // Le pasamos al servicio
            AvisoMaterial nuevoMaterial = avisoService.añadirMaterialAAviso(id, avisoMaterial);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevoMaterial);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Extrae el desglose o la factura de materiales de un parte de trabajo ya terminado.
     * Permite al jefe de equipo ver exactamente que cosas gasto el empleado en esa reparacion.
     *
     * @param id El numero de la averia a consultar.
     * @return La lista detallada de los productos utilizados y sus cantidades.
     */
    // GET http://localhost:8080/avisos/1/materiales
    // Sirve para que el jefe vea el desglose de materiales de un parte de trabajo
    @GetMapping("/{id}/materiales")
    public ResponseEntity<List<AvisoMaterial>> getMaterialesDeUnAviso(@PathVariable Long id) {
        return ResponseEntity.ok(avisoService.getMaterialesDeUnAviso(id));
    }

    // ==========================================
    // 4. FINALIZAR TRABAJO DESDE LA APP MÓVIL
    // ==========================================

    /**
     * Es la funcion estrella de la aplicacion movil.
     * Recibe un gran paquete de datos cuando el trabajador cierra una averia en la casa del cliente.
     * Guarda las observaciones, la foto del desperfecto, la firma tactil del dueño, las estrellas de valoracion
     * y descuenta del almacen todos los materiales que el empleado haya gastado.
     *
     * @param id El numero del trabajo que se acaba de terminar.
     * @param peticion Un paquete especial que agrupa la foto, la firma y los materiales descontados.
     * @return Una señal de todo correcto para que la aplicacion movil sepa que el proceso ha ido bien.
     */
    @PostMapping("/{id}/finalizar")
    public ResponseEntity<?> finalizarAvisoDesdeApp(
            @PathVariable Long id,
            @RequestBody com.dmontoro.fixitapi.dto.FinalizarAvisoRequest peticion) {
        try {
            avisoService.finalizarTrabajo(id, peticion);
            // Mandamos un mapa/objeto para que Retrofit no se líe con el String
            return ResponseEntity.ok().body(java.util.Collections.singletonMap("mensaje", "OK"));
        } catch (RuntimeException e) {
            e.printStackTrace(); // Esto hará que el error salga en la consola de IntelliJ
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}