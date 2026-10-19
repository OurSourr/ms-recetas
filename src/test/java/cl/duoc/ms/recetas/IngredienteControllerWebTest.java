package cl.duoc.ms.recetas;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import cl.duoc.ms.recetas.controller.IngredienteController;
import cl.duoc.ms.recetas.exception.GlobalExceptionHandler;
import cl.duoc.ms.recetas.model.Ingrediente;
import cl.duoc.ms.recetas.service.IngredienteService;

/**
 * Pruebas HTTP del controlador de ingredientes.
 *
 * Verifica la ruta pública, la serialización JSON,
 * las listas vacías y el tratamiento de errores.
 *
 * No requiere servidor, MySQL ni otros microservicios.
 */
class IngredienteControllerWebTest {

    private MockMvc mockMvc;
    private IngredienteService ingredienteService;

    @BeforeEach
    void configurar() {

        ingredienteService = mock(IngredienteService.class);

        IngredienteController controlador =
                new IngredienteController(ingredienteService);

        mockMvc = MockMvcBuilders
                .standaloneSetup(controlador)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    /**
     * Verifica que los ingredientes entregados por el servicio
     * se transformen correctamente en respuestas JSON.
     */
    @Test
    void debeRetornarIngredientesActivosEnJson() throws Exception {

        Ingrediente gin = crearIngrediente(
                1, "Gin", "Destilado de enebro.");

        Ingrediente limon = crearIngrediente(
                2, "Limón", "Fruta cítrica.");

        when(ingredienteService.listarIngredientesActivos())
                .thenReturn(List.of(gin, limon));

        mockMvc.perform(get("/api/v1/ingredientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].idIngrediente").value(1))
                .andExpect(jsonPath("$[0].nombre").value("Gin"))
                .andExpect(jsonPath("$[0].descripcion")
                        .value("Destilado de enebro."))
                .andExpect(jsonPath("$[1].idIngrediente").value(2))
                .andExpect(jsonPath("$[1].nombre").value("Limón"))
                .andExpect(jsonPath("$[1].descripcion")
                        .value("Fruta cítrica."));

        verify(ingredienteService).listarIngredientesActivos();
        verifyNoMoreInteractions(ingredienteService);
    }

    /**
     * Cuando no existen ingredientes activos,
     * debe devolver HTTP 200 con un arreglo vacío.
     */
    @Test
    void debeRetornarListaVaciaCuandoNoHayIngredientes()
            throws Exception {

        when(ingredienteService.listarIngredientesActivos())
                .thenReturn(List.of());

        mockMvc.perform(get("/api/v1/ingredientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(ingredienteService).listarIngredientesActivos();
        verifyNoMoreInteractions(ingredienteService);
    }

    /**
     * Comprueba que el controlador delegue el filtrado
     * de ingredientes activos en la capa de servicio.
     */
    @Test
    void debeConsultarUnicamenteElServicioDeIngredientes()
            throws Exception {

        when(ingredienteService.listarIngredientesActivos())
                .thenReturn(List.of());

        mockMvc.perform(get("/api/v1/ingredientes"))
                .andExpect(status().isOk());

        verify(ingredienteService, times(1))
                .listarIngredientesActivos();

        verifyNoMoreInteractions(ingredienteService);
    }

    /**
     * Un ingrediente con datos inválidos no debe
     * producir una respuesta JSON incorrecta.
     */
    @Test
    void debeRetornar400CuandoIngredienteTieneDatosInvalidos()
            throws Exception {

        Ingrediente ingrediente = crearIngrediente(
                0, "Gin", "Destilado de enebro.");

        when(ingredienteService.listarIngredientesActivos())
                .thenReturn(List.of(ingrediente));

        mockMvc.perform(get("/api/v1/ingredientes"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message")
                        .value("La solicitud contiene parámetros inválidos."))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(ingredienteService).listarIngredientesActivos();
        verifyNoMoreInteractions(ingredienteService);
    }

    /**
     * Construye un ingrediente simulado reutilizable.
     */
    private Ingrediente crearIngrediente(
            Integer id,
            String nombre,
            String descripcion) {

        Ingrediente ingrediente = mock(Ingrediente.class);

        when(ingrediente.getIdIngrediente()).thenReturn(id);
        when(ingrediente.getNombre()).thenReturn(nombre);
        when(ingrediente.getDescripcion()).thenReturn(descripcion);

        return ingrediente;
    }
}