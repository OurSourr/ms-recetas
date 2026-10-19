package cl.duoc.ms.recetas.model;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

/**
 * Representa la asociación entre una receta
 * y un producto del catálogo Our Sour.
 *
 * El identificador del producto es externo y
 * no establece una relación JPA con Catálogo.
 */
@Entity
@Table(name = "receta_producto")
public class RecetaProducto {

    @EmbeddedId
    private RecetaProductoId id;

    @MapsId("idReceta")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_receta", nullable = false)
    private Receta receta;

    @Column(name = "principal", nullable = false)
    private boolean principal = false;

    /**
     * Constructor requerido por JPA.
     */
    protected RecetaProducto() {
    }

    // Métodos de lectura

    public RecetaProductoId getId() {
        return id;
    }

    public Receta getReceta() {
        return receta;
    }

    public boolean isPrincipal() {
        return principal;
    }
}