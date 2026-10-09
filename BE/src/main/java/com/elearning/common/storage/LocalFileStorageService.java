package com.elearning.common.storage;

import com.elearning.common.exception.BusinessException;
import com.elearning.common.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * Lưu ảnh vào ổ đĩa của máy chủ dưới {@link StorageProperties#baseDir()}.
 * <p>
 * Không tin đuôi file hay {@code Content-Type} do client gửi: đuôi phải thuộc danh sách cho phép
 * <b>và</b> các byte đầu file phải đúng chữ ký của định dạng đó, để không lưu được file thực thi
 * đổi tên thành {@code .png}.
 */
@Service
public class LocalFileStorageService implements FileStorageService {

    private static final Map<String, String> EXTENSION_TO_FORMAT = Map.of(
            "png", "png", "gif", "gif", "jpg", "jpeg", "jpeg", "jpeg");

    private final Path baseDir;
    private final String publicPath;

    public LocalFileStorageService(StorageProperties properties) {
        this.baseDir = Path.of(properties.baseDir()).toAbsolutePath().normalize();
        this.publicPath = trimTrailingSlash(properties.publicPath());
    }

    @Override
    public String storeImage(MultipartFile file, String directory) {
        String extension = extensionOf(file.getOriginalFilename());
        String expectedFormat = EXTENSION_TO_FORMAT.get(extension);
        if (file.isEmpty() || expectedFormat == null || !expectedFormat.equals(detectFormat(file))) {
            throw new BusinessException(ErrorCode.FILE_TYPE_NOT_SUPPORTED);
        }
        String fileName = UUID.randomUUID() + "." + extension;
        Path targetDir = baseDir.resolve(directory);
        try {
            Files.createDirectories(targetDir);
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, targetDir.resolve(fileName));
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Không lưu được file ảnh vào " + targetDir, e);
        }
        return publicPath + "/" + directory + "/" + fileName;
    }

    private String extensionOf(String originalName) {
        if (originalName == null || originalName.lastIndexOf('.') < 0) {
            return "";
        }
        return originalName.substring(originalName.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
    }

    private String detectFormat(MultipartFile file) {
        byte[] head = new byte[8];
        int read;
        try (InputStream in = file.getInputStream()) {
            read = in.readNBytes(head, 0, head.length);
        } catch (IOException e) {
            throw new UncheckedIOException("Không đọc được file ảnh tải lên", e);
        }
        if (read >= 8 && (head[0] & 0xFF) == 0x89 && head[1] == 'P' && head[2] == 'N' && head[3] == 'G') {
            return "png";
        }
        if (read >= 4 && head[0] == 'G' && head[1] == 'I' && head[2] == 'F' && head[3] == '8') {
            return "gif";
        }
        if (read >= 3 && (head[0] & 0xFF) == 0xFF && (head[1] & 0xFF) == 0xD8 && (head[2] & 0xFF) == 0xFF) {
            return "jpeg";
        }
        return "";
    }

    private String trimTrailingSlash(String path) {
        return path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
    }
}
