package uv.listi.sicae_estacionamiento.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uv.listi.sicae_estacionamiento.dto.EntradaRequest;
import uv.listi.sicae_estacionamiento.dto.MensajeResponse;
import uv.listi.sicae_estacionamiento.dto.SalidaRequest;
import uv.listi.sicae_estacionamiento.dto.ValidacionUsuarioResponse;
import uv.listi.sicae_estacionamiento.dto.ValidacionVehiculoResponse;
import uv.listi.sicae_estacionamiento.dto.VehiculoClienteResponse;
import uv.listi.sicae_estacionamiento.model.Espacio;
import uv.listi.sicae_estacionamiento.model.Movimiento;
import uv.listi.sicae_estacionamiento.repository.EstacionamientoRepository;
import uv.listi.sicae_estacionamiento.security.JwtUtil;

import java.util.List;

@Service
public class EstacionamientoService {
    private final EstacionamientoRepository repository;
    private final JwtUtil jwtUtil;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${servicios.usuarios.url}")
    private String usuariosUrl;

    @Value("${servicios.vehiculos.url}")
    private String vehiculosUrl;

    public EstacionamientoService(EstacionamientoRepository repository, JwtUtil jwtUtil) {
        this.repository = repository;
        this.jwtUtil = jwtUtil;
    }

    public List<Espacio> consultarEspacios(String authorization) {
        jwtUtil.validar(authorization);
        return repository.consultarEspacios();
    }

    public Object registrarEntrada(String authorization, EntradaRequest request) {
        jwtUtil.validar(authorization);
        if (request == null || vacio(request.claveUsuario()) || vacio(request.placa()) ||
                request.idEspacio() == null || request.tarifaHora() == null) {
            return new MensajeResponse(false, "Faltan datos obligatorios");
        }

        ValidacionUsuarioResponse usuario = validarUsuario(request.claveUsuario());
        if (usuario == null || !usuario.activo()) {
            return new MensajeResponse(false, usuario == null ? "No se pudo validar el usuario" : usuario.mensaje());
        }

        ValidacionVehiculoResponse vehiculo = validarVehiculo(request.placa(), usuario.idUsuario());
        if (vehiculo == null || !vehiculo.valido()) {
            return new MensajeResponse(false, vehiculo == null ? "No se pudo validar el vehiculo" : vehiculo.mensaje());
        }

        Espacio espacio = repository.buscarEspacio(request.idEspacio());
        if (espacio == null || !Boolean.TRUE.equals(espacio.getEstatus())) {
            return new MensajeResponse(false, "El espacio no existe o esta inactivo");
        }
        if (Boolean.TRUE.equals(espacio.getOcupado())) {
            return new MensajeResponse(false, "El espacio ya esta ocupado");
        }
        if (repository.contarMovimientoAbierto(vehiculo.idVehiculo()) > 0) {
            return new MensajeResponse(false, "El vehiculo ya tiene una entrada activa");
        }
        if (contarVehiculosDentro(authorization, usuario.idUsuario()) >= 2) {
            return new MensajeResponse(false, "El usuario ya tiene 2 vehiculos dentro del estacionamiento");
        }

        Movimiento movimiento = new Movimiento();
        movimiento.setIdVehiculo(vehiculo.idVehiculo());
        movimiento.setIdEspacio(request.idEspacio());
        movimiento.setTarifaHora(request.tarifaHora());
        repository.registrarEntrada(movimiento);
        repository.actualizarOcupado(request.idEspacio(), true);
        return repository.buscarMovimiento(movimiento.getIdMovimiento());
    }

    public Object registrarSalida(String authorization, SalidaRequest request) {
        jwtUtil.validar(authorization);
        if (request == null || vacio(request.claveUsuario()) || vacio(request.placa())) {
            return new MensajeResponse(false, "Faltan datos obligatorios");
        }
        ValidacionUsuarioResponse usuario = validarUsuario(request.claveUsuario());
        if (usuario == null || !usuario.activo()) {
            return new MensajeResponse(false, usuario == null ? "No se pudo validar el usuario" : usuario.mensaje());
        }
        ValidacionVehiculoResponse vehiculo = validarVehiculo(request.placa(), usuario.idUsuario());
        if (vehiculo == null || !vehiculo.valido()) {
            return new MensajeResponse(false, vehiculo == null ? "No se pudo validar el vehiculo" : vehiculo.mensaje());
        }
        Movimiento abierto = repository.buscarMovimientoAbierto(vehiculo.idVehiculo());
        if (abierto == null) {
            return new MensajeResponse(false, "No hay una entrada activa para ese vehiculo");
        }
        repository.registrarSalida(abierto.getIdMovimiento());
        repository.actualizarOcupado(abierto.getIdEspacio(), false);
        return repository.buscarMovimiento(abierto.getIdMovimiento());
    }

    private ValidacionUsuarioResponse validarUsuario(String claveUsuario) {
        String url = usuariosUrl + "/api/usuarios/validar/" + claveUsuario;
        return restTemplate.getForObject(url, ValidacionUsuarioResponse.class);
    }

    private ValidacionVehiculoResponse validarVehiculo(String placa, Integer idUsuario) {
        String url = vehiculosUrl + "/api/vehiculos/validar?placa=" + placa + "&idUsuario=" + idUsuario;
        return restTemplate.getForObject(url, ValidacionVehiculoResponse.class);
    }

    private int contarVehiculosDentro(String authorization, Integer idUsuario) {
        String url = vehiculosUrl + "/api/vehiculos/usuario/" + idUsuario;
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authorization);
        ResponseEntity<VehiculoClienteResponse[]> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                VehiculoClienteResponse[].class
        );
        VehiculoClienteResponse[] vehiculos = response.getBody();
        if (vehiculos == null) {
            return 0;
        }
        int total = 0;
        for (VehiculoClienteResponse vehiculo : vehiculos) {
            if (repository.contarMovimientoAbierto(vehiculo.idVehiculo()) > 0) {
                total++;
            }
        }
        return total;
    }

    private boolean vacio(String valor) {
        return valor == null || valor.trim().isEmpty();
    }
}
