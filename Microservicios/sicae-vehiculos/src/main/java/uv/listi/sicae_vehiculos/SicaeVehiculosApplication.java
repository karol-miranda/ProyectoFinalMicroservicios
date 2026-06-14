package uv.listi.sicae_vehiculos;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("uv.listi.sicae_vehiculos.repository")
public class SicaeVehiculosApplication {
    public static void main(String[] args) {
        SpringApplication.run(SicaeVehiculosApplication.class, args);
    }
}
