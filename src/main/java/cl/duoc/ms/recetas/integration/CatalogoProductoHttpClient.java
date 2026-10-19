package cl.duoc.ms.recetas.integration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import cl.duoc.ms.recetas.service.CatalogoProductoService;

/**
 * Cliente HTTP para consultar productos del microservicio Catálogo.
 */
@Component
public class CatalogoProductoHttpClient implements CatalogoProductoService {

    private final RestClient restClient;

    @Autowired
    public CatalogoProductoHttpClient(
            RestClient.Builder restClientBuilder,
            @Value("${catalogo.base-url}") String catalogoBaseUrl) {

        if (catalogoBaseUrl == null || catalogoBaseUrl.isBlank()) {
            throw new IllegalArgumentException(
                    "La URL del microservicio Catálogo es obligatoria.");
        }

        this.restClient = restClientBuilder
                .baseUrl(catalogoBaseUrl)
                .build();
    }

    @Override
    public boolean existeProductoActivo(Integer idProductoExterno) {

        if (idProductoExterno == null || idProductoExterno <= 0) {
            throw new IllegalArgumentException(
                    "El identificador del producto debe ser positivo.");
        }

        try {
            ProductoCatalogoResponse producto = restClient.get()
                    .uri("/api/v1/productos/{id}", idProductoExterno)
                    .retrieve()
                    .onStatus(
                            estado -> estado.value() == 404,
                            (solicitud, respuesta) -> {
                                // Un producto inexistente no cumple la regla.
                                throw new ProductoNoEncontradoException();
                            })
                    .onStatus(
                            HttpStatusCode::isError,
                            (solicitud, respuesta) -> {
                                throw new IllegalStateException(
                                        "Catálogo devolvió un error HTTP: "
                                                + respuesta.getStatusCode());
                            })
                    .body(ProductoCatalogoResponse.class);

            if (producto == null
                    || producto.idProducto() == null
                    || !idProductoExterno.equals(producto.idProducto())
                    || producto.activo() == null) {

                throw new IllegalStateException(
                        "Catálogo devolvió una respuesta inválida.");
            }

            return producto.activo();

        } catch (ProductoNoEncontradoException excepcion) {
            return false;

        } catch (RestClientException excepcion) {
            throw new IllegalStateException(
                    "No fue posible verificar el producto en Catálogo.",
                    excepcion);
        }
    }

    /**
     * Representa únicamente los campos necesarios de la respuesta.
     */
    private record ProductoCatalogoResponse(
            Integer idProducto,
            Boolean activo) {
    }

    /**
     * Distingue un producto inexistente de un fallo de comunicación.
     */
    private static class ProductoNoEncontradoException
            extends RuntimeException {

        private static final long serialVersionUID = 1L;
    }
}