package uv.listi.sicae_usuarios.repository;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import uv.listi.sicae_usuarios.model.Usuario;

@Mapper
public interface UsuarioRepository {
    @Select("""
            SELECT "idUsuario", nombre, "apellidoPaterno", "apellidoMaterno", "claveUsuario",
                   email, telefono, username, password, CAST(estatus AS text) AS estatus,
                   "idRol", rol, "idTipoUsuario", "tipoUsuario", "idProgramaEducativo",
                   "programaEducativo", "tiempoCreacion", "tempoActualizacion"
            FROM "usuarioFullInfo"
            WHERE username = #{username}
            """)
    Usuario buscarPorUsername(String username);

    @Select("""
            SELECT "idUsuario", nombre, "apellidoPaterno", "apellidoMaterno", "claveUsuario",
                   email, telefono, username, password, CAST(estatus AS text) AS estatus,
                   "idRol", rol, "idTipoUsuario", "tipoUsuario", "idProgramaEducativo",
                   "programaEducativo", "tiempoCreacion", "tempoActualizacion"
            FROM "usuarioFullInfo"
            WHERE "idUsuario" = #{idUsuario}
            """)
    Usuario buscarPorId(Integer idUsuario);

    @Select("""
            SELECT "idUsuario", nombre, "apellidoPaterno", "apellidoMaterno", "claveUsuario",
                   email, telefono, username, password, CAST(estatus AS text) AS estatus,
                   "idRol", rol, "idTipoUsuario", "tipoUsuario", "idProgramaEducativo",
                   "programaEducativo", "tiempoCreacion", "tempoActualizacion"
            FROM "usuarioFullInfo"
            WHERE "claveUsuario" = #{claveUsuario}
            """)
    Usuario buscarPorClave(String claveUsuario);

    @Select("SELECT COUNT(*) FROM usuario WHERE email = #{email}")
    int contarPorEmail(String email);

    @Select("SELECT COUNT(*) FROM usuario WHERE username = #{username}")
    int contarPorUsername(String username);

    @Select("SELECT COUNT(*) FROM usuario WHERE \"claveUsuario\" = #{claveUsuario}")
    int contarPorClave(String claveUsuario);

    @Insert("""
            INSERT INTO usuario(nombre, "apellidoPaterno", "apellidoMaterno", "claveUsuario",
                                email, telefono, username, password, estatus, "idRol",
                                "idTipoUsuario", "idProgramaEducativo", "tiempoCreacion")
            VALUES (#{nombre}, #{apellidoPaterno}, #{apellidoMaterno}, #{claveUsuario},
                    #{email}, #{telefono}, #{username}, #{password}, B'1', #{idRol},
                    #{idTipoUsuario}, #{idProgramaEducativo}, now())
            """)
    @Options(useGeneratedKeys = true, keyProperty = "idUsuario", keyColumn = "idUsuario")
    int registrar(Usuario usuario);

    @Update("""
            UPDATE usuario
            SET nombre = #{nombre},
                "apellidoPaterno" = #{apellidoPaterno},
                "apellidoMaterno" = #{apellidoMaterno},
                email = #{email},
                telefono = #{telefono},
                "idRol" = #{idRol},
                "idTipoUsuario" = #{idTipoUsuario},
                "idProgramaEducativo" = #{idProgramaEducativo},
                "tempoActualizacion" = now()
            WHERE "idUsuario" = #{idUsuario}
            """)
    int actualizar(Usuario usuario);

    @Update("UPDATE usuario SET estatus = CAST(#{estatus} AS bit), \"tempoActualizacion\" = now() WHERE \"idUsuario\" = #{idUsuario}")
    int cambiarEstatus(@Param("idUsuario") Integer idUsuario, @Param("estatus") String estatus);
}
