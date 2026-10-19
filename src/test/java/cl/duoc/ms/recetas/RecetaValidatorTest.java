package cl.duoc.ms.recetas;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import cl.duoc.ms.recetas.model.RecetaIngrediente;
import cl.duoc.ms.recetas.model.RecetaProducto;
import cl.duoc.ms.recetas.model.RecetaProductoId;
import cl.duoc.ms.recetas.service.RecetaValidator;

class RecetaValidatorTest {

    private RecetaValidator recetaValidator;

    @BeforeEach
    void configurar() {
        // Pruebas unitarias sin iniciar Spring Boot.
        recetaValidator = new RecetaValidator();
    }

    @Test
    void debeAceptarRecetaConIngredienteObligatorio() {
        RecetaIngrediente ingrediente = crearIngrediente(true);

        assertDoesNotThrow(() ->
                recetaValidator.validarIngredientesObligatorios(
                        List.of(ingrediente)));
    }

    @Test
    void debeRechazarRecetaConIngredientesSoloOpcionales() {
        RecetaIngrediente ingrediente = crearIngrediente(false);

        IllegalStateException excepcion = assertThrows(
                IllegalStateException.class,
                () -> recetaValidator.validarIngredientesObligatorios(
                        List.of(ingrediente)));

        assertTrue(excepcion.getMessage().contains("obligatorio"));
    }

    @Test
    void debeRechazarRecetaSinIngredientes() {
        assertThrows(
                IllegalStateException.class,
                () -> recetaValidator.validarIngredientesObligatorios(
                        List.of()));
    }

    @Test
    void debeRechazarListaNula() {
        assertThrows(
                IllegalArgumentException.class,
                () -> recetaValidator.validarIngredientesObligatorios(null));
    }

    @Test
    void debeAceptarRecetaConProductoAsociadoValido() {
        RecetaProducto producto = crearProducto(10);

        assertDoesNotThrow(() ->
                recetaValidator.validarProductosAsociados(
                        List.of(producto)));
    }

    @Test
    void debeRechazarRecetaSinProductosAsociados() {
        assertThrows(
                IllegalStateException.class,
                () -> recetaValidator.validarProductosAsociados(
                        List.of()));
    }

    @Test
    void debeRechazarListaNulaDeProductos() {
        assertThrows(
                IllegalArgumentException.class,
                () -> recetaValidator.validarProductosAsociados(null));
    }

    @Test
    void debeRechazarProductoConIdentificadorInvalido() {
        RecetaProducto producto = crearProducto(-1);

        assertThrows(
                IllegalStateException.class,
                () -> recetaValidator.validarProductosAsociados(
                        List.of(producto)));
    }

    /**
     * Simula un ingrediente sin depender de JPA.
     */
    private RecetaIngrediente crearIngrediente(boolean obligatorio) {
        RecetaIngrediente ingrediente = mock(RecetaIngrediente.class);
        when(ingrediente.isObligatorio()).thenReturn(obligatorio);
        return ingrediente;
    }

    /**
     * Simula la asociación de una receta con un producto externo.
     */
    private RecetaProducto crearProducto(Integer idProductoExterno) {
        RecetaProducto producto = mock(RecetaProducto.class);

        when(producto.getId()).thenReturn(
                new RecetaProductoId(1, idProductoExterno));

        return producto;
    }
}