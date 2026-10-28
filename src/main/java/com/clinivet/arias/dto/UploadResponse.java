package com.clinivet.arias.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO que encapsula la respuesta tras subir un archivo exitosamente a Supabase Storage (igual que VetHD).
 */
@Schema(description = "Respuesta de carga de archivo en Supabase Storage")
public class UploadResponse {

    @Schema(description = "URL pública de acceso directo al archivo", example = "https://gxjbvbgppabpumctkrrh.supabase.co/storage/v1/object/public/clinivet-arias/mascotas/uuid-foto.jpg")
    private String url;

    @Schema(description = "Nombre original del archivo subido", example = "foto_mascota.jpg")
    private String fileName;

    @Schema(description = "Tamaño del archivo en bytes", example = "1048576")
    private long size;

    public UploadResponse() {
    }

    public UploadResponse(String url, String fileName, long size) {
        this.url = url;
        this.fileName = fileName;
        this.size = size;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }
}
