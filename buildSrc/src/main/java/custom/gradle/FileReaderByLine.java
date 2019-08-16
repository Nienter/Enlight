package custom.gradle;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class FileReaderByLine {
    private BufferedReader mReader = null;

    public FileReaderByLine(File file){
        try {
            mReader = new BufferedReader(new FileReader(file));
        } catch (Throwable e){
            e.printStackTrace();
        }
    }

    public String nextLine(){
        String line = null;

        if(mReader!=null){
            try {
                line = mReader.readLine();
            } catch (Throwable err){
                err.printStackTrace();
            }

            if(line==null){
                try {
                    mReader.close();
                    mReader = null;
                } catch (Throwable err){
                    err.printStackTrace();
                }
            }

        }

        return line;
    }
}
