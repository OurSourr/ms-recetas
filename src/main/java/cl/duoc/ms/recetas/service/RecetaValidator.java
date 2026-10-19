package cl.duoc.ms.recetas.service;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Component;

import cl.duoc.ms.recetas.model.RecetaIngrediente;
import cl.duoc.ms.recetas.model.RecetaProducto;

/**
 * Centraliza las validaciones de negocio de las recetas.
 */
@Component
public class RecetaValidator {

    /**
     * Comprueba RN-PROP-01 sin lanzar excepciones.
     * Puede reutilizarse al consultar recetas publicadas.
     */
    public boolean tieneIngredientesObligatorios(
            List<RecetaIngrediente> ingredientes) {

        return ingredientes != null
                && ingredientes.stream()
                        .filter(Objects::nonNull)
                        .anyMatch(RecetaIngrediente::isObligatorio);
    }

    /**
     * RN-PROP-01:
     * Toda receta publicada debe tener al menos
     * un ingrediente obligatorio.
     */
    public void validarIngredientesObligatorios(
            List<RecetaIngrediente> ingredientes) {

        if (ingredientes == null) {
            throw new IllegalArgumentException(
                    "La lista de ingredientes no puede ser nula.");
        }

        if (!tieneIngredientesObligatorios(ingredientes)) {
            throw new IllegalStateException(
                    "Una receta publicada debe tener al menos un ingrediente obligatorio.");
        }
    }

    /**
     * Verifica que una receta tenga al menos
     * una asociación válida con un producto externo.
     *
     * Esta validación no comprueba si el producto
     * está activo en el microservicio de Catálogo.
     */
    public void validarProductosAsociados(
            List<RecetaProducto> productos) {

        if (productos == null) {
            throw new IllegalArgumentException(
                    "La lista de productos no puede ser nula.");
        }

        boolean tieneProductoAsociado = productos.stream()
                .filter(Objects::nonNull)
                .map(RecetaProducto::getId)
                .filter(Objects::nonNull)
                .map(id -> id.getIdProductoExterno())
                .anyMatch(id -> id != null && id > 0);

        if (!tieneProductoAsociado) {
            throw new IllegalStateException(
                    "Una receta publicada debe tener al menos un producto Our Sour asociado.");
        }
    }
}