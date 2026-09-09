package com.parkfinder.users;

/** Se lanza al intentar registrar un email que ya tiene cuenta (criterio de aceptación HU-01/HU-16). */
public class EmailYaRegistradoException extends RuntimeException {

    public EmailYaRegistradoException(String email) {
        super("Ya existe una cuenta registrada con el correo: " + email);
    }
}
