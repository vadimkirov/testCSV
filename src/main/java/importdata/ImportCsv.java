package importdata;

import com.opencsv.bean.CsvToBeanBuilder;
import model.Product;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static utils.Least.checkAndChangeBean;
import static utils.Least.leastN;

public class ImportCsv {

    static Scanner sc = new Scanner(System.in);
    public static void main(String[] args) {


        String temp = uiAnswer("Введите символ разделителя данных в файле(ах)(пример разделителя  ;    :");
        char delimiter = temp.length()>0? temp.charAt(0): ',';
        lokForDir(delimiter);
        sc.close();

    }

    private static String uiAnswer(String question){
        String answer="";

        while (answer.equals("")){
            System.out.print(question);
            answer = sc.next();
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




        ConcurrentHashMap<Integer, PriorityBlockingQueue<Product>> mapResult = new ConcurrentHashMap<> (maxSizeMap,0.5f,treadCount );
        AtomicInteger countSize = new AtomicInteger ( 0 );
        PriorityBlockingQueue<Float> priceQueue = new PriorityBlockingQueue<>(maxSizeMap,Comparator.reverseOrder());

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

        PriorityQueue<Product> res = new PriorityQueue<>(maxSizeMap);
        mapResult.forEach((key, value) -> res.addAll(value));


        //Переделать потом в выгрузку в файл
        for (int i = 0; i < maxSizeMap; i++) {
            System.out.println(res.poll());
        }

    }




    private static void makerFinalList(File f, Character delimiter, ConcurrentHashMap<Integer, PriorityBlockingQueue<Product>> mapResult, int maxSizeOutFile, int maxRep, AtomicInteger count, PriorityBlockingQueue<Float> priceQueue) throws FileNotFoundException {
        Queue<Product> inputList = makerToMap (f,delimiter,maxSizeOutFile);
        for (Product p: inputList) {
            if(count.get() == 0){
                priceQueue.add(p.getPrice());
                count.addAndGet (1);
            }
            assert priceQueue.peek() != null;
            if(priceQueue.peek().compareTo(p.getPrice())>0){
                if(mapResult.containsKey (p.getId ())){
                    checkAndChangeBean (p,mapResult.get (p.getId ()), maxRep,count);
                }else {
                    PriorityBlockingQueue<Product> queue = new PriorityBlockingQueue<>(maxRep, Collections.reverseOrder());
                    queue.offer (p);
                    mapResult.put (p.getId (),queue);
                    count.addAndGet (1);
                }
            }

        }

    }

}
