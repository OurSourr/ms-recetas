
package cl.duoc.ms.recetas.service.impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cl.duoc.ms.recetas.dto.RecetaRecomendacionResponse;
import cl.duoc.ms.recetas.model.Receta;
import cl.duoc.ms.recetas.model.RecetaIngrediente;
import cl.duoc.ms.recetas.repository.RecetaIngredienteRepository;
import cl.duoc.ms.recetas.service.CalculadorCompatibilidad;
import cl.duoc.ms.recetas.service.DescubrimientoService;
import cl.duoc.ms.recetas.service.RecetaService;

/**
 * Genera recomendaciones de recetas según los ingredientes
 * disponibles del usuario.
 *
 * Reutiliza las reglas de visibilidad pública y evita
 * consultar los ingredientes individualmente por receta.
 */
@Service
@Transactional(readOnly = true)
public class DescubrimientoServiceImpl
        implements DescubrimientoService {

    private final RecetaService recetaService;
    private final RecetaIngredienteRepository recetaIngredienteRepository;
    private final CalculadorCompatibilidad calculadorCompatibilidad;

    @Autowired
    public DescubrimientoServiceImpl(
            RecetaService recetaService,
            RecetaIngredienteRepository recetaIngredienteRepository,
            CalculadorCompatibilidad calculadorCompatibilidad) {

        this.recetaService = Objects.requireNonNull(
                recetaService, "El servicio de recetas es obligatorio.");

        this.recetaIngredienteRepository = Objects.requireNonNull(
                recetaIngredienteRepository,
                "El repositorio de ingredientes es obligatorio.");

        this.calculadorCompatibilidad = Objects.requireNonNull(
                calculadorCompatibilidad,
                "El calculador de compatibilidad es obligatorio.");
    }

    @Override
    public List<RecetaRecomendacionResponse> recomendarRecetas(
            Set<Integer> idsIngredientesDisponibles) {

        validarIngredientesDisponibles(idsIngredientesDisponibles);

        // Reutiliza las reglas existentes de visibilidad pública.
        List<Receta> recetas = recetaService.listarRecetasPublicadas();

        if (recetas.isEmpty()) {
            return List.of();
        }

        List<Integer> idsRecetas = recetas.stream()
                .map(Receta::getIdReceta)
                .toList();

        // Consulta conjunta para evitar una consulta por receta.
        List<RecetaIngrediente> relaciones =
                recetaIngredienteRepository.findByReceta_IdRecetaIn(
                        idsRecetas);

        Map<Integer, List<RecetaIngrediente>> ingredientesPorReceta =
                new HashMap<>();

        for (RecetaIngrediente relacion : relaciones) {

            Integer idReceta = relacion.getId()
                    .getIdReceta();

            ingredientesPorReceta
                    .computeIfAbsent(
                            idReceta,
                            id -> new ArrayList<>())
                    .add(relacion);
        }

        Set<Integer> disponibles =
                new HashSet<>(idsIngredientesDisponibles);

        List<RecetaRecomendacionResponse> recomendaciones =
                new ArrayList<>();

        for (Receta receta : recetas) {

            List<RecetaIngrediente> ingredientes =
                    ingredientesPorReceta.getOrDefault(
                            receta.getIdReceta(),
                            List.of());

            RecetaRecomendacionResponse recomendacion =
                    calculadorCompatibilidad.calcular(
                            receta,
                            ingredientes,
                            disponibles);

            recomendaciones.add(recomendacion);
        }

        // Mayor compatibilidad primero; ID como desempate estable.
        recomendaciones.sort(
                Comparator.comparingDouble(
                        RecetaRecomendacionResponse::
                                porcentajeCompatibilidad)
                        .reversed()
                        .thenComparing(
                                RecetaRecomendacionResponse::idReceta));

        return List.copyOf(recomendaciones);
    }

    /**
     * Rechaza identificadores nulos o no positivos antes
     * de realizar consultas a la base de datos.
     */
    private void validarIngredientesDisponibles(
            Set<Integer> idsIngredientesDisponibles) {

        if (idsIngredientesDisponibles == null) {
            throw new IllegalArgumentException(
                    "La lista de ingredientes disponibles es obligatoria.");
        }

        if (idsIngredientesDisponibles.stream()
                .anyMatch(id -> id == null || id <= 0)) {

            throw new IllegalArgumentException(
                    "Los identificadores de ingredientes deben ser positivos.");
        }
    }
}
