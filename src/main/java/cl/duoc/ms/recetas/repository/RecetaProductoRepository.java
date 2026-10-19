package cl.duoc.ms.recetas.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import cl.duoc.ms.recetas.model.RecetaProducto;
import cl.duoc.ms.recetas.model.RecetaProductoId;

/**
 * Repositorio de persistencia para las asociaciones
 * entre recetas y productos del catálogo Our Sour.
 *
 * Utiliza una clave primaria compuesta.
 */
public interface RecetaProductoRepository
        extends JpaRepository<RecetaProducto, RecetaProductoId> {

    /**
     * Recupera los productos externos asociados a una receta.
     *
     * @param idReceta identificador de la receta
     * @return asociaciones de productos de la receta
     */
    List<RecetaProducto> findByReceta_IdReceta(Integer idReceta);
}