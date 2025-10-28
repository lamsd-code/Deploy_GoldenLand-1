package com.example.demo.api;

import com.example.demo.entity.Building;
import com.example.demo.entity.Customer;
import com.example.demo.entity.Review;
import com.example.demo.model.response.ResponseDTO;
import com.example.demo.repository.BuildingRepository;
import com.example.demo.repository.CustomerRepository;
import com.example.demo.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/review")
public class ReviewAPI {

    @Autowired private ReviewRepository reviewRepository;
    @Autowired private BuildingRepository buildingRepository;
    @Autowired private CustomerRepository customerRepository;

    // 📦 Lấy tất cả review của 1 tòa nhà
    @GetMapping("/{buildingId}")
    public List<ReviewResponse> getAll(@PathVariable Long buildingId) {
        return reviewRepository.findAllByBuildingIdOrderByCreatedDateDesc(buildingId)
                .stream()
                .map(ReviewResponse::new)
                .toList();
    }

    // ✍️ Thêm review mới
    @PostMapping
    public ResponseDTO addReview(@RequestBody ReviewRequest req, Principal principal) {
        ResponseDTO res = new ResponseDTO();
        try {
            if (principal == null) throw new RuntimeException("Bạn cần đăng nhập để bình luận.");

            String username = principal.getName();
            Customer customer = customerRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Customer not found"));
            Building building = buildingRepository.findById(req.getBuildingId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy tòa nhà."));

            Review review = new Review();
            review.setCustomer(customer);
            review.setBuilding(building);
            review.setContent(req.getContent());
            review.setRating(req.getRating());
            review.setCreatedDate(new Date());

            reviewRepository.save(review);
            res.setMessage("success");
        } catch (Exception e) {
            res.setMessage("❌ " + e.getMessage());
        }
        return res;
    }

    // 📦 Request DTO
    public static class ReviewRequest {
        private Long buildingId;
        private String content;
        private Integer rating;
        public Long getBuildingId() { return buildingId; }
        public void setBuildingId(Long buildingId) { this.buildingId = buildingId; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public Integer getRating() { return rating; }
        public void setRating(Integer rating) { this.rating = rating; }
    }

    // 📦 Response DTO (trả username thay vì fullname)
    public static class ReviewResponse {
        public Long id;
        public String content;
        public Integer rating;
        public Date createdDate;
        public String username;

        public ReviewResponse(Review r) {
            this.id = r.getId();
            this.content = r.getContent();
            this.rating = r.getRating();
            this.createdDate = r.getCreatedDate();
            this.username = (r.getCustomer() != null && r.getCustomer().getUsername() != null)
                    ? r.getCustomer().getUsername()
                    : "Ẩn danh";
        }
    }
 // 🗑 Xóa bình luận của chính mình
    @DeleteMapping("/{id}")
    public ResponseDTO deleteMyReview(@PathVariable Long id, Principal principal) {
        ResponseDTO res = new ResponseDTO();
        try {
            if (principal == null)
                throw new RuntimeException("Bạn cần đăng nhập để xoá bình luận.");

            String username = principal.getName();
            Customer customer = customerRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Customer not found"));;

            Review review = reviewRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy bình luận."));

            // ✅ Chỉ cho phép xoá nếu bình luận này thuộc về user hiện tại
            if (!review.getCustomer().getId().equals(customer.getId()))
                throw new RuntimeException("Bạn không thể xoá bình luận của người khác.");

            reviewRepository.delete(review);
            res.setMessage("success");
        } catch (Exception e) {
            res.setMessage("❌ " + e.getMessage());
        }
        return res;
    }

}
