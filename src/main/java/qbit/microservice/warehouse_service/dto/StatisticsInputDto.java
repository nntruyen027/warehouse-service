package qbit.microservice.warehouse_service.dto;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class StatisticsInputDto {
    List<Long> ids;
    LocalDate startDate;
    LocalDate endDate;
}
