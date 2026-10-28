package com.clinivet.arias.service;

import com.clinivet.arias.service.impl.SupabaseStorageServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;

class SupabaseStorageServiceImplTest {

    private SupabaseStorageServiceImpl storageService;

    @BeforeEach
    void setUp() {
        storageService = new SupabaseStorageServiceImpl(
                "https://test.supabase.co",
                "test_key",
                "test-bucket"
        );
    }

    @Test
    @DisplayName("Subida válida en modo test retorna URL pública")
    void uploadFile_ValidImage_ReturnsPublicUrl() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "avatar.png", "image/png", "fake image content".getBytes()
        );

        String url = storageService.uploadFile(file, "avatars");

        assertNotNull(url);
        assertTrue(url.startsWith("https://test.supabase.co/storage/v1/object/public/test-bucket/avatars/"));
        assertTrue(url.endsWith(".png"));
    }

    @Test
    @DisplayName("Archivo vacío lanza 400 Bad Request")
    void uploadFile_EmptyFile_ThrowsBadRequest() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "empty.png", "image/png", new byte[0]
        );

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                storageService.uploadFile(file, "avatars")
        );

        assertEquals(400, ex.getStatusCode().value());
        assertTrue(ex.getReason().contains("vacío"));
    }

    @Test
    @DisplayName("Tipo de contenido no permitido lanza 400 Bad Request")
    void uploadFile_InvalidContentType_ThrowsBadRequest() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "script.exe", "application/x-msdownload", "binary".getBytes()
        );

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                storageService.uploadFile(file, "avatars")
        );

        assertEquals(400, ex.getStatusCode().value());
        assertTrue(ex.getReason().contains("Tipo de archivo no permitido"));
    }
}
