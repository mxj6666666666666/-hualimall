package com.xinjiema.hualimall.controll;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.xinjiema.hualimall.mapper.OrderMapper;
import com.xinjiema.hualimall.mapper.ProductMapper;
import com.xinjiema.hualimall.pojo.*;
import com.xinjiema.hualimall.utils.AuthContext;
import com.xinjiema.hualimall.utils.UuidV7Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Locale;

@Slf4j
@RestController
@RequestMapping("/merchant")
public class MerchantController {

    private final ProductMapper productMapper;
    private final OrderMapper orderMapper;

    public MerchantController(ProductMapper productMapper, OrderMapper orderMapper) {
        this.productMapper = productMapper;
        this.orderMapper = orderMapper;
    }

    @GetMapping("/products")
    public Result<PageResult<Product>> listProducts(ProQueryParams queryParams) {
        if (queryParams.getPage() == null || queryParams.getPage() < 1) {
            throw new IllegalArgumentException("page 必须大于等于 1");
        }
        if (queryParams.getPageSize() == null || queryParams.getPageSize() < 1) {
            throw new IllegalArgumentException("pageSize 必须大于等于 1");
        }
        Long merchantId = AuthContext.requireCurrentUserId();
        PageHelper.startPage(queryParams.getPage(), queryParams.getPageSize());
        List<Product> products = productMapper.selectProductPageByMerchant(
                merchantId,
                queryParams.getCategoryId(),
                queryParams.getStatus(),
                queryParams.getKeyword()
        );
        Page<Product> productPage = (Page<Product>) products;
        return Result.success(new PageResult<>(productPage.getTotal(), productPage.getResult()));
    }

    @PostMapping("/products")
    public Result<String> addProduct(@RequestBody Product product) {
        validateProductForCreate(product);
        Long merchantId = AuthContext.requireCurrentUserId();
        product.setMerchantId(merchantId);
        productMapper.insertProduct(product);
        return Result.success("商家商品新增成功");
    }

    @PutMapping("/products/{id:\\d+}")
    public Result<String> updateProduct(@PathVariable Long id, @RequestBody Product product) {
        validateProductForUpdate(product);
        Long merchantId = AuthContext.requireCurrentUserId();
        product.setId(id);
        int affectedRows = productMapper.updateProductByMerchant(product, merchantId);
        if (affectedRows == 0) {
            throw new SecurityException("无权修改该商品");
        }
        return Result.success("商家商品修改成功");
    }

    @DeleteMapping("/products/{id:\\d+}")
    public Result<String> deleteProduct(@PathVariable Long id) {
        Long merchantId = AuthContext.requireCurrentUserId();
        int affectedRows = productMapper.deleteProductByMerchant(id, merchantId);
        if (affectedRows == 0) {
            throw new SecurityException("无权删除该商品");
        }
        return Result.success("商家商品删除成功");
    }

    @GetMapping("/orders/category-stats")
    public Result<List<MerchantCategoryStats>> categoryStats() {
        Long merchantId = AuthContext.requireCurrentUserId();
        List<MerchantCategoryStats> stats = orderMapper.selectMerchantCategoryStats(merchantId);
        return Result.success(stats);
    }

    @PostMapping(value = {"/products/image", "/products/upload"}, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<String> uploadProductImage(@RequestParam("file") MultipartFile file) {
        Long merchantId = AuthContext.requireCurrentUserId();
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("请上传图片文件");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
            throw new IllegalArgumentException("仅支持上传图片文件");
        }
        try {
            Path uploadsDir = Paths.get(System.getProperty("user.dir"), "uploads", "products", String.valueOf(merchantId));
            Files.createDirectories(uploadsDir);
            String extension = extractExtension(file.getOriginalFilename());
            validateImageContent(file, extension);
            String baseName = normalizeBaseName(file.getOriginalFilename());
            String filename = baseName + "_" + UuidV7Utils.generate() + extension;
            Path targetPath = uploadsDir.resolve(filename).normalize();
            if (!targetPath.startsWith(uploadsDir.normalize())) {
                throw new SecurityException("非法的文件路径");
            }
            // 1. 业务层增加对文件大小的验证（例如最大 5MB）
            if (file.getSize() > 5 * 1024 * 1024) {
                throw new IllegalArgumentException("文件大小不能超过 5MB");
            }

            try (InputStream inputStream = file.getInputStream()) {
                // 2. 去除 REPLACE_EXISTING，若文件已存在则抛出异常，避免意外覆盖
                Files.copy(inputStream, targetPath);
            }

            return Result.success("/uploads/products/" + merchantId + "/" + filename);
        } catch (IOException e) {
            log.error("商家上传商品图片失败，merchantId: {}", merchantId, e);
            throw new RuntimeException("图片上传失败");
        }
    }

