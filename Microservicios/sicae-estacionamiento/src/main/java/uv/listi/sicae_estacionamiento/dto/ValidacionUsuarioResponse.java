package uv.listi.sicae_estacionamiento.dto;

public record ValidacionUsuarioResponse(
        boolean activo,
        Integer idUsuario,
        String claveUsuario,
        String mensaje
) {
}
