import java.io.*;
import java.util.*;

public class Main {
    /*
    * PROBLEMS:
    * FIXED (by making sort only sort the print, not the actual data): Sets don't really work, because we are constantly shuffling around the words. Sets only work until you add or remove flashcards, then they break;
    * FIXED (In proposed way) Removing and editing has been broken by the change in sorting. PROPOSED FIX: Make the inputed number recognize the sorted word, then find the exact match in the actual list and remove/edit that one instead
    */
    /*
    * TODO:
    *
    * Make all the commands have a special activation character in front, such as "/" so that they are not accidentally triggered when trying to type in a quick false response or if there is an actual flashcard with the exact text
    * Add a /help instead of having all the commands written in the main sysout
    * (Optional) Clean-up and document code
    * (Dreamful) Make this an actual UI program for multi-language learning
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
        System.out.println("Generated new seed.");
    }



    //USER INPUT-------------------------------------------------------------------------------------------------------------------------------------------------
    int streakLimit = 3;
    boolean doSet = false;
    boolean completeRandom = false;
    int counter = 0; //loop through flashcards in order of seed
    int actualIndexForUse = -1; //Counter, but parsed through the seed to find what we're actually looking at
    void detectUserInput(){
        String userInput = "";
        int skipCounter = 0;
        while(!userInput.equals("q")){
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
                String toPrint = flashcards.get(actualIndexForUse);
                toPrint = unChingCheng(toPrint);

                System.out.println(toPrint + " || " + "!help, Current limit: " + streakLimit); //get flashcard from seed number and not in numerical order
            }

            Scanner scan = new Scanner(System.in);
            
            userInput = scan.nextLine();

            StringBuilder sb = new StringBuilder("");
            for(int i=0; i<70; i++)sb.append("\n");
            System.out.println(sb.toString());

            if(userInput.length()==0){
                saveStatus();
                continue;
            }
            if(userInput.substring(0,1).equals("!")){
                //command options------------------------------------------------------------------------------------------------------------------------
                userCommands(userInput.substring(1,userInput.length()));
            } else {
                //Check for correct or wrong answer if it didn't detect any custom commands--------------------------------------------------------------
                //check for duplicates. If a duplicate exists say that it's correct, but not what the program wanted
                String userInputOriginalCopy = userInput;
                userInput = userInput.toLowerCase();
                int duplicates = 0;
                boolean wrongAnswer = true;
                for(int i=0; i<flashcards.size(); i++){
                    if(flashcards.get(i).equals(flashcards.get(actualIndexForUse))){
                        duplicates++;
                    }
                }
                if(userInput.equals(answers.get(actualIndexForUse))){
                    wrongAnswer = false;
                    System.out.printf("Correct; %s=%s%n",unChingCheng(flashcards.get(actualIndexForUse)),unChingCheng(userInputOriginalCopy));
                    streaks.set(actualIndexForUse, streaks.get(actualIndexForUse)+1);
                    counter++;
                    if(counter>=answers.size()){
                        counter=0;
                        generateSeed();
                    }
                } else if(duplicates>1){
                    for(int i=0; i<answers.size(); i++){
                        if(flashcards.get(i).equals(flashcards.get(actualIndexForUse))){
                            if(userInput.equals(answers.get(i))){
                                System.out.println("Also correct, but not what the program wanted. Try again.");
                                wrongAnswer = false;
                            }
                        }
                    }
                }
                if(wrongAnswer){
                    System.out.println("Duplicates = " + duplicates);
                    System.out.println("Wrong/////////; Correct answer: " + unChingCheng(answers.get(actualIndexForUse)) + "         Your answers: " + unChingCheng(userInputOriginalCopy));
                    streaks.set(actualIndexForUse, -1);
                }
            }

            
            saveStatus();
            checkAllDuplicates();
        }
    }
    //command options------------------------------------------------------------------------------------------------------------------------
    void userCommands(String userInput) {
        Scanner scan = new Scanner(System.in);
        switch(userInput){
            case "", "h", "help":
                helpMenu();
                break;
            case "rf":
                readFlashcards();
                break;
            case "sl":
                System.out.printf("Set the new limit(%s):%n",streakLimit);
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
            case "b":
                counter--;
                if(counter<0){
                    counter = flashcards.size()-1;
                }
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
                for(int i=0; i<set.size(); i++){
                    System.out.println(flashcards.get(set.get(i)) + " - " + answers.get(set.get(i)));
                }
                break;
            case "n":
                counter++;
                if(counter>=answers.size()){
                    counter=0;
                    generateSeed();
                }
                break;
            case "a":
                addNewFlashcard();
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
            case "m 0":
                set.clear();
                for(int i=0; i<streaks.size(); i++){
                    if(streaks.get(i)==0) set.add(i);
                }
                break;
            case "ca", "rs":
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
            case "r", "e":
                removeOrEdit(userInput, scan);
                break;
            case "el":
                System.out.println(flashcards.get(flashcards.size() - 1) + " - " + answers.get(answers.size() -1));
                saveStatus();
                int flashcardamount = flashcards.size();
                addNewFlashcard();
                if(flashcardamount<flashcards.size()){
                    flashcards.remove(flashcards.size() - 2);
                    answers.remove(answers.size()-2);
                    streaks.remove(streaks.size()-2);
                }
                break;
            case "ec":
                System.out.println(flashcards.get(actualIndexForUse) + " - " + answers.get(actualIndexForUse));
                saveStatus();
                int index = actualIndexForUse;
                flashcardamount = flashcards.size();
                addNewFlashcard();
                if(flashcardamount<flashcards.size()){
                    flashcards.remove(index);
                    answers.remove(index);
                    streaks.remove(index);
                }
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

                //Custom commands that can't be in the switch case---------------------------------------------------------------------------------------
                //Online definition
                if(userInput.length()>5 && userInput.substring(0,5).equals("defi ")){
                    boolean internet = true;
                    define(internet, userInput.substring(5,userInput.length()), userInputOriginalCopy.substring(5,userInputOriginalCopy.length()));
                    break;
                }
                //Flashcard or online definition
                if(userInput.length()>4 && userInput.substring(0,4).equals("def ")){
                    boolean internet = false;
                    define(internet, userInput.substring(4,userInput.length()), userInputOriginalCopy.substring(4,userInputOriginalCopy.length()));
                    break;
                }
                //Konjugate
                if(userInput.length()>5 && userInput.substring(0,5).equals("konj ")){
                    konjugate(userInput.substring(5,userInput.length()));
                    break;
                }
                //Make set out of words that start with...
                if(userInput.length()>4 && userInput.substring(0,4).equals("m f ")){
                    String keyword = userInput.substring(4, userInput.length());
                    set.clear();
                    for(int i=0; i<flashcards.size(); i++){
                        if(flashcards.get(i).substring(0, keyword.length()).equals(keyword)){
                            set.add(i);
                        }
                    }
                    break;
                }
                //Make set out of words that end with...
                if(userInput.length()>4 && userInput.substring(0,4).equals("m l ")){
                    String keyword = userInput.substring(4, userInput.length());
                    System.out.println("Keyword: '" + keyword + "'");
                    set.clear();
                    for(int i=0; i<flashcards.size(); i++){
                        if(flashcards.get(i).substring(flashcards.get(i).length()-keyword.length(),flashcards.get(i).length()).equals(keyword)){
                            set.add(i);
                        }
                    }
                    break;
                }

                saveStatus();
                break;
        }
    }
    void helpMenu(){
        System.out.println("!q - Quit");
        System.out.println("!n - Next flashcard");
        System.out.println("!a - Add new flashcard");
        System.out.println("!rf - Read flashcards from file. (Useful if you added new flashcards straight into the file while the program was running)");
        System.out.println("!b - Back/Previous flashcard");
        System.out.println("!r - Remove");
        System.out.println("!m - Make set (of flashcards)");
        System.out.println("!m pr. - Make a set out of flashcards that end with \"pr.\"");
        System.out.println("!m f *keyword* - Make a set out of flashcards that start with keyword");
        System.out.println("!m l *keyword* - Make a set out of flashcards that end with keyword");
        System.out.println("!ds - Do set / Use the active set");
        System.out.println("!ss - Save set / Save the active set");
        System.out.println("!os - Open set / Open a saved set / Load a saved set");
        System.out.println("!ps - Print set / Print the active set and its flashcards");
        System.out.println("!rand - True randomness. No seeds, only random.");
        System.out.println("!g - Generate new seed");
        System.out.println("!e - Edit a flashcard");
        System.out.println("!el - Edit the last added flashcard (!lu to find the last added flashcard)");
        System.out.println("!ec - Edit current flashcard");
        System.out.println("!r - Remove a flashcard");
        System.out.println("!l - List all the flashcards sorted A-Z");
        System.out.println("!lu - List all the flashcards unsorted (In the order which they were added)");
        System.out.println("!sl - Set limit / Set the streak limit, before the flashcards stop showing");
        System.out.println("!rev - Reverse the flashcards and answers");
        System.out.println("!konj *german verb* - Konjugates the german verb into all needed forms");
        System.out.println("!def *word* - Define / Translates the word from Slovene to German and vice versa. Tries to find the answer within your flashcards. If it fails, it searches online");
        System.out.println("!defi *word* - Same as !def, but straight to the interner");
        System.out.println("!setc - Set the streak of the current word");
        System.out.println("!ca - Clear all flashcards from file <- Don't do this");
        System.out.println("!rs - Reset all streaks back to 0");
        //System.out.printf("q-Quit, n-next, a-Add, rf-Read flashcards from file, b-Back r-Remove, m-make set, m pr.-Make set just out of pr. flashcards, m f keyword-make set ouf of flashcards that start with keyword, m l keyword-make set out of flashcards that end in keyword, rand-complete random, ds-do set, ss-save set, os-open set, ps-printSet g-Generate seed, e-Edit, el-Edit last, ec-Edit current, r-Remove, ca-Clear all, rs-Reset streaks, l-List, lu-List unsorted, sl-Set limit(%s), rev-reverse, konj+word-Konjugates word to all forms, defi-Search PONS, def+word-Finds what word means either from your flashcards or from web, setc-set the streak of current word%n", streakLimit);
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
        if(userInput.equals("e")) addNewFlashcard();
        generateSeed();
    }
    void addNewFlashcard(){
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

        String fullFlash = userInput1 +  "@" + userInput2 + "@0";
        
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
            fullFlash = fullFlash.toLowerCase();
            pw.println(fullFlash);
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
            toPrint = unChingCheng(toPrint);

            //Print everything after the zeroes--------------
            System.out.println(i + " " + toPrint);
        }
        saveStatus();
    }
    String unChingCheng(String toPrint) {
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
        return toPrint;
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

    void define(boolean internet, String userInput, String originalInput){
        if(!internet){
            boolean foundCard = false;
            for(int i=0; i<flashcards.size(); i++){
                String toPrint = "";
                if(flashcards.get(i).equals(userInput)){
                    toPrint = userInput + " = " + answers.get(i);
                } else if(answers.get(i).equals(userInput)){
                    toPrint = userInput + " = " + flashcards.get(i);
                }
                if(toPrint.length()>0){
                    System.out.println(unChingCheng(toPrint));
                    foundCard = true;
                }
            }
            if(foundCard){
                return;
            }
            System.out.println("Couldn't find that flashcard.");
        }
        System.out.println("Searching web.");
        String toPrint = unChingCheng(originalInput);
        toPrint = recodeIntoWebsiteSymbols(toPrint);

        try{
            new TranslateWord(toPrint);
        } catch(Exception e){
            System.out.println("Failed online translation.");
        }
    }
    String recodeIntoWebsiteSymbols(String toPrint){
        for(int i=0; i<toPrint.length(); i++){
            char[] unknownSymbols = {'ä','ü','ö','ß','č','š','ž'};
            for(int j=0; j<unknownSymbols.length; j++){
                if(toPrint.charAt(i)==unknownSymbols[j]){
                    char currentSymbol = unknownSymbols[j];
                    String newSymbols = "";
                    System.out.println(currentSymbol + " <--- current detected unknown symbol in Main");
                    switch(currentSymbol){
                        case 'ä': newSymbols = "%C3%A4"; break;
                        case 'ü': newSymbols = "%C3%BC"; break;
                        case 'ö': newSymbols = "%C3%B6"; break;
                        case 'ß': newSymbols = "%C3%9F"; break;
                        case 'č': newSymbols = "%C4%8D"; break;
                        case 'š': newSymbols = "%C5%A1"; break;
                        case 'ž': newSymbols = "%C5%BE"; break;
                        default: System.out.println("New unrecognized symbol"); break;
                    }
                    toPrint = toPrint.substring(0, i) + newSymbols + toPrint.substring(i+1);
                }
            }
        }
        return toPrint;
    }

    void konjugate(String userInput){
        try{
            new KonjugateWord(userInput);
        } catch(Exception e){
            System.out.println("Failed konjugation.");
        }
    }

    void checkAllDuplicates(){
        for(int i=0; i<flashcards.size(); i++){
            for(int j=0; j<flashcards.size(); j++){
                if(j==i) continue;
                String flash1 = flashcards.get(i)+"@"+answers.get(i);
                String flash2 = flashcards.get(j)+"@"+answers.get(j);
                String flash3 = answers.get(j)+"@"+flashcards.get(j);
                if(flash1.equals(flash2) || flash1.equals(flash3)){
                    System.out.println("You have duplicate flashcards; " + flash1 + "(" + i + ")" + " = " + flash2 + "(" + j + ")");
                }
            }
        }
    }
}
