
package cl.duoc.ms.recetas;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import cl.duoc.ms.recetas.dto.RecetaRecomendacionResponse;
import cl.duoc.ms.recetas.model.Ingrediente;
import cl.duoc.ms.recetas.model.Receta;
import cl.duoc.ms.recetas.model.RecetaIngrediente;
import cl.duoc.ms.recetas.service.CalculadorCompatibilidad;

/**
 * Pruebas unitarias del cálculo de compatibilidad.
 *
 * No requiere Spring Boot, MySQL ni servicios externos.
 */
class CalculadorCompatibilidadTest {

    private CalculadorCompatibilidad calculador;

    @BeforeEach
    void configurar() {
        calculador = new CalculadorCompatibilidad();
    }

    @Test
    void debeCalcularCompatibilidadCompleta() {

        Receta receta = crearReceta(1, "Gin Sour");

        List<RecetaIngrediente> ingredientes = List.of(
                crearIngrediente(10, "Gin", true, true),
                crearIngrediente(20, "Limón", true, true)
        );

        RecetaRecomendacionResponse resultado =
                calculador.calcular(
                        receta,
                        ingredientes,
                        Set.of(10, 20));

        assertEquals(2, resultado.ingredientesObligatorios());
        assertEquals(2, resultado.ingredientesDisponibles());
        assertEquals(100.0, resultado.porcentajeCompatibilidad());
        assertTrue(resultado.puedePreparar());
        assertTrue(resultado.ingredientesFaltantes().isEmpty());
    }

    @Test
    void debeCalcularCompatibilidadParcial() {

        Receta receta = crearReceta(1, "Gin Sour");

        List<RecetaIngrediente> ingredientes = List.of(
                crearIngrediente(10, "Gin", true, true),
                crearIngrediente(20, "Limón", true, true),
                crearIngrediente(30, "Hielo", true, true),
                crearIngrediente(40, "Azúcar", true, true)
        );

        RecetaRecomendacionResponse resultado =
                calculador.calcular(
                        receta,
                        ingredientes,
                        Set.of(10, 20, 30));

        assertEquals(4, resultado.ingredientesObligatorios());
        assertEquals(3, resultado.ingredientesDisponibles());
        assertEquals(75.0, resultado.porcentajeCompatibilidad());
        assertFalse(resultado.puedePreparar());

        assertEquals(1, resultado.ingredientesFaltantes().size());
        assertEquals(
                40,
                resultado.ingredientesFaltantes()
                        .get(0).idIngrediente());
        assertEquals(
                "Azúcar",
                resultado.ingredientesFaltantes()
                        .get(0).nombre());
    }

    @Test
    void debeCalcularCompatibilidadCero() {

        Receta receta = crearReceta(1, "Gin Sour");

        List<RecetaIngrediente> ingredientes = List.of(
                crearIngrediente(10, "Gin", true, true),
                crearIngrediente(20, "Limón", true, true)
        );

        RecetaRecomendacionResponse resultado =
                calculador.calcular(
                        receta,
                        ingredientes,
                        Set.of());

        assertEquals(0, resultado.ingredientesDisponibles());
        assertEquals(0.0, resultado.porcentajeCompatibilidad());
        assertFalse(resultado.puedePreparar());
        assertEquals(2, resultado.ingredientesFaltantes().size());
    }

    @Test
    void debeIgnorarIngredientesOpcionalesEnElPorcentaje() {

        Receta receta = crearReceta(1, "Gin Sour");

        List<RecetaIngrediente> ingredientes = List.of(
                crearIngrediente(10, "Gin", true, true),
                crearIngrediente(20, "Decoración", false, true)
        );

        RecetaRecomendacionResponse resultado =
                calculador.calcular(
                        receta,
                        ingredientes,
                        Set.of(10));

        assertEquals(1, resultado.ingredientesObligatorios());
        assertEquals(1, resultado.ingredientesDisponibles());
        assertEquals(100.0, resultado.porcentajeCompatibilidad());
        assertTrue(resultado.puedePreparar());
    }

    @Test
    void debeRechazarRecetaSinIngredientesObligatorios() {

        Receta receta = crearReceta(1, "Gin Sour");

        List<RecetaIngrediente> ingredientes = List.of(
                crearIngrediente(20, "Decoración", false, true)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> calculador.calcular(
                        receta,
                        ingredientes,
                        Set.of(20)));
    }

    @Test
    void debeRechazarIngredienteObligatorioInactivo() {

        Receta receta = crearReceta(1, "Gin Sour");

        List<RecetaIngrediente> ingredientes = List.of(
                crearIngrediente(10, "Gin", true, false)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> calculador.calcular(
                        receta,
                        ingredientes,
                        Set.of(10)));
    }

    @Test
    void debeRechazarIngredientesObligatoriosDuplicados() {

        Receta receta = crearReceta(1, "Gin Sour");

        List<RecetaIngrediente> ingredientes = List.of(
                crearIngrediente(10, "Gin", true, true),
                crearIngrediente(10, "Gin", true, true)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> calculador.calcular(
                        receta,
                        ingredientes,
                        Set.of(10)));
    }

    @Test
    void debeRechazarIdentificadoresDisponiblesInvalidos() {

        Receta receta = crearReceta(1, "Gin Sour");

        List<RecetaIngrediente> ingredientes = List.of(
                crearIngrediente(10, "Gin", true, true)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> calculador.calcular(
                        receta,
                        ingredientes,
                        Set.of(0)));
    }

    /**
     * Crea una receta simulada sin modificar la entidad JPA.
     */
    private Receta crearReceta(Integer id, String titulo) {

        Receta receta = mock(Receta.class);

        when(receta.getIdReceta()).thenReturn(id);
        when(receta.getTitulo()).thenReturn(titulo);

        return receta;
    }

    /**
     * Crea una relación simulada con un ingrediente.
     *
     * Los mocks se crean antes de configurar sus respuestas
     * para evitar problemas de anidamiento con Mockito.
     */
    private RecetaIngrediente crearIngrediente(
            Integer id,
            String nombre,
            boolean obligatorio,
            boolean activo) {

        Ingrediente ingrediente = mock(Ingrediente.class);
        RecetaIngrediente relacion = mock(RecetaIngrediente.class);

        when(ingrediente.getIdIngrediente()).thenReturn(id);
        when(ingrediente.isActivo()).thenReturn(activo);

        if (obligatorio && activo) {
            when(ingrediente.getNombre()).thenReturn(nombre);
        } else if (obligatorio) {
            // No se necesita el nombre si el ingrediente está inactivo.
        }

        when(relacion.isObligatorio()).thenReturn(obligatorio);

        if (obligatorio) {
            when(relacion.getIngrediente()).thenReturn(ingrediente);
        }

        return relacion;
    }
}
