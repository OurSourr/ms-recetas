package cl.duoc.ms.recetas.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import cl.duoc.ms.recetas.model.PasoReceta;

/**
 * Repositorio de persistencia para los pasos de preparación.
 */
public interface PasoRecetaRepository
        extends JpaRepository<PasoReceta, Integer> {

    /**
     * Recupera los pasos de una receta ordenados
     * ascendentemente por su número.
     *
     * @param idReceta identificador de la receta
     * @return lista ordenada de pasos
     */
    List<PasoReceta> findByReceta_IdRecetaOrderByNumeroPasoAsc(Integer idReceta);
}