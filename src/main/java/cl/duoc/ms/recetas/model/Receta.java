package cl.duoc.ms.recetas.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Representa una receta de coctelería.
 *
 * Las recetas pueden mantenerse como borradores
 * hasta cumplir las condiciones necesarias para publicarse.
 */
@Entity
@Table(name = "receta")
public class Receta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_receta")
    private Integer idReceta;

    @Column(name = "titulo", nullable = false, length = 160)
    private String titulo;

    @Column(name = "descripcion", length = 600)
    private String descripcion;

    @Column(name = "publicada", nullable = false)
    private boolean publicada = false;

    @Column(name = "activo", nullable = false)
    private boolean activo = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Constructor requerido por JPA.
     */
    protected Receta() {
    }

    // Métodos de lectura

    public Integer getIdReceta() {
        return idReceta;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public boolean isPublicada() {
        return publicada;
    }

    public boolean isActivo() {
        return activo;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
 * Cambia el estado de la receta a publicada.
 *
 * Debe invocarse únicamente después de superar
 * las validaciones de negocio.
 */
public void marcarComoPublicada() {
    this.publicada = true;
}
}