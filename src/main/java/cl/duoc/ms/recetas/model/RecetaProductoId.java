package cl.duoc.ms.recetas.model;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class RecetaProductoId implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "id_receta", nullable = false)
    private Integer idReceta;

    @Column(name = "id_producto_externo", nullable = false)
    private Integer idProductoExterno;

    protected RecetaProductoId() {
        // Constructor requerido por JPA.
    }

    public RecetaProductoId(Integer idReceta, Integer idProductoExterno) {
        this.idReceta = Objects.requireNonNull(idReceta, "idReceta es obligatorio");
        this.idProductoExterno = Objects.requireNonNull(
            idProductoExterno, "idProductoExterno es obligatorio"
        );
    }

    public Integer getIdReceta() {
        return idReceta;
    }

    public Integer getIdProductoExterno() {
        return idProductoExterno;
    }

    @Override
    public boolean equals(Object objeto) {
        if (this == objeto) {
            return true;
        }
        if (!(objeto instanceof RecetaProductoId otro)) {
            return false;
        }
        return Objects.equals(idReceta, otro.idReceta)
                && Objects.equals(idProductoExterno, otro.idProductoExterno);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idReceta, idProductoExterno);
    }
}