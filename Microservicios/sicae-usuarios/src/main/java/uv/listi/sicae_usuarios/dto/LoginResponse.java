package uv.listi.sicae_usuarios.dto;

public record LoginResponse(
        boolean exito,
        String mensaje,
        Integer idUsuario,
        Integer idRol,
        String rol,
        String usuario,
        String nombreCompleto,
        Integer idTipoUsuario,
        String tipoUsuario,
        String claveUsuario,
        String token
) {
}
