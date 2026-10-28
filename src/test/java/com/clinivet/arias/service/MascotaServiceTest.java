package com.clinivet.arias.service;

import com.clinivet.arias.dto.MascotaCreateRequest;
import com.clinivet.arias.dto.MascotaResponse;
import com.clinivet.arias.dto.MascotaUpdateRequest;
import com.clinivet.arias.model.Mascota;
import com.clinivet.arias.model.Propietario;
import com.clinivet.arias.repository.MascotaRepository;
import com.clinivet.arias.repository.PropietarioRepository;
import com.clinivet.arias.service.impl.MascotaServiceImpl;
import com.clinivet.arias.exception.ResourceNotFoundException;
import com.clinivet.arias.repository.ConsultaRepository;
import com.clinivet.arias.dto.HistorialConsultaResponse;
import com.clinivet.arias.model.*;
import com.clinivet.arias.repository.CitaRepository;
import com.clinivet.arias.repository.TratamientoRepository;
import com.clinivet.arias.repository.VacunaAplicadaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
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
 * - RF-10: Ficha completa de la mascota con peso de consultas médicas.
 * - RF-11: Edición de datos de mascota manteniendo vínculo permanente con propietario.
 */
@ExtendWith(MockitoExtension.class)
class MascotaServiceTest {

    @Mock
    private MascotaRepository mascotaRepository;

    @Mock
    private PropietarioRepository propietarioRepository;

    @Mock
    private ConsultaRepository consultaRepository;

    @Mock
    private CitaRepository citaRepository;

    @Mock
    private TratamientoRepository tratamientoRepository;

    @Mock
    private VacunaAplicadaRepository vacunaAplicadaRepository;

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

    @Test
    @DisplayName("RF-10 / RF-12: Debe listar todas las mascotas activas con su peso más reciente obtenido de consultas")
    void listarTodas_retornaMascotasConPesoActualizado() {
        Mascota mascota1 = new Mascota(
                MASCOTA_ID,
                propietarioExistente,
                "Bobby",
                "Canino",
                "Golden Retriever",
                "Dorado",
                "Macho",
                LocalDate.of(2022, 4, 10),
                new BigDecimal("25.00"),
                true
        );

        when(mascotaRepository.findByActivoTrue()).thenReturn(List.of(mascota1));
        when(consultaRepository.findPesosRecientesPorMascotaId(MASCOTA_ID))
                .thenReturn(List.of(new BigDecimal("28.50")));

        List<MascotaResponse> resultado = mascotaService.listarTodas();

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("Bobby", resultado.get(0).getNombre());
        assertEquals(new BigDecimal("28.50"), resultado.get(0).getPesoActualKg());
        verify(mascotaRepository, times(1)).findByActivoTrue();
    }

