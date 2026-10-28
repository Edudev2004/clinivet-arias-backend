package com.clinivet.arias.service;

import com.clinivet.arias.dto.MascotaCreateRequest;
import com.clinivet.arias.dto.MascotaResponse;
import com.clinivet.arias.model.Mascota;
import com.clinivet.arias.model.Propietario;
import com.clinivet.arias.repository.MascotaRepository;
import com.clinivet.arias.repository.PropietarioRepository;
import com.clinivet.arias.service.impl.MascotaServiceImpl;
import com.clinivet.arias.exception.ResourceNotFoundException;
import com.clinivet.arias.repository.ConsultaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para {@link MascotaServiceImpl} con Mockito.
 * Valida los criterios de aceptación de:
 * - RF-09: Registro de mascotas asociadas a propietario (RN-01).
 */
@ExtendWith(MockitoExtension.class)
class MascotaServiceTest {

    @Mock
    private MascotaRepository mascotaRepository;

    @Mock
    private PropietarioRepository propietarioRepository;

    @Mock
    private ConsultaRepository consultaRepository;

    @InjectMocks
    private MascotaServiceImpl mascotaService;

    private final UUID PROPIETARIO_ID = UUID.fromString("e0000000-0000-0000-0000-000000000001");
    private final UUID MASCOTA_ID = UUID.fromString("f0000000-0000-0000-0000-000000000001");
    private Propietario propietarioExistente;
    private MascotaCreateRequest requestValido;

    @BeforeEach
    void setUp() {
        propietarioExistente = new Propietario(
                PROPIETARIO_ID,
                "Carlos",
                "Mendoza Ramos",
                "45871236",
                "987654321",
                "carlos.mendoza@gmail.com",
                "Av. Universitaria 1240",
                true
        );

        requestValido = new MascotaCreateRequest(
                PROPIETARIO_ID,
                "Firulais",
                "Canino",
                "Golden Retriever",
                "Dorado",
                "Macho",
                LocalDate.of(2022, 3, 10),
                new BigDecimal("28.50"),
                "https://storage.supabase.co/clinivet/photos/firulais.jpg"
        );
    }

    @Test
    @DisplayName("RF-09.1: Debe registrar exitosamente una nueva mascota vinculada al propietario")
    void registrar_exitoso() {
        when(propietarioRepository.findById(PROPIETARIO_ID)).thenReturn(Optional.of(propietarioExistente));
        when(mascotaRepository.save(any(Mascota.class))).thenAnswer(invocation -> {
            Mascota m = invocation.getArgument(0);
            m.setId(MASCOTA_ID);
            return m;
        });

        MascotaResponse response = mascotaService.registrar(requestValido);

        assertNotNull(response);
        assertEquals(MASCOTA_ID, response.getId());
        assertEquals("Firulais", response.getNombre());
        assertEquals("Canino", response.getEspecie());
        assertEquals("Golden Retriever", response.getRaza());
        assertEquals("Dorado", response.getColor());
        assertEquals("Macho", response.getSexo());
        assertEquals(LocalDate.of(2022, 3, 10), response.getFechaNacimiento());
        assertEquals(new BigDecimal("28.50"), response.getPesoActualKg());
        assertEquals(PROPIETARIO_ID, response.getPropietarioId());
        assertEquals("Carlos Mendoza Ramos", response.getPropietarioNombre());
        assertTrue(response.getActivo());

        verify(propietarioRepository, times(1)).findById(PROPIETARIO_ID);
        verify(mascotaRepository, times(1)).save(any(Mascota.class));
    }

    @Test
    @DisplayName("RF-09.2: Debe rechazar con IllegalArgumentException (HTTP 400) si el propietario no existe (RN-01)")
    void registrar_propietarioInexistente_lanzaIllegalArgumentException() {
        UUID propietarioInexistente = UUID.randomUUID();
        requestValido.setPropietarioId(propietarioInexistente);

        when(propietarioRepository.findById(propietarioInexistente)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> mascotaService.registrar(requestValido)
        );

        assertTrue(ex.getMessage().contains("no existe en la base de datos (RN-01)"));
        verify(propietarioRepository, times(1)).findById(propietarioInexistente);
        verify(mascotaRepository, never()).save(any(Mascota.class));
    }

    @Test
    @DisplayName("RF-09.3: Debe rechazar si la solicitud o propietarioId es nulo")
    void registrar_nulos_lanzaExcepcion() {
        assertThrows(NullPointerException.class, () -> mascotaService.registrar(null));

        requestValido.setPropietarioId(null);
        assertThrows(NullPointerException.class, () -> mascotaService.registrar(requestValido));

        verify(mascotaRepository, never()).save(any(Mascota.class));
    }

