
package cl.duoc.ms.recetas.exception;

import java.time.Instant;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Centraliza el tratamiento de excepciones de los
 * controladores REST del microservicio.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Convierte los argumentos inválidos en respuestas
     * HTTP 400 sin exponer detalles internos del sistema.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> manejarArgumentoInvalido(
            IllegalArgumentException excepcion) {

        Map<String, Object> respuesta = Map.of(
                "timestamp", Instant.now().toString(),
                "status", HttpStatus.BAD_REQUEST.value(),
                "error", "Bad Request",
                "message", "La solicitud contiene parámetros inválidos."
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(respuesta);
    }
}
