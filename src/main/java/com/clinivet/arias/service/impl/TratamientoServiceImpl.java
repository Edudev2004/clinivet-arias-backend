package com.clinivet.arias.service.impl;

import com.clinivet.arias.dto.TratamientoCreateRequest;
import com.clinivet.arias.dto.TratamientoResponse;
import com.clinivet.arias.exception.ResourceNotFoundException;
import com.clinivet.arias.model.Consulta;
import com.clinivet.arias.model.Medicamento;
import com.clinivet.arias.model.Tratamiento;
import com.clinivet.arias.repository.ConsultaRepository;
import com.clinivet.arias.repository.MedicamentoRepository;
import com.clinivet.arias.repository.TratamientoRepository;
import com.clinivet.arias.service.IDosisCalculatorService;
import com.clinivet.arias.service.ITratamientoService;
import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementación de la lógica de negocio para la gestión y prescripción de tratamientos.
 * Aplica principios SOLID:
 * - SRP: Encargado de coordinar la prescripción y persistencia del tratamiento en consulta.
 * - DIP: Inyecta interfaces (ConsultaRepository, MedicamentoRepository, TratamientoRepository, IDosisCalculatorService).
 * - Uso estricto de Guava Preconditions y Apache Commons StringUtils.
 * Cumple con RN-04, RN-12 y RF-23 según APF2.
 */
@Service
public class TratamientoServiceImpl implements ITratamientoService {

    private static final Logger log = LoggerFactory.getLogger(TratamientoServiceImpl.class);

    private final ConsultaRepository consultaRepository;
    private final MedicamentoRepository medicamentoRepository;
    private final TratamientoRepository tratamientoRepository;
    private final IDosisCalculatorService dosisCalculatorService;

    // Inyección estricta por constructor (DIP)
    public TratamientoServiceImpl(ConsultaRepository consultaRepository,
                                  MedicamentoRepository medicamentoRepository,
                                  TratamientoRepository tratamientoRepository,
                                  IDosisCalculatorService dosisCalculatorService) {
        this.consultaRepository = consultaRepository;
        this.medicamentoRepository = medicamentoRepository;
        this.tratamientoRepository = tratamientoRepository;
        this.dosisCalculatorService = dosisCalculatorService;
    }

    @Override
    @Transactional
    public TratamientoResponse prescribirTratamiento(UUID consultaId, TratamientoCreateRequest request) {
        Preconditions.checkNotNull(consultaId, "El identificador de la consulta no puede ser nulo");
        Preconditions.checkNotNull(request, "La solicitud de prescripción no puede ser nula");
        Preconditions.checkNotNull(request.getMedicamentoId(), "El identificador del medicamento es obligatorio");

        // 1. Validar existencia de la consulta médica
        Consulta consulta = consultaRepository.findById(consultaId)
                .orElseThrow(() -> new ResourceNotFoundException("Consulta médica no encontrada con ID: " + consultaId));

        // 2. Criterio de aceptación RF-23: Si el peso no está registrado en la consulta, retorna HTTP 400
        BigDecimal pesoConsulta = consulta.getPesoKg();
        if (pesoConsulta == null || pesoConsulta.compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("Intento de prescripción en consulta [{}] sin peso registrado de la mascota", consultaId);
            throw new IllegalArgumentException(
                    "La consulta médica no tiene registrado el peso de la mascota. Se requiere registrar el peso para calcular la dosis automáticamente (RF-23).");
        }

        // 3. Validar existencia del medicamento prescrito
        Medicamento medicamento = medicamentoRepository.findById(request.getMedicamentoId())
                .orElseThrow(() -> new ResourceNotFoundException("Medicamento no encontrado con ID: " + request.getMedicamentoId()));

        if (Boolean.FALSE.equals(medicamento.getActivo())) {
            throw new IllegalArgumentException("El medicamento '" + medicamento.getNombre() + "' se encuentra inactivo");
        }

        // 4. Calcular automáticamente la dosis total: DosisTotal (mg) = Peso (kg) * Dosis (mg/kg) (RN-12 / RF-23)
        BigDecimal dosisTotalMg = dosisCalculatorService.calcularDosisTotal(pesoConsulta, request.getDosisMgPorKg());

        // 5. Crear y persistir el tratamiento con la desnormalización controlada de dosis_total_mg
        Tratamiento tratamiento = new Tratamiento();
        tratamiento.setConsulta(consulta);
        tratamiento.setMedicamento(medicamento);
        tratamiento.setDosisMgPorKg(request.getDosisMgPorKg());
        tratamiento.setDosisTotalMg(dosisTotalMg);
        tratamiento.setDuracionDias(request.getDuracionDias());
        tratamiento.setIndicaciones(StringUtils.trimToNull(request.getIndicaciones()));

        Tratamiento guardado = tratamientoRepository.save(tratamiento);

        log.info("Tratamiento [{}] prescrito con éxito en consulta [{}]. Medicamento: [{}], DosisTotal: [{} mg]",
                guardado.getId(), consultaId, medicamento.getNombre(), dosisTotalMg);

        return mapToResponse(guardado, pesoConsulta);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TratamientoResponse> listarPorConsulta(UUID consultaId) {
        Preconditions.checkNotNull(consultaId, "El identificador de la consulta no puede ser nulo");

        Consulta consulta = consultaRepository.findById(consultaId)
                .orElseThrow(() -> new ResourceNotFoundException("Consulta médica no encontrada con ID: " + consultaId));

        BigDecimal peso = consulta.getPesoKg();

        return tratamientoRepository.findByConsultaIdOrderByCreatedAtAsc(consultaId).stream()
                .map(t -> mapToResponse(t, peso))
                .collect(Collectors.toList());
    }

    private TratamientoResponse mapToResponse(Tratamiento t, BigDecimal pesoKg) {
        return new TratamientoResponse(
                t.getId(),
                t.getConsulta().getId(),
                t.getMedicamento().getId(),
                t.getMedicamento().getNombre(),
                t.getMedicamento().getPrincipioActivo(),
                pesoKg,
                t.getDosisMgPorKg(),
                t.getDosisTotalMg(),
                t.getDuracionDias(),
                t.getIndicaciones(),
                t.getCreatedAt()
        );
    }
}
