
package cl.duoc.ms.recetas;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import cl.duoc.ms.recetas.controller.CategoriaIngredienteController;
import cl.duoc.ms.recetas.model.CategoriaIngrediente;
import cl.duoc.ms.recetas.service.CategoriaIngredienteService;

/**
 * Pruebas HTTP del controlador de categorías de ingredientes.
 *
 * Se utiliza MockMvc sin levantar el servidor ni conectarse
 * a la base de datos.
 */
class CategoriaIngredienteControllerWebTest {

    private CategoriaIngredienteService categoriaIngredienteService;
    private MockMvc mockMvc;

    @BeforeEach
    void configurar() {
        categoriaIngredienteService = mock(CategoriaIngredienteService.class);

        CategoriaIngredienteController controller =
                new CategoriaIngredienteController(categoriaIngredienteService);

        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void listarCategoriasRetornaCategoriasActivas() throws Exception {
        CategoriaIngrediente categoria = crearCategoria(
                1, "Destilados", "Bebidas alcohólicas destiladas.");

        when(categoriaIngredienteService.listarCategoriasActivas())
                .thenReturn(List.of(categoria));

        mockMvc.perform(get("/api/v1/categorias-ingredientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idCategoria").value(1))
                .andExpect(jsonPath("$[0].nombre").value("Destilados"))
                .andExpect(jsonPath("$[0].descripcion")
                        .value("Bebidas alcohólicas destiladas."));
    }

    @Test
    void listarCategoriasRetornaListaVacia() throws Exception {
        when(categoriaIngredienteService.listarCategoriasActivas())
                .thenReturn(List.of());

        mockMvc.perform(get("/api/v1/categorias-ingredientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void listarCategoriasConsultaElServicioUnaVez() throws Exception {
        when(categoriaIngredienteService.listarCategoriasActivas())
                .thenReturn(List.of());

        mockMvc.perform(get("/api/v1/categorias-ingredientes"))
                .andExpect(status().isOk());

        verify(categoriaIngredienteService).listarCategoriasActivas();
    }

    @Test
    void listarCategoriasNoExponeCampoActivo() throws Exception {
        CategoriaIngrediente categoria = crearCategoria(
                2, "Frutas", "Frutas utilizadas en coctelería.");

        when(categoriaIngredienteService.listarCategoriasActivas())
                .thenReturn(List.of(categoria));

        mockMvc.perform(get("/api/v1/categorias-ingredientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].activo").doesNotExist());
    }

    /**
     * Crea una entidad simulada para evitar depender de
     * constructores de persistencia o de MySQL.
     */
    private CategoriaIngrediente crearCategoria(
            Integer id, String nombre, String descripcion) {

        CategoriaIngrediente categoria = mock(CategoriaIngrediente.class);

        when(categoria.getIdCategoria()).thenReturn(id);
        when(categoria.getNombre()).thenReturn(nombre);
        when(categoria.getDescripcion()).thenReturn(descripcion);

        return categoria;
    }
}
