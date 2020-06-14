package importdata;

import com.opencsv.bean.CsvToBeanBuilder;
import model.Product;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static utils.Least.checkAndChangeBean;
import static utils.Least.leastN;

public class ImportCsv {

    static Scanner sc = new Scanner(System.in);
    public static void main(String[] args) {


        String temp = uiAnswer("Введите символ разделителя данных в файле(ах), актуальным будет только первый символ, введеной строки, остальные - игнорируются(пример разделителя  ;    :");
        char delimiter = temp.length()>0? temp.charAt(0): ',';
//        char delimiter = ',';
        lokForDir(delimiter);
        sc.close();

    }

    private static String uiAnswer(String question){
        String answer="";
        //            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
//            Scanner sc = new Scanner(System.in);

        while (answer.equals("")){
            System.out.print(question);
            answer = sc.next();
//                answer = reader.readLine();
        }

//            reader.close();
//            sc.close();

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





        // Считываем исходный каталог для поиска файлов.

        String temp   = uiAnswer("Введите исходную директорию для поиска файлов:");
        String directoryPath = temp.length()>0? temp: "";
        File directory = new File(directoryPath);
        // Убедимся, что директория найдена и это реально директория, а не файл.
        if (directory.exists() && directory.isDirectory()) {
            processDirectory(directory,delimiter);
        } else {
            System.out.println("Не удалось найти директорию по указанному пути.");
        }

    }


//    private static void select(final File directory, final Character delimiter) {
//        FilenameFilter filter = (file, name)-> name.endsWith(".csv");
//
//        File[] files = directory.listFiles(filter);
//
////        добавить обход папок в глубину
//
//        if (files == null) {
//            System.out.println("Нет доступных файлов для обработки.");
//            return;
//        } else {
//            System.out.println("Количество файлов для обработки: " + files.length);
//        }
//        int waitTime;
//        int temp = Integer.parseInt("Введите ожидаемое время работы программы в минутах(по умолчанию 1 000):");
//        if (temp<1){
//            waitTime =1000;
//        }else {
//            waitTime = temp;
//        }
//
////        ConcurrentHashMap <>
//
//    }


//    private static void sortBySystem(String path, String fileName, Character delimiter, Map<String, CurrentProcessingData> mapSortFiles){
//        {
////           создать папку для хранения временных файлов
//
//            try
//            {
//                //здесь "sleep 15" и есть ваша консольная команда
//
//                // -t, - если делитель - запятая
//
////          sort.exe yourfile.csv -t, -k5 -g -o sorted.csv
//                String outFileName = fileName.substring(0,fileName.indexOf('.') ) + "sort.csv";
//
//                String command = path+"\\sort.exe "+ fileName + " -t" + delimiter + " -k5 -g -o  " + outFileName; // sort -k5 -g file.csv > sorted.csv для Linux
//                Process proc = Runtime.getRuntime().exec(command);
//                proc.waitFor();
//                proc.destroy();
//                mapSortFiles.put(outFileName, new CurrentProcessingData(0,false,  -1.0f));
//            }
//            catch (IOException | InterruptedException e)
//            {
//                e.printStackTrace();
//            }
//        }
//    }

    private static Queue<Product> makerToMap(File file, Character delimiter, int maxSizeMap ) throws FileNotFoundException {

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

        int temp = Integer.parseInt(uiAnswer("Введите ожидаемое время работы программы в минутах(по умолчанию 1 000):"));
        int waitTime =   temp>0 ? temp : 1000;


        temp = Integer.parseInt(uiAnswer("Введите максимальное количество выводимых данных(строк,по умолчанию 1 000):"));
        int maxSizeMap = temp > 0 ? temp : 1000;

        temp = Integer.parseInt(uiAnswer("Введите максимальное количество элементов с одинаковым ID(строк,по умолчанию 20):"));
        int maxRep =  temp > 0 ? temp : 20;

        // Непосредственно многопоточная обработка файлов.
        final int treadCount = Runtime.getRuntime().availableProcessors() + 1; // количество потоков, которые могу запустить
        ConcurrentHashMap<Integer, PriorityBlockingQueue<Product>> mapResult = new ConcurrentHashMap<> (maxSizeMap,0.5f,treadCount  );
        AtomicInteger countSize = new AtomicInteger ( 0 );
        PriorityBlockingQueue<Float> priceQueue = new PriorityBlockingQueue<>(maxSizeMap,Comparator.reverseOrder());

//        ConcurrentHashMap<Integer, CountProduct> mapResult = new ConcurrentHashMap<Integer, CountProduct> (1000,0.5f,treadCount);

        ExecutorService service = Executors.newFixedThreadPool(treadCount);

        for (final File f : files) {
            service.execute(() -> {
                try {
                    makerFinalList (f,delimiter, mapResult, maxSizeMap, maxRep, countSize, priceQueue);
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

//        AtomicReference<PriorityQueue<Product>> res = new AtomicReference<>(new PriorityQueue<>(maxSizeMap, Comparator.reverseOrder()));
//        mapResult.forEach((key, value) -> res.get().addAll(value));


    }







    private static void makerFinalList(File f, Character delimiter, ConcurrentHashMap<Integer, PriorityBlockingQueue<Product>> mapResult, int maxSizeOutFile, int maxRep, AtomicInteger count, PriorityBlockingQueue<Float> priceQueue) throws FileNotFoundException {
        Queue<Product> inputList = makerToMap (f,delimiter,maxSizeOutFile);
        for (Product p: inputList) {
            priceQueue.add(p.getPrice());
            assert priceQueue.peek() != null;
            if(priceQueue.peek().compareTo(p.getPrice())>0){
                if(mapResult.containsKey (p.getId ())){
                    checkAndChangeBean (p,mapResult.get (p.getId ()), maxRep,count);
                }else {
                    PriorityBlockingQueue<Product> queue = new PriorityBlockingQueue<>(maxRep, Collections.reverseOrder());
                    queue.add (p);
                    mapResult.put (p.getId (),queue);
                    count.addAndGet (1);
                }
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
