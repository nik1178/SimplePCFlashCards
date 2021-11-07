import java.net.*;  
import java.io.*;  

public class KonjugateWord{  

    String input;

    KonjugateWord(String input){
        this.input = input;
        foldWord(input);
    }

    String foldWord(String input){
        String siteURL = "https://konjugator.reverso.net/konjugation-deutsch-verb-" + input + ".html";
        String output  = getUrlContents(siteURL); 
        printToFile(output, "Konjugation");
        searchForFold(output);

        return "";
    }

    void searchForFold(String output){
        String keyword = "\"verbtxt\">";
        String[] splitOutput = output.split(keyword);
        //System.out.println(splitOutput[1]);
        System.out.println("Split the website into " + splitOutput.length + " pieces.");


        int prasenStartsAt = 1; //(first object in array is null)
        int prateriumStartsAt = splitOutput.length-1;
        int perfektStartsAt = splitOutput.length-1;

        //get all prasens and prateriums + perfekt without ge
        int forLoopLength = splitOutput.length;
        //if(forLoopLength>30) forLoopLength = 30; //avoid any data after the folds that we are looking for
        String[] allUsableWords = new String[forLoopLength];
        
        
        for(int i=1; i<forLoopLength; i++){

            //check index for praterium----------------- \/ - this checks if it's the first match it found, so it doesn't accidentally go too far
            if(splitOutput[i].contains("Präteritum") && i<prateriumStartsAt){
                prateriumStartsAt=i+1;
            }
            if(splitOutput[i].contains("Perfekt") && i<perfektStartsAt){
                perfektStartsAt=i+1;
            }

            //form words from ---------------------------------------------
            char currentChar = splitOutput[i].charAt(0);
            int currentCharIndex = 1;
            int tempCounter = 0;
            String currentWord = "";
            while(currentChar!='<'){
                currentWord+=currentChar;
                currentChar = splitOutput[i].charAt(currentCharIndex);
                currentCharIndex++;
                tempCounter++;
                if(tempCounter>10000){
                    System.out.println("Got stuck in a while loop");
                    return;
                }
            }
            allUsableWords[i]=currentWord;
        }
        /* System.out.println("Prasen starts at: " + prasenStartsAt);
        System.out.println("Prateritum starts at: " + prateriumStartsAt);
        System.out.println("Perfekt starts at: " + perfektStartsAt);
        System.out.println("Got all the folds. There are " + allUsableWords.length + " usable words."); */

        //add the folds to logical arrays--------------------------------
        String[] prasenFolds = new String[6];
        String[] prateriumFolds = new String[6];
        String perfekt = "";
        for(int i = 0; i<6; i++){
            prasenFolds[i] = allUsableWords[prasenStartsAt+i];
            prateriumFolds[i] = allUsableWords[prateriumStartsAt+i];
        }
        perfekt = allUsableWords[perfektStartsAt];
        /* System.out.println("Made arrays of correct words"); */

        //habe or sind for perfekt--------------------------------------
        String habeOrSind = "";
        String[] habeOrSindSplit = splitOutput[perfektStartsAt-1].split("\"auxgraytxt\">");
        System.out.println("Split the split website to find where habe or sind are located. The length of the new array is: " + habeOrSindSplit.length);
        //if first letter is h it's habe, else if it's b it is bin aka. sind
        try{
            if(habeOrSindSplit[1].charAt(0)=='h'){
                habeOrSind = "haben ";
            } else if(habeOrSindSplit[1].charAt(0)=='b'){
                habeOrSind = "sind ";
            }
            System.out.println("Found habe or sind");
        }catch(Exception e){
            System.out.println("Failed getting habe or sind.");
            habeOrSind = "N/A";
        }

        //check for perfekt particle-------------------------------------------
        String perfektParticle = "";
        if(splitOutput[perfektStartsAt-1].contains("<i class=\"particletxt\">")){
            perfektParticle = "ge";
        }
        perfekt = perfektParticle+perfekt;
        System.out.println("Found perfekt particle");

        /* System.out.println(prasenStartsAt);
        System.out.println(prateriumStartsAt);
        System.out.println(perfektStartsAt); */
        /* System.out.println(Arrays.toString(allUsableWords)); */
        //Old system:
        /* String pasteReady = gerWord + "\t\t\t" + habeOrSind + perfekt + "\n" + 
                            "\n" + 
                            "ich "  + "\t" + prasenFolds[0] + "\t\t" + prateriumFolds[0] + "\n" +
                            "du "   + "\t" + prasenFolds[1] + "\t\t" + prateriumFolds[1]  + "\n" +
                            "er "   + "\t" + prasenFolds[2] + "\t\t" + prateriumFolds[2]  + "\n" +
                            "wir "  + "\t" + prasenFolds[3] + "\t\t" + prateriumFolds[3] + "\n" +
                            "ihr "  + "\t" + prasenFolds[4] + "\t\t" + prateriumFolds[4] + "\n" +
                            "sind " + "\t" + prasenFolds[5] + "\t\t" + prateriumFolds[5]; */

        //One \t equals 8 spaces in monosize. Make tester
        int minTabs = 500;
        int maxTabs = 0;
        int[] allTabs = new int[6];
        for(int i=0; i<prasenFolds.length; i++){
            int wordLength = prasenFolds[i].length();
            int tabAmount = 0;
            while(wordLength>=0){
                wordLength-=8;
                tabAmount++;
                if(tabAmount > 10000){
                    System.out.println("Somehow magically got stuck in while loop");
                    return;
                }
            }
            allTabs[i] = tabAmount;
            if(tabAmount > maxTabs){
                maxTabs = tabAmount;
            }
            if(tabAmount < minTabs){
                minTabs = tabAmount;
            }
        }
        int tabDiff = maxTabs - minTabs;
        System.out.println("Maxtabs: " + maxTabs);
        System.out.println("Got tab amount:");
        for(int i=0; i<allTabs.length; i++){
            System.out.println("Tabs " + i + " = " + allTabs[i]);
        }
        System.out.println("Amount of prasen fold = " + prasenFolds.length);

        //new system:
        StringBuilder sb = new StringBuilder("");
        sb.append(input + "\t\t\t" + habeOrSind + perfekt + "\n\n");
        for(int i=0; i<prasenFolds.length; i++){
            System.out.println("The forloop that's supposed to go 6 times");
            switch(i){
                case 0: sb.append("ich"); break;
                case 1: sb.append("du"); break;
                case 2: sb.append("er"); break;
                case 3: sb.append("wir"); break;
                case 4: sb.append("ihr"); break;
                case 5: sb.append("sind"); break;
                default: sb.append("Something went wrong when finding folds."); break;
            }
            sb.append("\t" + prasenFolds[i] + "\t\t");
            for(int j=0; j<maxTabs-allTabs[i]; j++){
                sb.append("\t");
            }
            sb.append(prateriumFolds[i]);
            if(i<5) sb.append("\n");
        }

        String pasteReady = sb.toString();
        //Convert ching cheng symbols to ä ü and ö
        pasteReady = unChingCheng(pasteReady);

        System.out.println();
        System.out.println(pasteReady);
    }
    String unChingCheng(String toPrint) {
        for(int i=0; i<toPrint.length()-5; i++){
            if(toPrint.charAt(i)=='%'){
                String currentSymbols = toPrint.substring(i, i+6);
                char newLetter = '/';
                System.out.println(currentSymbols + " <--- current detected unknown symbols in TranslateWord");
                switch(currentSymbols){
                    case "%C3%A4": newLetter = 'ä'; break;
                    case "%C3%BC": newLetter = 'ü'; break;
                    case "%C3%B6": newLetter = 'ö'; break;
                    case "%C3%9F": newLetter = 'ß'; break;
                    case "%C4%8D": newLetter = 'č'; break;
                    case "%C5%A1": newLetter = 'š'; break;
                    case "%C5%BE": newLetter = 'ž'; break;
                    default: System.out.println("New unrecognized symbol"); break;
                }
                toPrint = toPrint.substring(0, i) + newLetter + toPrint.substring(i+6);
            }
        }
        return toPrint;
    }

    static void printToFile(String output, String websiteName){
        try{
            File file = new File(websiteName + ".txt");
            if(file.createNewFile()){
                System.out.println("Created new file.");
            }
            FileWriter fw = new FileWriter(file);
            PrintWriter printWriter = new PrintWriter(fw);
            printWriter.print(output);
        }catch(Exception e){
            System.out.println("Failed print to file");
        }
    }

    private static String getUrlContents(String theUrl)  {  
        StringBuilder content = new StringBuilder();  
        // Use try and catch to avoid the exceptions  
        System.out.println("Attempting download website: " + theUrl);
        try {  
            URL url = new URL(theUrl); // creating a url object  
            URLConnection urlConnection = url.openConnection(); // creating a urlconnection object  

            // wrapping the urlconnection in a bufferedreader  
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));  
            String line;  
            // reading from the urlconnection using the bufferedreader  
            while ((line = bufferedReader.readLine()) != null)  
            {  
                content.append(line + "\n");  
            }  
            bufferedReader.close();  
            System.out.println("Downloaded website: " + theUrl);

        }catch(Exception e) {  
            System.out.println("Failed getting website data");
        }  
        return content.toString();  
    }

}  