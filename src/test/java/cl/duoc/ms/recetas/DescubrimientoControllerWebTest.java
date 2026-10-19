
package cl.duoc.ms.recetas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import cl.duoc.ms.recetas.controller.DescubrimientoController;
import cl.duoc.ms.recetas.dto.IngredienteFaltanteResponse;
import cl.duoc.ms.recetas.dto.RecetaRecomendacionResponse;
import cl.duoc.ms.recetas.exception.GlobalExceptionHandler;
import cl.duoc.ms.recetas.service.DescubrimientoService;

/**
 * Pruebas HTTP del controlador de descubrimiento.
 *
 * Verifica parámetros, respuestas JSON y códigos HTTP.
 * No requiere iniciar Spring Boot, MySQL ni Catálogo.
 */
class DescubrimientoControllerWebTest {

    private MockMvc mockMvc;
    private DescubrimientoService descubrimientoService;

    @BeforeEach
    void configurar() {

        descubrimientoService = mock(DescubrimientoService.class);

        DescubrimientoController controlador =
                new DescubrimientoController(descubrimientoService);

        mockMvc = MockMvcBuilders
                .standaloneSetup(controlador)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    /**
     * Comprueba la respuesta JSON de una receta
     * que el usuario puede preparar completamente.
     */
    @Test
    void debeRetornarRecomendacionCompletaEnJson()
            throws Exception {

        RecetaRecomendacionResponse recomendacion =
                new RecetaRecomendacionResponse(
                        1,
                        "Gin Sour",
                        2,
                        2,
                        100.0,
                        true,
                        List.of());

        when(descubrimientoService.recomendarRecetas(
                Set.of(10, 20)))
                .thenReturn(List.of(recomendacion));

        mockMvc.perform(get(
                        "/api/v1/descubrimiento/recomendaciones")
                        .param("ingredientes", "10,20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].idReceta").value(1))
                .andExpect(jsonPath("$[0].titulo")
                        .value("Gin Sour"))
                .andExpect(jsonPath(
                        "$[0].ingredientesObligatorios").value(2))
                .andExpect(jsonPath(
                        "$[0].ingredientesDisponibles").value(2))
                .andExpect(jsonPath(
                        "$[0].porcentajeCompatibilidad").value(100.0))
                .andExpect(jsonPath(
                        "$[0].puedePreparar").value(true))
                .andExpect(jsonPath(
                        "$[0].ingredientesFaltantes.length()").value(0));

        verify(descubrimientoService)
                .recomendarRecetas(Set.of(10, 20));

        verifyNoMoreInteractions(descubrimientoService);
    }

    /**
     * Comprueba que los ingredientes faltantes
     * se serialicen correctamente.
     */
    @Test
    void debeRetornarIngredientesFaltantesEnJson()
            throws Exception {

        RecetaRecomendacionResponse recomendacion =
                new RecetaRecomendacionResponse(
                        2,
                        "Cóctel de naranja",
                        2,
                        1,
                        50.0,
                        false,
                        List.of(
                                new IngredienteFaltanteResponse(
                                        30, "Naranja")));

        when(descubrimientoService.recomendarRecetas(
                Set.of(10)))
                .thenReturn(List.of(recomendacion));

        mockMvc.perform(get(
                        "/api/v1/descubrimiento/recomendaciones")
                        .param("ingredientes", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idReceta").value(2))
                .andExpect(jsonPath(
                        "$[0].porcentajeCompatibilidad").value(50.0))
                .andExpect(jsonPath(
                        "$[0].puedePreparar").value(false))
                .andExpect(jsonPath(
                        "$[0].ingredientesFaltantes.length()").value(1))
                .andExpect(jsonPath(
                        "$[0].ingredientesFaltantes[0].idIngrediente")
                        .value(30))
                .andExpect(jsonPath(
                        "$[0].ingredientesFaltantes[0].nombre")
                        .value("Naranja"));

        verify(descubrimientoService)
                .recomendarRecetas(Set.of(10));
    }

    /**
     * Sin recomendaciones, el endpoint debe devolver
     * HTTP 200 con un arreglo JSON vacío.
     */
    @Test
    void debeRetornarListaVaciaCuandoNoHayRecomendaciones()
            throws Exception {

        when(descubrimientoService.recomendarRecetas(
                Set.of(10)))
                .thenReturn(List.of());

        mockMvc.perform(get(
                        "/api/v1/descubrimiento/recomendaciones")
                        .param("ingredientes", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(descubrimientoService)
                .recomendarRecetas(Set.of(10));
    }

    /**
     * El servicio rechaza identificadores no positivos
     * y el manejador global responde HTTP 400.
     */
    @Test
    void debeRetornar400CuandoIdentificadorEsInvalido()
            throws Exception {

        when(descubrimientoService.recomendarRecetas(
                Set.of(0)))
                .thenThrow(new IllegalArgumentException(
                        "El identificador debe ser positivo."));

        mockMvc.perform(get(
                        "/api/v1/descubrimiento/recomendaciones")
                        .param("ingredientes", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error")
                        .value("Bad Request"))
                .andExpect(jsonPath("$.message")
                        .value("La solicitud contiene parámetros inválidos."))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(descubrimientoService)
                .recomendarRecetas(Set.of(0));
    }

    /**
     * Verifica que una solicitud sin el parámetro
     * obligatorio sea rechazada por Spring MVC.
     */
    @Test
    void debeRetornar400CuandoFaltaParametroIngredientes()
            throws Exception {

        mockMvc.perform(get(
                        "/api/v1/descubrimiento/recomendaciones"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(descubrimientoService);
    }

    /**
     * Verifica que Spring convierta correctamente
     * identificadores repetidos a un conjunto único.
     */
    @Test
    void debeEliminarIngredientesDuplicados()
            throws Exception {

        when(descubrimientoService.recomendarRecetas(
                Set.of(10, 20)))
                .thenReturn(List.of());

        mockMvc.perform(get(
                        "/api/v1/descubrimiento/recomendaciones")
                        .param("ingredientes", "10,20,10"))
                .andExpect(status().isOk());

        verify(descubrimientoService)
                .recomendarRecetas(Set.of(10, 20));

        verifyNoMoreInteractions(descubrimientoService);
    }
}
