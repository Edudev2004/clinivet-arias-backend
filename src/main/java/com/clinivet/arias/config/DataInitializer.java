package com.clinivet.arias.config;

import com.clinivet.arias.model.Rol;
import com.clinivet.arias.model.Usuario;
import com.clinivet.arias.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Inicializador de datos de prueba para ambiente de desarrollo.
 * Si la base de datos no contiene usuarios, crea automáticamente
 * las cuentas iniciales para Administrador, Veterinario y Recepcionista.
 */
@Configuration
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Bean
    public CommandLineRunner initDatabase(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (usuarioRepository.count() == 0) {
                log.info("Base de datos sin usuarios. Creando cuentas iniciales de prueba...");

                Usuario admin = new Usuario(
                        null,
                        "Rommel",
                        "Navarro",
                        "admin@clinivet.pe",
                        passwordEncoder.encode("Admin123!"),
                        "987654321",
                        Rol.ADMIN,
                        true
                );

                Usuario veterinario = new Usuario(
                        null,
                        "Beckhan",
                        "Elias",
                        "veterinario@clinivet.pe",
                        passwordEncoder.encode("Vet123!"),
                        "987654322",
                        Rol.VETERINARIO,
                        true
                );

                Usuario recepcionista = new Usuario(
                        null,
                        "Lucero",
                        "Perez",
                        "recepcion@clinivet.pe",
                        passwordEncoder.encode("Recep123!"),
                        "987654323",
                        Rol.RECEPCIONISTA,
                        true
                );

                usuarioRepository.save(admin);
                usuarioRepository.save(veterinario);
                usuarioRepository.save(recepcionista);

                log.info("Cuentas iniciales creadas: admin@clinivet.pe, veterinario@clinivet.pe, recepcion@clinivet.pe");
            }
        };
    }
}
