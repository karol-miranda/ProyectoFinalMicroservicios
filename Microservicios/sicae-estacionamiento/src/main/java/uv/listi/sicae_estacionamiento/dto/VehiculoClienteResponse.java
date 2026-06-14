package uv.listi.sicae_estacionamiento.dto;

public record VehiculoClienteResponse(
        Integer idVehiculo,
        Integer idUsuario,
        String placa,
        Boolean estatus
) {
}
