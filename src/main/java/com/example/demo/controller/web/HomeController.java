package com.example.demo.controller.web;

import com.example.demo.model.request.BuildingSearchRequest;
import com.example.demo.model.response.BuildingSearchResponse;
import com.example.demo.service.BuildingService;
import com.example.demo.utils.DistrictCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller(value = "homeControllerOfWeb")
public class HomeController {
	
	@Autowired
    private BuildingService buildingService;

	// 🏠 Trang chủ
    @RequestMapping(value = "/trang-chu", method = RequestMethod.GET)
    public ModelAndView homePage(BuildingSearchRequest buildingSearchRequest, HttpServletRequest request) {
        ModelAndView mav = new ModelAndView("web/home");
        mav.addObject("modelSearch", buildingSearchRequest);
        mav.addObject("districts", DistrictCode.type());

        // ⚙️ Nếu form "Tìm kiếm nhanh" có dữ liệu
        Map<String, Object> params = new HashMap<>();

        if (buildingSearchRequest.getDistrict() != null && !buildingSearchRequest.getDistrict().isBlank()) {
            params.put("district", buildingSearchRequest.getDistrict());
        }
        if (buildingSearchRequest.getTypeCode() != null && !buildingSearchRequest.getTypeCode().isEmpty()) {
            params.put("typeCode", buildingSearchRequest.getTypeCode());
        }
        if (buildingSearchRequest.getName() != null && !buildingSearchRequest.getName().isBlank()) {
            params.put("name", buildingSearchRequest.getName());
        }

        // ✅ Nếu có điều kiện tìm kiếm, gọi service để lấy danh sách
        if (!params.isEmpty()) {
            List<BuildingSearchResponse> buildings = buildingService.findAll(params, buildingSearchRequest.getTypeCode());
            mav.addObject("projects", buildings);
        }

        return mav;
    }


    // 📘 Giới thiệu
    @GetMapping("/gioi-thieu")
    public ModelAndView introducePage() {
        return new ModelAndView("web/introduce");
    }

 // 🏗️ Sản phẩm (danh sách dự án / tòa nhà)
    @GetMapping("/san-pham")
    public ModelAndView buildingList(BuildingSearchRequest buildingSearchRequest) {
        ModelAndView mav = new ModelAndView("web/list");

        // ⚙️ Chuyển request -> Map params để truyền vào service
        Map<String, Object> params = new HashMap<>();
        if (buildingSearchRequest.getName() != null && !buildingSearchRequest.getName().isEmpty()) {
            params.put("name", buildingSearchRequest.getName());
        }
        if (buildingSearchRequest.getDistrict() != null && !buildingSearchRequest.getDistrict().isEmpty()) {
            params.put("district", buildingSearchRequest.getDistrict());
        }
        if (buildingSearchRequest.getWard() != null && !buildingSearchRequest.getWard().isEmpty()) {
            params.put("ward", buildingSearchRequest.getWard());
        }
        if (buildingSearchRequest.getStreet() != null && !buildingSearchRequest.getStreet().isEmpty()) {
            params.put("street", buildingSearchRequest.getStreet());
        }
        if (buildingSearchRequest.getDirection() != null && !buildingSearchRequest.getDirection().isEmpty()) {
            params.put("direction", buildingSearchRequest.getDirection());
        }
        if (buildingSearchRequest.getNumberOfBasement() != null) {
            params.put("numberOfBasement", buildingSearchRequest.getNumberOfBasement());
        }
        if (buildingSearchRequest.getFloorArea() != null) {
            params.put("floorArea", buildingSearchRequest.getFloorArea());
        }
        if (buildingSearchRequest.getRentPriceFrom() != null) {
            params.put("rentPriceFrom", buildingSearchRequest.getRentPriceFrom());
        }
        if (buildingSearchRequest.getRentPriceTo() != null) {
            params.put("rentPriceTo", buildingSearchRequest.getRentPriceTo());
        }

        // ⚙️ Gọi service để lấy danh sách tòa nhà (lọc theo tiêu chí)
        List<BuildingSearchResponse> buildings = buildingService.findAll(
                params,
                buildingSearchRequest.getTypeCode()
        );

        // ✅ Gửi dữ liệu ra view
        mav.addObject("projects", buildings);
        mav.addObject("search", buildingSearchRequest);
        mav.addObject("districts", DistrictCode.type());
        return mav;
    }
    
 // 🏢 Trang chi tiết tòa nhà
    @GetMapping("/chi-tiet-toa-nha/{id}")
    public ModelAndView buildingDetail(@PathVariable("id") Long id) {
        ModelAndView mav = new ModelAndView("web/detail");

        // 🔹 1. Tăng lượt xem
        buildingService.increaseViewCount(id);

        // 🔹 2. Lấy thông tin tòa nhà chi tiết
        var building = buildingService.findBuildingById(id);

        // 🔹 3. Lấy danh sách tòa nhà tương tự
        var related = buildingService.getRelatedBuildings(building.getDistrict(), id);

        // 🔹 4. Truyền sang view
        mav.addObject("building", building);
        mav.addObject("related", related);
        return mav;
    }



    // 📰 Tin tức
    @GetMapping("/tin-tuc")
    public ModelAndView newsPage() {
        return new ModelAndView("web/news");
    }

    // 📞 Liên hệ
    @GetMapping("/lien-he")
    public ModelAndView contactPage() {
        return new ModelAndView("web/contact");
    }

    // 🔐 Đăng nhập
    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public ModelAndView login() {
        return new ModelAndView("login");
    }

    // 🚫 Truy cập bị từ chối
    @RequestMapping(value = "/access-denied", method = RequestMethod.GET)
    public ModelAndView accessDenied() {
        return new ModelAndView("redirect:/login?accessDenied");
    }

    // 🚪 Đăng xuất
    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    public ModelAndView logout(HttpServletRequest request, HttpServletResponse response, HttpSession session) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }
        return new ModelAndView("redirect:/trang-chu");
    }

    //Add
    @GetMapping("/chat")
    public String chatPage() {
        return "web/chat";
    }
    //End Add
}
