package org.parkfinder.estadias.dominio.excepcion;

/** Base de los errores de negocio del ingreso y salida de vehículos; cada uno trae su código HTTP. */
public class EstadiaException extends RuntimeException {

    private final int status;
    private final String codigo;

    protected EstadiaException(String mensaje, int status, String codigo) {
        super(mensaje);
        this.status = status;
        this.codigo = codigo;
    }

    public int getStatus() {
        return status;
    }

    public String getCodigo() {
        return codigo;
    }

    /** Datos inválidos: placa mal escrita, sin vehículos, más vehículos que los reservados... */
    public static EstadiaException invalida(String mensaje) {
        return new EstadiaException(mensaje, 400, "ESTADIA-ERR-400");
    }

    /** El parqueadero no pertenece al administrador que hace la operación. */
    public static EstadiaException noPermitida(String mensaje) {
        return new EstadiaException(mensaje, 403, "ESTADIA-ERR-403");
    }

    /** No hay un vehículo con esa placa dentro del parqueadero. */
    public static EstadiaException noEncontrada(String mensaje) {
        return new EstadiaException(mensaje, 404, "ESTADIA-ERR-404");
    }

    /** La placa ya está adentro, no hay cupo o la estadía ya se cerró. */
    public static EstadiaException conflicto(String mensaje) {
        return new EstadiaException(mensaje, 409, "ESTADIA-ERR-409");
    }
}