    /**
     * 从文件名中提取扩展名并进行白名单验证
     * 只允许图片格式：.jpg, .jpeg, .png, .gif, .webp
     */
    private String extractExtension(String originalFilename) {
        if (originalFilename == null) {
            throw new IllegalArgumentException("无效的文件名");
        }
        int index = originalFilename.lastIndexOf('.');
        if (index < 0 || index == originalFilename.length() - 1) {
            throw new IllegalArgumentException("无效的文件名");
        }
        String extension = originalFilename.substring(index).toLowerCase(Locale.ROOT);

        // 严格白名单校验
        List<String> allowedExtensions = List.of(".jpg", ".jpeg", ".png", ".gif", ".webp");
        if (!allowedExtensions.contains(extension)) {
            throw new IllegalArgumentException("不支持的文件类型");
        }
        return extension;
    }

    /**
     * 通过文件头Magic Bytes验证文件的真实内容是否匹配其扩展名
     * 防止攻击者修改文件扩展名来绕过上传限制
     */
    private void validateImageContent(MultipartFile file, String extension) throws IOException {
        byte[] header = new byte[4];
        try (InputStream is = file.getInputStream()) {
            is.read(header);
        }

        // 简单的图片文件头检查
        int headerValue = ((header[0] & 0xFF) << 24) |
                ((header[1] & 0xFF) << 16) |
                ((header[2] & 0xFF) << 8) |
                (header[3] & 0xFF);

        switch (extension) {
            case ".jpg":
            case ".jpeg":
                // JPEG starts with FFD8
                if ((headerValue & 0xFFFF00) != 0xFFD800) {
                    throw new SecurityException("不是有效的JPEG文件");
                }
                break;
            case ".png":
                // PNG starts with 89504E47
                if (headerValue != 0x89504E47) {
                    throw new SecurityException("不是有效的PNG文件");
                }
                break;
            case ".gif":
                // GIF starts with 47494638
                if (headerValue != 0x47494638) {
                    throw new SecurityException("不是有效的GIF文件");
                }
                break;
            case ".webp":
                // WebP starts with 52494646
                if (headerValue != 0x52494646) {
                    throw new SecurityException("不是有效的WebP文件");
                }
                break;
        }
    }


    private String normalizeBaseName(String originalFilename) {
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            return "image";
        }
        String trimmed = originalFilename.trim();
        int index = trimmed.lastIndexOf('.');
        String base = index > 0 ? trimmed.substring(0, index) : trimmed;
        String normalized = base.replaceAll("[^a-zA-Z0-9_-]", "_");
        if (normalized.isEmpty()) {
            return "image";
        }
        return normalized.length() > 60 ? normalized.substring(0, 60) : normalized;
    }

    private void validateProductForCreate(Product product) {
        if (product == null) {
            throw new IllegalArgumentException("商品参数不能为空");
        }
        if (product.getName() == null || product.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("商品名称不能为空");
        }
        if (product.getPrice() == null) {
            throw new IllegalArgumentException("商品价格不能为空");
        }
        if (product.getPrice().signum() < 0) {
            throw new IllegalArgumentException("商品价格不能小于0");
        }
        if (product.getStock() == null) {
            throw new IllegalArgumentException("商品库存不能为空");
        }
        if (product.getStock() < 0) {
            throw new IllegalArgumentException("商品库存不能小于0");
        }
    }

    private void validateProductForUpdate(Product product) {
        if (product == null) {
            throw new IllegalArgumentException("商品参数不能为空");
        }
        if (product.getPrice() != null && product.getPrice().signum() < 0) {
            throw new IllegalArgumentException("商品价格不能小于0");
        }
        if (product.getStock() != null && product.getStock() < 0) {
            throw new IllegalArgumentException("商品库存不能小于0");
        }
    }
}
