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
                streaks.add(Integer.parseInt(questionAndAnswer[2]));
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
    int streakLimit = 3;
    void detectUserInput(){
        String userInput = "";
        int counter = 0; //loop through flashcards in order of seed
        int skipCounter = 0;
        while(!userInput.equals("q")){
            int actualIndexForUse = -1;
            if(!answers.isEmpty()){
                actualIndexForUse = seed.get(counter);
            }

            //Check if this has been answered more times than the streaklimit allows. If so, skip it. If it's had to skip through all the items it resets the limit
            if(actualIndexForUse>=0 && streaks.get(actualIndexForUse)>=streakLimit){
                counter++;
                skipCounter++;
                if(skipCounter==flashcards.size()){
                    System.out.println("You have mastered all the flashcards. Reset streaks.");
                    skipCounter=0;
                    for(int i=0;i<streaks.size(); i++){
                        streaks.set(i, 0);
                    }
                    counter=0;
                    generateSeed();
                }
                if(counter>=flashcards.size()){
                    counter=0;
                    generateSeed();
                }
                continue;
            } else{
                skipCounter=0;
            }
            //END

            if(!answers.isEmpty()) {
                System.out.print(flashcards.get(seed.get(counter)) + " || "); //get flashcard from seed number and not in numerical order
            }
            System.out.printf("q-Quit, n-next, a-Add, r-Remove, g-Generate seed, e-Edit, r-Remove, ca-Clear all, rs-Reset streaks, l-List, sl-Set limit(%s), setc-set the streak of current word%n", streakLimit);
            Scanner scan = new Scanner(System.in);
            
            userInput = scan.nextLine();

            StringBuilder sb = new StringBuilder("");
            for(int i=0; i<70; i++)sb.append("\n");
            System.out.println(sb.toString());

            switch(userInput){
                case "sl":
                    System.out.println("Set the new limit:");
                    try{
                        int prevLimit = streakLimit;
                        streakLimit = scan.nextInt();
                        if(streakLimit<1){
                            streakLimit=prevLimit;
                            System.out.println("Invalid number. Reset streak limit back to prev. value.");
                        } 
                    }catch(Exception e){
                        System.out.println("Please input a number next time.");
                        System.out.println(e);
                    }
                    break;
                case "q":
                    saveStatus();
                    break;
                case "n":
                    counter++;
                    if(counter>=answers.size()){
                        counter=0;
                        generateSeed();
                    }
                    break;
                case "a":
                    addNewFlashcard(userInput);
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
                            actualIndexForUse = -1;
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
                    removeOrEdit(userInput, scan);
                    
                    break;
                case "rev":
                    reverseQuestionsAndAnswers();
                    break;
                case "setc":
                    setCurrentWordStreak(actualIndexForUse);
                    counter++;
                    if(counter>=answers.size()){
                        counter=0;
                        generateSeed();
                    }
                    break;
                default:
                    userInput = userInput.toLowerCase();
                    if(actualIndexForUse<0){
                        System.out.println("You have no flashcards created. Try \"a\" to create a new flashcard.");
                        break;
                    }
                    if(userInput.length()==0){
                        saveStatus();
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
                    if(counter>=answers.size()){
                        counter=0;
                        generateSeed();
                    }
                    break;
            }
            saveStatus();
        }
    }
    void removeOrEdit(String userInput, Scanner scan){
        listAll();
        System.out.printf("Which flashcard? (0-%s)%n", flashcards.size()-1);
        int selectedIndex = -1;
        try{
            selectedIndex = scan.nextInt();
            if(selectedIndex<0 || selectedIndex>flashcards.size()-1){
                System.out.println("Invalid input, out of bounds.");
                return;
            }
        }catch(Exception e){
            System.out.println("Please input a number next time");
            return;
        }
        flashcards.remove(selectedIndex);
        answers.remove(selectedIndex);
        streaks.remove(selectedIndex);
        saveStatus();
        if(userInput.equals("e")) addNewFlashcard(userInput);
        generateSeed();
    }
    void addNewFlashcard(String userInput){
        Scanner scan = new Scanner(System.in);
        System.out.println("Question:");
        userInput = scan.nextLine();
        if(userInput.length()<1){
            System.out.println("Better luck next time buckaroo.");
            return;
        }
        System.out.println("Answer:");
        String userInputNew = scan.nextLine();
        if(userInputNew.length()<1){
            System.out.println("Better luck next time buckaroo.");
            return;
        }
        userInput += "-" + userInputNew + "-0";
        
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
        //Actual sort: Sort everything just by the first letter to not overcomplicate
        bubbleSortFirstLetter();

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
        saveStatus();
    }
    void bubbleSortFirstLetter(){
        if(flashcards.size()>1){
            for(int i=0; i<flashcards.size(); i++){ //How many times we have to loop through all nums to get sorted
                for(int j=0; j<flashcards.size()-i-1; j++){ //current words in the array we are looking at
                    if((int)flashcards.get(j).charAt(0)>(int)flashcards.get(j+1).charAt(0)){
                        String stringtemp = flashcards.get(j);
                        flashcards.set(j, flashcards.get(j+1));
                        flashcards.set(j+1, stringtemp);
    
                        stringtemp = answers.get(j);
                        answers.set(j, answers.get(j+1));
                        answers.set(j+1, stringtemp);
    
                        int temp = streaks.get(j);
                        streaks.set(j, streaks.get(j+1));
                        streaks.set(j+1, temp);
                    }
                }
            }
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
    void reverseQuestionsAndAnswers(){
        listAll();
        Scanner scan = new Scanner(System.in);
        System.out.println("a-All, x-y - x to y");
        String userInput = scan.nextLine();
        int startIndex = -1;
        int endIndex = -1;
        if(userInput.equals("a")){
            startIndex=0;
            endIndex=flashcards.size();
        } else{
            String[] xAndY = userInput.split("-");
            if(xAndY.length<2 || xAndY.length>2){
                System.out.println("Invalid input.");
                return;
            }
            try{
                startIndex = Integer.parseInt(xAndY[0]);
                endIndex = Integer.parseInt(xAndY[1])+1;
            }catch(Exception e){
                System.out.println("Please input numbers next time.");
                System.out.println(e);
                return;
            }
            if(startIndex<0 || endIndex>flashcards.size()){
                System.out.println("Index out of bounds.");
                return;
            }
        }
        for(int i=startIndex; i<endIndex; i++){
            String temp = answers.get(i);
            answers.set(i, flashcards.get(i));
            flashcards.set(i, temp);
        }
        System.out.println();
    }
    void setCurrentWordStreak(int index){
        Scanner scan = new Scanner(System.in);
        try{
            int newStreak = scan.nextInt();
            if(newStreak<0){
                System.out.println("Invalid streak.");
                return;
            }
            streaks.set(index,newStreak);
        } catch(Exception e){
            System.out.println("Please input a number next time");
            System.out.println(e);
        }
    }
}
