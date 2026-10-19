package cl.duoc.ms.recetas.model;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class RecetaIngredienteId implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "id_receta", nullable = false)
    private Integer idReceta;

    @Column(name = "id_ingrediente", nullable = false)
    private Integer idIngrediente;

    protected RecetaIngredienteId() {
        // Constructor requerido por JPA.
    }

    public RecetaIngredienteId(Integer idReceta, Integer idIngrediente) {
        this.idReceta = Objects.requireNonNull(idReceta, "idReceta es obligatorio");
        this.idIngrediente = Objects.requireNonNull(
            idIngrediente, "idIngrediente es obligatorio"
        );
    }

    public Integer getIdReceta() {
        return idReceta;
    }

    public Integer getIdIngrediente() {
        return idIngrediente;
    }

    @Override
    public boolean equals(Object objeto) {
        if (this == objeto) {
            return true;
        }
        if (!(objeto instanceof RecetaIngredienteId otro)) {
            return false;
        }
        return Objects.equals(idReceta, otro.idReceta)
                && Objects.equals(idIngrediente, otro.idIngrediente);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idReceta, idIngrediente);
    }
}