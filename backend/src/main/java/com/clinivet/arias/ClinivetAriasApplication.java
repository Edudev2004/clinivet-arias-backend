package com.clinivet.arias;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada principal de la aplicación Clinivet Arias.
 *
 * <p>{@code @SpringBootApplication} es un atajo que combina tres anotaciones:
 * <ul>
 *   <li>{@code @Configuration} — marca la clase como fuente de beans de Spring.</li>
 *   <li>{@code @EnableAutoConfiguration} — activa la configuración automática
 *       según las dependencias presentes en el classpath (Tomcat, JPA, Security...).</li>
 *   <li>{@code @ComponentScan} — escanea todos los sub-paquetes de
 *       {@code com.clinivet.arias} buscando componentes (@Controller, @Service,
 *       @Repository, @Component).</li>
 * </ul>
 *
 * @author Grupo 5 — UTP Desarrollo de Software
 * @version 0.0.1-SNAPSHOT
 */
@SpringBootApplication
public class ClinivetAriasApplication {

    /**
     * Método main: arranca el contenedor de Spring Boot con el servidor
     * embebido Tomcat en el puerto configurado en {@code application.yml}.
     *
     * @param args argumentos de línea de comandos (no utilizados actualmente)
     */
    public static void main(String[] args) {
        SpringApplication.run(ClinivetAriasApplication.class, args);
    }
}
