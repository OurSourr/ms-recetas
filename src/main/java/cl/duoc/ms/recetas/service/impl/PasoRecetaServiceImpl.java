package cl.duoc.ms.recetas.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cl.duoc.ms.recetas.model.PasoReceta;
import cl.duoc.ms.recetas.repository.PasoRecetaRepository;
import cl.duoc.ms.recetas.service.PasoRecetaService;

@Service
public class PasoRecetaServiceImpl implements PasoRecetaService {

    private final PasoRecetaRepository pasoRecetaRepository;

    @Autowired
    public PasoRecetaServiceImpl(PasoRecetaRepository pasoRecetaRepository) {
        this.pasoRecetaRepository = pasoRecetaRepository;
    }

    @Override
    public List<PasoReceta> listarPasosPorReceta(Integer idReceta) {
        if (idReceta == null || idReceta <= 0) {
            throw new IllegalArgumentException(
                    "El identificador de la receta debe ser un número positivo.");
        }

        return pasoRecetaRepository
                .findByReceta_IdRecetaOrderByNumeroPasoAsc(idReceta);
    }
}