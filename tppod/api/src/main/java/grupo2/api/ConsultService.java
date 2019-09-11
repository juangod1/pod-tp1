package grupo2.api;

import java.util.List;

public interface ConsultService {

    List<Result> consultTotal();
    List<Result> consultProvince(Province province);
    List<Result> consultTable(int tableId);
}
