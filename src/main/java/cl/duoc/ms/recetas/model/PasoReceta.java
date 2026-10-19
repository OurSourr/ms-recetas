package cl.duoc.ms.recetas.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
    name = "paso_receta",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_paso_receta_numero",
        columnNames = {"id_receta", "numero_paso"}
    )
)
public class PasoReceta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_paso")
    private Integer idPaso;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_receta", nullable = false)
    private Receta receta;

    @Column(name = "numero_paso", nullable = false)
    private Integer numeroPaso;

    @Column(name = "instruccion", nullable = false, columnDefinition = "TEXT")
    private String instruccion;

    protected PasoReceta() {
        // Constructor requerido por JPA.
    }
}