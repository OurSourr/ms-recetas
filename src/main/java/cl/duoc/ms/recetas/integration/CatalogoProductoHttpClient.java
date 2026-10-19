
package cl.duoc.ms.recetas.integration;

import java.net.http.HttpClient;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.JdkClientHttpRequestFactory;
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

    /**
     * Constructor utilizado por Spring en producción.
     * Configura los tiempos máximos de conexión y respuesta.
     */
    @Autowired
    public CatalogoProductoHttpClient(
            RestClient.Builder restClientBuilder,
            @Value("${catalogo.base-url}") String catalogoBaseUrl,
            @Value("${catalogo.connect-timeout}") Duration connectTimeout,
            @Value("${catalogo.read-timeout}") Duration readTimeout) {

        validarConfiguracion(
                catalogoBaseUrl,
                connectTimeout,
                readTimeout);

        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(connectTimeout)
                .build();

        JdkClientHttpRequestFactory requestFactory =
                new JdkClientHttpRequestFactory(httpClient);

        requestFactory.setReadTimeout(readTimeout);

        this.restClient = restClientBuilder
                .baseUrl(catalogoBaseUrl)
                .requestFactory(requestFactory)
                .build();
    }

    /**
     * Constructor alternativo para pruebas con un cliente HTTP simulado.
     * No reemplaza la fábrica HTTP configurada en el builder.
     */
    public CatalogoProductoHttpClient(
            RestClient.Builder restClientBuilder,
            String catalogoBaseUrl,
            Duration connectTimeout,
            Duration readTimeout,
            boolean usarClienteSimulado) {

        validarConfiguracion(
                catalogoBaseUrl,
                connectTimeout,
                readTimeout);

        if (!usarClienteSimulado) {
            throw new IllegalArgumentException(
                    "Este constructor está reservado para clientes simulados.");
        }

        this.restClient = restClientBuilder
                .baseUrl(catalogoBaseUrl)
                .build();
    }

    private static void validarConfiguracion(
            String catalogoBaseUrl,
            Duration connectTimeout,
            Duration readTimeout) {

        if (catalogoBaseUrl == null || catalogoBaseUrl.isBlank()) {
            throw new IllegalArgumentException(
                    "La URL del microservicio Catálogo es obligatoria.");
        }

        if (connectTimeout == null
                || connectTimeout.isZero()
                || connectTimeout.isNegative()) {
            throw new IllegalArgumentException(
                    "El tiempo de conexión debe ser positivo.");
        }

        if (readTimeout == null
                || readTimeout.isZero()
                || readTimeout.isNegative()) {
            throw new IllegalArgumentException(
                    "El tiempo de respuesta debe ser positivo.");
        }
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

    private record ProductoCatalogoResponse(
            Integer idProducto,
            Boolean activo) {
    }

    private static class ProductoNoEncontradoException
            extends RuntimeException {

        private static final long serialVersionUID = 1L;
    }
}
