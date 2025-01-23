package com.c0324.casestudym5.service;

import com.c0324.casestudym5.model.Blogs;
import com.c0324.casestudym5.repository.BlogsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import javax.annotation.PostConstruct;

@Service
public class BlogsService {

    @Autowired
    private BlogsRepository blogsRepository;

    @Value("${upload.path}")
    private String uploadPath;

    private final String IMAGE_URL_PREFIX = "/uploads/blogs/";

    @PostConstruct
    public void init() {
        // Tạo thư mục upload nếu chưa tồn tại
        try {
            Path uploadDir = Paths.get(uploadPath + "/blogs");
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
                System.out.println("Đã tạo thư mục upload: " + uploadDir);
            }
        } catch (IOException e) {
            throw new RuntimeException("Không thể tạo thư mục upload", e);
        }
    }

    public Page<Blogs> getAllBlogs(int pageNumber) {
        Pageable pageable = PageRequest.of(pageNumber, 9, Sort.by("createdAt").descending());
        return blogsRepository.findAll(pageable);
    }

    public Blogs getBlogById(Long id) {
        return blogsRepository.findById(id).orElse(null);
    }

    public Blogs createBlog(Blogs blog, MultipartFile imageFile) throws IOException {
        if (!imageFile.isEmpty()) {
            String fileName = saveImage(imageFile);
            blog.setImg(IMAGE_URL_PREFIX + fileName);
        }
        return blogsRepository.save(blog);
    }

    public Blogs updateBlog(Long id, Blogs blogDetails, MultipartFile imageFile) throws IOException {
        Blogs blog = blogsRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Blog không tồn tại"));

        if (!imageFile.isEmpty()) {
            // Xóa ảnh cũ nếu có
            if (blog.getImg() != null) {
                deleteImage(blog.getImg());
            }
            String fileName = saveImage(imageFile);
            blog.setImg(IMAGE_URL_PREFIX + fileName);
        }

        blog.setName(blogDetails.getName());
        blog.setDescription(blogDetails.getDescription());

        return blogsRepository.save(blog);
    }

    public void deleteBlog(Long id) {
        Blogs blog = blogsRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Blog không tồn tại"));

        if (blog.getImg() != null) {
            deleteImage(blog.getImg());
        }

        blogsRepository.deleteById(id);
    }

    private String saveImage(MultipartFile file) throws IOException {
        Path uploadDir = Paths.get(uploadPath + "/blogs");
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        Path filePath = uploadDir.resolve(fileName);
        Files.copy(file.getInputStream(), filePath);

        return fileName;
    }

    private void deleteImage(String imageUrl) {
        try {
            String fileName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
            Path filePath = Paths.get(uploadPath + "/blogs/" + fileName);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<Blogs> getLatestBlogs(int limit) {
        return blogsRepository.findTop6ByOrderByCreatedAtDesc();
    }
}
