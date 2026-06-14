package uv.listi.sicae_usuarios;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("uv.listi.sicae_usuarios.repository")
public class SicaeUsuariosApplication {
    public static void main(String[] args) {
        SpringApplication.run(SicaeUsuariosApplication.class, args);
    }
}
