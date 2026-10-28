package com.clinivet.arias.controller;

import com.clinivet.arias.dto.UploadResponse;
import com.clinivet.arias.service.IStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * Controlador REST para carga y gestión de archivos en Supabase Storage.
 * Cumple con SRP (orquestación HTTP) y DIP (depende de IStorageService) igual que VetHD.
 */
@RestController
@RequestMapping({"/api/storage", "/storage"})
@Tag(name = "Almacenamiento Cloud", description = "Endpoints para subida de fotos y documentos a Supabase Storage")
public class StorageController {

    private final IStorageService storageService;

    public StorageController(IStorageService storageService) {
        this.storageService = storageService;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Subir archivo a Supabase Storage",
            description = "Sube una imagen o documento al bucket configurado de Supabase Storage y retorna la URL pública permanente.")
    public ResponseEntity<UploadResponse> uploadFile(
            @Parameter(description = "Archivo binario a subir (JPG, PNG, WEBP, GIF, PDF)")
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "Carpeta de destino (ej. 'mascotas', 'avatares')")
            @RequestParam(value = "folder", defaultValue = "general") String folder) {
        String url = storageService.uploadFile(file, folder);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new UploadResponse(url, file.getOriginalFilename(), file.getSize()));
    }
}
