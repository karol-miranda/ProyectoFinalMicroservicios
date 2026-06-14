package uv.listi.sicae_estacionamiento.repository;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import uv.listi.sicae_estacionamiento.model.Espacio;
import uv.listi.sicae_estacionamiento.model.Movimiento;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface EstacionamientoRepository {
    @Select("SELECT idEspacio, claveEspacio, tipo, ocupado, estatus FROM espacioestacionamiento WHERE estatus = b'1'")
    List<Espacio> consultarEspacios();

    @Select("SELECT idEspacio, claveEspacio, tipo, ocupado, estatus FROM espacioestacionamiento WHERE idEspacio = #{idEspacio}")
    Espacio buscarEspacio(Integer idEspacio);

    @Update("UPDATE espacioestacionamiento SET ocupado = #{ocupado} WHERE idEspacio = #{idEspacio}")
    int actualizarOcupado(@Param("idEspacio") Integer idEspacio, @Param("ocupado") boolean ocupado);

    @Insert("""
            INSERT INTO movimiento(idVehiculo, tiempoEntrada, tarifaHora, tiempoCreacion, idEspacio)
            VALUES (#{idVehiculo}, now(), #{tarifaHora}, now(), #{idEspacio})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "idMovimiento", keyColumn = "idMovimiento")
    int registrarEntrada(Movimiento movimiento);

    @Select("""
            SELECT m.idMovimiento, m.idVehiculo, m.tiempoEntrada, m.tiempoSalida,
                   m.minutosEstacionado, m.horasCobradas, m.costoTotal, m.tarifaHora,
                   m.tiempoCreacion, m.tiempoActualizacion, m.idEspacio, e.claveEspacio
            FROM movimiento m
            JOIN espacioestacionamiento e ON e.idEspacio = m.idEspacio
            WHERE m.idMovimiento = #{idMovimiento}
            """)
    Movimiento buscarMovimiento(Integer idMovimiento);

    @Select("""
            SELECT m.idMovimiento, m.idVehiculo, m.tiempoEntrada, m.tiempoSalida,
                   m.minutosEstacionado, m.horasCobradas, m.costoTotal, m.tarifaHora,
                   m.tiempoCreacion, m.tiempoActualizacion, m.idEspacio, e.claveEspacio
            FROM movimiento m
            JOIN espacioestacionamiento e ON e.idEspacio = m.idEspacio
            WHERE m.idVehiculo = #{idVehiculo} AND m.tiempoSalida IS NULL
            LIMIT 1
            """)
    Movimiento buscarMovimientoAbierto(Integer idVehiculo);

    @Select("SELECT COUNT(*) FROM movimiento WHERE idVehiculo = #{idVehiculo} AND tiempoSalida IS NULL")
    int contarMovimientoAbierto(Integer idVehiculo);

    @Update("""
            UPDATE movimiento
            SET tiempoSalida = now(),
                minutosEstacionado = TIMESTAMPDIFF(MINUTE, tiempoEntrada, now()),
                horasCobradas = GREATEST(1, CEIL(TIMESTAMPDIFF(MINUTE, tiempoEntrada, now()) / 60)),
                costoTotal = GREATEST(1, CEIL(TIMESTAMPDIFF(MINUTE, tiempoEntrada, now()) / 60)) * tarifaHora,
                tiempoActualizacion = now()
            WHERE idMovimiento = #{idMovimiento}
            """)
    int registrarSalida(Integer idMovimiento);
}
