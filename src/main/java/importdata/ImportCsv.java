package importdata;

import java.io.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ImportCsv {


    public static void main(String[] args) {

        String delimiter = uiAnswer("Введите разделитель данных в файле(ах)(пример  ,    :");
        lokForDir(delimiter.charAt(0));

    }

    private static String uiAnswer(String question){
         String answer="";
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

            while (answer.equals("")){
                System.out.print(question);
                answer = reader.readLine();
            }

            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return answer;
    }


    private static void lokForDir(Character delimiter){
        File directory;
        // Считываем исходный каталог для поиска файлов.
        final String directoryPath = uiAnswer("Введите исходную директорию для поиска файлов:");

        directory = new File(directoryPath);
        // Убедимся, что директория найдена и это реально директория, а не файл.
        if (directory.exists() && directory.isDirectory()) {
            processDirectory(directory,delimiter);
        } else {
            System.out.println("Не удалось найти директорию по указанному пути.");
        }

    }


    private static void sortBySystem(String path, String fileName, Character delimiter, Map<String, CurrentProcessingData> mapSortFiles){
        {
//           создать папку для хранения временных файлов

            try
            {
                //здесь "sleep 15" и есть ваша консольная команда

                // -t, - если делитель - запятая

//          sort.exe yourfile.csv -t, -k5 -g -o sorted.csv
                String outFileName = fileName.substring(0,fileName.indexOf('.') ) + "sort.csv";

                String command = path+"\\sort.exe "+ fileName + " -t" + delimiter + " -k5 -g -o  " + outFileName; // sort -k5 -g file.csv > sorted.csv для Linux
                Process proc = Runtime.getRuntime().exec(command);
                proc.waitFor();
                proc.destroy();
                mapSortFiles.put(outFileName, new CurrentProcessingData(0,false,  0.0f));
            }
            catch (IOException | InterruptedException e)
            {
                e.printStackTrace();
            }
        }
    }

    private static void processDirectory(final File directory, final Character delimiter) {
        // Получаем список доступных файлов в указанной директории.
        FilenameFilter filter = (file, name)-> name.endsWith(".csv");

        File[] files = directory.listFiles(filter);

        if (files == null) {
            System.out.println("Нет доступных файлов для обработки.");
            return;
        } else {
            System.out.println("Количество файлов для обработки: " + files.length);
        }

        int waitTime = Integer.parseInt("Введите ожидаемое время работы программы в минутах:");

        ConcurrentHashMap<String,CurrentProcessingData> mapSortFiles = new ConcurrentHashMap<>();

        // Непосредственно многопоточная обработка файлов.
        final int treadCount = Runtime.getRuntime().availableProcessors() + 1; // количество потоков, которые могу запустить

        ExecutorService service = Executors.newFixedThreadPool(treadCount);

        for (final File f : files) {
            if (!f.isFile()) {
                continue;
            }
            service.execute(() -> sortBySystem(directory.toString(),f.toString(),delimiter, mapSortFiles));
        }
        // Новые задачи более не принимаем, выполняем только оставшиеся.
        service.shutdown();
        // Ждем завершения выполнения потоков не более 10 минут.
        try {
            service.awaitTermination(waitTime, TimeUnit.MINUTES); //Максимальное время обработки
        } catch (InterruptedException e) {
            e.printStackTrace();
        }





    }

//    private static void readCSV(String fileName, Character delimiter) {
//
//
//        try (Connection connection = GetConnection())
//        {
//            connection.setAutoCommit(false);
//            String loadQuery = "LOAD DATA LOCAL INFILE '" + fileName + "' INTO TABLE txn_tbl FIELDS TERMINATED BY " +delimiter + " LINES TERMINATED BY '\n' (txn_amount, card_number, terminal_id) ";
//            System.out.println(loadQuery);
//            Statement stmt = connection.createStatement();
//            stmt.execute(loadQuery);
//        }
//        catch (Exception e)
//        {
//            e.printStackTrace();
//        }
//    }
//
//    private static Connection GetConnection() {
//        Connection connection = null;
//        try {
//            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/", "root", "root");
//        } catch (SQLException throwables) {
//            throwables.printStackTrace();
//        }
//
//        return connection;
//    }

}
