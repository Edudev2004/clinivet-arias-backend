package com.clinivet.arias.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO para la solicitud de registro de nuevo propietario (RF-05).
 */
@Schema(description = "Payload para registrar un nuevo cliente/propietario de mascotas en la veterinaria")
public class PropietarioCreateRequest {

    @NotBlank(message = "El nombre del propietario es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre debe contener entre 2 y 100 caracteres")
    @Schema(description = "Nombre(s) del propietario", example = "Carlos")
    private String nombre;

    @NotBlank(message = "El apellido del propietario es obligatorio")
    @Size(min = 2, max = 100, message = "El apellido debe contener entre 2 y 100 caracteres")
    @Schema(description = "Apellido(s) del propietario", example = "Mendoza Ramos")
    private String apellido;

    @NotBlank(message = "El DNI es obligatorio")
    @Pattern(regexp = "^[0-9]{8,12}$", message = "El DNI debe contener entre 8 y 12 dígitos numéricos")
    @Schema(description = "Documento Nacional de Identidad único", example = "45871236")
    private String dni;

    @NotBlank(message = "El teléfono de contacto es obligatorio")
    @Size(min = 6, max = 20, message = "El teléfono debe contener entre 6 y 20 caracteres")
    @Schema(description = "Número telefónico de contacto", example = "987654321")
    private String telefono;

    @Email(message = "El formato del correo electrónico no es válido")
    @Size(max = 150, message = "El correo electrónico no puede superar los 150 caracteres")
    @Schema(description = "Correo electrónico del propietario", example = "carlos.mendoza@gmail.com")
    private String email;

    @Size(max = 255, message = "La dirección no puede superar los 255 caracteres")
    @Schema(description = "Dirección domiciliaria del propietario", example = "Av. Universitaria 1240, Carabayllo")
    private String direccion;

    public PropietarioCreateRequest() {
    }

    public PropietarioCreateRequest(String nombre, String apellido, String dni, String telefono, String email, String direccion) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.dni = dni;
        this.telefono = telefono;
        this.email = email;
        this.direccion = direccion;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }
}
