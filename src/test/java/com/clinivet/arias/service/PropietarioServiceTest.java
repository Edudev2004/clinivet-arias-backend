package com.clinivet.arias.service;

import com.clinivet.arias.dto.PropietarioCreateRequest;
import com.clinivet.arias.dto.PropietarioResponse;
import com.clinivet.arias.exception.DuplicateResourceException;
import com.clinivet.arias.exception.ResourceNotFoundException;
import com.clinivet.arias.model.Propietario;
import com.clinivet.arias.repository.PropietarioRepository;
import com.clinivet.arias.service.impl.PropietarioServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para {@link PropietarioServiceImpl} con Mockito.
 * Valida los criterios de aceptación de RF-05:
 * - Registro exitoso de propietario con ID generado
 * - DNI único: si ya existe retorna HTTP 409 (DuplicateResourceException)
 * - Limpieza de datos (trimming) y validación defensiva con Guava
 */
@ExtendWith(MockitoExtension.class)
class PropietarioServiceTest {

    @Mock
    private PropietarioRepository propietarioRepository;

    @InjectMocks
    private PropietarioServiceImpl propietarioService;

    private PropietarioCreateRequest requestValido;
    private Propietario propietarioPersistido;
    private final UUID PROPIETARIO_ID = UUID.fromString("e0000000-0000-0000-0000-000000000001");

    @BeforeEach
    void setUp() {
        requestValido = new PropietarioCreateRequest(
                "Carlos",
                "Mendoza Ramos",
                "45871236",
                "987654321",
                "carlos.mendoza@gmail.com",
                "Av. Universitaria 1240, Carabayllo"
        );

        propietarioPersistido = new Propietario(
                PROPIETARIO_ID,
                "Carlos",
                "Mendoza Ramos",
                "45871236",
                "987654321",
                "carlos.mendoza@gmail.com",
                "Av. Universitaria 1240, Carabayllo",
                true
        );
    }

    @Test
    @DisplayName("RF-05.1: Debe registrar exitosamente un propietario nuevo con DNI único")
    void registrar_exitoso() {
        when(propietarioRepository.findByDni("45871236")).thenReturn(Optional.empty());
        when(propietarioRepository.save(any(Propietario.class))).thenReturn(propietarioPersistido);

        PropietarioResponse response = propietarioService.registrar(requestValido);

        assertNotNull(response);
        assertEquals(PROPIETARIO_ID, response.getId());
        assertEquals("Carlos", response.getNombre());
        assertEquals("Mendoza Ramos", response.getApellido());
        assertEquals("Carlos Mendoza Ramos", response.getNombreCompleto());
        assertEquals("45871236", response.getDni());
        assertEquals("987654321", response.getTelefono());
        assertEquals("carlos.mendoza@gmail.com", response.getEmail());
        assertTrue(response.getActivo());

        verify(propietarioRepository, times(1)).findByDni("45871236");
        verify(propietarioRepository, times(1)).save(any(Propietario.class));
    }

    @Test
    @DisplayName("RF-05.2: Debe rechazar con DuplicateResourceException (409) si el DNI ya existe")
    void registrar_dniDuplicado_lanzaConflicto() {
        when(propietarioRepository.findByDni("45871236")).thenReturn(Optional.of(propietarioPersistido));

        DuplicateResourceException exception = assertThrows(
                DuplicateResourceException.class,
                () -> propietarioService.registrar(requestValido)
        );

        assertTrue(exception.getMessage().contains("Ya existe un propietario registrado con el DNI: 45871236"));
        verify(propietarioRepository, times(1)).findByDni("45871236");
        verify(propietarioRepository, never()).save(any(Propietario.class));
    }

    @Test
    @DisplayName("RF-05.3: Debe recortar espacios en blanco (trim) de DNI, nombre y apellido antes de guardar")
    void registrar_conEspacios_aplicaTrimming() {
        PropietarioCreateRequest requestConEspacios = new PropietarioCreateRequest(
                "  Carlos  ",
                "  Mendoza Ramos  ",
                "  45871236  ",
                "  987654321  ",
                "  carlos.mendoza@gmail.com  ",
                "  Av. Universitaria 1240  "
        );

        when(propietarioRepository.findByDni("45871236")).thenReturn(Optional.empty());
        when(propietarioRepository.save(any(Propietario.class))).thenAnswer(invocation -> {
            Propietario p = invocation.getArgument(0);
            p.setId(PROPIETARIO_ID);
            return p;
        });

        PropietarioResponse response = propietarioService.registrar(requestConEspacios);

        assertNotNull(response);
        assertEquals("Carlos", response.getNombre());
        assertEquals("Mendoza Ramos", response.getApellido());
        assertEquals("45871236", response.getDni());
        assertEquals("987654321", response.getTelefono());
        assertEquals("carlos.mendoza@gmail.com", response.getEmail());
        assertEquals("Av. Universitaria 1240", response.getDireccion());
    }

    @Test
    @DisplayName("RF-05.4: Debe rechazar con NullPointerException si la solicitud es nula")
    void registrar_requestNulo_lanzaExcepcion() {
        assertThrows(NullPointerException.class, () -> propietarioService.registrar(null));
        verify(propietarioRepository, never()).save(any(Propietario.class));
    }

    @Test
    @DisplayName("RF-05.5: Debe consultar exitosamente un propietario por ID")
    void obtenerPorId_exitoso() {
        when(propietarioRepository.findById(PROPIETARIO_ID)).thenReturn(Optional.of(propietarioPersistido));

        PropietarioResponse response = propietarioService.obtenerPorId(PROPIETARIO_ID);

        assertNotNull(response);
        assertEquals(PROPIETARIO_ID, response.getId());
        assertEquals("45871236", response.getDni());
        verify(propietarioRepository, times(1)).findById(PROPIETARIO_ID);
    }

    @Test
    @DisplayName("RF-05.6: Debe lanzar ResourceNotFoundException si el propietario no existe")
    void obtenerPorId_noEncontrado_lanzaExcepcion() {
        UUID noExistente = UUID.randomUUID();
        when(propietarioRepository.findById(noExistente)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> propietarioService.obtenerPorId(noExistente));
        verify(propietarioRepository, times(1)).findById(noExistente);
    }
}
