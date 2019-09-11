package grupo2.client;

import com.opencsv.CSVWriter;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import grupo2.api.*;
import org.apache.commons.cli.*;

import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.lang.System.exit;

public class ConsultingClient {

    public static void main(String[] args){
        CommandLine parsedCommandLine = null;
        try{
            parsedCommandLine = getParsedCommandLine(args);
        }
        catch (ParseException e){
            System.err.println("Unexpected Command line arguments: '"+e.getMessage()+"'");
            exit(-1);
        }

        String ipAdd = parsedCommandLine.getOptionValue("A");
        String path = parsedCommandLine.getOptionValue("P");

        String provinceStr = parsedCommandLine.getOptionValue("N");
        String tableStr = parsedCommandLine.getOptionValue("I");


        executeConsultation(ipAdd, path, provinceStr, tableStr);
    }

    private static void executeConsultation(String ipAdd, String path, String provinceStr, String tableStr) {
        try{
            final ConsultService handle = (ConsultService) Naming.lookup(ipAdd);

            List<Result> results;
            if(provinceStr==null && tableStr == null){
                results = handle.consultTotal();
            }
            else if(provinceStr!=null){
                Province province = Province.valueOf(provinceStr);
                results = handle.consultProvince(province);
            }
            else{
                int tableId = Integer.parseInt(tableStr);
                results = handle.consultTable(tableId);
            }
            outputResults(results,path);

        } catch (RemoteException | NotBoundException | MalformedURLException e) {
            System.err.println("Unexpected ipAddress: '"+e.getMessage()+"'");
            System.exit(-1);
        }
    }

    private static void outputResults(List<Result> results, String path) {
        try(FileWriter fw =new FileWriter(path)){
            StatefulBeanToCsv<Result> beanWriter = new StatefulBeanToCsvBuilder<Result>(fw).build();
            beanWriter.write(results); //todo: ; separator. Change Header Names.

        } catch (IOException | CsvRequiredFieldEmptyException | CsvDataTypeMismatchException e) {
            e.printStackTrace(); //todo: handle
            exit(-1);
        }
    }


    private static CommandLine getParsedCommandLine(String[] args) throws ParseException {
        Options options = initializeOptions();
        CommandLineParser parser = new DefaultParser();
        return parser.parse(options, args);
    }

    private static Options initializeOptions() {
        final OptionGroup optionGroup = new OptionGroup();

        optionGroup.addOption(Option.builder("N")
                .longOpt("Dstate")
                .required()
                .desc( "El nombre de la provincia elegida para resolver la consulta 2"  )
                .hasArg()
                .argName( "NAME" )
                .build());
        optionGroup.addOption(Option.builder("I")
                .longOpt("Did")
                .required()
                .desc( "el número de la mesa elegida para resolver la consulta 3."  )
                .hasArg()
                .argName( "ID" )
                .build());

        final Options options = new Options();
        options.addOption(Option.builder("A")
                .longOpt("DserverAddress")
                .required()
                .desc( "Dirección IP y el puerto donde está publicado el servicio de votación."  )
                .hasArg()
                .argName( "IPADD" )
                .build());
        options.addOption(Option.builder("P")
                .required()
                .longOpt("DoutPath")
                .desc( "Path del archivo de salida con los resultados de la consulta elegida."  )
                .hasArg()
                .argName( "PATH" )
                .build());
        options.addOptionGroup(optionGroup);
        return options;

    }
}
