package uv.listi.sicae_vehiculos.dto;

public record ValidacionVehiculoResponse(
        boolean valido,
        Integer idVehiculo,
        Integer idUsuario,
        String placa,
        String mensaje
) {
}
