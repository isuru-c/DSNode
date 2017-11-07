package dsnode.model;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

/**
 * @author Isuru Chandima
 */
public class FileHandler {

    private String[] fileList;

    private Queue<String> searchIdList;

    public FileHandler() {

        Random rand = new Random();

        // Get random number of files between 3-5 for this node
        int nof = rand.nextInt(3) + 3;
        fileList = new String[nof];

        String totalFileList[] = {"Adventures of Tintin", "Jack and Jill", "Glee", "The Vampire Diarie",
                "King Arthur", "Windows XP", "Harry Potter", "Kung Fu Panda", "Lady Gaga", "Twilight",
                "Windows 8", "Mission Impossible", "Turn Up The Music", "Super Mario", "American Pickers",
                "Microsoft Office 2010", "Happy Feet", "Modern Family", "American Idol", "Hacking for Dummies"};

        int count = 0;
        int randomNumberList[] = new int[nof];

        while (count < nof) {

            int nextRandomNumber = rand.nextInt(totalFileList.length);
            boolean newRandomNumberFound = false;

            while (!newRandomNumberFound) {
                for (int i = 0; i <= count; i++) {
                    if (randomNumberList[i] == nextRandomNumber)
                        break;
                    if (i == count) {
                        newRandomNumberFound = true;
                        randomNumberList[count] = nextRandomNumber;
                    }
                }
                if (!newRandomNumberFound)
                    nextRandomNumber = rand.nextInt(totalFileList.length);
            }

            fileList[count] = totalFileList[nextRandomNumber];
            count++;
        }

        this.searchIdList = new LinkedList<>();
    }

    public String[] getFileList() {
        return fileList;
    }

    public ArrayList<String> searchFiles(String fileName) {

        ArrayList<String> list = new ArrayList<>();

        for (String file : fileList) {
            if (isMatched(file, fileName)) {
                list.add(file.replace(' ', '_'));
            }
        }

        return list;
    }

    private boolean isMatched(String file, String searchName) {

        file = file.toLowerCase();
        searchName = searchName.toLowerCase();

        if (file.contains(searchName)) {
            int startOfName = file.indexOf(searchName);
            int endOfName = startOfName + searchName.length() - 1;

            if (startOfName == 0) {
                return endOfName == file.length() - 1 || file.charAt(endOfName + 1) == ' ';
            } else {
                if (endOfName == file.length() - 1) {
                    return file.charAt(startOfName - 1) == ' ';
                } else {
                    return file.charAt(startOfName - 1) == ' ' && file.charAt(endOfName + 1) == ' ';
                }
            }
        }

        return false;
    }

    public boolean isNewSearchId(String searchId){
        int searchIdLimit = 100;

        for(String oldSearchId : searchIdList){
            if(oldSearchId.equals(searchId))
                return false;
        }

        if(searchIdList.size()==searchIdLimit)
            searchIdList.remove();

        searchIdList.add(searchId);
        return true;
    }

    public void addSearchId(String searchId){
        int searchIdLimit = 100;

        if(searchIdList.size()==searchIdLimit)
            searchIdList.remove();

        searchIdList.add(searchId);
    }
}
