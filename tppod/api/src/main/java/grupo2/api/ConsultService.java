package grupo2.api;

public interface ConsultService {
    ElectionResults consultTotal();
    ElectionResults consultProvince(Province province);
    ElectionResults consultTable(int tableId);
}
