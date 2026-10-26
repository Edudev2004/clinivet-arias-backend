package com.clinivet.arias.controller;

import com.clinivet.arias.service.IStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class StorageControllerTest {

    @Mock
    private IStorageService storageService;

    @InjectMocks
    private StorageController storageController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(storageController).build();
    }

    @Test
    @DisplayName("POST /api/storage/upload sube archivo y retorna 201 Created con URL pública")
    void upload_ValidFile_ReturnsCreated() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "perro.jpg", "image/jpeg", "image bytes".getBytes()
        );

        when(storageService.uploadFile(any(), eq("mascotas")))
                .thenReturn("https://gxjbvbgppabpumctkrrh.supabase.co/storage/v1/object/public/clinivet-arias/mascotas/uuid-perro.jpg");

        mockMvc.perform(multipart("/api/storage/upload")
                        .file(file)
                        .param("folder", "mascotas"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.url").value("https://gxjbvbgppabpumctkrrh.supabase.co/storage/v1/object/public/clinivet-arias/mascotas/uuid-perro.jpg"))
                .andExpect(jsonPath("$.fileName").value("perro.jpg"));
    }
}
