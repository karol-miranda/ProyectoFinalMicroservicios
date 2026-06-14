package uv.listi.sicae_estacionamiento.dto;

import java.math.BigDecimal;

public record EntradaRequest(
        String claveUsuario,
        String placa,
        Integer idEspacio,
        BigDecimal tarifaHora
) {
}
