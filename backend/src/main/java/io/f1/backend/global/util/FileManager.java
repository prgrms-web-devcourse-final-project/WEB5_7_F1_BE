package io.f1.backend.global.util;

import io.f1.backend.global.exception.CustomException;
import io.f1.backend.global.exception.errorcode.QuizErrorCode;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FileManager {
    public static void deleteFile(String filePath) {
        Path path = Paths.get(filePath).toAbsolutePath();

        try {
            boolean deleted = Files.deleteIfExists(path);
            if (deleted) {
                log.info("파일 삭제 완료 : {}", path);
            } else {
                log.info("기존 파일 존재 X : {}", path);
            }
        } catch (IOException e) {
            log.error("파일 삭제 중 오류 : {}", path);
            throw new CustomException(QuizErrorCode.IMAGE_DELETE_FAILED);
        }
    }

    public static String saveMultipartFile(MultipartFile imageFile, String filePath) {
        String originalFilename = imageFile.getOriginalFilename();
        String ext = getExtension(originalFilename);
        String savedFilename = UUID.randomUUID().toString() + "." + ext;

        try {
            Path savePath = Paths.get(filePath, savedFilename).toAbsolutePath();
            imageFile.transferTo(savePath.toFile());
        } catch (IOException e) {
            log.error("파일 업로드 중 IOException 발생", e);
            throw new CustomException(QuizErrorCode.IMAGE_SAVE_FAILED);
        }

        return filePath + savedFilename;
    }

    public static String getExtension(String filename) {
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }
}
