
package cl.duoc.ms.recetas.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cl.duoc.ms.recetas.model.Receta;
import cl.duoc.ms.recetas.model.RecetaIngrediente;
import cl.duoc.ms.recetas.model.RecetaProducto;
import cl.duoc.ms.recetas.repository.RecetaIngredienteRepository;
import cl.duoc.ms.recetas.repository.RecetaProductoRepository;
import cl.duoc.ms.recetas.repository.RecetaRepository;
import cl.duoc.ms.recetas.service.CatalogoProductoService;
import cl.duoc.ms.recetas.service.RecetaService;
import cl.duoc.ms.recetas.service.RecetaValidator;

/**
 * Implementación de las operaciones de negocio para recetas.
 */
@Service
public class RecetaServiceImpl implements RecetaService {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(RecetaServiceImpl.class);

    private final RecetaRepository recetaRepository;
    private final RecetaIngredienteRepository recetaIngredienteRepository;
    private final RecetaProductoRepository recetaProductoRepository;
    private final RecetaValidator recetaValidator;
    private final CatalogoProductoService catalogoProductoService;

    @Autowired
    public RecetaServiceImpl(
            RecetaRepository recetaRepository,
            RecetaIngredienteRepository recetaIngredienteRepository,
            RecetaProductoRepository recetaProductoRepository,
            RecetaValidator recetaValidator,
            CatalogoProductoService catalogoProductoService) {

        this.recetaRepository = recetaRepository;
        this.recetaIngredienteRepository = recetaIngredienteRepository;
        this.recetaProductoRepository = recetaProductoRepository;
        this.recetaValidator = recetaValidator;
        this.catalogoProductoService = catalogoProductoService;
    }

    /**
     * Recupera recetas publicadas y activas.
     *
     * Aplica RN-PROP-01 y exige al menos un producto activo
     * en Catálogo antes de incluir una receta en la respuesta.
     *
     * Las asociaciones locales se consultan en bloque.
     */
    @Override
    @Transactional(readOnly = true)
    public List<Receta> listarRecetasPublicadas() {

        List<Receta> recetas =
                recetaRepository.findByPublicadaTrueAndActivoTrue();

        if (recetas.isEmpty()) {
            return List.of();
        }

        List<Integer> idsRecetas = recetas.stream()
                .map(Receta::getIdReceta)
                .toList();

        List<RecetaIngrediente> ingredientes =
                recetaIngredienteRepository
                        .findByReceta_IdRecetaIn(idsRecetas);

        List<RecetaProducto> productos =
                recetaProductoRepository
                        .findByReceta_IdRecetaIn(idsRecetas);

        Map<Integer, List<RecetaIngrediente>> ingredientesPorReceta =
                ingredientes.stream()
                        .collect(Collectors.groupingBy(
                                ingrediente ->
                                        ingrediente.getId().getIdReceta()));

        Map<Integer, List<RecetaProducto>> productosPorReceta =
                productos.stream()
                        .collect(Collectors.groupingBy(
                                producto ->
                                        producto.getId().getIdReceta()));

        // Evita consultar repetidamente el mismo producto externo.
        // La caché existe solamente durante esta operación.
        Map<Integer, Boolean> estadoProductos = new HashMap<>();

        List<Receta> resultado = new ArrayList<>();

        for (Receta receta : recetas) {

            Integer idReceta = receta.getIdReceta();

            List<RecetaIngrediente> ingredientesReceta =
                    ingredientesPorReceta.getOrDefault(
                            idReceta, List.of());

            if (!recetaValidator.tieneIngredientesObligatorios(
                    ingredientesReceta)) {

                LOGGER.warn(
                        "RN-PROP-01: receta publicada ID {} "
                                + "sin ingredientes obligatorios. "
                                + "Se excluye de la consulta pública.",
                        idReceta);

                continue;
            }

            List<RecetaProducto> productosReceta =
                    productosPorReceta.getOrDefault(
                            idReceta, List.of());

            if (!tieneProductoActivo(
                    idReceta, productosReceta, estadoProductos)) {
                continue;
            }

            resultado.add(receta);
        }

        return List.copyOf(resultado);
    }

    /**
     * Comprueba que exista al menos un producto activo
     * asociado a la receta.
     *
     * Ante errores de Catálogo, no se considera válido
     * el producto cuya condición no pudo verificarse.
     */
    private boolean tieneProductoActivo(
            Integer idReceta,
            List<RecetaProducto> productos,
            Map<Integer, Boolean> estadoProductos) {

        if (productos == null || productos.isEmpty()) {

            LOGGER.warn(
                    "Receta publicada ID {} sin productos asociados. "
                            + "Se excluye de la consulta pública.",
                    idReceta);

            return false;
        }

        Set<Integer> idsProductos = productos.stream()
                .filter(producto -> producto != null
                        && producto.getId() != null)
                .map(producto ->
                        producto.getId().getIdProductoExterno())
                .filter(id -> id != null && id > 0)
                .collect(Collectors.toSet());

        if (idsProductos.isEmpty()) {

            LOGGER.warn(
                    "Receta publicada ID {} sin identificadores "
                            + "de productos válidos.",
                    idReceta);

            return false;
        }

        for (Integer idProducto : idsProductos) {

            try {
                boolean activo = estadoProductos.computeIfAbsent(
                        idProducto,
                        catalogoProductoService::existeProductoActivo);

                if (activo) {
                    return true;
                }

            } catch (IllegalStateException excepcion) {

                LOGGER.warn(
                        "No fue posible verificar el producto {} "
                                + "de la receta {}: {}",
                        idProducto,
                        idReceta,
                        excepcion.getMessage());
            }
        }

        LOGGER.warn(
                "Receta publicada ID {} sin productos activos "
                        + "verificados. Se excluye de la consulta pública.",
                idReceta);

        return false;
    }

    /**
     * Publica una receta únicamente cuando cumple
     * las reglas de negocio correspondientes.
     */
    @Override
    @Transactional
    public Receta publicarReceta(Integer idReceta) {

        if (idReceta == null || idReceta <= 0) {
            throw new IllegalArgumentException(
                    "El identificador de la receta debe ser positivo.");
        }

        Receta receta = recetaRepository.findById(idReceta)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No existe una receta con identificador: " + idReceta));

        if (!receta.isActivo()) {
            throw new IllegalStateException(
                    "No se puede publicar una receta inactiva.");
        }

        List<RecetaIngrediente> ingredientes =
                recetaIngredienteRepository
                        .findByReceta_IdRecetaOrderByOrdenAsc(idReceta);

        recetaValidator.validarIngredientesObligatorios(ingredientes);

        List<RecetaProducto> productos =
                recetaProductoRepository.findByReceta_IdReceta(idReceta);

        recetaValidator.validarProductosAsociados(productos);

        boolean existeProductoActivo = productos.stream()
                .filter(producto -> producto != null
                        && producto.getId() != null
                        && producto.getId().getIdProductoExterno() != null
                        && producto.getId().getIdProductoExterno() > 0)
                .anyMatch(producto ->
                        catalogoProductoService.existeProductoActivo(
                                producto.getId().getIdProductoExterno()));

        if (!existeProductoActivo) {
            throw new IllegalStateException(
                    "La receta debe tener al menos un producto activo en Catálogo.");
        }

        receta.marcarComoPublicada();

        return recetaRepository.save(receta);
    }
}
