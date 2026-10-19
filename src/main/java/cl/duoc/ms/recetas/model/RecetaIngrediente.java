package cl.duoc.ms.recetas.model;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

/**
 * Representa la asociación entre una receta y un ingrediente.
 *
 * Permite identificar ingredientes obligatorios y opcionales,
 * sus cantidades y el orden de presentación.
 */
@Entity
@Table(name = "receta_ingrediente")
public class RecetaIngrediente {

    @EmbeddedId
    private RecetaIngredienteId id;

    @MapsId("idReceta")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_receta", nullable = false)
    private Receta receta;

    @MapsId("idIngrediente")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_ingrediente", nullable = false)
    private Ingrediente ingrediente;

    @Column(name = "cantidad", precision = 10, scale = 2)
    private BigDecimal cantidad;

    @Column(name = "unidad", length = 30)
    private String unidad;

    @Column(name = "obligatorio", nullable = false)
    private boolean obligatorio = true;

    @Column(name = "orden", nullable = false)
    private Integer orden = 1;

    /**
     * Constructor requerido por JPA.
     */
    protected RecetaIngrediente() {
    }

    // Métodos de lectura

    public RecetaIngredienteId getId() {
        return id;
    }

    public Receta getReceta() {
        return receta;
    }

    public Ingrediente getIngrediente() {
        return ingrediente;
    }

    public BigDecimal getCantidad() {
        return cantidad;
    }

    public String getUnidad() {
        return unidad;
    }

    public boolean isObligatorio() {
        return obligatorio;
    }

    public Integer getOrden() {
        return orden;
    }
}