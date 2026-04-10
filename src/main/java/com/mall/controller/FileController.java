package com.mall.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import com.mall.common.Result;
import com.mall.exception.BusinessException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 文件上传控制器
 */
@Api(tags = "文件上传")
@RestController
@RequestMapping("/api/file")
public class FileController {

    @Value("${file.upload-path:./uploads}")
    private String uploadPath;

    @Value("${file.access-url:http://localhost:8080/uploads}")
    private String accessUrl;

    /**
     * 初始化上传目录
     */
    @PostConstruct
    public void init() {
        File dir = new File(uploadPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    @ApiOperation("上传文件")
    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException("请选择要上传的文件");
        }

        // 获取文件后缀
        String originalFilename = file.getOriginalFilename();
        String suffix = FileUtil.getSuffix(originalFilename);

        // 验证文件类型（只允许图片）
        String[] allowedTypes = {"jpg", "jpeg", "png", "gif", "webp"};
        boolean isAllowed = false;
        for (String type : allowedTypes) {
            if (type.equalsIgnoreCase(suffix)) {
                isAllowed = true;
                break;
            }
        }
        if (!isAllowed) {
            throw new BusinessException("只支持上传图片文件（jpg、jpeg、png、gif、webp）");
        }

        // 生成新文件名
        String newFileName = IdUtil.simpleUUID() + "." + suffix;

        // 按日期分目录
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String relativePath = datePath + "/" + newFileName;

        try {
            // 创建目录
            Path destDir = Paths.get(uploadPath, datePath);
            if (!Files.exists(destDir)) {
                Files.createDirectories(destDir);
            }

            // 保存文件
            Path destFile = Paths.get(uploadPath, relativePath);
            file.transferTo(destFile.toFile());

            // 返回访问URL
            String url = accessUrl + "/" + relativePath;
            return Result.success(url);
        } catch (IOException e) {
            e.printStackTrace();
            throw new BusinessException("文件上传失败: " + e.getMessage());
        }
    }
}
