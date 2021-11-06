import java.net.*;  
import java.io.*;  

public class TranslateWord{  
    
    String input;
    TranslateWord(String input){
        this.input = input;
        translateWord();
    }

    void translateWord(){
        String siteURL = "https://sl.pons.com/prevod/sloven%C5%A1%C4%8Dina-nem%C5%A1%C4%8Dina/"+input;
        String output  = getUrlContents(siteURL);
        String language = "slo";

        String validationString = "Ogledujete si podobne rezultate";
        for(int i=0; i<output.length()-validationString.length(); i++){
            if(output.substring(i, i+validationString.length()).equals(validationString)){
                System.out.println("Wrong language.");
                siteURL = "https://sl.pons.com/prevod/nem%C5%A1%C4%8Dina-sloven%C5%A1%C4%8Dina/"+input;
                output  = getUrlContents(siteURL);
                language = "ger";
            }
        } 

        printToFile(output, "PONS"); 
        searchForGerTranslation(output, language);
    }

    void printToFile(String output, String websiteName){
        try{
            File file = new File(websiteName + ".txt");
            if(file.createNewFile()){
                System.out.println("Created translation file.");
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

    void searchForGerTranslation(String output, String language){
        String keyword = "";
        if(language.equals("slo")){
            keyword = "<a href='/prevod/nem%C5%A1%C4%8Dina-sloven%C5%A1%C4%8Dina/";
        } else if(language.equals("ger")){
            keyword = "<a href='/prevod/sloven%C5%A1%C4%8Dina-nem%C5%A1%C4%8Dina/";
        }
        String[] splitOutput = output.split(keyword);
        char currentChar = ' ';
        String gerWord = "";

        //Sometimes nouns have examples before they have their actual translation. This skips the examples:
        int getFind = 1;

        for(int i=0; i<splitOutput.length; i++){
            if(splitOutput[i].contains("\"entry")){
                if(!splitOutput[i].contains("Geslo uporabnika")){
                    getFind = i+1;
                    break;
                }
            }
        }

        // pleskati
        // kidati
        if(splitOutput[getFind-1].charAt(splitOutput[getFind-1].length()-1)=='(') {
            getFind++;
        }

        //System.out.println(output);
        int counter = 0;
        while(currentChar!='\''){
            currentChar = splitOutput[getFind].charAt(counter);
            gerWord+=currentChar;
            counter++;
            currentChar = splitOutput[getFind].charAt(counter);
            if(counter>10000){
                System.out.println("Got stuck in a while loop");
                return;
            }
        }

        //pomeni da je dvo beseden prevod
        if(splitOutput[getFind].length()<"<a href='/prevod/sloven%C5%A1%C4%8Dina-nem%C5%A1%C4%8Dina/avtomobil'>avtomobilavtomobilavtomobil</a>".length()){
            gerWord+=" ";
            counter = 0;
            currentChar = ' ';
            while(currentChar!='\''){
                currentChar = splitOutput[getFind+1].charAt(counter);
                gerWord+=currentChar;
                counter++;
                currentChar = splitOutput[getFind+1].charAt(counter);
                if(counter>10000){
                    System.out.println("Got stuck in a while loop");
                    return;
                }
            }
        }

        //Get the gender word
        String gerGender = "";
        boolean samostalnik = false;
        int samostalnikIndex = 0;
        String samostalnikKeyword = "<span class=\"wordclass\"><acronym title=\"samostalnik\">SAM.</acronym></span>";
        for(int i=0; i<splitOutput[0].length()-samostalnikKeyword.length(); i++){
            if(splitOutput[0].substring(i, i+samostalnikKeyword.length()).equals(samostalnikKeyword)){
                samostalnik = true;
                samostalnikIndex = i+samostalnikKeyword.length();
                break;
            }
        }
        if(samostalnik){
            String[] genderKeyWords = {"<acronym title=\"moški spol\"", "<acronym title=\"ženski spol\"", "<acronym title=\"srednji spol\""};
            if(language.equals("slo")){
                for(int i=0; i<splitOutput[getFind].length()-genderKeyWords[2].length(); i++){
                    if(splitOutput[getFind].substring(i,i+genderKeyWords[0].length()).equals(genderKeyWords[0])){
                        gerGender = "der";
                        break;
                    } else if(splitOutput[getFind].substring(i,i+genderKeyWords[1].length()).equals(genderKeyWords[1])){
                        gerGender = "die";
                        break;
                    } else if(splitOutput[getFind].substring(i,i+genderKeyWords[2].length()).equals(genderKeyWords[2])){
                        gerGender = "das";
                        break;
                    }
                }
            } else if(language.equals("ger")){
                for(int i=samostalnikIndex; i<samostalnikIndex+50; i++){
                    if(output.substring(i,i+genderKeyWords[0].length()).equals(genderKeyWords[0])){
                        gerGender = "der";
                        break;
                    } else if(output.substring(i,i+genderKeyWords[1].length()).equals(genderKeyWords[1])){
                        gerGender = "die";
                        break;
                    } else if(output.substring(i,i+genderKeyWords[2].length()).equals(genderKeyWords[2])){
                        gerGender = "das";
                        break;
                    }
                }
            }
            gerGender += " ";
        }

        System.out.println();
        if(language.equals("slo")) System.out.println(this.input + " = " + gerGender +gerWord);
        else if(language.equals("ger")) System.out.println(gerGender + this.input + " = " + gerWord);
    }
}  