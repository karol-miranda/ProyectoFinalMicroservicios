package uv.listi.sicae_usuarios.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import uv.listi.sicae_usuarios.dto.LoginRequest;
import uv.listi.sicae_usuarios.dto.LoginResponse;
import uv.listi.sicae_usuarios.dto.MensajeResponse;
import uv.listi.sicae_usuarios.dto.UsuarioPerfilResponse;
import uv.listi.sicae_usuarios.dto.UsuarioRequest;
import uv.listi.sicae_usuarios.dto.ValidacionUsuarioResponse;
import uv.listi.sicae_usuarios.model.Usuario;
import uv.listi.sicae_usuarios.repository.UsuarioRepository;
import uv.listi.sicae_usuarios.security.JwtUtil;

import java.util.Map;

@Service
public class UsuarioService {
    private final UsuarioRepository repository;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public UsuarioService(UsuarioRepository repository, JwtUtil jwtUtil) {
        this.repository = repository;
        this.jwtUtil = jwtUtil;
    }

    public LoginResponse login(LoginRequest request) {
        if (request == null) {
            return new LoginResponse(false, "La solicitud de login viene vacia", null, null, null, null, null, null, null, null, null);
        }
        if (vacio(request.usuario()) || vacio(request.password())) {
            return new LoginResponse(false, "Usuario y password son obligatorios", null, null, null, null, null, null, null, null, null);
        }
        Usuario usuario = repository.buscarPorUsername(request.usuario());
        if (usuario == null) {
            return new LoginResponse(false, "No existe un usuario registrado con ese username", null, null, null, null, null, null, null, null, null);
        }
        if (!"1".equals(usuario.getEstatus())) {
            return new LoginResponse(false, "El usuario existe, pero esta inactivo", null, null, null, null, null, null, null, null, null);
        }
        if (!encoder.matches(request.password(), usuario.getPassword())) {
            return new LoginResponse(false, "Password incorrecto", null, null, null, null, null, null, null, null, null);
        }
        String token = jwtUtil.generarToken(usuario.getIdUsuario(), usuario.getIdRol(), usuario.getUsername(), usuario.getClaveUsuario());
        return new LoginResponse(true, "Login correcto", usuario.getIdUsuario(), usuario.getIdRol(), usuario.getRol(),
                usuario.getUsername(), nombreCompleto(usuario), usuario.getIdTipoUsuario(), usuario.getTipoUsuario(),
                usuario.getClaveUsuario(), token);
    }

    public MensajeResponse registrar(String authorization, UsuarioRequest request) {
        Map<String, Object> token = jwtUtil.validar(authorization);
        if (((Number) token.get("idRol")).intValue() != 1) {
            return new MensajeResponse(false, "Solo un administrador puede registrar usuarios");
        }
        String error = validarRequest(request, true);
        if (error != null) {
            return new MensajeResponse(false, error);
        }
        if (repository.contarPorEmail(request.email()) > 0) {
            return new MensajeResponse(false, "Ya existe un usuario registrado con ese correo");
        }
        if (repository.contarPorUsername(request.username()) > 0) {
            return new MensajeResponse(false, "Ya existe un usuario registrado con ese username");
        }
        Usuario usuario = mapear(request);
        usuario.setPassword(encoder.encode(request.password()));
        usuario.setClaveUsuario(generarClaveUnica(request.nombre(), request.apellidoPaterno()));
        repository.registrar(usuario);
        return new MensajeResponse(true, "Usuario registrado con clave " + usuario.getClaveUsuario());
    }

    public MensajeResponse actualizar(String authorization, Integer idUsuario, UsuarioRequest request) {
        jwtUtil.validar(authorization);
        String error = validarRequest(request, false);
        if (error != null) {
            return new MensajeResponse(false, error);
        }
        Usuario actual = repository.buscarPorId(idUsuario);
        if (actual == null) {
            return new MensajeResponse(false, "Usuario no encontrado");
        }
        Usuario usuario = mapear(request);
        usuario.setIdUsuario(idUsuario);
        repository.actualizar(usuario);
        return new MensajeResponse(true, "Usuario actualizado correctamente");
    }