    // ──────────────────────────────────────────────────────────────────────────
    // RF-11: PRUEBAS DE EDICIÓN DE MASCOTA
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("RF-11.1: Debe actualizar exitosamente los datos de la mascota manteniendo permanente el vínculo del propietario")
    void actualizar_exitoso_mantienePropietarioPermanente() {
        Mascota mascotaExistente = new Mascota(
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

        MascotaUpdateRequest updateRequest = new MascotaUpdateRequest(
                "Firulais Editado",
                "Canino",
                "Golden Retriever Puro",
                "Dorado Oscuro",
                "Macho",
                LocalDate.of(2022, 3, 10),
                new BigDecimal("29.90"),
                "https://fotos.com/firulais_nuevo.jpg",
                true
        );

        when(mascotaRepository.findById(MASCOTA_ID)).thenReturn(Optional.of(mascotaExistente));
        when(mascotaRepository.save(any(Mascota.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MascotaResponse response = mascotaService.actualizar(MASCOTA_ID, updateRequest);

        assertNotNull(response);
        assertEquals(MASCOTA_ID, response.getId());
        assertEquals("Firulais Editado", response.getNombre());
        assertEquals("Golden Retriever Puro", response.getRaza());
        assertEquals("Dorado Oscuro", response.getColor());
        assertEquals(new BigDecimal("29.90"), response.getPesoActualKg());
        assertEquals("https://fotos.com/firulais_nuevo.jpg", response.getFotoUrl());

        // Verificar criterio de aceptación estricto: Vínculo permanente con el propietario original
        assertEquals(PROPIETARIO_ID, response.getPropietarioId(), "El propietario no debe cambiar");
        assertEquals("Carlos Mendoza Ramos", response.getPropietarioNombre());

        verify(mascotaRepository, times(1)).findById(MASCOTA_ID);
        verify(mascotaRepository, times(1)).save(mascotaExistente);
    }

    @Test
    @DisplayName("RF-11.2: Debe lanzar ResourceNotFoundException (404) cuando la mascota a actualizar no existe")
    void actualizar_mascotaInexistente_lanzaNotFound() {
        UUID inexistente = UUID.randomUUID();
        MascotaUpdateRequest updateRequest = new MascotaUpdateRequest(
                "Milo",
                "Felino",
                "Siamés",
                "Blanco y Cafe",
                "Macho",
                null,
                new BigDecimal("4.20"),
                null,
                true
        );

        when(mascotaRepository.findById(inexistente)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> mascotaService.actualizar(inexistente, updateRequest));

        verify(mascotaRepository, times(1)).findById(inexistente);
        verify(mascotaRepository, never()).save(any(Mascota.class));
    }

    @Test
    @DisplayName("RF-11.3: Debe aplicar trimming y normalizar sexo al actualizar")
    void actualizar_conEspaciosYNormalizacion_aplicaFormato() {
        Mascota mascotaExistente = new Mascota(
                MASCOTA_ID,
                propietarioExistente,
                "Luna",
                "Felino",
                "Comun",
                "Negro",
                "Hembra",
                LocalDate.of(2021, 5, 20),
                new BigDecimal("3.50"),
                true
        );

        MascotaUpdateRequest updateRequest = new MascotaUpdateRequest(
                "  Luna Actualizada  ",
                "  Felino  ",
                "  Angora  ",
                "  Gris  ",
                "H",
                LocalDate.of(2021, 5, 20),
                new BigDecimal("3.80"),
                "  https://fotos.com/luna.jpg  ",
                false
        );

        when(mascotaRepository.findById(MASCOTA_ID)).thenReturn(Optional.of(mascotaExistente));
        when(mascotaRepository.save(any(Mascota.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MascotaResponse response = mascotaService.actualizar(MASCOTA_ID, updateRequest);

        assertNotNull(response);
        assertEquals("Luna Actualizada", response.getNombre());
        assertEquals("Felino", response.getEspecie());
        assertEquals("Angora", response.getRaza());
        assertEquals("Gris", response.getColor());
        assertEquals("Hembra", response.getSexo());
        assertEquals("https://fotos.com/luna.jpg", response.getFotoUrl());
        assertFalse(response.getActivo());
    }

    // ========================================================
    // RF-25 y RF-26: Historial clinico completo con filtros
    // ========================================================

    @Test
    @DisplayName("RF-25: Consulta exitosa de historial completo con diagnostico, tratamientos y vacunas")
    void obtenerHistorial_exitoso() {
        UUID consultaId = UUID.randomUUID();
        UUID citaId = UUID.randomUUID();
        UUID vetId = UUID.randomUUID();

        Usuario usuarioVet = new Usuario();
        usuarioVet.setNombre("Patricia");
        usuarioVet.setApellido("Gomez");

        Veterinario vet = new Veterinario();
        vet.setId(vetId);
        vet.setUsuario(usuarioVet);

        Cita cita = new Cita();
        cita.setId(citaId);
        cita.setFechaHora(LocalDateTime.of(2026, 10, 20, 10, 0));
        cita.setVeterinario(vet);

        Consulta consulta = new Consulta(
                consultaId,
                citaId,
                new BigDecimal("28.50"),
                new BigDecimal("38.5"),
                "Control general",
                "Gastroenteritis leve",
                "Observaciones de control"
        );
        consulta.setCreatedAt(LocalDateTime.of(2026, 10, 20, 10, 30));

        Medicamento med = new Medicamento();
        med.setId(UUID.randomUUID());
        med.setNombre("Amoxicilina");
        med.setPrincipioActivo("Amoxicilina trihidrato");

        Tratamiento tratamiento = new Tratamiento(
                UUID.randomUUID(),
                consulta,
                med,
                new BigDecimal("12.50"),
                new BigDecimal("356.25"),
                7,
                "Tomar cada 12h"
        );

        VacunaCatalogo cat = new VacunaCatalogo(UUID.randomUUID(), "Sextuple", "Vacuna canina");
        Mascota mascota = new Mascota();
        mascota.setId(MASCOTA_ID);
        mascota.setNombre("Firulais");

        VacunaAplicada vacuna = new VacunaAplicada(
                UUID.randomUUID(),
                mascota,
                cat,
                consulta,
                vet,
                LocalDate.of(2026, 10, 20),
                LocalDate.of(2027, 10, 20)
        );

        when(mascotaRepository.existsById(MASCOTA_ID)).thenReturn(true);
        when(consultaRepository.findHistorialPorMascota(eq(MASCOTA_ID), isNull(), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(consulta)));
        when(citaRepository.findById(citaId)).thenReturn(Optional.of(cita));
        when(tratamientoRepository.findByConsultaIdOrderByCreatedAtAsc(consultaId)).thenReturn(List.of(tratamiento));
        when(vacunaAplicadaRepository.findByConsultaId(consultaId)).thenReturn(List.of(vacuna));

        Page<HistorialConsultaResponse> pagina = mascotaService.obtenerHistorial(
                MASCOTA_ID, null, null, null, PageRequest.of(0, 20));

        assertNotNull(pagina);
        assertEquals(1, pagina.getTotalElements());
        HistorialConsultaResponse item = pagina.getContent().get(0);
        assertEquals(consultaId, item.getConsultaId());
        assertEquals("Gastroenteritis leve", item.getDiagnostico());
        assertEquals("Patricia Gomez", item.getVeterinarioNombre());
        assertEquals(1, item.getTratamientos().size());
        assertEquals(1, item.getVacunas().size());
    }

    @Test
    @DisplayName("RF-25: Lanza ResourceNotFoundException si la mascota no existe al consultar historial")
    void obtenerHistorial_mascotaInexistente_lanza404() {
        when(mascotaRepository.existsById(MASCOTA_ID)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () ->
                mascotaService.obtenerHistorial(MASCOTA_ID, null, null, null, PageRequest.of(0, 20)));
    }

    @Test
    @DisplayName("RF-26: Consulta de historial con filtros combinados de fecha y veterinario")
    void obtenerHistorial_conFiltros() {
        UUID vetId = UUID.randomUUID();
        LocalDate inicio = LocalDate.of(2026, 10, 1);
        LocalDate fin = LocalDate.of(2026, 10, 31);

        when(mascotaRepository.existsById(MASCOTA_ID)).thenReturn(true);
        when(consultaRepository.findHistorialPorMascota(
                eq(MASCOTA_ID), eq(vetId), any(LocalDateTime.class), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        Page<HistorialConsultaResponse> pagina = mascotaService.obtenerHistorial(
                MASCOTA_ID, inicio, fin, vetId, PageRequest.of(0, 20));

        assertNotNull(pagina);
        assertEquals(0, pagina.getTotalElements());
    }
}
