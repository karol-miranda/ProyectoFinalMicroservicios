package uv.listi.sicae_vehiculos.dto;

public record VehiculoRequest(
        Integer idUsuario,
        Integer idModelo,
        String placa,
        String color,
        Integer anio,
        String descripcion
) {
}
