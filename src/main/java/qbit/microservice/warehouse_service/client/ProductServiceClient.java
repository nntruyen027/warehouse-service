package qbit.microservice.warehouse_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import qbit.microservice.warehouse_service.config.FeignClientConfig;
import qbit.microservice.warehouse_service.dto.ProductVersionDto;

@FeignClient(name = "product-service", url = "${product.service.url}", configuration = FeignClientConfig.class)
public interface ProductServiceClient {
    @GetMapping(value = "/products/{productId}/versions/{id}")
    ResponseEntity<ProductVersionDto> findById(@PathVariable Long productId, @PathVariable Long id,
                                               @RequestHeader("Authorization") String authorizationHeader);

    @PutMapping(value = "/products/{productId}/versions/{id}/add/{quantity}")
    ResponseEntity<ProductVersionDto> addItem(@PathVariable Long productId, @PathVariable Long id, @PathVariable int quantity,
                                              @RequestHeader("Authorization") String authorizationHeader);

    @PutMapping(value = "/products/{productId}/versions/{id}/remove/{quantity}")
    ResponseEntity<ProductVersionDto> removeItem(@PathVariable Long productId, @PathVariable Long id, @PathVariable int quantity,
                                              @RequestHeader("Authorization") String authorizationHeader);
}
