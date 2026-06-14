package uv.listi.sicae_usuarios.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uv.listi.sicae_usuarios.dto.MensajeResponse;
import uv.listi.sicae_usuarios.dto.UsuarioPerfilResponse;
import uv.listi.sicae_usuarios.dto.UsuarioRequest;
import uv.listi.sicae_usuarios.dto.ValidacionUsuarioResponse;
import uv.listi.sicae_usuarios.service.UsuarioService;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {
    private final UsuarioService service;

    public UsuarioController(UsuarioService service) {
        this.service = service;
    }

    @PostMapping
    public MensajeResponse registrar(@RequestHeader("Authorization") String token, @RequestBody UsuarioRequest request) {
        return service.registrar(token, request);
    }

    @PutMapping("/{idUsuario}")
    public MensajeResponse actualizar(@RequestHeader("Authorization") String token,
                                      @PathVariable Integer idUsuario,
                                      @RequestBody UsuarioRequest request) {
        return service.actualizar(token, idUsuario, request);
    }

    @GetMapping("/{idUsuario}")
    public UsuarioPerfilResponse perfil(@RequestHeader("Authorization") String token, @PathVariable Integer idUsuario) {
        return service.perfil(token, idUsuario);
    }

    @PatchMapping("/{idUsuario}/estatus")
    public MensajeResponse cambiarEstatus(@RequestHeader("Authorization") String token, @PathVariable Integer idUsuario) {
        return service.cambiarEstatus(token, idUsuario);
    }

    @GetMapping("/validar/{claveUsuario}")
    public ValidacionUsuarioResponse validar(@PathVariable String claveUsuario) {
        return service.validarPorClave(claveUsuario);
    }
}
