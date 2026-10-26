package com.clinivet.arias.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * Contrato de servicio para el almacenamiento cloud de archivos (Supabase Storage).
 * Cumple con ISP y DIP (arquitectura desacoplada idéntica a VetHD).
 */
public interface IStorageService {

    /**
     * Sube un archivo a Supabase Storage y retorna su URL pública accesible.
     *
     * @param file Archivo multipart a subir
     * @param folder Carpeta dentro del bucket (ej: 'mascotas', 'avatares')
     * @return URL pública del archivo en Supabase Storage
     */
    String uploadFile(MultipartFile file, String folder);
}
