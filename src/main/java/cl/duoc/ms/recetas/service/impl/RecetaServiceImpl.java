package cl.duoc.ms.recetas.service.impl;

import java.util.List;

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
     * Excluye recetas publicadas que incumplan RN-PROP-01
     * y registra las anomalías detectadas.
     */
    @Override
    @Transactional(readOnly = true)
    public List<Receta> listarRecetasPublicadas() {

        return recetaRepository.findByPublicadaTrueAndActivoTrue()
                .stream()
                .filter(this::cumpleReglaIngredientesObligatorios)
                .toList();
    }

    private boolean cumpleReglaIngredientesObligatorios(Receta receta) {

        List<RecetaIngrediente> ingredientes =
                recetaIngredienteRepository
                        .findByReceta_IdRecetaOrderByOrdenAsc(
                                receta.getIdReceta());

        boolean cumpleRegla =
                recetaValidator.tieneIngredientesObligatorios(ingredientes);

        if (!cumpleRegla) {
            LOGGER.warn(
                    "RN-PROP-01: receta publicada con ID {} "
                            + "sin ingredientes obligatorios. "
                            + "Se excluye de la consulta pública.",
                    receta.getIdReceta());
        }

        return cumpleRegla;
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

        // Catálogo determina si existe al menos un producto activo.
        // Ante errores de comunicación, la publicación se interrumpe.
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