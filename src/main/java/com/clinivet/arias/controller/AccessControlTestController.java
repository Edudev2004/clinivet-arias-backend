package com.clinivet.arias.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Controlador de prueba para verificación del control de acceso por rol (RF-03).
 * Valida los permisos específicos de cada rol definido en el sistema.
 */
@RestController
@RequestMapping("/api/test/roles")
@Tag(name = "Control de Acceso (Test)", description = "Endpoints de verificación de permisos por rol")
public class AccessControlTestController {

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Recurso de Administrador", description = "Accesible únicamente por usuarios con rol ADMINISTRADOR")
    public ResponseEntity<Map<String, String>> recursoAdmin() {
        return ResponseEntity.ok(Map.of("mensaje", "Acceso concedido a zona de ADMINISTRADOR"));
    }

    @GetMapping("/veterinario")
    @PreAuthorize("hasRole('VETERINARIO')")
    @Operation(summary = "Recurso de Veterinario", description = "Accesible únicamente por médicos veterinarios")
    public ResponseEntity<Map<String, String>> recursoVeterinario() {
        return ResponseEntity.ok(Map.of("mensaje", "Acceso concedido a zona de VETERINARIO"));
    }

    @GetMapping("/inventario")
    @PreAuthorize("hasRole('ENCARGADO_INVENTARIO')")
    @Operation(summary = "Recurso de Inventario", description = "Accesible únicamente por el Encargado de Inventario")
    public ResponseEntity<Map<String, String>> recursoInventario() {
        return ResponseEntity.ok(Map.of("mensaje", "Acceso concedido a zona de ENCARGADO_INVENTARIO"));
    }

    @GetMapping("/recepcion")
    @PreAuthorize("hasRole('RECEPCIONISTA')")
    @Operation(summary = "Recurso de Recepción", description = "Accesible únicamente por Recepcionistas")
    public ResponseEntity<Map<String, String>> recursoRecepcion() {
        return ResponseEntity.ok(Map.of("mensaje", "Acceso concedido a zona de RECEPCIONISTA"));
    }
}
