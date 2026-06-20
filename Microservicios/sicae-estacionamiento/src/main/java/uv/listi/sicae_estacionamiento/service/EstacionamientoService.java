package uv.listi.sicae_estacionamiento.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
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

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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
        String error = validarEntrada(request);
        if (error != null) {
            return new MensajeResponse(false, error);
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
        int vehiculosDentro = contarVehiculosDentro(authorization, usuario.idUsuario());
        if (vehiculosDentro < 0) {
            return new MensajeResponse(false, "No se pudo consultar la lista de vehiculos del usuario");
        }
        if (vehiculosDentro >= 2) {
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
        String error = validarSalida(request);
        if (error != null) {
            return new MensajeResponse(false, error);
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
        try {
            String clave = URLEncoder.encode(claveUsuario, StandardCharsets.UTF_8);
            String url = usuariosUrl + "/api/usuarios/validar/" + clave;
            return restTemplate.getForObject(url, ValidacionUsuarioResponse.class);
        } catch (RestClientException e) {
            return null;
        }
    }

    private ValidacionVehiculoResponse validarVehiculo(String placa, Integer idUsuario) {
        try {
            String placaUrl = URLEncoder.encode(placa.toUpperCase(), StandardCharsets.UTF_8);
            String url = vehiculosUrl + "/api/vehiculos/validar?placa=" + placaUrl + "&idUsuario=" + idUsuario;
            return restTemplate.getForObject(url, ValidacionVehiculoResponse.class);
        } catch (RestClientException e) {
            return null;
        }
    }

    private int contarVehiculosDentro(String authorization, Integer idUsuario) {
        VehiculoClienteResponse[] vehiculos;
        try {
            String url = vehiculosUrl + "/api/vehiculos/usuario/" + idUsuario;
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", authorization);
            ResponseEntity<VehiculoClienteResponse[]> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    VehiculoClienteResponse[].class
            );
            vehiculos = response.getBody();
        } catch (RestClientException e) {
            return -1;
        }
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

    private String validarEntrada(EntradaRequest request) {
        if (request == null) {
            return "La solicitud de entrada viene vacia";
        }
        String faltantes = camposFaltantesEntrada(request);
        if (!faltantes.isEmpty()) {
            return "Faltan datos obligatorios: " + faltantes;
        }
        if (request.tarifaHora().compareTo(BigDecimal.ZERO) <= 0) {
            return "La tarifaHora debe ser mayor a cero";
        }
        return null;
    }

    private String validarSalida(SalidaRequest request) {
        if (request == null) {
            return "La solicitud de salida viene vacia";
        }
        StringBuilder campos = new StringBuilder();
        agregarCampo(campos, vacio(request.claveUsuario()), "claveUsuario");
        agregarCampo(campos, vacio(request.placa()), "placa");
        if (campos.length() > 0) {
            return "Faltan datos obligatorios: " + campos;
        }
        return null;
    }

    private String camposFaltantesEntrada(EntradaRequest request) {
        StringBuilder campos = new StringBuilder();
        agregarCampo(campos, vacio(request.claveUsuario()), "claveUsuario");
        agregarCampo(campos, vacio(request.placa()), "placa");
        agregarCampo(campos, request.idEspacio() == null, "idEspacio");
        agregarCampo(campos, request.tarifaHora() == null, "tarifaHora");
        return campos.toString();
    }

    private void agregarCampo(StringBuilder campos, boolean falta, String nombre) {
        if (!falta) {
            return;
        }
        if (campos.length() > 0) {
            campos.append(", ");
        }
        campos.append(nombre);
    }

    private boolean vacio(String valor) {
        return valor == null || valor.trim().isEmpty();
    }
}
