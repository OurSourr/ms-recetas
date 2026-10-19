package cl.duoc.ms.recetas.model;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

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

    protected RecetaProducto() {
        // Constructor requerido por JPA.
    }
}