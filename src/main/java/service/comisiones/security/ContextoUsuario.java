package service.comisiones.security;

import service.comisiones.enums.RolUsuario;

import java.util.UUID;

public record ContextoUsuario(UUID userId, RolUsuario rol) {
}
