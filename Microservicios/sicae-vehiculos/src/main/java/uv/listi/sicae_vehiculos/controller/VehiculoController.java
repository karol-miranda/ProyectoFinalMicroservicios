package uv.listi.sicae_vehiculos.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uv.listi.sicae_vehiculos.dto.MensajeResponse;
import uv.listi.sicae_vehiculos.dto.ValidacionVehiculoResponse;
import uv.listi.sicae_vehiculos.dto.VehiculoRequest;
import uv.listi.sicae_vehiculos.model.Vehiculo;
import uv.listi.sicae_vehiculos.service.VehiculoService;

import java.util.List;

@RestController
@RequestMapping("/api/vehiculos")
public class VehiculoController {
    private final VehiculoService service;

    public VehiculoController(VehiculoService service) {
        this.service = service;
    }

    @GetMapping("/usuario/{idUsuario}")
    public List<Vehiculo> buscarPorUsuario(@RequestHeader("Authorization") String token, @PathVariable Integer idUsuario) {
        return service.buscarPorUsuario(token, idUsuario);
    }

    @PostMapping
    public MensajeResponse registrar(@RequestHeader("Authorization") String token, @RequestBody VehiculoRequest request) {
        return service.registrar(token, request);
    }

    @PutMapping("/{idVehiculo}")
    public MensajeResponse actualizar(@RequestHeader("Authorization") String token,
                                      @PathVariable Integer idVehiculo,
                                      @RequestBody VehiculoRequest request) {
        return service.actualizar(token, idVehiculo, request);
    }

    @PatchMapping("/{idVehiculo}/estatus")
    public MensajeResponse cambiarEstatus(@RequestHeader("Authorization") String token, @PathVariable Integer idVehiculo) {
        return service.cambiarEstatus(token, idVehiculo);
    }

    @GetMapping("/validar")
    public ValidacionVehiculoResponse validar(@RequestParam String placa, @RequestParam Integer idUsuario) {
        return service.validar(placa, idUsuario);
    }
}
