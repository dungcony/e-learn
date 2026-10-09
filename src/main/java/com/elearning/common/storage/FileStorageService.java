package com.elearning.common.storage;

import org.springframework.web.multipart.MultipartFile;

/**
 * Lưu ảnh người dùng tải lên (ảnh đại diện, ảnh minh họa khóa học) và trả URL để lưu vào entity.
 */
public interface FileStorageService {

    /**
     * Lưu ảnh vào thư mục con {@code directory}, đặt tên ngẫu nhiên để không đè file của người khác.
     *
     * @param file      ảnh tải lên; chỉ nhận png, gif, jpg, jpeg
     * @param directory thư mục con chỉ gồm chữ thường, do mã nguồn truyền vào (vd {@code avatars}), không phải đầu vào của client
     * @return URL tương đối của file, vd {@code /files/avatars/3f2a....png}
     * @throws com.elearning.common.exception.BusinessException {@code FILE_TYPE_NOT_SUPPORTED} nếu file rỗng,
     *                                                          sai đuôi hoặc nội dung không phải ảnh đúng định dạng
     */
    String storeImage(MultipartFile file, String directory);
}