    public UsuarioPerfilResponse perfil(String authorization, Integer idUsuario) {
        jwtUtil.validar(authorization);
        Usuario usuario = repository.buscarPorId(idUsuario);
        if (usuario == null) {
            return null;
        }
        return new UsuarioPerfilResponse(usuario.getIdUsuario(), nombreCompleto(usuario), usuario.getClaveUsuario(),
                usuario.getEmail(), usuario.getTelefono(), usuario.getUsername(), usuario.getRol(),
                usuario.getTipoUsuario(), usuario.getProgramaEducativo(),
                "1".equals(usuario.getEstatus()) ? "activo" : "inactivo",
                String.valueOf(usuario.getTiempoCreacion()), String.valueOf(usuario.getTempoActualizacion()));
    }

    public MensajeResponse cambiarEstatus(String authorization, Integer idUsuario) {
        Map<String, Object> token = jwtUtil.validar(authorization);
        if (((Number) token.get("idRol")).intValue() != 1) {
            return new MensajeResponse(false, "Solo un administrador puede cambiar estatus");
        }
        Usuario usuario = repository.buscarPorId(idUsuario);
        if (usuario == null) {
            return new MensajeResponse(false, "Usuario no encontrado");
        }
        String nuevo = "1".equals(usuario.getEstatus()) ? "0" : "1";
        repository.cambiarEstatus(idUsuario, nuevo);
        return new MensajeResponse(true, "Estatus actualizado correctamente");
    }

    public ValidacionUsuarioResponse validarPorClave(String claveUsuario) {
        Usuario usuario = repository.buscarPorClave(claveUsuario);
        if (usuario == null) {
            return new ValidacionUsuarioResponse(false, null, claveUsuario, "Usuario no encontrado");
        }
        boolean activo = "1".equals(usuario.getEstatus());
        return new ValidacionUsuarioResponse(activo, usuario.getIdUsuario(), usuario.getClaveUsuario(),
                activo ? "Usuario activo" : "Usuario inactivo");
    }

    private Usuario mapear(UsuarioRequest request) {
        Usuario usuario = new Usuario();
        usuario.setIdRol(request.idRol());
        usuario.setIdTipoUsuario(request.idTipoUsuario());
        usuario.setIdProgramaEducativo(request.idProgramaEducativo());
        usuario.setNombre(request.nombre());
        usuario.setApellidoPaterno(request.apellidoPaterno());
        usuario.setApellidoMaterno(request.apellidoMaterno());
        usuario.setUsername(request.username());
        usuario.setEmail(request.email());
        usuario.setTelefono(request.telefono());
        return usuario;
    }

    private String validarRequest(UsuarioRequest request, boolean validarPassword) {
        if (request == null) {
            return "La solicitud de usuario viene vacia";
        }
        String faltantes = camposFaltantes(request, validarPassword);
        if (!faltantes.isEmpty()) {
            return "Faltan datos obligatorios: " + faltantes;
        }
        if (!request.email().contains("@")) {
            return "El correo no tiene formato valido";
        }
        return null;
    }

    private String camposFaltantes(UsuarioRequest request, boolean validarPassword) {
        StringBuilder campos = new StringBuilder();
        agregarCampo(campos, request.idRol() == null, "idRol");
        agregarCampo(campos, request.idTipoUsuario() == null, "idTipoUsuario");
        agregarCampo(campos, request.idProgramaEducativo() == null, "idProgramaEducativo");
        agregarCampo(campos, vacio(request.nombre()), "nombre");
        agregarCampo(campos, vacio(request.apellidoPaterno()), "apellidoPaterno");
        agregarCampo(campos, vacio(request.username()), "username");
        agregarCampo(campos, vacio(request.email()), "email");
        agregarCampo(campos, vacio(request.telefono()), "telefono");
        agregarCampo(campos, validarPassword && vacio(request.password()), "password");
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

    private String generarClaveUnica(String nombre, String apellido) {
        String clave = generarClave(nombre, apellido);
        while (repository.contarPorClave(clave) > 0) {
            clave = generarClave(nombre, apellido);
        }
        return clave;
    }

    private String generarClave(String nombre, String apellido) {
        String n = nombre.substring(0, Math.min(1, nombre.length())).toUpperCase();
        String a = apellido.substring(0, Math.min(2, apellido.length())).toUpperCase();
        int numero = (int) (Math.random() * 900 + 100);
        return n + a + "-" + numero;
    }

    private String nombreCompleto(Usuario usuario) {
        String materno = usuario.getApellidoMaterno() == null ? "" : " " + usuario.getApellidoMaterno();
        return usuario.getNombre() + " " + usuario.getApellidoPaterno() + materno;
    }

    private boolean vacio(String valor) {
        return valor == null || valor.trim().isEmpty();
    }
}
