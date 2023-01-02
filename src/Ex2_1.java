import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;

/**
 * The purpose of this class is to apply different methods on text files to calculate their total number of lines.
 * There is 4 methods : the first method (createTextFiles) generates n text files with random number of lines ,
 * and 3 other different methods that calculate the total number of lines in these files that generated by the first method.
 * <p>The first method (getNumOfLines) calculates the total number of lines in a normal way.
 * <p>The second method (getNumOfLinesThreads) calculates the total number of lines using threads.
 * <p>The third method (getNumOfLinesThreadPool) calculates the total number of lines using ThreadPool.
 * @author Nael Aboraya , Marwan Hresh
 */
public class Ex2_1 {


    /**
     * Creates n files and saves them in an output folder , each file has a random number of lines (at most
     * {@code bound}) and each line has a random number of chars (between 10 and 30) , the chars are also picked randomly
     * (between 'a' and 'z').
     * @param n number of files to be generated.
     * @param seed value used to initialize the pseudo-random number generator used by the Random class,
     *            which determines the sequence of random numbers that will be generated.
     * @param bound upper bound of the number of lines.
     * @return array that contains the names of all files generated.
     */
    public static String[] createTextFiles(int n, int seed, int bound){
        String[] names = new String[n];//The output : array of strings that will contain the names of the files
        Random rand = new Random(seed);//a Random object with the seed value

        String folderPath = "./OutputFiles";//name of folder to store the files in it
        File folder = new File(folderPath);
        folder.mkdir();//creating the folder
        for(int i=0;i<n;i++){
            String file_name = "file_" + (i + 1) ;
            String filePath = folderPath + "/" + file_name;
            int numOfLines = rand.nextInt(bound) + 1;//generating a random number of lines between 1 and seed
            try {
                FileWriter fw = new FileWriter(filePath);//creating a file in the folder

                for (int line = 0; line < numOfLines; line++) {//for each line in the text file :
                    StringBuilder sb = new StringBuilder();
                    int length = rand.nextInt(21) + 10;  // random length between 10 and 30 (length of line)
                    for (int ch = 0; ch < length; ch++) {
                        char c = (char)(rand.nextInt(26) + 'a');
                        sb.append(c);//appending to every line a random string
                    }
                    fw.write(sb.toString() + "\n");//writing the random string to the text file
                }

                names[i] = "file_" + (i + 1);//adding the name (without ".txt") of file i to the array at index i
                fw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return names;
    }



    //A private helping function that calculates the number of lines in a single Text file.
    //This function can be used in the normal case and in the threads case (using boolean variable).
    private static int TextFileNumOfLines(String filename,boolean is_Threads_used) {
        if(is_Threads_used){//In case of threads

            NumOfLinesThreads thread = new NumOfLinesThreads(filename);

            thread.start();

            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            int num_of_lines = thread.getNum_of_lines();
            return num_of_lines;
        }

        else{//In the normal case

        int count = 0;
        String folderPath = "./OutputFiles";
        String filePath = folderPath + "/" + filename;
        try {
            FileReader fr = new FileReader(filePath);
            BufferedReader br = new BufferedReader(fr);

            while (br.readLine() != null) {
                count++;
            }
            br.close();
            fr.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return count;
    }
    }


    /**
     * Calculates the total number of lines in all the given files in the normal way (without using threads or something else).
     * <p>This method iterates through the array of files names ,
     * and for each file it calls the helping method {@code TextFileNumOfLines} to calculate the number of lines in this file.
     * <p>In the end , it sums all the values returned by {@code TextFileNumOfLines}.
     * @param fileNames names of files.
     * @return the to total number of lines in all the given files.
     */
    public static int getNumOfLines(String[] fileNames){
        int num_of_total_files_lines = 0;

        for (String file : fileNames){
            num_of_total_files_lines += TextFileNumOfLines(file,false);
        }
        return num_of_total_files_lines;
    }

   //class (Threads)

    /**
     * This inner class extends the {@link Thread} class and is used to count the number of lines in a text file.
     *<p> The file name is provided as an argument to the constructor, and the number of lines is stored as an instance variable.
     * <p>The {@link #run()} method reads the file line by line and increments the count for each line.
     *<p> The {@link #getNum_of_lines()} method can be used to retrieve the final count.
     */
   static class NumOfLinesThreads extends Thread{
        private String file_name;
        private int num_of_lines;

        public NumOfLinesThreads(String file_name){
            this.file_name = file_name;
            this.num_of_lines = 0;
        }

        public int getNum_of_lines() {
            return this.num_of_lines;
        }

        public void run(){
            String folderPath = "./OutputFiles";
            String filePath = folderPath + "/" + this.file_name;
            try {
                FileReader fr = new FileReader(filePath);
                BufferedReader br = new BufferedReader(fr);

                while(br.readLine()!=null){
                    this.num_of_lines++;
                }
                br.close();
                fr.close();
            }catch (FileNotFoundException e) {
                e.printStackTrace();
            }catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    //

    /**
     * Calculates the total number of lines in all the given files in the threads way (using threads).
     * <p>creates a separate thread for each file and waits for the thread to complete before moving on to the next file.
     * <p>This method iterates through the array of files names ,
     * and for each file it calls the helping method {@code TextFileNumOfLines} which starts a thread that calculates
     * the number of lines in this file.
     * <p>In the end , it sums all the values returned by {@code TextFileNumOfLines}.
     * @param fileNames names of files.
     * @return the to total number of lines in all the given files.
     */
    public static int getNumOfLinesThreads(String[] fileNames){
        int num_of_total_files_lines = 0;

        for (String file : fileNames){
            num_of_total_files_lines += TextFileNumOfLines(file,true);
        }
        return num_of_total_files_lines;
    }


    // class (Threadpool)

    /**
     * This inner class represents a task that counts the number of lines in a text file using thread pool.
     *
     * It implements the Callable interface and contains a method called "call" that returns the number of lines in the text file.
     */
    static class NumOfLinesThreadPool implements Callable<Integer> {
        private String file_name;

        public NumOfLinesThreadPool (String file_name){
            this.file_name = file_name;
        }

        @Override
        public Integer call() throws Exception {
            return TextFileNumOfLines(file_name,false);
        }
    }
    //

    /**
     * Calculates the total number of lines in the given list of text files using a thread pool.
     *<p>A fixed size thread pool is created with the size equal to the number of files.
     *<p>Callable tasks are created to count the number of lines in each file and are submitted to the thread pool.
     *<p>The result of each task is summed and returned.
     * @param fileNames names of files.
     * @return the to total number of lines in all the given files.
     */
    public static int getNumOfLinesThreadPool(String[] fileNames) {
        int num_of_total_files_lines = 0;

        // Create a thread pool with a fixed size equal to the number of files
        ExecutorService executor = Executors.newFixedThreadPool(fileNames.length);

        // Create a list of Callable tasks to count the number of lines in each file
        List<Callable<Integer>> tasks = new ArrayList<>();
        for (String fileName : fileNames) {
            tasks.add(new NumOfLinesThreadPool(fileName));
        }

        // Submit the tasks to the thread pool and get the list of Futures
        List<Future<Integer>> futures = null;
        try {
            futures = executor.invokeAll(tasks);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Wait for the tasks to complete and sum the results
        for (Future<Integer> future : futures) {
            try {
                num_of_total_files_lines += future.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
        executor.shutdown();
        return num_of_total_files_lines;
    }



}
