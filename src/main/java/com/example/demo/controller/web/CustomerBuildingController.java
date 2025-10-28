package com.example.demo.controller.web;

import com.example.demo.converter.BuildingDTOConverter;
import com.example.demo.entity.Building;
import com.example.demo.entity.Customer;
import com.example.demo.enums.DistrictCode;
import com.example.demo.enums.BuildingType;
import com.example.demo.model.dto.BuildingDTO;
import com.example.demo.model.dto.MyUserDetail;
import com.example.demo.repository.BuildingRepository;
import com.example.demo.repository.CustomerRepository;
import com.example.demo.service.BuildingService;
import com.example.demo.service.impl.CloudinaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
@RequestMapping("/web")
public class                            CustomerBuildingController {

    @Autowired
    private BuildingRepository buildingRepository;

    @Autowired
    private BuildingService buildingService;

    @Autowired
    private CloudinaryService cloudinaryService;

    @Autowired
    private BuildingDTOConverter buildingDTOConverter;

    @Autowired
    private CustomerRepository customerRepository;

    // 🔹 Helper: lấy Customer hiện tại từ session
    private Customer getCurrentCustomer() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken)
            return null;

        Object principal = auth.getPrincipal();
        if (principal instanceof MyUserDetail userDetail) {
            // chỉ khi đăng nhập bằng Customer
            if (userDetail.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_CUSTOMER"))) {
                return customerRepository.findById(userDetail.getId()).orElse(null);
            }
        }
        return null;
    }

    /** ✅ Danh sách bài đăng của người dùng */
    @GetMapping("/my-posts")
    public String getMyPosts(Model model) {
        Customer currentCustomer = getCurrentCustomer();
        if (currentCustomer == null) {
            return "redirect:/login?redirect=/web/my-posts";
        }

        List<Building> posts = buildingRepository.findByCustomerId(currentCustomer.getId());
        model.addAttribute("posts", posts);
        return "web/my-posts";
    }

    /** ✅ Trang tạo bài đăng mới */
    @GetMapping("/building-create")
    public String createForm(Model model) {
        Customer currentCustomer = getCurrentCustomer();
        if (currentCustomer == null) {
            return "redirect:/login?redirect=/web/building-create";
        }

        model.addAttribute("buildingEdit", new BuildingDTO());
        model.addAttribute("districts", DistrictCode.type());
        model.addAttribute("typeCodes", BuildingType.type());
        return "web/building-form";
    }

    /** ✅ Xử lý đăng bài mới */
    @PostMapping("/building-create")
    public String createBuilding(
            @Validated @ModelAttribute("buildingEdit") BuildingDTO buildingDTO,
            @RequestParam(value = "avatarFile", required = false) MultipartFile avatarFile,
            Model model) {

        Customer currentCustomer = getCurrentCustomer();
        if (currentCustomer == null) {
            return "redirect:/login?redirect=/web/building-create";
        }

        try {
            if (avatarFile != null && !avatarFile.isEmpty()) {
                String imageUrl = cloudinaryService.uploadFile(avatarFile, "buildings");
                buildingDTO.setAvatar(imageUrl);
            }

            buildingDTO.setStatus("PENDING");
            buildingDTO.setCreatedBy("CUSTOMER_" + currentCustomer.getId());
            buildingDTO.setCustomerId(currentCustomer.getId()); // 🔹 gán customer

            buildingService.save(buildingDTO);

            model.addAttribute("successMessage", "✅ Đăng bài thành công! Vui lòng chờ duyệt.");
            return "redirect:/web/my-posts";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "❌ Lỗi khi đăng bài: " + e.getMessage());
            model.addAttribute("districts", DistrictCode.type());
            model.addAttribute("typeCodes", BuildingType.type());
            return "web/building-form";
        }
    }

    /** ✅ Trang chỉnh sửa bài đăng */
    @GetMapping("/building-edit/{id}")
    public String editBuilding(@PathVariable Long id, Model model) {
        Customer currentCustomer = getCurrentCustomer();
        if (currentCustomer == null) {
            return "redirect:/login?redirect=/web/building-edit/" + id;
        }

        Building building = buildingRepository.findById(id).orElse(null);
        if (building == null || !building.getCustomer().getId().equals(currentCustomer.getId())) {
            return "redirect:/web/my-posts";
        }

        BuildingDTO dto = buildingDTOConverter.toBuildingDTO(building);
        model.addAttribute("buildingEdit", dto);
        model.addAttribute("districts", DistrictCode.type());
        model.addAttribute("typeCodes", BuildingType.type());
        return "web/building-form";
    }

    /** ✅ Xử lý cập nhật bài đăng */
    @PostMapping("/building-edit/{id}")
    public String updateBuilding(
            @PathVariable Long id,
            @Validated @ModelAttribute("buildingEdit") BuildingDTO buildingDTO,
            @RequestParam(value = "avatarFile", required = false) MultipartFile avatarFile,
            Model model) {

        Customer currentCustomer = getCurrentCustomer();
        if (currentCustomer == null) {
            return "redirect:/login?redirect=/web/building-edit/" + id;
        }

        try {
            Building building = buildingRepository.findById(id).orElse(null);
            if (building == null || !building.getCustomer().getId().equals(currentCustomer.getId())) {
                return "redirect:/web/my-posts";
            }

            if (avatarFile != null && !avatarFile.isEmpty()) {
                String imageUrl = cloudinaryService.uploadFile(avatarFile, "buildings");
                buildingDTO.setAvatar(imageUrl);
            } else {
                buildingDTO.setAvatar(building.getAvatar());
            }

            buildingDTO.setId(id);
            buildingDTO.setStatus("PENDING");
            buildingDTO.setCreatedBy("CUSTOMER_" + currentCustomer.getId());
            buildingDTO.setCustomerId(currentCustomer.getId());

            buildingService.save(buildingDTO);

            model.addAttribute("successMessage", "✅ Cập nhật bài đăng thành công!");
            return "redirect:/web/my-posts";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "❌ Lỗi khi cập nhật: " + e.getMessage());
            model.addAttribute("districts", DistrictCode.type());
            model.addAttribute("typeCodes", BuildingType.type());
            return "web/building-form";
        }
    }

    /** ✅ Xóa bài đăng */
    @GetMapping("/building-delete/{id}")
    public String deleteBuilding(@PathVariable Long id) {
        Customer currentCustomer = getCurrentCustomer();
        if (currentCustomer == null) {
            return "redirect:/login?redirect=/web/my-posts";
        }

        Building building = buildingRepository.findById(id).orElse(null);
        if (building == null || !building.getCustomer().getId().equals(currentCustomer.getId())) {
            return "redirect:/web/my-posts";
        }

        buildingRepository.delete(building);
        return "redirect:/web/my-posts";
    }
}
