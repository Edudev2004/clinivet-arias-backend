package com.clinivet.arias.service;

import com.clinivet.arias.dto.PropietarioCreateRequest;
import com.clinivet.arias.dto.PropietarioResponse;
import com.clinivet.arias.dto.PropietarioUpdateRequest;
import com.clinivet.arias.exception.DuplicateResourceException;
import com.clinivet.arias.exception.ResourceNotFoundException;
import com.clinivet.arias.model.Propietario;
import com.clinivet.arias.repository.MascotaRepository;
import com.clinivet.arias.repository.PropietarioRepository;
import com.clinivet.arias.service.impl.PropietarioServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para {@link PropietarioServiceImpl} con Mockito.
 * Valida los criterios de aceptación de:
 * - RF-05: Registro exitoso, DNI único (409) y validación defensiva.
 * - RF-06: Búsqueda flexible por nombre/apellido, DNI, UUID, lista vacía y conteo de mascotas.
 */
@ExtendWith(MockitoExtension.class)
class PropietarioServiceTest {

    @Mock
    private PropietarioRepository propietarioRepository;

    @Mock
    private MascotaRepository mascotaRepository;

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
        when(mascotaRepository.countByPropietarioIdAndActivoTrue(PROPIETARIO_ID)).thenReturn(2L);

        PropietarioResponse response = propietarioService.obtenerPorId(PROPIETARIO_ID);

        assertNotNull(response);
        assertEquals(PROPIETARIO_ID, response.getId());
        assertEquals("45871236", response.getDni());
        assertEquals(2L, response.getCantidadMascotas());
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

    // ──────────────────────────────────────────────────────────────────────────
    // RF-06: PRUEBAS DE BÚSQUEDA Y PAGINACIÓN
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("RF-06.1: Si el término es nulo o vacío, debe retornar lista paginada de todos los activos")
    void buscar_terminoVacio_retornaTodosActivos() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Propietario> paginaPropietarios = new PageImpl<>(List.of(propietarioPersistido), pageable, 1);

        when(propietarioRepository.findByActivoTrue(pageable)).thenReturn(paginaPropietarios);
        when(mascotaRepository.countByPropietarioIdAndActivoTrue(PROPIETARIO_ID)).thenReturn(3L);

