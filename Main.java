import java.io.*;
import java.lang.reflect.Array;
import java.util.*;

public class Main {
    /*
    * PROBLEMS:
    * FIXED (by making sort only sort the print, not the actual data): Sets don't really work, because we are constantly shuffling around the words. Sets only work until you add or remove flashcards, then they break;
    * FIXED (In proposed way) Removing and editing has been broken by the change in sorting. PROPOSED FIX: Make the inputed number recognize the sorted word, then find the exact match in the actual list and remove/edit that one instead
    */
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
            //int counter = 0;
            /* while(br.ready()){
                counter++;
                String fileLine = br.readLine();
                String[] questionAndAnswer = fileLine.split("@");
                flashcards.add(questionAndAnswer[0]);
                answers.add(questionAndAnswer[1]);
                streaks.add(Integer.parseInt(questionAndAnswer[2]));
            } */
            readFlashcards();
            System.out.printf("Read from file. You have %s flashcards created.%n", flashcards.size());
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
        streaks.clear();
        try{
            BufferedReader br = new BufferedReader(new FileReader(file));
        
            while(br.ready()){
                String fileLine = br.readLine();
                if(fileLine.charAt(0) == '[') continue;
                String[] questionAndAnswer = fileLine.split("@");
                flashcards.add(questionAndAnswer[0]);
                answers.add(questionAndAnswer[1]);
                streaks.add(Integer.parseInt(questionAndAnswer[2]));
            }
            br.close();
            generateSeed();
        }catch(Exception e){
            System.out.println("Couldn't read from file");
        }
        try {
            autoBackup();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            System.out.println("Something went wrong with autoBackup");
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
    boolean doSet = false;
    boolean completeRandom = false;
    void detectUserInput(){
        String userInput = "";
        int counter = 0; //loop through flashcards in order of seed
        int skipCounter = 0;
        while(!userInput.equals("q")){
            int actualIndexForUse = -1;
            if(completeRandom) generateSeed();
            
            if(!answers.isEmpty()){
                actualIndexForUse = seed.get(counter);
                if(streaks.get(actualIndexForUse)>=0){
                    for(int i=0; i<streaks.size(); i++){
                        if(streaks.get(i)<0) streaks.set(i, 0);
                    }
                }
            }
            

            if(doSet && set.isEmpty()){
                System.out.println("Your set is empty.");
                doSet = false;
                continue;
            }

            //Check if this has been answered more times than the streaklimit allows. If so, skip it. If it's had to skip through all the items it resets the limit
            if(actualIndexForUse>=0 && (streaks.get(actualIndexForUse)>=streakLimit || (doSet && !set.contains(actualIndexForUse)))){
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
                    saveStatus();
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
            System.out.printf("q-Quit, n-next, a-Add, r-Remove, m-make set, m pr.-Make set just out of pr. flashcards, rand-complete random, ds-do set, ss-save set, os-open set, ps-printSet g-Generate seed, e-Edit, el-Edit last, r-Remove, ca-Clear all, rs-Reset streaks, l-List, lu-List unsorted, sl-Set limit(%s), rev-reverse, konj+word-Konjugates word to all forms, def+word-Finds what word means either from your flashcards or from web, setc-set the streak of current word%n", streakLimit);
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
                case "ds":
                    if(doSet==false)doSet=true;
                    else doSet=false;
                    System.out.println("Do set: " + doSet);
                    break;
                case "ss":
                    saveSetToFile();
                    break;
                case "os":
                    openSetFromFile();
                    break;
                case "ps":
                    System.out.println(set);
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
                case "m":
                    makeNewSet();
                    break;
                case "m pr.":
                    set.clear();
                    for(int i=0; i<flashcards.size(); i++){
                        if(flashcards.get(i).length()<3) continue;
                        if(flashcards.get(i).substring(flashcards.get(i).length()-3, flashcards.get(i).length()).equals("pr.")){
                            set.add(i);
                            continue;
                        }
                        if(answers.get(i).length()<3) continue;
                        if(answers.get(i).substring(answers.get(i).length()-3, answers.get(i).length()).equals("pr.")){
                            set.add(i);
                        }
                    }
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
                case "lu":
                    listAll(false);
                    break;
                case "l":
                    listAll(true);
                    break;
                case "r":
                case "e":
                    removeOrEdit(userInput, scan);
                    break;
                case "el":
                    System.out.println(flashcards.get(flashcards.size() - 1) + " " + answers.get(answers.size() -1));
                    flashcards.remove(flashcards.size() - 1);
                    answers.remove(answers.size()-1);
                    streaks.remove(streaks.size()-1);
                    saveStatus();
                    addNewFlashcard(userInput);
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
                case "rand":
                    if(!completeRandom) completeRandom=true;
                    else completeRandom=false;
                    System.out.println("Complete random: " + completeRandom);
                    break;
                default:
                    //this copy is for the definition method
                    String userInputOriginalCopy = userInput;
                    userInput = userInput.toLowerCase();
                    if(actualIndexForUse<0){
                        System.out.println("You have no flashcards created. Try \"a\" to create a new flashcard.");
                        break;
                    }
                    if(userInput.length()==0){
                        saveStatus();
                        break;
                    }

                    //check for definitions
                    if(userInput.length()>4 && userInput.substring(0,4).equals("def ")){
                        define(userInput.substring(4,userInput.length()), userInputOriginalCopy.substring(4,userInputOriginalCopy.length()));
                        break;
                    }
                    if(userInput.length()>5 && userInput.substring(0,5).equals("konj ")){
                        konjugate(userInput.substring(5,userInput.length()));
                        break;
                    }

                    if(userInput.equals(answers.get(actualIndexForUse))){
                        System.out.printf("Correct; %s=%s%n",flashcards.get(actualIndexForUse),userInput);
                        streaks.set(actualIndexForUse, streaks.get(actualIndexForUse)+1);
                    } else {
                        System.out.println("Wrong/////////; Correct answer: " + answers.get(actualIndexForUse) + "         Your answers: " + userInputOriginalCopy);
                        streaks.set(actualIndexForUse, -1);
                        counter--;
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
        listAll(true);
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
        //The number we have gotten now is equal to the temp lists, not the actual lists, so we now have to compare the two to find the actual index we want
        String wanted = tempFlashcards.get(selectedIndex) + "@" + tempAnswers.get(selectedIndex) + "@" + tempStreaks.get(selectedIndex);
        System.out.println(wanted);
        int actualIndex = -1;
        for(int i=0; i<flashcards.size(); i++){
            String current = flashcards.get(i) + "@" + answers.get(i) + "@" + streaks.get(i);
            if(current.equals(wanted)) actualIndex = i;
        }
        if(actualIndex < 0){
            System.out.println("What..? Couldn't find the selected index in the actual lists.");
            return;
        }
        flashcards.remove(actualIndex);
        answers.remove(actualIndex);
        streaks.remove(actualIndex);
        saveStatus();
        if(userInput.equals("e")) addNewFlashcard(userInput);
        generateSeed();
    }
    void addNewFlashcard(String userInput){
        Scanner scan = new Scanner(System.in);
        System.out.println("Question:");
        String userInput1 = scan.nextLine();
        if(userInput1.length()<1){
            System.out.println("Better luck next time buckaroo.");
            return;
        }
        System.out.println("Answer:");
        String userInput2 = scan.nextLine();
        if(userInput2.length()<1){
            System.out.println("Better luck next time buckaroo.");
            return;
        }

        for(int i=userInput1.length()-1; i>=0; i--){
            if(userInput1.charAt(i) == ' '){
                userInput1 = userInput1.substring(0, i);
            } else break;
        }
        for(int i=userInput2.length()-1; i>=0; i--){
            if(userInput2.charAt(i) == ' '){
                userInput2 = userInput2.substring(0, i);
            } else break;
        }

        userInput = userInput1 +  "@" + userInput2 + "@0";
        
        //check if an exact copy already exists
        for(int i=0; i<flashcards.size(); i++){
            String checkString = flashcards.get(i) + "@" + answers.get(i);
            if(checkString.equals(userInput1 + "@" + userInput2)){
                System.out.println("This exact flashcard already exists.");
                return;
            } else if(checkString.equals(userInput2 + "@" + userInput1)){
                System.out.println("This flashcard already exists, but in reverse order.");
                return;
            }
        }

        try{
            PrintWriter pw = new PrintWriter(new FileWriter(file, true));
            userInput = userInput.toLowerCase();
            pw.println(userInput);
            pw.close();
            readFlashcards();
            autoBackup();
        } catch(Exception e){
            System.out.println("Couldn't add new flashcard or something went wrong with autobackup.");
        }
    }
    ArrayList<String> tempFlashcards = new ArrayList<>();
    ArrayList<String> tempAnswers = new ArrayList<>();
    ArrayList<Integer> tempStreaks = new ArrayList<>();
    void listAll(boolean sort){
        readFlashcards();

        tempFlashcards.clear();
        tempAnswers.clear();
        tempStreaks.clear();
        for(String x : flashcards) tempFlashcards.add(x);
        for(String x : answers) tempAnswers.add(x);
        for(Integer x : streaks) tempStreaks.add(x);

        //Actual sort: Sort everything just by the first letter to not overcomplicate
        if(sort)radixSortWords();

        //------Decide how many zeroes are needed
        int biggestIndex = tempFlashcards.size();
        int zerocounter = 0;
        while(biggestIndex>0){
            zerocounter++;
            biggestIndex/=10;
        }

        //print them all----------------
        for(int i=0; i<tempAnswers.size(); i++){
            String toPrint = tempFlashcards.get(i) + "-" + tempAnswers.get(i) + "-" + tempStreaks.get(i);
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

            //change the c^ and u: types to normal types
            for(int j=0; j<toPrint.length(); j++){
                if(toPrint.charAt(j) == '^' || toPrint.charAt(j) == ':'){
                    char whichChar = toPrint.charAt(j-1);
                    char charToReplaceWith = ' ';
                    switch(whichChar){
                        case 'c':
                            charToReplaceWith = 'č';
                            break;
                        case 'z':
                            charToReplaceWith = 'ž';
                            break;
                        case 'a':
                            charToReplaceWith = 'ä';
                            break;
                        case 'u':
                            charToReplaceWith = 'ü';
                            break;
                        case 'o':
                            charToReplaceWith = 'ö';
                            break;
                        default:
                            if(whichChar == 's'){
                                if(toPrint.charAt(j) == ':'){
                                    charToReplaceWith = 'ß';
                                } else charToReplaceWith = 'š';
                            } else{
                                System.out.println("Something went wrong with turning ching cheng hanji into readable");
                            }
                            break;

                    }
                    toPrint = toPrint.substring(0, j-1) + charToReplaceWith + toPrint.substring(j+1, toPrint.length());

                }
            }

            //Print everything after the zeroes--------------
            System.out.println(i + " " + toPrint);
        }
        saveStatus();
    }
    void radixSortWords(){
        int longestLength = findLongestWord();
        for(int currentLength = longestLength-1; currentLength>=0; currentLength--){
            for(int i=0; i<tempFlashcards.size(); i++){
                for(int j=0; j<tempFlashcards.size()-i-1; j++){
                    if(tempFlashcards.get(j).length() <= currentLength) continue;
                    //if(tempFlashcards.get(j+1).length() <= currentLength) continue;
                    if(/* tempFlashcards.get(j).length() > tempFlashcards.get(j+1).length() || */tempFlashcards.get(j+1).length()<=currentLength || tempFlashcards.get(j).charAt(currentLength)>tempFlashcards.get(j+1).charAt(currentLength)){
                        String stringtemp = tempFlashcards.get(j);
                        tempFlashcards.set(j, tempFlashcards.get(j+1));
                        tempFlashcards.set(j+1, stringtemp);
    
                        stringtemp = tempAnswers.get(j);
                        tempAnswers.set(j, tempAnswers.get(j+1));
                        tempAnswers.set(j+1, stringtemp);
    
                        int temp = tempStreaks.get(j);
                        tempStreaks.set(j, tempStreaks.get(j+1));
                        tempStreaks.set(j+1, temp);
                    }
                    /* StringBuilder sb = new StringBuilder("");
                    for(String x : tempFlashcards){
                        sb.append(x+"\n");
                    }
                    System.out.println(sb.toString());
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } */
                }
            }
        }
        //old first letter bubble sort
        /* if(tempFlashcards.size()>1){
            for(int i=0; i<tempFlashcards.size(); i++){ //How many times we have to loop through all nums to get sorted
                for(int j=0; j<tempFlashcards.size()-i-1; j++){ //current words in the array we are looking at
                    if((int)tempFlashcards.get(j).charAt(0)>(int)tempFlashcards.get(j+1).charAt(0)){
                        String stringtemp = tempFlashcards.get(j);
                        tempFlashcards.set(j, tempFlashcards.get(j+1));
                        tempFlashcards.set(j+1, stringtemp);
    
                        stringtemp = tempAnswers.get(j);
                        tempAnswers.set(j, tempAnswers.get(j+1));
                        tempAnswers.set(j+1, stringtemp);
    
                        int temp = tempStreaks.get(j);
                        tempStreaks.set(j, tempStreaks.get(j+1));
                        tempStreaks.set(j+1, temp);
                    }
                }
            }
        } */
    }
    int findLongestWord(){
        int longestLength = 0;
        for(int i=0; i<tempFlashcards.size(); i++){
            if(tempFlashcards.get(i).length() > longestLength){
                longestLength = tempFlashcards.get(i).length();
            }
        }
        return longestLength;
    }
    void saveStatus(){
        try{
            PrintWriter pw = new PrintWriter(new FileWriter(file, false));
            for(int i=0; i<answers.size(); i++){
                int toPrintStreaks = 0;
                if(streaks.get(i)>0) toPrintStreaks = streaks.get(i);
                String toPrint = flashcards.get(i) + "@" + answers.get(i) + "@" + toPrintStreaks;
                toPrint = toPrint.toLowerCase();
                pw.println(toPrint);
            }
            pw.close();
            autoBackup();

        } catch(Exception e){
            System.out.println("Couldn't save current score or something went wrong with autobackup.");
            System.out.println(e);
        }
    }
    void reverseQuestionsAndAnswers(){
        listAll(true);
        Scanner scan = new Scanner(System.in);
        System.out.println("a-All, x-y - x to y");
        String userInput = scan.nextLine();
        int startIndex = -1;
        int endIndex = -1;
        if(userInput.equals("a")){
            startIndex=0;
            endIndex=flashcards.size();
        } else{
            String[] xAndY = userInput.split("@");
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
    ArrayList<Integer> set = new ArrayList<>();
    void makeNewSet(){
        set.clear();
        Scanner scan = new Scanner(System.in);
        for(int i=0; i<flashcards.size(); i++){
            if(checkSetDuplicate(i)) continue;
            System.out.println(i + ": " + flashcards.get(i) + " - " + answers.get(i) + " - Add? (a), Skip to number (any num), Quit? (q)");
            String response = scan.nextLine();
            try{
                int integerResponse = Integer.parseInt(response);
                i=integerResponse-1;
            } catch(Exception e){
                if(response.equals("a")) {
                    set.add(i);
                }
                else if(response.equals("q"))break;
            }
        }
    }
    boolean checkSetDuplicate(int i){
        for(int j=0; j<set.size(); j++){
            if(set.get(j)==i){
                return true;
            }
        }
        return false;
    }
    File setFile = new File("sets.txt");
    void saveSetToFile(){
        if(set.isEmpty()){
            System.out.println("Set empty.");
            return;
        }
        try{
            if(setFile.createNewFile()){
                System.out.println("Created new set file.");
            }
            PrintWriter pw1 = new PrintWriter(new FileWriter(setFile, true));
            StringBuilder sb = new StringBuilder("");
            for(int i=0; i<set.size(); i++){
                sb.append(set.get(i).toString());
                if(i<set.size()-1) sb.append("@");
            }
            pw1.println(sb.toString());
            System.out.println(sb.toString());
            pw1.close();
            System.out.println("Set saved to file.");
        } catch(Exception e){
            System.out.println("Couldn't save current set.");
            System.out.println(e);
        }
    }
    void openSetFromFile(){
        try{
            BufferedReader br1 = new BufferedReader(new FileReader(setFile));
        
            ArrayList<String> allSetsInFile = new ArrayList<>();
            System.out.println("Your sets:");
            int setCounter = 0;
            while(br1.ready()){
                String fileLine = br1.readLine();
                allSetsInFile.add(fileLine);
                System.out.println(setCounter + ": " + fileLine);
                setCounter++;
            }
            br1.close();
            System.out.printf("Which set do you want? 0-%s%n",allSetsInFile.size()-1);
            Scanner scanner = new Scanner(System.in);
            int userInputInt = scanner.nextInt();
            if(userInputInt>allSetsInFile.size()-1 || userInputInt<0){
                System.out.println("Invalid input.");
                return;
            }
            set.clear();
            String[] tempStringSet = allSetsInFile.get(userInputInt).split("@");
            for(int i=0; i<tempStringSet.length; i++){
                set.add(Integer.parseInt(tempStringSet[i]));
            }
            
        }catch(Exception e){
            System.out.println("Couldn't read sets from file or invalid number.");
        }
    }
    File backupFile = new File("dataAutoCopy.txt");
    void autoBackup() throws Exception {
        //A file that never deletes its contents, only adds
        if(backupFile.createNewFile()){
            System.out.println("Created backup file.");
        }
        BufferedReader br = new BufferedReader(new FileReader(backupFile));
        ArrayList<String> backupFileStrings = new ArrayList<>();
        while(br.ready()){
            backupFileStrings.add(br.readLine());
        }
        PrintWriter writer = new PrintWriter(new FileWriter(backupFile, true));
        for(int i=0; i<flashcards.size(); i++){
            String toPrint = flashcards.get(i) + "@" + answers.get(i) + "@0";
            String toPrint1 = answers.get(i) + "@" + flashcards.get(i) + "@0";
            if(!backupFileStrings.contains(toPrint) && !backupFileStrings.contains(toPrint1)){
                writer.println(toPrint);
            }
        }
        writer.close();
    }

    void define(String userInput, String originalInput){
        for(int i=0; i<flashcards.size(); i++){
            if(flashcards.get(i).equals(userInput)){
                System.out.println(userInput + " = " + answers.get(i));
                return;
            } else if(answers.get(i).equals(userInput)){
                System.out.println(answers.get(i) + " = " + userInput);
                return;
            }
        }
        System.out.println("Couldn't find that flashcard.");
        System.out.println("Searching web.");
        try{
            new TranslateWord(originalInput);
        } catch(Exception e){
            System.out.println("Failed online translation.");
        }
    }

    void konjugate(String userInput){
        try{
            new KonjugateWord(userInput);
        } catch(Exception e){
            System.out.println("Failed konjugation.");
        }
    }
}
