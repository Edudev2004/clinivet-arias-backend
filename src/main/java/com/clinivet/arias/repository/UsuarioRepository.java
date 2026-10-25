package com.clinivet.arias.repository;

import com.clinivet.arias.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio de acceso a datos para la entidad {@link Usuario}.
 * Utiliza identificador UUID conforme al modelo físico de APF2.
 */
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {

    /**
     * Busca un usuario por su correo electrónico institucional.
     *
     * @param email correo del usuario
     * @return Optional con el usuario si existe
     */
    Optional<Usuario> findByEmail(String email);

    /**
     * Verifica la existencia de un usuario registrado con dicho email.
     *
     * @param email correo a verificar
     * @return true si existe, false en caso contrario
     */
    boolean existsByEmail(String email);
}
