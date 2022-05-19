import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * WordMatchTester
 */
public class WordMatchTester {
    private ArrayList<String> lexicon = new ArrayList<String>();
    private ArrayList<Integer> numberOfOccurances = new ArrayList<Integer>();

    /**
     * Constructor
     * @param pattern
     */
    public WordMatchTester(String[] args) {
        readArgs(args);
    }

    /**
     * 
     * @param args
     */
    public static void main(String[] args) {
        if (args.length < 4) {
            System.out.println("Please provide all 4 files name");
        } else {
            new WordMatchTester(args);
        }
    }

    void readArgs(String[] args) {
        try {
            File file = new File(args[0]);
            Scanner fileReader = new Scanner(file);
            while (fileReader.hasNextLine()) {
                readFile(fileReader.nextLine());
            }
            fileReader.close();

            createLexicon();
            writeFile(args[1]);

            file = new File(args[2]);
            fileReader = new Scanner(file);
            String regex = compile(fileReader.nextLine().toLowerCase());
            fileReader.close();

            writeFileWithPattern(args[3], regex);
        } catch (Exception e) {
            System.out.println("An error occured");
            e.printStackTrace();
            
        }
    }

    /**
     * Function to read the file
     * @param fileName
     */
    void readFile(String fileName) {
        try {
            File file = new File(fileName);
            Scanner fileReader = new Scanner(file);
            String fileLine = "";
            while (fileReader.hasNext()) {
                // Reading file lines one by one and removing punctuations.
                fileLine = fileReader.nextLine().toLowerCase().replaceAll("\\p{Punct}", "");
                // Escaping empty lines
                if (fileLine.isEmpty()) {
                    continue;
                }
                // Extracting words from a line by splitting by space character
                String[] fileLineWords = fileLine.split("\\s");
                for (String word : fileLineWords) {
                    // Escaping single number 
                    if (word.matches("\\d+")) {
                        continue;
                    }
                    lexicon.add(word);
                }
            }
            // Sorting lexicon by alphabets
            sort(lexicon, 0, lexicon.size()-1, false);
            fileReader.close();
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    /**
     * Function to write the file
     * @param fileName
     */
    void writeFile(String fileName) {
        try {
            ArrayList<String> lexiconClone = new ArrayList<String>(lexicon);
            sort(lexiconClone, 0, lexiconClone.size()-1, true);
            String fileLine = "";
            for (int i = 0; i < lexicon.size(); i++) {
                fileLine += lexicon.get(i) + " " + numberOfOccurances.get(i) + " " + getNeighbours(lexiconClone, lexicon.get(i)).toString() + "\n";
            }
            FileWriter fileWriter = new FileWriter(fileName);
            fileWriter.write(fileLine);
            fileWriter.close();
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    /**
     * Function to write the file
     * @param fileName
     * @param pattern
     */
    void writeFileWithPattern(String fileName, String pattern) {
        try {
            String fileLine = "";
            boolean found = false;
            String regex = compile(pattern.toLowerCase());
            for (int i = 0; i < lexicon.size(); i++) {
                if (lexicon.get(i).matches(regex)) {
                    found = true;
                    fileLine += lexicon.get(i) + " " + numberOfOccurances.get(i) + "\n";
                } 
            }
            if (found) {
                System.out.println(fileLine);
                FileWriter fileWriter = new FileWriter(fileName);
                fileWriter.write(fileLine);
                fileWriter.close();
            } else {
                System.out.println("No words in the lexicon match the pattern");
            }
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    /** 
     * Function to count the number of occurances
     * of a word in the lexicon,
     * and to remove the duplicate words from lexicon.
     */
    private void createLexicon() {
        Integer count = 0;
        for (int i = 0; i < lexicon.size(); i++) {
            for (int j = i; j < lexicon.size(); j++) {
                if (lexicon.get(i).equals(lexicon.get(j))) {
                    count++;
                    if (count > 1) {
                        lexicon.remove(j);
                        j -= 1;
                    }
                } else {
                    i = j-1;
                    break;
                }
            }
            numberOfOccurances.add(count);
            count = 0;
        }
    }

    /**
     * Function to compile the pattern
     * into regular expression
     * eg. ?a*t* = (.?)a(.*)t(.*)
     * @param pattern
     * @return
     */
    String compile(String pattern) {
        String regex = "";
        for (int i = 0; i < pattern.length(); i++) {
            if (pattern.charAt(i) == '*') {
                regex += "(.*)";
            } else if (pattern.charAt(i) == '?') {
                regex += "[a-z]";
            } else {
                regex += pattern.charAt(i);
            }
        }
        
        return regex;
    }

    /**
     * 
     * @param word
     * @return
     */
    ArrayList<String> getNeighbours(ArrayList<String> lexiconClone, String word) {
        ArrayList<String> neighbours = new ArrayList<String>();
        int missmatch = 0;
        Integer index = lexiconClone.indexOf(word);

        for (int j = 0; j < lexiconClone.size(); j++) {
            if (index == j) {
                continue;
            }
            if (lexiconClone.get(j).length() > lexiconClone.get(index).length()) {
                break;
            }
            if (lexiconClone.get(j).length() < lexiconClone.get(index).length()) {
                continue;
            }
            for (int j2 = 0; j2 < lexiconClone.get(index).length(); j2++) {
                if (lexiconClone.get(index).charAt(j2) != lexiconClone.get(j).charAt(j2)) {
                    missmatch += 1;
                }
            }
            if (missmatch == 1) {
                neighbours.add(lexiconClone.get(j));
            }
            missmatch = 0;   
        }
        sort(neighbours, 0, neighbours.size()-1, false);

        return neighbours;
    }

    /**
     * Function to sort(Quick Sort) the arraylist.
     * @param lexicon
     * @param low
     * @param high
     * @param byLength
     * Parameter "byLength" is used to chose the type of sort
     * i.e. sort by alphaphets or sort by word length
     * byLength=true means sort by length
     * byLength=false means sort by alphabets
     */
    void sort(ArrayList<String> lexicon, int low, int high, boolean byLength) {
        if (low < high) {
            int partitionIndex = partition(lexicon, low, high, byLength);
            sort(lexicon, low, partitionIndex-1, byLength);
            sort(lexicon, partitionIndex+1, high, byLength);
        }
    }

    /**
     * Function to make partition based on pivot element
     * @param lexicon
     * @param low
     * @param high
     * @param byLength
     * @return
     */
    int partition(ArrayList<String> lexicon, int low, int high, boolean byLength) {
        String pivot = lexicon.get(high);
        int i = (low-1);
        for (int j = low; j < high; j++) {
            if (byLength) {
                if (lexicon.get(j).length() < pivot.length()) {
                    i++;
                    swap(lexicon, i, j);
                }
            } else {
                if (lexicon.get(j).compareTo(pivot) < 0) {
                    i++;
                    swap(lexicon, i, j);
                }
            }
        }
        swap(lexicon, i+1, high);

        return i+1;
    }

    /**
     * Function to swap the values
     * @param lexicon
     * @param first
     * @param second
     */
    void swap(ArrayList<String> lexicon, int first, int second) {
        String temp = lexicon.get(first);
        lexicon.set(first, lexicon.get(second));
        lexicon.set(second, temp);
    }
}