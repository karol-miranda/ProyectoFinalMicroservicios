package uv.listi.sicae_vehiculos.controller;

import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import uv.listi.sicae_vehiculos.dto.MensajeResponse;

@RestControllerAdvice
public class ErrorController {
    @ExceptionHandler(IllegalArgumentException.class)
    public MensajeResponse errorValidacion(IllegalArgumentException e) {
        return new MensajeResponse(false, e.getMessage());
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public MensajeResponse errorHeader(MissingRequestHeaderException e) {
        return new MensajeResponse(false, "Falta el header requerido: " + e.getHeaderName());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public MensajeResponse errorJson() {
        return new MensajeResponse(false, "El JSON enviado no tiene formato valido");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public MensajeResponse errorParametro(MethodArgumentTypeMismatchException e) {
        return new MensajeResponse(false, "El parametro " + e.getName() + " tiene un tipo de dato incorrecto");
    }
}