        Page<PropietarioResponse> resultado = propietarioService.buscar(null, pageable);

        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());
        assertEquals("Carlos", resultado.getContent().get(0).getNombre());
        assertEquals(3L, resultado.getContent().get(0).getCantidadMascotas());
        verify(propietarioRepository, times(1)).findByActivoTrue(pageable);
    }

    @Test
    @DisplayName("RF-06.2: Debe buscar coincidencias por nombre o apellido parcial")
    void buscar_porNombre_retornaCoincidencias() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Propietario> paginaPropietarios = new PageImpl<>(List.of(propietarioPersistido), pageable, 1);

        when(propietarioRepository.buscarPorTermino("Mendoza", pageable)).thenReturn(paginaPropietarios);
        when(mascotaRepository.countByPropietarioIdAndActivoTrue(PROPIETARIO_ID)).thenReturn(1L);

        Page<PropietarioResponse> resultado = propietarioService.buscar("Mendoza", pageable);

        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());
        assertEquals("Mendoza Ramos", resultado.getContent().get(0).getApellido());
        verify(propietarioRepository, times(1)).buscarPorTermino("Mendoza", pageable);
    }

    @Test
    @DisplayName("RF-06.3: Debe buscar coincidencia por DNI")
    void buscar_porDni_retornaCoincidencia() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Propietario> paginaPropietarios = new PageImpl<>(List.of(propietarioPersistido), pageable, 1);

        when(propietarioRepository.buscarPorTermino("45871236", pageable)).thenReturn(paginaPropietarios);
        when(mascotaRepository.countByPropietarioIdAndActivoTrue(PROPIETARIO_ID)).thenReturn(0L);

        Page<PropietarioResponse> resultado = propietarioService.buscar("45871236", pageable);

        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());
        assertEquals("45871236", resultado.getContent().get(0).getDni());
        assertEquals(0L, resultado.getContent().get(0).getCantidadMascotas());
    }

    @Test
    @DisplayName("RF-06.4: Debe buscar por UUID directamente si el término tiene formato de identificador")
    void buscar_porUUID_retornaCoincidenciaDirecta() {
        Pageable pageable = PageRequest.of(0, 20);
        when(propietarioRepository.findById(PROPIETARIO_ID)).thenReturn(Optional.of(propietarioPersistido));
        when(mascotaRepository.countByPropietarioIdAndActivoTrue(PROPIETARIO_ID)).thenReturn(2L);

        Page<PropietarioResponse> resultado = propietarioService.buscar(PROPIETARIO_ID.toString(), pageable);

        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());
        assertEquals(PROPIETARIO_ID, resultado.getContent().get(0).getId());
        verify(propietarioRepository, times(1)).findById(PROPIETARIO_ID);
    }

    @Test
    @DisplayName("RF-06.5: Si no hay coincidencias, debe retornar lista vacía con 0 elementos (sin error)")
    void buscar_sinCoincidencias_retornaPaginaVacia() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Propietario> paginaVacia = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(propietarioRepository.buscarPorTermino("Inexistente999", pageable)).thenReturn(paginaVacia);

        Page<PropietarioResponse> resultado = propietarioService.buscar("Inexistente999", pageable);

        assertNotNull(resultado);
        assertEquals(0, resultado.getTotalElements());
        assertTrue(resultado.getContent().isEmpty());
    }

    // ──────────────────────────────────────────────────────────────────────────
    // RF-07: PRUEBAS DE ACTUALIZACIÓN DE DATOS DE PROPIETARIO
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("RF-07.1: Debe actualizar exitosamente los datos del propietario manteniendo el mismo DNI")
    void actualizar_mismoDni_exitoso() {
        PropietarioUpdateRequest updateRequest = new PropietarioUpdateRequest(
                "Carlos Alberto",
                "Mendoza Ramos",
                "45871236", // Mismo DNI
                "999888777",
                "carlos.nuevo@gmail.com",
                "Nueva Direccion 123",
                true
        );

        when(propietarioRepository.findById(PROPIETARIO_ID)).thenReturn(Optional.of(propietarioPersistido));
        when(propietarioRepository.save(any(Propietario.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(mascotaRepository.countByPropietarioIdAndActivoTrue(PROPIETARIO_ID)).thenReturn(1L);

        PropietarioResponse response = propietarioService.actualizar(PROPIETARIO_ID, updateRequest);

        assertNotNull(response);
        assertEquals(PROPIETARIO_ID, response.getId());
        assertEquals("Carlos Alberto", response.getNombre());
        assertEquals("999888777", response.getTelefono());
        assertEquals("carlos.nuevo@gmail.com", response.getEmail());
        assertEquals("Nueva Direccion 123", response.getDireccion());
        assertEquals("45871236", response.getDni());
        assertEquals(1L, response.getCantidadMascotas());

        verify(propietarioRepository, times(1)).findById(PROPIETARIO_ID);
        verify(propietarioRepository, never()).findByDni(anyString());
        verify(propietarioRepository, times(1)).save(any(Propietario.class));
    }

    @Test
    @DisplayName("RF-07.2: Debe actualizar exitosamente cuando el DNI cambia a uno disponible")
    void actualizar_nuevoDniDisponible_exitoso() {
        PropietarioUpdateRequest updateRequest = new PropietarioUpdateRequest(
                "Carlos",
                "Mendoza",
                "77889900", // Nuevo DNI
                "987654321",
                "carlos@gmail.com",
                "Av. Universitaria 1240",
                true
        );

        when(propietarioRepository.findById(PROPIETARIO_ID)).thenReturn(Optional.of(propietarioPersistido));
        when(propietarioRepository.findByDni("77889900")).thenReturn(Optional.empty());
        when(propietarioRepository.save(any(Propietario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PropietarioResponse response = propietarioService.actualizar(PROPIETARIO_ID, updateRequest);

        assertNotNull(response);
        assertEquals("77889900", response.getDni());
        verify(propietarioRepository, times(1)).findByDni("77889900");
        verify(propietarioRepository, times(1)).save(any(Propietario.class));
    }

    @Test
    @DisplayName("RF-07.3: Debe rechazar con DuplicateResourceException (409) si el nuevo DNI ya pertenece a otro propietario")
    void actualizar_dniDeOtroPropietario_lanzaConflicto() {
        UUID otroId = UUID.fromString("e0000000-0000-0000-0000-000000000002");
        Propietario otroPropietario = new Propietario();
        otroPropietario.setId(otroId);
        otroPropietario.setDni("88888888");

        PropietarioUpdateRequest updateRequest = new PropietarioUpdateRequest(
                "Carlos",
                "Mendoza",
                "88888888",
                "987654321",
                null,
                null,
                true
        );

        when(propietarioRepository.findById(PROPIETARIO_ID)).thenReturn(Optional.of(propietarioPersistido));
        when(propietarioRepository.findByDni("88888888")).thenReturn(Optional.of(otroPropietario));

        DuplicateResourceException ex = assertThrows(
                DuplicateResourceException.class,
                () -> propietarioService.actualizar(PROPIETARIO_ID, updateRequest)
        );

        assertTrue(ex.getMessage().contains("88888888"));
        verify(propietarioRepository, never()).save(any(Propietario.class));
    }

    @Test
    @DisplayName("RF-07.4: Debe lanzar ResourceNotFoundException (404) si el propietario no existe")
    void actualizar_propietarioNoEncontrado_lanzaNotFound() {
        UUID noExistente = UUID.randomUUID();
        PropietarioUpdateRequest updateRequest = new PropietarioUpdateRequest(
                "Carlos",
                "Mendoza",
                "45871236",
                "987654321",
                null,
                null,
                true
        );

        when(propietarioRepository.findById(noExistente)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> propietarioService.actualizar(noExistente, updateRequest));
        verify(propietarioRepository, never()).save(any(Propietario.class));
    }

    @Test
    @DisplayName("RF-07.5: Debe aplicar trim a los datos de actualización")
    void actualizar_aplicaTrimming() {
        PropietarioUpdateRequest updateRequest = new PropietarioUpdateRequest(
                "  Carlos Alberto  ",
                "  Mendoza Ramos  ",
                "  45871236  ",
                "  999888777  ",
                "  carlos@gmail.com  ",
                "  Av. Universitaria  ",
                true
        );

        when(propietarioRepository.findById(PROPIETARIO_ID)).thenReturn(Optional.of(propietarioPersistido));
        when(propietarioRepository.save(any(Propietario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PropietarioResponse response = propietarioService.actualizar(PROPIETARIO_ID, updateRequest);

        assertNotNull(response);
        assertEquals("Carlos Alberto", response.getNombre());
        assertEquals("Mendoza Ramos", response.getApellido());
        assertEquals("45871236", response.getDni());
        assertEquals("999888777", response.getTelefono());
        assertEquals("carlos@gmail.com", response.getEmail());
        assertEquals("Av. Universitaria", response.getDireccion());
    }
}
