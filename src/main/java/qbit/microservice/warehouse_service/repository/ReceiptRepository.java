package qbit.microservice.warehouse_service.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import qbit.microservice.warehouse_service.entity.Receipt;

@Repository
public interface ReceiptRepository extends MongoRepository<Receipt, String> {

}
