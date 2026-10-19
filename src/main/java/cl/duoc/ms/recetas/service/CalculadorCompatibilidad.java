
package cl.duoc.ms.recetas.service;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.springframework.stereotype.Component;

import cl.duoc.ms.recetas.dto.IngredienteFaltanteResponse;
import cl.duoc.ms.recetas.dto.RecetaRecomendacionResponse;
import cl.duoc.ms.recetas.model.Ingrediente;
import cl.duoc.ms.recetas.model.Receta;
import cl.duoc.ms.recetas.model.RecetaIngrediente;

/**
 * Calcula la compatibilidad de una receta con los ingredientes
 * disponibles del usuario.
 *
 * No realiza consultas a la base de datos ni peticiones HTTP.
 */
@Component
public class CalculadorCompatibilidad {

    /**
     * Calcula qué ingredientes obligatorios están disponibles,
     * cuáles faltan y el porcentaje de compatibilidad.
     *
     * Las recetas sin ingredientes obligatorios se rechazan
     * para respetar RN-PROP-01.
     */
    public RecetaRecomendacionResponse calcular(
            Receta receta,
            List<RecetaIngrediente> ingredientesReceta,
            Set<Integer> idsIngredientesDisponibles) {

        Objects.requireNonNull(receta, "La receta es obligatoria.");
        Objects.requireNonNull(
                ingredientesReceta,
                "Los ingredientes de la receta son obligatorios.");
        Objects.requireNonNull(
                idsIngredientesDisponibles,
                "Los ingredientes disponibles son obligatorios.");

        if (idsIngredientesDisponibles.stream()
                .anyMatch(id -> id == null || id <= 0)) {
            throw new IllegalArgumentException(
                    "Los identificadores disponibles deben ser positivos.");
        }

        Set<Integer> disponibles = new HashSet<>(idsIngredientesDisponibles);
        Set<Integer> ingredientesEvaluados = new HashSet<>();

        List<IngredienteFaltanteResponse> faltantes =
                new java.util.ArrayList<>();

        int totalObligatorios = 0;
        int totalDisponibles = 0;

        for (RecetaIngrediente relacion : ingredientesReceta) {

            if (relacion == null) {
                throw new IllegalArgumentException(
                        "La receta contiene una relación de ingrediente nula.");
            }

            if (!relacion.isObligatorio()) {
                continue;
            }

            Ingrediente ingrediente = Objects.requireNonNull(
                    relacion.getIngrediente(),
                    "El ingrediente obligatorio no puede ser nulo.");

            Integer idIngrediente = ingrediente.getIdIngrediente();

            if (idIngrediente == null || idIngrediente <= 0) {
                throw new IllegalArgumentException(
                        "El ingrediente obligatorio tiene un identificador inválido.");
            }

            if (!ingredientesEvaluados.add(idIngrediente)) {
                throw new IllegalArgumentException(
                        "La receta contiene ingredientes obligatorios duplicados.");
            }

            // Los ingredientes inactivos no se consideran disponibles.
            if (!ingrediente.isActivo()) {
                throw new IllegalArgumentException(
                        "La receta contiene un ingrediente obligatorio inactivo.");
            }

            totalObligatorios++;

            if (disponibles.contains(idIngrediente)) {
                totalDisponibles++;
            } else {
                faltantes.add(new IngredienteFaltanteResponse(
                        idIngrediente,
                        ingrediente.getNombre()));
            }
        }

        if (totalObligatorios == 0) {
            throw new IllegalArgumentException(
                    "La receta no tiene ingredientes obligatorios.");
        }

        double porcentaje =
                totalDisponibles * 100.0 / totalObligatorios;

        boolean puedePreparar =
                totalDisponibles == totalObligatorios;

        return new RecetaRecomendacionResponse(
                receta.getIdReceta(),
                receta.getTitulo(),
                totalObligatorios,
                totalDisponibles,
                porcentaje,
                puedePreparar,
                faltantes);
    }
}
