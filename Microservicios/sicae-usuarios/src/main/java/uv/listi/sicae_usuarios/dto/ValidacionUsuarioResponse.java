package uv.listi.sicae_usuarios.dto;

public record ValidacionUsuarioResponse(
        boolean activo,
        Integer idUsuario,
        String claveUsuario,
        String mensaje
) {
}
