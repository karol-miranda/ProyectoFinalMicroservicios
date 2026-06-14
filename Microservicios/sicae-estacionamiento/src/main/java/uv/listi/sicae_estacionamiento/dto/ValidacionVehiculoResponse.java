package uv.listi.sicae_estacionamiento.dto;

public record ValidacionVehiculoResponse(
        boolean valido,
        Integer idVehiculo,
        Integer idUsuario,
        String placa,
        String mensaje
) {
}
