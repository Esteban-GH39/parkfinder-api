package org.parkfinder.aplicacion;



import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.parkfinder.parqueaderos.aplicacion.ParqueaderoServicio;
import org.parkfinder.parqueaderos.dominio.modelo.Parqueadero;
import org.parkfinder.parqueaderos.dominio.repositorio.ParqueaderoRepositorio;
import org.parkfinder.parqueaderos.infraestructura.dto.ParqueaderoDto;


import java.time.LocalTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@QuarkusTest
public class ParqueaderoServicioTest {
    @Inject
    ParqueaderoServicio parqueaderoServicio;

    @InjectMock
    ParqueaderoRepositorio parqueaderoRepositorio;
    @Test
    public void testCrearParqueadero(){

    }
    @Test
    public void tewtCrearParqueaderoVacios(){

    }
}
