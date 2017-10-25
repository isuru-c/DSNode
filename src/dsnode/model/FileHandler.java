package dsnode.model;

import java.util.Random;

/**
 * @author Isuru Chandima
 */
public class FileHandler {

    private String[] fileList;

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

    }

    public String[] getFileList() {
        return fileList;
    }
}
