package uv.listi.sicae_estacionamiento.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uv.listi.sicae_estacionamiento.dto.EntradaRequest;
import uv.listi.sicae_estacionamiento.dto.SalidaRequest;
import uv.listi.sicae_estacionamiento.model.Espacio;
import uv.listi.sicae_estacionamiento.service.EstacionamientoService;

import java.util.List;

@RestController
@RequestMapping("/api/estacionamiento")
public class EstacionamientoController {
    private final EstacionamientoService service;

    public EstacionamientoController(EstacionamientoService service) {
        this.service = service;
    }

    @GetMapping("/espacios")
    public List<Espacio> espacios(@RequestHeader("Authorization") String token) {
        return service.consultarEspacios(token);
    }

    @PostMapping("/entrada")
    public Object entrada(@RequestHeader("Authorization") String token, @RequestBody EntradaRequest request) {
        return service.registrarEntrada(token, request);
    }

    @PutMapping("/salida")
    public Object salida(@RequestHeader("Authorization") String token, @RequestBody SalidaRequest request) {
        return service.registrarSalida(token, request);
    }
}
