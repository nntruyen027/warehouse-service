package qbit.microservice.warehouse_service.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import qbit.microservice.warehouse_service.entity.PhieuXuat;

public interface PhieuXuatRepository extends MongoRepository<PhieuXuat, String> {
}
