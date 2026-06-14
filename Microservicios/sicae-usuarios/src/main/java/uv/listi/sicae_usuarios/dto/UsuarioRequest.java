package uv.listi.sicae_usuarios.dto;

public record UsuarioRequest(
        Integer idRol,
        Integer idTipoUsuario,
        Integer idProgramaEducativo,
        String nombre,
        String apellidoPaterno,
        String apellidoMaterno,
        String username,
        String password,
        String email,
        String telefono
) {
}
