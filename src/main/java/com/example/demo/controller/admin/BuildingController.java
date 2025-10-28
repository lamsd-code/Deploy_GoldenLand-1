package com.example.demo.controller.admin;

import com.example.demo.constant.SystemConstant;
import com.example.demo.converter.BuildingDTOConverter;
import com.example.demo.enums.BuildingType;
import com.example.demo.enums.DistrictCode;
import com.example.demo.model.dto.BuildingDTO;
import com.example.demo.model.request.BuildingSearchRequest;
import com.example.demo.model.response.BuildingSearchResponse;
import com.example.demo.model.response.ResponseDTO;
import com.example.demo.security.utils.SecurityUtils;
import com.example.demo.service.BuildingService;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Transactional
@RestController(value="buildingControllerOfAdmin")
public class  BuildingController {
    @Autowired
    private UserService userService;
    @Autowired
    private BuildingService buildingService;
    @Autowired
    private BuildingDTOConverter buildingDTOConverter;

    @GetMapping(value = "/admin/building-list")
    public ModelAndView buildingList(@ModelAttribute BuildingSearchRequest buildingSearchRequest,
                                     @RequestParam Map<String, Object> conditions,
                                     @RequestParam(name = "typeCode", required = false) List<String> typeCode,
                                     @ModelAttribute(SystemConstant.MODEL) BuildingDTO model,
                                     HttpServletRequest request,
                                     @RequestParam(name = "page", required = false, defaultValue = "0") int page,   // [ADDED]
                                     @RequestParam(name = "size", required = false, defaultValue = "10") int size    // [ADDED]
    ) {

        // ✅ Lấy thông tin người dùng hiện tại
        Long currentUserId = SecurityUtils.getPrincipal().getId();
        List<String> roles = SecurityUtils.getAuthorities();

        // ✅ Nếu là STAFF → chỉ xem tòa nhà được giao
        if (roles.contains(SystemConstant.STAFF_ROLE)) {
            conditions.put("staffId", currentUserId);
        }

        // [ADDED] Tạo Pageable (controller dùng page 0-based cho thuận Thymeleaf)
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.max(size, 1));

        // [ADDED] Gọi service phân trang (song song giữ hàm cũ)
        Page<BuildingSearchResponse> buildingPage =
                buildingService.findAll(conditions, typeCode, pageable);

        // [CHANGED nhẹ] Danh sách để bảng hiển thị vẫn dùng "buildingList"
        List<BuildingSearchResponse> responseList = buildingPage.getContent();

        // ✅ Phân trang (giữ code cũ – không xóa)
        model.setMaxPageItems(5);
        model.setTotalItem(responseList.size()); // giữ nguyên logic cũ

        // ✅ Gửi dữ liệu sang View
        ModelAndView mav = new ModelAndView("admin/building/list");
        mav.addObject("modelSearch", buildingSearchRequest);
        mav.addObject("buildingList", responseList);
        mav.addObject("buildingPage", buildingPage);            // [ADDED]

        // ✅ Chỉ MANAGER và ADMIN mới thấy danh sách nhân viên
        if (roles.contains(SystemConstant.MANAGER_ROLE) || roles.contains(SystemConstant.ADMIN_ROLE)) {
            mav.addObject("staffs", userService.getStaffs());
        }

        mav.addObject("districts", DistrictCode.type());
        mav.addObject("typeCodes", BuildingType.type());
        return mav;
    }

    @GetMapping(value = "/admin/building-edit")
    public ModelAndView buildingEdit(@ModelAttribute BuildingDTO buildingDTO, HttpServletRequest request){
        ModelAndView mav = new ModelAndView("admin/building/edit");

        mav.addObject("buildingEdit", buildingDTO);
        mav.addObject("districts", DistrictCode.type());
        mav.addObject("typeCodes", BuildingType.type());
        return mav;
    }
    @GetMapping(value = "/admin/building-edit-{id}")
    public ModelAndView buildingEdit(@PathVariable("id") Long id, HttpServletRequest request){
        ModelAndView mav = new ModelAndView("admin/building/edit");
        BuildingDTO buildingDTO = buildingService.findBuildingById(id);
        mav.addObject("buildingEdit", buildingDTO);
        mav.addObject("districts", DistrictCode.type());
        mav.addObject("typeCodes", BuildingType.type());
        return mav;
    }
    




}
