package com.c0324.casestudym5.controller;

import com.c0324.casestudym5.model.Blogs;
import com.c0324.casestudym5.service.BlogsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.data.domain.Page;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/blogs")
public class BlogsController {

    @Autowired
    private BlogsService blogsService;


    @GetMapping
    public String listBlogs(@RequestParam(defaultValue = "0") int page, Model model) {
        try {
            Page<Blogs> blogs = blogsService.getAllBlogs(page); // Lấy danh sách blog với phân trang
            model.addAttribute("blogs", blogs);
            System.out.println("Số lượng blogs: " + blogs.getContent().size()); // Log để debug
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Có lỗi xảy ra khi tải danh sách thông báo");
            model.addAttribute("blogs", new ArrayList<>());
        }
        return "blogs/list";
    }
    @GetMapping("/{id}")
    public String viewBlog(@PathVariable Long id, Model model) {
        Blogs blog = blogsService.getBlogById(id);
        if (blog == null) {
            return "redirect:/blogs";
        }
        model.addAttribute("blog", blog);
        return "blogs/detail";
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @GetMapping("/create")
    public String createBlogForm(Model model) {
        model.addAttribute("blog", new Blogs());
        return "blogs/form";
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @PostMapping("/create")
    public String createBlog(@ModelAttribute Blogs blog,
                             @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                             RedirectAttributes redirectAttributes) {
        try {
            // Kiểm tra nếu blog không có tiêu đề hoặc nội dung
            if (blog.getName() == null || blog.getName().isEmpty() ||
                    blog.getDescription() == null || blog.getDescription().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Tiêu đề và nội dung không được để trống!");
                return "redirect:/blogs/create";
            }
            blogsService.createBlog(blog, imageFile);
            redirectAttributes.addFlashAttribute("toastMessage", "Thông báo đã được tạo thành công!");
            redirectAttributes.addFlashAttribute("toastType", "success");
            return "redirect:/blogs";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("toastMessage", "Có lỗi xảy ra: " + e.getMessage());
            redirectAttributes.addFlashAttribute("toastType", "danger");
            return "redirect:/blogs/create";
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @GetMapping("/edit/{id}")
    public String editBlogForm(@PathVariable Long id, Model model) {
        Blogs blog = blogsService.getBlogById(id);
        if (blog == null) {
            return "redirect:/blogs";
        }
        model.addAttribute("blog", blog);
        return "blogs/form";
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @PostMapping("/edit/{id}")
    public String editBlog(@PathVariable Long id,
                           @ModelAttribute Blogs blog,
                           @RequestParam("imageFile") MultipartFile imageFile,
                           RedirectAttributes redirectAttributes) {
        try {
            blogsService.updateBlog(id, blog, imageFile);
            redirectAttributes.addFlashAttribute("toastMessage", "Thông báo đã được cập nhật thành công!");
            redirectAttributes.addFlashAttribute("toastType", "success");
            return "redirect:/blogs";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("toastMessage", "Có lỗi xảy ra: " + e.getMessage());
            redirectAttributes.addFlashAttribute("toastType", "danger");
            return "redirect:/blogs/edit/" + id;
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @PostMapping("delete/{id}")
    public String deleteBlog(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            blogsService.deleteBlog(id);
            redirectAttributes.addFlashAttribute("toastMessage", "Xóa thông báo thành công!");
            redirectAttributes.addFlashAttribute("toastType", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("toastMessage", "Đã xảy ra lỗi khi xóa thông báo.");
            redirectAttributes.addFlashAttribute("toastType", "danger");
        }
        return "redirect:/blogs";
    }
}
