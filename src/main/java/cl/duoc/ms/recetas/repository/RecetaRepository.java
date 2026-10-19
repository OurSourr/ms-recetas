package cl.duoc.ms.recetas.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import cl.duoc.ms.recetas.model.Receta;

/**
 * Repositorio de persistencia para las recetas.
 */
public interface RecetaRepository extends JpaRepository<Receta, Integer> {

    /**
     * Recupera únicamente las recetas publicadas y activas.
     *
     * Spring Data JPA genera la consulta a partir del nombre
     * del método y de las propiedades de la entidad Receta.
     *
     * @return lista de recetas publicadas y activas
     */
    List<Receta> findByPublicadaTrueAndActivoTrue();
}