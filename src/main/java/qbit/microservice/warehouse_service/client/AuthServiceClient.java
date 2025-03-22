package qbit.microservice.warehouse_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import qbit.microservice.warehouse_service.config.FeignClientConfig;
import qbit.microservice.warehouse_service.dto.AccountDto;

@FeignClient(name = "auth-service", url = "${auth.service.url}", configuration = FeignClientConfig.class)
public interface AuthServiceClient {
    @GetMapping(value = "/self")
    ResponseEntity<AccountDto> getUserByJwt(@RequestHeader("Authorization") String authorizationHeader);
}