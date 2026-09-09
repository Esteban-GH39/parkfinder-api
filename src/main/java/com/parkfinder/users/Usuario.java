package com.parkfinder.users;

import java.time.Instant;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

/**
 * Cuenta de usuario, sea cliente o administrador (HU-01, HU-16). El registro
 * en sí (email/contraseña) es común a ambos roles; los datos específicos de
 * cada rol (ej. nombre del establecimiento para el administrador) se agregan
 * cuando se implemente esa historia.
 */
@Entity
public class Usuario extends PanacheEntity {

    @Column(nullable = false)
    public String nombre;

    @Column(nullable = false, unique = true)
    public String email;

    @Column(name = "password_hash", nullable = false)
    public String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public Rol rol;

    @Column(name = "creado_en", nullable = false)
    public Instant creadoEn;

    public static Usuario buscarPorEmail(String email) {
        return find("email", email).firstResult();
    }
}
