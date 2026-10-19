
package cl.duoc.ms.recetas.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cl.duoc.ms.recetas.model.Receta;
import cl.duoc.ms.recetas.repository.RecetaRepository;
import cl.duoc.ms.recetas.service.RecetaService;

/**
 * Implementación de la lógica de negocio para recetas.
 */
@Service
public class RecetaServiceImpl implements RecetaService {

    private final RecetaRepository recetaRepository;

    @Autowired
    public RecetaServiceImpl(RecetaRepository recetaRepository) {
        this.recetaRepository = recetaRepository;
    }

    /**
     * Obtiene las recetas publicadas y activas.
     *
     * @return lista de recetas disponibles para consulta
     */
    @Override
    public List<Receta> listarRecetasPublicadas() {
        return recetaRepository.findByPublicadaTrueAndActivoTrue();
    }
}
