package com.btg.fundmanagement.exception;

public class ApiException extends RuntimeException {

    private final int status;

    public ApiException(String message, int status) {
        super(message);
        this.status = status;
    }

    public int status() { return status; }

    public static class InsufficientBalance extends ApiException {
        public InsufficientBalance(String fundName) {
            super("No tiene saldo disponible para vincularse al fondo " + fundName, 400);
        }
    }

    public static class FundNotFound extends ApiException {
        public FundNotFound(String fundId) {
            super("Fondo no encontrado: " + fundId, 404);
        }
    }

    public static class UserNotFound extends ApiException {
        public UserNotFound(String userId) {
            super("Usuario no encontrado: " + userId, 404);
        }
    }

    public static class RoleNotFound extends ApiException {
        public RoleNotFound(String roleId) {
            super("Rol no encontrado: " + roleId, 404);
        }
    }

    public static class AlreadySubscribed extends ApiException {
        public AlreadySubscribed(String fundName) {
            super("Ya esta suscrito al fondo " + fundName, 409);
        }
    }

    public static class NotSubscribed extends ApiException {
        public NotSubscribed(String fundId) {
            super("No tiene suscripcion activa al fondo: " + fundId, 404);
        }
    }

    public static class EmailAlreadyExists extends ApiException {
        public EmailAlreadyExists() {
            super("El email ya esta registrado", 409);
        }
    }

    public static class InvalidCredentials extends ApiException {
        public InvalidCredentials() {
            super("Email o password invalidos", 401);
        }
    }

    public static class RoleAlreadyExists extends ApiException {
        public RoleAlreadyExists(String roleId) {
            super("El rol ya existe: " + roleId, 409);
        }
    }

    public static class AdminAlreadyExists extends ApiException {
        public AdminAlreadyExists() {
            super("Ya existe un administrador en el sistema", 409);
        }
    }
}
