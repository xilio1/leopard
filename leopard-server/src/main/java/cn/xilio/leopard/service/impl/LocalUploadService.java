package cn.xilio.leopard.service.impl;

import cn.hutool.core.io.FileTypeUtil;
import cn.hutool.core.util.RandomUtil;

import cn.xilio.leopard.core.common.exception.BizException;
import cn.xilio.leopard.core.common.util.WebUtils;
import cn.xilio.leopard.service.AbstractUploadService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;


@Primary
@Component
@ConditionalOnProperty(name = "leopard.file.upload-model", havingValue = "Local")
public class LocalUploadService extends AbstractUploadService {
    @Value("${leopard.file.local-path}")
    private String localPath;
    @Value("${leopard.file.public-path}")
    private String publicPath;


    @Override
    public String upload(InputStream inputStream, String fileType) {
        try {
            // 1. 生成文件名
            String id = RandomUtil.randomString(32);
            String fileName = id + "." + fileType;
            String filePath = localPath + "/" + fileName;

            // 2. 确保目录存在
            Path directory = Paths.get(localPath);
            if (!Files.exists(directory)) {
                Files.createDirectories(directory);
            }

            // 3. 保存文件
            Path targetPath = Paths.get(filePath);
            Files.copy(inputStream, targetPath);

            // 4. 构造访问全路径
            String domain = WebUtils.getDomain();
            String relativePath = publicPath + "/" + fileName;
            return domain + relativePath;
        } catch (Exception e) {
            throw new BizException("6006");
        }
    }

    @Override
    public String upload(MultipartFile file) {
        try {
            String fileType = FileTypeUtil.getType(file.getInputStream());
            // 1. 生成文件名
            String id = RandomUtil.randomString(32);
            String fileName = id + "." + fileType;
            String filePath = localPath + "/" + fileName;

            // 2. 确保目录存在
            Path directory = Paths.get(localPath);
            if (!Files.exists(directory)) {
                Files.createDirectories(directory);
            }

            // 3. 保存文件
            Path targetPath = Paths.get(filePath);
            file.transferTo(targetPath);

            // 4. 构造访问全路径
            String domain = WebUtils.getDomain();
            String relativePath = publicPath + "/" + fileName;
            return domain + relativePath;
        } catch (Exception e) {
            throw new BizException("6006");
        }
    }
}