    @Test
    @DisplayName("RF-09.4: Debe aplicar trimming a los datos de la mascota antes de guardar")
    void registrar_conEspacios_aplicaTrimming() {
        MascotaCreateRequest requestConEspacios = new MascotaCreateRequest(
                PROPIETARIO_ID,
                "  Bobby  ",
                "  Canino  ",
                "  Beagle  ",
                "  Tricolor  ",
                "M",
                LocalDate.of(2023, 1, 1),
                new BigDecimal("12.00"),
                "  https://fotos.com/bobby.jpg  "
        );

        when(propietarioRepository.findById(PROPIETARIO_ID)).thenReturn(Optional.of(propietarioExistente));
        when(mascotaRepository.save(any(Mascota.class))).thenAnswer(invocation -> {
            Mascota m = invocation.getArgument(0);
            m.setId(MASCOTA_ID);
            return m;
        });

        MascotaResponse response = mascotaService.registrar(requestConEspacios);

        assertNotNull(response);
        assertEquals("Bobby", response.getNombre());
        assertEquals("Canino", response.getEspecie());
        assertEquals("Beagle", response.getRaza());
        assertEquals("Tricolor", response.getColor());
        assertEquals("Macho", response.getSexo());
        assertEquals("https://fotos.com/bobby.jpg", response.getFotoUrl());
    }

    @Test
    @DisplayName("RF-09.5: Debe normalizar sexo abreviado 'H' a 'Hembra'")
    void registrar_sexoAbreviado_normalizaSexo() {
        requestValido.setSexo("H");

        when(propietarioRepository.findById(PROPIETARIO_ID)).thenReturn(Optional.of(propietarioExistente));
        when(mascotaRepository.save(any(Mascota.class))).thenAnswer(invocation -> {
            Mascota m = invocation.getArgument(0);
            m.setId(MASCOTA_ID);
            return m;
        });

        MascotaResponse response = mascotaService.registrar(requestValido);

        assertNotNull(response);
        assertEquals("Hembra", response.getSexo());
    }

    // ──────────────────────────────────────────────────────────────────────────
    // RF-10: PRUEBAS DE FICHA COMPLETA DE LA MASCOTA
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("RF-10.1: Debe obtener la ficha completa con el peso más reciente registrado en consultas médicas")
    void obtenerPorId_existente_conPesoDeConsulta_retornaFichaConPesoReciente() {
        Mascota mascotaPersistida = new Mascota(
                MASCOTA_ID,
                propietarioExistente,
                "Firulais",
                "Canino",
                "Golden Retriever",
                "Dorado",
                "Macho",
                LocalDate.of(2022, 3, 10),
                new BigDecimal("28.50"), // Peso inicial
                true
        );

        when(mascotaRepository.findById(MASCOTA_ID)).thenReturn(Optional.of(mascotaPersistida));
        when(consultaRepository.findPesosRecientesPorMascotaId(MASCOTA_ID))
                .thenReturn(List.of(new BigDecimal("30.25"), new BigDecimal("29.10")));

        MascotaResponse response = mascotaService.obtenerPorId(MASCOTA_ID);

        assertNotNull(response);
        assertEquals(MASCOTA_ID, response.getId());
        assertEquals("Firulais", response.getNombre());
        assertEquals("Golden Retriever", response.getRaza());
        assertEquals("Carlos Mendoza Ramos", response.getPropietarioNombre());
        assertEquals(new BigDecimal("30.25"), response.getPesoActualKg(), "Debe reflejar el peso de la consulta médica más reciente");

        verify(mascotaRepository, times(1)).findById(MASCOTA_ID);
        verify(consultaRepository, times(1)).findPesosRecientesPorMascotaId(MASCOTA_ID);
    }

    @Test
    @DisplayName("RF-10.2: Debe retornar el peso inicial si la mascota aún no tiene consultas médicas registradas")
    void obtenerPorId_existente_sinConsultas_retornaFichaConPesoInicial() {
        Mascota mascotaPersistida = new Mascota(
                MASCOTA_ID,
                propietarioExistente,
                "Firulais",
                "Canino",
                "Golden Retriever",
                "Dorado",
                "Macho",
                LocalDate.of(2022, 3, 10),
                new BigDecimal("28.50"),
                true
        );

        when(mascotaRepository.findById(MASCOTA_ID)).thenReturn(Optional.of(mascotaPersistida));
        when(consultaRepository.findPesosRecientesPorMascotaId(MASCOTA_ID))
                .thenReturn(Collections.emptyList());

        MascotaResponse response = mascotaService.obtenerPorId(MASCOTA_ID);

        assertNotNull(response);
        assertEquals(new BigDecimal("28.50"), response.getPesoActualKg(), "Debe conservar el peso registrado inicialmente");
    }

    @Test
    @DisplayName("RF-10.3: Debe lanzar ResourceNotFoundException (404) si la mascota no existe")
    void obtenerPorId_inexistente_lanzaNotFound() {
        UUID inexistente = UUID.randomUUID();
        when(mascotaRepository.findById(inexistente)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> mascotaService.obtenerPorId(inexistente));

        verify(mascotaRepository, times(1)).findById(inexistente);
        verify(consultaRepository, never()).findPesosRecientesPorMascotaId(any());
    }
}
