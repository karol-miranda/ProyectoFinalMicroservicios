package uv.listi.sicae_vehiculos.repository;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import uv.listi.sicae_vehiculos.model.Vehiculo;

import java.util.List;

@Mapper
public interface VehiculoRepository {
    @Select("""
            SELECT v.idVehiculo, v.idUsuario, v.claveVehiculo, ma.idMarca, ma.marca,
                   v.idModelo, mo.modelo, v.placa, v.color, v.anio, v.descripcion, v.estatus
            FROM vehiculo v
            JOIN modelo mo ON mo.idModelo = v.idModelo
            JOIN marca ma ON ma.idMarca = mo.idMarca
            WHERE v.idUsuario = #{idUsuario}
            """)
    List<Vehiculo> buscarPorUsuario(Integer idUsuario);

    @Select("""
            SELECT v.idVehiculo, v.idUsuario, v.claveVehiculo, ma.idMarca, ma.marca,
                   v.idModelo, mo.modelo, v.placa, v.color, v.anio, v.descripcion, v.estatus
            FROM vehiculo v
            JOIN modelo mo ON mo.idModelo = v.idModelo
            JOIN marca ma ON ma.idMarca = mo.idMarca
            WHERE v.idVehiculo = #{idVehiculo}
            """)
    Vehiculo buscarPorId(Integer idVehiculo);

    @Select("""
            SELECT v.idVehiculo, v.idUsuario, v.claveVehiculo, ma.idMarca, ma.marca,
                   v.idModelo, mo.modelo, v.placa, v.color, v.anio, v.descripcion, v.estatus
            FROM vehiculo v
            JOIN modelo mo ON mo.idModelo = v.idModelo
            JOIN marca ma ON ma.idMarca = mo.idMarca
            WHERE v.placa = #{placa}
            """)
    Vehiculo buscarPorPlaca(String placa);

    @Select("SELECT COUNT(*) FROM vehiculo WHERE idUsuario = #{idUsuario} AND estatus = b'1'")
    int contarActivosPorUsuario(Integer idUsuario);

    @Select("SELECT COUNT(*) FROM vehiculo WHERE placa = #{placa} AND idVehiculo <> #{idVehiculo}")
    int contarPlacaEnOtroVehiculo(@Param("placa") String placa, @Param("idVehiculo") Integer idVehiculo);

    @Insert("""
            INSERT INTO vehiculo(idUsuario, claveVehiculo, idModelo, placa, color, anio, descripcion, estatus)
            VALUES (#{idUsuario}, #{claveVehiculo}, #{idModelo}, #{placa}, #{color}, #{anio}, #{descripcion}, b'1')
            """)
    @Options(useGeneratedKeys = true, keyProperty = "idVehiculo", keyColumn = "idVehiculo")
    int registrar(Vehiculo vehiculo);

    @Update("""
            UPDATE vehiculo
            SET idModelo = #{idModelo}, placa = #{placa}, color = #{color},
                anio = #{anio}, descripcion = #{descripcion}
            WHERE idVehiculo = #{idVehiculo} AND idUsuario = #{idUsuario}
            """)
    int actualizar(Vehiculo vehiculo);

    @Update("UPDATE vehiculo SET estatus = NOT estatus WHERE idVehiculo = #{idVehiculo} AND idUsuario = #{idUsuario}")
    int cambiarEstatus(@Param("idVehiculo") Integer idVehiculo, @Param("idUsuario") Integer idUsuario);
}
