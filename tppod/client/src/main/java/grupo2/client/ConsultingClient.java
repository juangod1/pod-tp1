package grupo2.client;

import grupo2.api.iface.ConsultService;
import grupo2.api.model.ElectionResults;
import grupo2.api.model.ElectionStateException;
import grupo2.api.model.Party;
import grupo2.api.model.Province;

import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.lang.System.exit;

public class ConsultingClient {

    public static void main(String[] args){
        String ipAdd = System.getProperty("serverAddress");
        String path = System.getProperty("outPath");

        String provinceStr = System.getProperty("state");
        String tableStr = System.getProperty("id");

        if(provinceStr!=null && tableStr!=null){
            System.out.println("Impossible request. Solicit either table or province");
            exit(-1);
        }

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

        }catch (ElectionStateException e){
            System.err.println(e.getMessage());
            System.exit(-1);
        }
        catch (RemoteException | NotBoundException | MalformedURLException e) {
            System.err.println("Unexpected ipAddress: '"+e.getMessage()+"'");//todo: handle remote exceptions...
            System.exit(-1);
        }
    }

    private static void outputResults(ElectionResults results, String path) {
        List<Result> parsedResults = parseResults(results);
        try(FileWriter fw =new FileWriter(path)){
            StringBuilder sb = new StringBuilder();
            sb.append("Porcentaje;Partido\n");
            parsedResults
                    .stream()
                    .sorted((r1, r2) -> {
                        int pctgCmp = Float.compare(r2.getPercentage(), r1.getPercentage());
                        if(pctgCmp == 0) {
                            return r1.getParty().toString().compareTo(r2.getParty().toString());
                        } else {
                            return pctgCmp;
                        }
                    })
                    .forEach((r)->sb.append(String.format("%.2f", 100*r.getPercentage()))
                    .append("%;").append(r.getParty()).append('\n'));
            fw.write(sb.toString());

        } catch (IOException e) {
            e.printStackTrace(); //todo: handle writing exceptions...
            exit(-1);
        }
    }

    private static List<Result> parseResults(ElectionResults results) {
        List<Result> resultList = new ArrayList<>();
        for(Map.Entry<Party,Double> entry : results.getResults().entrySet()){
            resultList.add(new Result(entry.getKey(),entry.getValue().floatValue()));
        }
        return resultList.stream().sorted(Comparator.comparing(x -> x.getParty().name())).collect(Collectors.toList());
    }
}
