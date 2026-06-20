package uv.listi.sicae_vehiculos.service;

import org.springframework.stereotype.Service;
import uv.listi.sicae_vehiculos.dto.MensajeResponse;
import uv.listi.sicae_vehiculos.dto.ValidacionVehiculoResponse;
import uv.listi.sicae_vehiculos.dto.VehiculoRequest;
import uv.listi.sicae_vehiculos.model.Vehiculo;
import uv.listi.sicae_vehiculos.repository.VehiculoRepository;
import uv.listi.sicae_vehiculos.security.JwtUtil;

import java.util.List;
import java.util.Map;

@Service
public class VehiculoService {
    private final VehiculoRepository repository;
    private final JwtUtil jwtUtil;

    public VehiculoService(VehiculoRepository repository, JwtUtil jwtUtil) {
        this.repository = repository;
        this.jwtUtil = jwtUtil;
    }

    public List<Vehiculo> buscarPorUsuario(String authorization, Integer idUsuario) {
        jwtUtil.validar(authorization);
        return repository.buscarPorUsuario(idUsuario);
    }

    public MensajeResponse registrar(String authorization, VehiculoRequest request) {
        jwtUtil.validar(authorization);
        String error = validarRequest(request);
        if (error != null) {
            return new MensajeResponse(false, error);
        }
        if (repository.buscarPorPlaca(request.placa().toUpperCase()) != null) {
            return new MensajeResponse(false, "Ya existe un vehiculo con esa placa");
        }
        if (repository.contarActivosPorUsuario(request.idUsuario()) >= 4) {
            return new MensajeResponse(false, "El usuario ya tiene 4 vehiculos activos");
        }
        Vehiculo vehiculo = mapear(request);
        vehiculo.setClaveVehiculo(generarClaveVehiculo(request.placa()));
        repository.registrar(vehiculo);
        return new MensajeResponse(true, "Vehiculo registrado correctamente");
    }

    public MensajeResponse actualizar(String authorization, Integer idVehiculo, VehiculoRequest request) {
        Map<String, Object> token = jwtUtil.validar(authorization);
        String error = validarRequest(request);
        if (error != null) {
            return new MensajeResponse(false, error);
        }
        Vehiculo actual = repository.buscarPorId(idVehiculo);
        if (actual == null) {
            return new MensajeResponse(false, "Vehiculo no encontrado");
        }
        Integer idUsuarioToken = ((Number) token.get("idUsuario")).intValue();
        if (!actual.getIdUsuario().equals(idUsuarioToken) && ((Number) token.get("idRol")).intValue() != 1) {
            return new MensajeResponse(false, "Solo se puede editar un vehiculo propio");
        }
        if (repository.contarPlacaEnOtroVehiculo(request.placa().toUpperCase(), idVehiculo) > 0) {
            return new MensajeResponse(false, "Ya existe otro vehiculo con esa placa");
        }
        Vehiculo vehiculo = mapear(request);
        vehiculo.setIdVehiculo(idVehiculo);
        int filas = repository.actualizar(vehiculo);
        if (filas == 0) {
            return new MensajeResponse(false, "No se actualizo el vehiculo porque el idUsuario enviado no coincide");
        }
        return new MensajeResponse(true, "Vehiculo actualizado correctamente");
    }

    public MensajeResponse cambiarEstatus(String authorization, Integer idVehiculo) {
        Map<String, Object> token = jwtUtil.validar(authorization);
        Integer idUsuario = ((Number) token.get("idUsuario")).intValue();
        Vehiculo actual = repository.buscarPorId(idVehiculo);
        if (actual == null) {
            return new MensajeResponse(false, "Vehiculo no encontrado");
        }
        if (!actual.getIdUsuario().equals(idUsuario) && ((Number) token.get("idRol")).intValue() != 1) {
            return new MensajeResponse(false, "Solo se puede cambiar estatus de un vehiculo propio");
        }
        repository.cambiarEstatus(idVehiculo, actual.getIdUsuario());
        return new MensajeResponse(true, "Estatus actualizado correctamente");
    }

    public ValidacionVehiculoResponse validar(String placa, Integer idUsuario) {
        if (vacio(placa) || idUsuario == null) {
            return new ValidacionVehiculoResponse(false, null, idUsuario, placa, "La placa y el idUsuario son obligatorios");
        }
        Vehiculo vehiculo = repository.buscarPorPlaca(placa.toUpperCase());
        if (vehiculo == null) {
            return new ValidacionVehiculoResponse(false, null, idUsuario, placa, "Vehiculo no encontrado");
        }
        if (!vehiculo.getIdUsuario().equals(idUsuario)) {
            return new ValidacionVehiculoResponse(false, vehiculo.getIdVehiculo(), idUsuario, placa, "El vehiculo no pertenece al usuario");
        }
        if (!Boolean.TRUE.equals(vehiculo.getEstatus())) {
            return new ValidacionVehiculoResponse(false, vehiculo.getIdVehiculo(), idUsuario, placa, "Vehiculo inactivo");
        }
        return new ValidacionVehiculoResponse(true, vehiculo.getIdVehiculo(), idUsuario, placa, "Vehiculo valido");
    }

    private Vehiculo mapear(VehiculoRequest request) {
        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setIdUsuario(request.idUsuario());
        vehiculo.setIdModelo(request.idModelo());
        vehiculo.setPlaca(request.placa() == null ? null : request.placa().toUpperCase());
        vehiculo.setColor(request.color());
        vehiculo.setAnio(request.anio());
        vehiculo.setDescripcion(request.descripcion());
        return vehiculo;
    }

    private String validarRequest(VehiculoRequest request) {
        if (request == null) {
            return "La solicitud de vehiculo viene vacia";
        }
        String faltantes = camposFaltantes(request);
        if (!faltantes.isEmpty()) {
            return "Faltan datos obligatorios: " + faltantes;
        }
        if (request.placa().length() > 7) {
            return "La placa no puede tener mas de 7 caracteres";
        }
        if (request.anio() < 1900) {
            return "El anio del vehiculo no es valido";
        }
        return null;
    }

    private String camposFaltantes(VehiculoRequest request) {
        StringBuilder campos = new StringBuilder();
        agregarCampo(campos, request.idUsuario() == null, "idUsuario");
        agregarCampo(campos, request.idModelo() == null, "idModelo");
        agregarCampo(campos, vacio(request.placa()), "placa");
        agregarCampo(campos, vacio(request.color()), "color");
        agregarCampo(campos, request.anio() == null, "anio");
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

    private String generarClaveVehiculo(String placa) {
        String limpia = placa.replaceAll("[^A-Za-z0-9]", "").toUpperCase();
        return limpia.substring(0, Math.min(4, limpia.length())) + "-" + (int) (Math.random() * 900 + 100);
    }

    private boolean vacio(String valor) {
        return valor == null || valor.trim().isEmpty();
    }
}
