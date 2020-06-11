package importdata;

import java.io.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ImportCsv {


    public static void main(String[] args) {
//        Scanner scanner = new Scanner(System.in);
//        String file = scanner.nextLine();
        String path = "F:\\dataTestCSV\\";
//        String fullPath = path+file;
        lokForDir(',');
//        sortBySystem(path,fullPath,',');
    }

    private static void lokForDir(Character delimiter){
        File directory;
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            // Считываем исходный каталог для поиска файлов.
            System.out.print("Введите исходную директорию для поиска файлов:");
            final String directoryPath = reader.readLine();
            reader.close();

            directory = new File(directoryPath);
            // Убедимся, что директория найдена и это реально директория, а не файл.
            if (directory.exists() && directory.isDirectory()) {
                processDirectory(directory,delimiter);
            } else {
                System.out.println("Не удалось найти директорию по указанному пути.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    private static void sortBySystem(String path, String fileName, Character delimiter){
        {
            try
            {
                //здесь "sleep 15" и есть ваша консольная команда
                // -t, - если делитель - запятая

//          sort.exe yourfile.csv -t, -k5 -g -o sorted.csv
                String outFileName = fileName.substring(0,fileName.indexOf('.') ) + "sort.csv";

                String command = path+"\\sort.exe "+ fileName + " -t" + delimiter + " -k5 -g -o " + outFileName;
                Process proc = Runtime.getRuntime().exec(command);
                proc.waitFor();
                proc.destroy();
            }
            catch (IOException | InterruptedException e)
            {
                e.printStackTrace();
            }
        }
    }

    private static void processDirectory(final File directory, final Character delimiter) {
        // Получаем список доступных файлов в указанной директории.
        FilenameFilter filter = new FilenameFilter() {
            @Override
            public boolean accept(File f, String name) {
                return name.endsWith(".csv");
            }
        };
        File[] files = directory.listFiles(filter);
        if (files == null) {
            System.out.println("Нет доступных файлов для обработки.");
            return;
        } else {
            System.out.println("Количество файлов для обработки: " + files.length);
        }

        // Непосредственно многопоточная обработка файлов.
        final int treadCount = Runtime.getRuntime().availableProcessors() + 1; // количество потоков, которые могу запустить
        ExecutorService service = Executors.newFixedThreadPool(treadCount);
        for (final File f : files) {
//            String ext = getFileExtension(f);
            if (!f.isFile()) {
                continue;
            }

            service.execute(new Runnable() {
                @Override
                public void run() {
//                    try (BufferedReader reader = new BufferedReader(new FileReader(f))) {
//                        int lines = 0;
//                        while (reader.readLine() != null) {
//                            ++lines;
//                        }
//                        System.out.println("Поток: " + Thread.currentThread().getName() + ". Файл: " + f.getName() + ". Количество строк: " + lines);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
                 sortBySystem(directory.toString(),f.toString(),delimiter);
                }
            });
        }
        // Новые задачи более не принимаем, выполняем только оставшиеся.
        service.shutdown();
        // Ждем завершения выполнения потоков не более 10 минут.
        try {
            service.awaitTermination(10, TimeUnit.MINUTES); //Максимальное время обработки
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
