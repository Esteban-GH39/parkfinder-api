package org.parkfinder.parqueaderos.dominio.excepcion;

/** Se lanza al operar sobre un parqueadero que no existe (HU-19). */
public class ParqueaderoNoEncontradoException extends RuntimeException {

    private final Long id;

    public ParqueaderoNoEncontradoException(Long id) {
        super("No existe un parqueadero con id " + id);
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
