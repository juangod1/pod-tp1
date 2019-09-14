package grupo2.client;

import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import grupo2.api.ConsultService;
import grupo2.api.ElectionResults;
import grupo2.api.Party;
import grupo2.api.Province;

import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.lang.System.exit;

public class ConsultingClient {

    public static void main(String[] args){
        String ipAdd = System.getProperty("serverAddress");
        String path = System.getProperty("outPath");

        String provinceStr = System.getProperty("state");
        String tableStr = System.getProperty("id");


        executeConsultation(ipAdd, path, provinceStr, tableStr);
    }

    private static void executeConsultation(String ipAdd, String path, String provinceStr, String tableStr) {
        try{
            final ConsultService handle = (ConsultService) Naming.lookup("//" + ipAdd + "/consulting-service");

            ElectionResults results;
            if(provinceStr==null && tableStr == null){
                results = handle.consultTotal();
            }
            else if(provinceStr != null){
                Province province = Province.valueOf(provinceStr);
                results = handle.consultProvince(province);
            }
            else{
                int tableId = Integer.parseInt(tableStr);
                results = handle.consultTable(tableId);
            }
            outputResults(results,path);

        } catch (RemoteException | NotBoundException | MalformedURLException e) {
            System.err.println("Unexpected ipAddress: '"+e.getMessage()+"'");//todo: handle remote exceptions...
            System.exit(-1);
        }
    }

    private static void outputResults(ElectionResults results, String path) {
        List<Result> parsedResults = parseResults(results);
        try(FileWriter fw =new FileWriter(path)){
            StatefulBeanToCsv<Result> beanWriter = new StatefulBeanToCsvBuilder<Result>(fw).build();
            beanWriter.write(parsedResults); //todo: ; separator. Change Header Names.

        } catch (IOException | CsvRequiredFieldEmptyException | CsvDataTypeMismatchException e) {
            e.printStackTrace(); //todo: handle writing exceptions...
            exit(-1);
        }
    }

    private static List<Result> parseResults(ElectionResults results) {
        List<Result> resultList = new ArrayList<>();
        for(Map.Entry<Party,Double> entry : results.getResults().entrySet()){
            resultList.add(new Result(entry.getKey(),entry.getValue().floatValue()));
        }
        return resultList;
    }
}
