package com.clinivet.arias.service.impl;

import com.clinivet.arias.service.IStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

/**
 * Implementación de almacenamiento en la nube conectada a Supabase Storage mediante REST API.
 * Réplica exacta del patrón utilizado en VetHD.
 */
@Service
public class SupabaseStorageServiceImpl implements IStorageService {

    private static final Logger log = LoggerFactory.getLogger(SupabaseStorageServiceImpl.class);

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/jpg",
            "image/png",
            "image/webp",
            "image/gif",
            "application/pdf"
    );

    private final RestClient restClient;
    private final String supabaseUrl;
    private final String supabaseKey;
    private final String bucket;

    public SupabaseStorageServiceImpl(
            @Value("${supabase.url:}") String supabaseUrl,
            @Value("${supabase.key:}") String supabaseKey,
            @Value("${supabase.bucket:clinivet-arias}") String bucket) {
        this.supabaseUrl = supabaseUrl != null ? supabaseUrl.replaceAll("/+$", "") : "";
        this.supabaseKey = supabaseKey;
        this.bucket = bucket;
        this.restClient = RestClient.builder().build();
    }

    @Override
    public String uploadFile(MultipartFile file, String folder) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El archivo no puede estar vacío");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El archivo excede el tamaño máximo permitido de 10MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tipo de archivo no permitido. Solo se aceptan imágenes (JPEG, PNG, WEBP, GIF) y documentos PDF");
        }

        String cleanFolder = (folder == null || folder.isBlank()) ? "general" : folder.replaceAll("[^a-zA-Z0-9_-]", "");
        String originalFilename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "archivo";
        String extension = getFileExtension(originalFilename);
        String uniqueFilename = UUID.randomUUID() + (extension.isEmpty() ? "" : "." + extension);
        String filePath = cleanFolder + "/" + uniqueFilename;

        String publicUrl = supabaseUrl + "/storage/v1/object/public/" + bucket + "/" + filePath;

        // Modo simulación o test si no hay clave configurada o inicia con 'test_'
        if (supabaseKey == null || supabaseKey.isBlank() || supabaseKey.startsWith("test_")) {
            log.info("[SUPABASE_STORAGE] Modo simulación/test: Archivo subido mock a {}", publicUrl);
            return publicUrl;
        }

        try {
            byte[] fileBytes = file.getBytes();
            String uploadUrl = supabaseUrl + "/storage/v1/object/" + bucket + "/" + filePath;

            restClient.post()
                    .uri(uploadUrl)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + supabaseKey)
                    .header("apikey", supabaseKey)
                    .header(HttpHeaders.CONTENT_TYPE, contentType)
                    .body(fileBytes)
                    .retrieve()
                    .toBodilessEntity();

            log.info("[SUPABASE_STORAGE] Archivo subido exitosamente a: {}", publicUrl);
            return publicUrl;
        } catch (IOException e) {
            log.error("[SUPABASE_STORAGE] Error leyendo bytes del archivo: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error procesando el archivo");
        } catch (Exception e) {
            log.error("[SUPABASE_STORAGE] Error al subir archivo a Supabase Storage: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al comunicarse con el almacenamiento de Supabase: " + e.getMessage());
        }
    }

    private String getFileExtension(String filename) {
        int lastIndex = filename.lastIndexOf('.');
        if (lastIndex > 0 && lastIndex < filename.length() - 1) {
            return filename.substring(lastIndex + 1).toLowerCase();
        }
        return "";
    }
}
