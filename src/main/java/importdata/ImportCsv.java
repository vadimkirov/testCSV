package importdata;

import com.opencsv.bean.CsvToBeanBuilder;
import model.CountProduct;
import model.CurrentProcessingData;
import model.Product;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static utils.Least.checkAndChangeBean;
import static utils.Least.leastN;

public class ImportCsv {


    public static void main(String[] args) {

        String delimiter = uiAnswer("Введите символ разделителя данных в файле(ах), актуальным будет только первый символ, введеной строки, остальные - игнорируются(пример разделителя  ;    :");
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
        /*
        File rootDir = new File(root);
List<String> result = new ArrayList<>();
Queue<File> fileTree = new PriorityQueue<>();

Collections.addAll(fileTree, rootDir.listFiles());

while (!fileTree.isEmpty())
{
    File currentFile = fileTree.remove();
    if(currentFile.isDirectory()){
        Collections.addAll(fileTree, currentFile.listFiles());
    } else {
        result.add(currentFile.getAbsolutePath());
    }
}

формирование списка файлов с обходом в глубину


         */




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


    private static void select(final File directory, final Character delimiter) {
        FilenameFilter filter = (file, name)-> name.endsWith(".csv");

        File[] files = directory.listFiles(filter);

//        добавить обход папок в глубину

        if (files == null) {
            System.out.println("Нет доступных файлов для обработки.");
            return;
        } else {
            System.out.println("Количество файлов для обработки: " + files.length);
        }
        int waitTime;
        int temp = Integer.parseInt("Введите ожидаемое время работы программы в минутах(по умолчанию 1 000):");
        if (temp<1){
            waitTime =1000;
        }else {
            waitTime = temp;
        }

//        ConcurrentHashMap <>

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
                mapSortFiles.put(outFileName, new CurrentProcessingData(0,false,  -1.0f));
            }
            catch (IOException | InterruptedException e)
            {
                e.printStackTrace();
            }
        }
    }

    private static List<Product> makerToMap(File file, Character delimiter, int maxSizeMap ) throws FileNotFoundException {

        return  leastN(
                new CsvToBeanBuilder(new FileReader(file)).withSeparator(delimiter)
                        .withType(Product.class).build().parse(),

                maxSizeMap);

    }






    private static void processDirectory(final File directory, final Character delimiter) {
        // Получаем список доступных файлов в указанной директории.
        FilenameFilter filter = (file, name)-> name.endsWith(".csv");

        File[] files = directory.listFiles(filter);

//        добавить обход папок в глубину

        if (files == null) {
            System.out.println("Нет доступных файлов для обработки.");
            return;
        } else {
            System.out.println("Количество файлов для обработки: " + files.length);
        }
        int waitTime;
        int temp = Integer.parseInt("Введите ожидаемое время работы программы в минутах(по умолчанию 1 000):");
        if (temp<1){
            waitTime =1000;
        }else {
            waitTime = temp;
        }




        // Непосредственно многопоточная обработка файлов.
        final int treadCount = Runtime.getRuntime().availableProcessors() + 1; // количество потоков, которые могу запустить
        ConcurrentHashMap<Integer, PriorityBlockingQueue<Product>> mapResult = new ConcurrentHashMap<> (1000,0.5f,treadCount  );
        AtomicInteger countSize = new AtomicInteger (  );

//        ConcurrentHashMap<Integer, CountProduct> mapResult = new ConcurrentHashMap<Integer, CountProduct> (1000,0.5f,treadCount);

        ExecutorService service = Executors.newFixedThreadPool(treadCount);

        for (final File f : files) {
            service.execute(() -> {
                try {
                    makerFinalList (f,delimiter, mapResult,1000,20, countSize);
                } catch (FileNotFoundException e) {
                    e.printStackTrace ( );
                }
            });
        }
        // Новые задачи более не принимаем, выполняем только оставшиеся.
        service.shutdown();
        // Ждем завершения выполнения потоков не более waitTime минут.
        try {
            service.awaitTermination(waitTime, TimeUnit.MINUTES); //Максимальное время обработки
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


//        List<Product>  result = new C




    }

    private static void makerFinalList(File f, Character delimiter, ConcurrentHashMap<Integer, PriorityBlockingQueue<Product>> mapResult, int maxSizeOutFile, int maxRepid, AtomicInteger count) throws FileNotFoundException {
        List<Product> inputList = makerToMap (f,delimiter,maxSizeOutFile);
        for (Product p: inputList) {
            if(mapResult.containsKey (p.getId ())){
                checkAndChangeBean (p,mapResult.get (p.getId ()), maxRepid,count );
            }else {
                PriorityBlockingQueue<Product> queue = new PriorityBlockingQueue<Product> ((Collection<? extends Product>) Collections.reverseOrder ());
                queue.add (p);
                mapResult.put (p.getId (),queue);
                count.addAndGet (1);
            }

        }

    }


//    private static List<Product> readCSV(String fileName, Character delimiter, int lastReadline){
////        Collection<Product> beans = null;
//        try {
////            while (fileName.en)
//            Collection<Product> beans = new CsvToBeanBuilder (new FileReader (fileName))
//                    .withType(Product.class).withSeparator (delimiter).withSkipLines (lastReadline).build().parse();
//
//        } catch (Exception e) {
//            e.printStackTrace ( );
//        }
//
//
//        return beans;
//
//    }




}
