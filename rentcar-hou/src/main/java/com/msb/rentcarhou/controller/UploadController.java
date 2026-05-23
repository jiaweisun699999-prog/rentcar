package com.msb.rentcarhou.controller;

import com.msb.rentcarhou.common.result.Result;
import com.msb.rentcarhou.vo.UploadResVo;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class UploadController {
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024L;
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "bmp", "webp", "pdf");

    @PostMapping("/upload")
    public Result<UploadResVo> upload(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        try {
            if (file == null || file.isEmpty()) {
                throw new RuntimeException("上传文件不能为空");
            }
            if (file.getSize() > MAX_FILE_SIZE) {
                throw new RuntimeException("文件大小不能超过10MB");
            }

            String originalFilename = file.getOriginalFilename();
            String extension = StringUtils.getFilenameExtension(originalFilename);
            if (!StringUtils.hasText(extension)) {
                throw new RuntimeException("文件格式不正确");
            }

            extension = extension.toLowerCase();
            if (!ALLOWED_EXTENSIONS.contains(extension)) {
                throw new RuntimeException("不支持的文件格式");
            }

            String datePath = LocalDate.now().toString();
            Path uploadDir = Paths.get(System.getProperty("user.dir"), "uploads", datePath);
            Files.createDirectories(uploadDir);

            String filename = UUID.randomUUID().toString().replace("-", "") + "." + extension;
            Path targetPath = uploadDir.resolve(filename);
            file.transferTo(targetPath.toFile());

            UploadResVo resVo = new UploadResVo();
            resVo.setUrl(buildFileUrl(request, datePath, filename));
            return Result.success("上传成功", resVo);
        } catch (IOException e) {
            return Result.error("文件保存失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    private String buildFileUrl(HttpServletRequest request, String datePath, String filename) {
        String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
        return baseUrl + "/uploads/" + datePath + "/" + filename;
    }
}
