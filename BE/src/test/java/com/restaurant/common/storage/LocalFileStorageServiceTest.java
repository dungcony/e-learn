package com.restaurant.common.storage;

import com.restaurant.common.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LocalFileStorageServiceTest {

    private static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 0, 0};
    private static final byte[] GIF = {'G', 'I', 'F', '8', '9', 'a', 0, 0};
    private static final byte[] JPEG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0, 0};

    @TempDir
    Path tempDir;

    private LocalFileStorageService service;

    @BeforeEach
    void setUp() {
        service = new LocalFileStorageService(new StorageProperties(tempDir.toString(), "/files/"));
    }

    @Test
    void storeImage_pngSavedUnderDirectoryWithRandomName() throws IOException {
        String url = service.storeImage(new MockMultipartFile("file", "me.PNG", "image/png", PNG), "avatars");

        assertThat(url).matches("/files/avatars/[0-9a-f-]{36}\\.png");
        Path saved = tempDir.resolve(url.substring("/files/".length()));
        assertThat(Files.readAllBytes(saved)).isEqualTo(PNG);
    }

    @Test
    void storeImage_acceptsGifJpgAndJpeg() {
        assertThat(service.storeImage(new MockMultipartFile("file", "a.gif", "image/gif", GIF), "x")).endsWith(".gif");
        assertThat(service.storeImage(new MockMultipartFile("file", "a.jpg", "image/jpeg", JPEG), "x")).endsWith(".jpg");
        assertThat(service.storeImage(new MockMultipartFile("file", "a.jpeg", "image/jpeg", JPEG), "x")).endsWith(".jpeg");
    }

    @Test
    void storeImage_rejectsDisallowedExtension() {
        assertRejected(new MockMultipartFile("file", "a.bmp", "image/bmp", PNG));
        assertRejected(new MockMultipartFile("file", "a.svg", "image/svg+xml", "<svg/>".getBytes()));
        assertRejected(new MockMultipartFile("file", "noextension", "image/png", PNG));
    }

    @Test
    void storeImage_rejectsScriptRenamedToPng() {
        assertRejected(new MockMultipartFile("file", "evil.png", "image/png", "#!/bin/sh\necho hi\n".getBytes()));
    }

    @Test
    void storeImage_rejectsExtensionThatDoesNotMatchContent() {
        assertRejected(new MockMultipartFile("file", "a.jpg", "image/jpeg", PNG));
    }

    @Test
    void storeImage_rejectsEmptyFile() {
        assertRejected(new MockMultipartFile("file", "a.png", "image/png", new byte[0]));
    }

    private void assertRejected(MockMultipartFile file) {
        assertThatThrownBy(() -> service.storeImage(file, "avatars"))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getCode()).isEqualTo("FILE_TYPE_NOT_SUPPORTED"));
    }
}
