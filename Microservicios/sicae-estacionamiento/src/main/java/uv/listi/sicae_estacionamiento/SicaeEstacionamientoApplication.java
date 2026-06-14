package uv.listi.sicae_estacionamiento;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("uv.listi.sicae_estacionamiento.repository")
public class SicaeEstacionamientoApplication {
    public static void main(String[] args) {
        SpringApplication.run(SicaeEstacionamientoApplication.class, args);
    }
}
