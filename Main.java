import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        new Main();
    }
    Main(){
        try{
            createNewFile();
        } catch (Exception e){
            System.out.println(e);
        }
    }
    ArrayList<String> flashcards = new ArrayList<>();
    ArrayList<String> answers = new ArrayList<>();
    ArrayList<Integer> streaks = new ArrayList<>();
    File file = new File("data.txt");
    void createNewFile() throws IOException{
        if(!file.createNewFile()){
            BufferedReader br = new BufferedReader(new FileReader(file));
            int counter = 0;
            while(br.ready()){
                counter++;
                String fileLine = br.readLine();
                String[] questionAndAnswer = fileLine.split("-");
                flashcards.add(questionAndAnswer[0]);
                answers.add(questionAndAnswer[1]);
                streaks.add(Integer.parseInt(questionAndAnswer[2]));
            }
            System.out.printf("Read from file. You have %s flashcards created.%n", counter);
            br.close();
            startProgram();
        } else {
            System.out.println("No file found. Created new file.");
            createNewFile();
        }
    }
    void readFlashcards(){
        flashcards.clear();
        answers.clear();
        try{
            BufferedReader br = new BufferedReader(new FileReader(file));
        
            while(br.ready()){
                String fileLine = br.readLine();
                String[] questionAndAnswer = fileLine.split("-");
                flashcards.add(questionAndAnswer[0]);
                answers.add(questionAndAnswer[1]);
            }
            br.close();
            generateSeed();
        }catch(Exception e){
            System.out.println("Couldn't read from file");
        }
    }
    ArrayList<Integer> seed = new ArrayList<>();
    void startProgram(){
        generateSeed();
        detectUserInput();
    }
    void generateSeed(){
        seed.clear();
        Random RNG = new Random();
        for(int i=0; i<answers.size(); i++){
            int currentNum = RNG.nextInt(answers.size());
            if(seed.contains(currentNum)){
                i--;
            } else{
                seed.add(currentNum);
            }
        }
    }
    void detectUserInput(){
        System.out.println("Even reached this gain");
        Scanner scan = new Scanner(System.in);
        String userInput = "";
        int counter = 0; //loop through flashcards in order of seed
        while(!userInput.equals("q")){
            int actualIndexForUse = -1;
            if(!answers.isEmpty()) {
                System.out.print(flashcards.get(seed.get(counter)) + " || "); //get flashcard from seed number and not in numerical order
                actualIndexForUse = seed.get(counter);
            }
            System.out.println("q-Quit, a-Add, r-Remove, g-Generate seed, e-Edit, r-Remove, ca-Clear all, rs-Reset streaks, l-List");
            userInput = scan.nextLine();
            for(int i=0; i<50; i++)System.out.println();
            switch(userInput){
                case "q":
                    saveStatus();
                    break;
                case "a":
                    addNewFlashcard(userInput, scan);
                    break;
                case "ca":
                case "rs":
                    int selectedInput = 0;
                    if(userInput.equals("ca")) selectedInput = 0;
                    else if(userInput.equals("rs")) selectedInput = 1;
                    System.out.println("Are you sure you want to do this: \"yes\"-confirm, anything else-cancel");
                    userInput = scan.nextLine();
                    if(userInput.equals("yes")){
                        if(selectedInput==0){
                            answers.clear();
                            flashcards.clear();
                            seed.clear();
                            counter = 0;
                        } else if(selectedInput == 1){
                            for(int i=0;i<streaks.size(); i++){
                                streaks.set(i, 0);
                            }
                        }
                    }
                    saveStatus();
                    break;
                
                case "g":
                    generateSeed();
                    break;
                case "l":
                    listAll();
                    break;
                case "r":
                case "e":
                    int selectedInput1 = 0;
                    if(userInput.equals("r")) selectedInput1 = 0;
                    else if(userInput.equals("e")) selectedInput1 = 1;
                    System.out.printf("Which flashcard? (0-%s)", flashcards.size()-1);
                    int selectedIndex = -1;
                    try{
                        selectedIndex = scan.nextInt();
                        if(selectedIndex<0 || selectedIndex>flashcards.size()-1){
                            System.out.println("Invalid input, out of bounds.");
                        }
                    }catch(Exception e){
                        System.out.println("Please input a number next time");
                    }
                    flashcards.remove(selectedIndex);
                    answers.remove(selectedIndex);
                    streaks.remove(selectedIndex);
                    if(selectedInput1==1) addNewFlashcard(userInput, scan);
                    generateSeed();
                    
                    break;
                default:
                    userInput = userInput.toLowerCase();
                    if(actualIndexForUse<0){
                        System.out.println("You have no flashcards created. Try \"a\" to create a new flashcard.");
                        break;
                    }
                    if(userInput.equals(answers.get(actualIndexForUse))){
                        System.out.println("Correct");
                        streaks.set(actualIndexForUse, streaks.get(actualIndexForUse)+1);
                    } else {
                        System.out.println("Wrong; Correct answer: " + answers.get(actualIndexForUse));
                        streaks.set(actualIndexForUse, 0);
                    }
                    saveStatus();
                    counter++;
                    if(counter>=answers.size()) counter=0;
                    break;
            }
        }
    }
    void addNewFlashcard(String userInput, Scanner scan){
        System.out.println("Question:");
        userInput = scan.nextLine();
        System.out.println("Answer:");
        userInput += "-" + scan.nextLine() + "-0";
        try{
            PrintWriter pw = new PrintWriter(new FileWriter(file, true));
            userInput = userInput.toLowerCase();
            pw.println(userInput);
            pw.close();
            readFlashcards();
        } catch(Exception e){
            System.out.println("Couldn't add new flashcard.");
        }
    }
    void listAll(){
        /* //Sort everything by alpabetical order. Implement simple bubble sort, where I convert every letter to its number counterpart-------------
        //Convert all the flashcards to their number form
        ArrayList<Integer> flashCardsNumForm = new ArrayList<>();
        for(int i=0; i<flashcards.size(); i++){
            int currentWordNum = 0;
            for(int j=0; j<flashcards.get(i).length(); j++){
                currentWordNum*=100;
                int currentLetterNum = (int)flashcards.get(i).charAt(j);
                currentWordNum+=currentLetterNum;
            }
            flashCardsNumForm.add(currentWordNum);
        }
        //Bubble Sort
        //i = how many times we have to repeat it to get sorted
        for(int i=0; i<answers.size(); i++){
            //j = up to which number we have to check
            for(int j=0; j<answers.size()-i-1; j++){
                if(!( flashCardsNumForm.get(j)<flashCardsNumForm.get(j+1) )){
                    int temp = flashCardsNumForm.get(j);
                    flashCardsNumForm.set(j, flashCardsNumForm.get(j+1));
                    flashCardsNumForm.set(j+1, temp);

                    String stringtemp = flashcards.get(j);
                    flashcards.set(j, flashcards.get(j+1));
                    flashcards.set(j+1, stringtemp);

                    stringtemp = answers.get(j);
                    answers.set(j, answers.get(j+1));
                    answers.set(j+1, stringtemp);

                    temp = streaks.get(j);
                    streaks.set(j, streaks.get(j+1));
                    streaks.set(j+1, temp);
                }
            }
        }
        System.out.println(flashCardsNumForm); */
        //^This was fun to make, but it obviously doesn't work
        //Actual sort: Sort everything just by the first letter to not overcomplicate
        if(flashcards.size()>1){
            for(int i=0; i<flashcards.size(); i++){ //How many times we have to loop through all nums to get sorted
                for(int j=0; j<flashcards.size()-i-1; j++){ //current words in the array we are looking at
                    if((int)flashcards.get(j).charAt(0)>(int)flashcards.get(j+1).charAt(0)){
                        String stringtemp = flashcards.get(j);
                        flashcards.set(j, flashcards.get(j+1));
                        flashcards.set(j+1, stringtemp);
                        System.out.println("Flashcards");
    
                        stringtemp = answers.get(j);
                        answers.set(j, answers.get(j+1));
                        answers.set(j+1, stringtemp);
                        System.out.println("answers");
    
                        int temp = streaks.get(j);
                        streaks.set(j, streaks.get(j+1));
                        streaks.set(j+1, temp);
                        System.out.println("streaks");
                    }
                }
            }
        }
        System.out.println("Completed sort");

        //------Decide how many zeroes are needed
        int biggestIndex = flashcards.size();
        int zerocounter = 0;
        while(biggestIndex>0){
            zerocounter++;
            biggestIndex/=10;
        }

        //print them all----------------
        for(int i=0; i<answers.size(); i++){
            String toPrint = flashcards.get(i) + "-" + answers.get(i) + "-" + streaks.get(i);
            toPrint = toPrint.toLowerCase();

            //-------Add the zeroes---------------
            if(i<Math.pow(10, zerocounter)){
                int iZeroes = 0;
                int iCopy = i;
                if(iCopy==0) iCopy=1;
                while(iCopy>0){
                    iZeroes++;
                    iCopy/=10;
                }
                int howManyZeroesNeeded = zerocounter-iZeroes;
                for(int j=0;j<howManyZeroesNeeded; j++){
                    System.out.print("0");
                }
            }

            //Print everything after the zeroes--------------
            System.out.println(i + " " + toPrint);
        }
    }
    void saveStatus(){
        try{
            PrintWriter pw = new PrintWriter(new FileWriter(file, false));
            for(int i=0; i<answers.size(); i++){
                String toPrint = flashcards.get(i) + "-" + answers.get(i) + "-" + streaks.get(i);
                toPrint = toPrint.toLowerCase();
                pw.println(toPrint);
            }
            pw.close();
        } catch(Exception e){
            System.out.println("Couldn't save current score.");
            System.out.println(e);
        }
    }
}
