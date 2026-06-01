package com.petcare.service;
import com.petcare.common.Result;
import com.petcare.dto.OrderDTO;
import org.springframework.web.multipart.MultipartFile;

public interface OrderService {
    Result<?> createOrder(OrderDTO dto, Long userId);
    Result<?> getMyOrders(Long userId);
    Result<?> getPendingOrders(Integer role);
    Result<?> pay(Long orderId, Long userId);
    Result<?> accept(Long orderId, Long staffId);
    Result<?> complete(Long orderId, Long staffId);
    Result<?> reject(Long orderId, String reason, Long staffId);
    Result<?> refund(Long orderId, String reason, Long operatorId);
    Result<?> cancel(Long orderId, String reason, Long userId);
    Result<?> review(Long orderId, Integer rating, String review, Long userId);
}
