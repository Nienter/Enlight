package custom.gradle;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.io.FileWriter;
import java.io.BufferedWriter;


public class EncryptStrTask extends DefaultTask {
    @TaskAction
    public void run() {
        for (String moduleSrc: StringEncryptPlugin.sModuleSrcList) {
            File srcFile = new File(moduleSrc);
            encryptStr(srcFile);
        }
    }

    private void encryptStr(File folder){
        for (File child:folder.listFiles()){
            if(child.isFile()){
                findAndReplaceStr(child);
            } else {
                encryptStr(child);
            }
        }
    }

    private void findAndReplaceStr(File file){
        FileReaderByLine reader = new FileReaderByLine(file);

        boolean fileChanged = false;

        String encryptStr = null;

        List<String> fileContents = new ArrayList<>();
        String line=reader.nextLine();
        while (line!=null){
            if(line.contains("@EncryptStr")
                    && line.contains("(")
                    && line.contains("value")
                    && line.contains("=")
                    && line.contains(")")){
                int index = line.indexOf("(");
                String subStr = line.substring(index+1);

                index = subStr.indexOf("value");
                subStr = subStr.substring(index+1);

                index = subStr.indexOf("=");
                subStr = subStr.substring(index+1);

                index = subStr.indexOf("\"");
                subStr = subStr.substring(index+1);

                index = subStr.indexOf("\"");
                String value = subStr.substring(0,index);

                encryptStr = EncryptUtil.encrypt(value);
                System.out.println(value+"-->"+encryptStr+"<--");
            } else if(encryptStr!=null){
                if(line.contains("=")){
                    int index = line.indexOf("=");
                    String changedLine = line.substring(0, index+1) + " \"" +encryptStr+ "\" ;" ;
                    line = changedLine;
                    fileChanged=true;

                } else if(line.contains("(") && line.contains(")")){
                    int start = line.indexOf("(");
                    int end   = line.lastIndexOf(")");
                    String changedLine = line.substring(0, start+1)
                            + "\"" +encryptStr+ "\""
                            + line.substring(end);
                    line = changedLine;
                    fileChanged=true;

                } else {
                    System.err.println("can't find the pattern:"+line);
                }

                System.out.println("new line:" + line);

                encryptStr = null;
            }
            fileContents.add(line);
            line=reader.nextLine();
        }


        if(fileChanged){
            System.out.println("updating:" + file.getAbsolutePath()+"\n\n");

            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(file));
                writer.write("");

                for (String str:fileContents) {
                    writer.write(str);
                    writer.newLine();
                }

                writer.flush();
                writer.close();
            } catch (Throwable err){
                err.printStackTrace();
            }
        }
    }
}