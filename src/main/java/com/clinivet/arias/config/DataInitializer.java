package com.clinivet.arias.config;

import com.clinivet.arias.model.Rol;
import com.clinivet.arias.model.Usuario;
import com.clinivet.arias.repository.RolRepository;
import com.clinivet.arias.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Inicializador de datos de prueba para ambiente de desarrollo.
 * Si la base de datos no contiene roles ni usuarios, crea automáticamente
 * las entidades iniciales según las especificaciones de APF2.
 */
@Configuration
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Bean
    public CommandLineRunner initDatabase(UsuarioRepository usuarioRepository,
                                          RolRepository rolRepository,
                                          PasswordEncoder passwordEncoder) {
        return args -> {
            if (rolRepository.count() == 0) {
                log.info("Creando roles iniciales según APF2 (Tabla 28)...");
                rolRepository.save(new Rol(null, "ADMINISTRADOR", "Administrador general del sistema"));
                rolRepository.save(new Rol(null, "VETERINARIO", "Médico veterinario especialista"));
                rolRepository.save(new Rol(null, "RECEPCIONISTA", "Personal de atención y agendamiento"));
                rolRepository.save(new Rol(null, "ENCARGADO_INVENTARIO", "Control de existencias y kardex"));
            }

            if (usuarioRepository.count() == 0) {
                log.info("Base de datos sin usuarios. Creando cuentas iniciales de prueba...");

                Rol rolAdmin = rolRepository.findByNombre("ADMINISTRADOR")
                        .orElseGet(() -> rolRepository.save(new Rol(null, "ADMINISTRADOR", "Admin")));
                Rol rolVet = rolRepository.findByNombre("VETERINARIO")
                        .orElseGet(() -> rolRepository.save(new Rol(null, "VETERINARIO", "Vet")));
                Rol rolRecep = rolRepository.findByNombre("RECEPCIONISTA")
                        .orElseGet(() -> rolRepository.save(new Rol(null, "RECEPCIONISTA", "Recep")));

                Usuario admin = new Usuario(
                        null,
                        "Rommel",
                        "Navarro",
                        "admin@clinivet.pe",
                        passwordEncoder.encode("Admin123!"),
                        rolAdmin,
                        true
                );

                Usuario veterinario = new Usuario(
                        null,
                        "Beckhan",
                        "Elias",
                        "veterinario@clinivet.pe",
                        passwordEncoder.encode("Vet123!"),
                        rolVet,
                        true
                );

                Usuario recepcionista = new Usuario(
                        null,
                        "Lucero",
                        "Perez",
                        "recepcion@clinivet.pe",
                        passwordEncoder.encode("Recep123!"),
                        rolRecep,
                        true
                );

                usuarioRepository.save(admin);
                usuarioRepository.save(veterinario);
                usuarioRepository.save(recepcionista);

                log.info("Cuentas de prueba creadas exitosamente: admin@clinivet.pe, veterinario@clinivet.pe, recepcion@clinivet.pe");
            }
        };
    }
}
