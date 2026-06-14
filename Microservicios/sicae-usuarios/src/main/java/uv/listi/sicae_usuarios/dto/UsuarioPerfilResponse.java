package uv.listi.sicae_usuarios.dto;

public record UsuarioPerfilResponse(
        Integer idUsuario,
        String nombreCompleto,
        String claveUsuario,
        String email,
        String telefono,
        String username,
        String rol,
        String tipoUsuario,
        String programaEducativo,
        String estatus,
        String tiempoCreacion,
        String tiempoActualizacion
) {
}
