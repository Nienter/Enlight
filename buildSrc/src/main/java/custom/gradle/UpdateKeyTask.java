package custom.gradle;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.io.FileWriter;
import java.io.BufferedWriter;

public class UpdateKeyTask extends DefaultTask{
    @TaskAction
    public void run() {
        for (String moduleSrc: StringEncryptPlugin.sModuleSrcList) {
            File srcFile = new File(moduleSrc);
            writeKeyAndIv(srcFile);
        }

    }

    private final String mKey=StringEncryptPlugin.sKey;
    private final String mIv=StringEncryptPlugin.sIv;

    private void writeKeyAndIv(File srcDir){
        for (File child:srcDir.listFiles()) {
            if(child.isFile() && child.getName().equals("FunctionUtil.java")){
                FileReaderByLine reader = new FileReaderByLine(child);

                List<String> fileContents = new ArrayList<>();
                String line=reader.nextLine();
                while (line!=null){
                    if(line.contains("ENCRYPTION_KEY")
                            && line.contains("private")
                            && line.contains("static") && line.contains("String")
                            && line.contains("=")){
                        line = "    final private static String ENCRYPTION_KEY = \""+mKey+"\";";
                    } else if(line.contains("ENCRYPTION_IV")
                            && line.contains("private")
                            && line.contains("static")
                            && line.contains("String")
                            && line.contains("=")){
                        line = "    final private static String ENCRYPTION_IV = \""+mIv+"\";";
                    }

                    fileContents.add(line);
                    line=reader.nextLine();
                }


                try {
                    BufferedWriter writer = new BufferedWriter(new FileWriter(child));
                    //writer.write("");

                    for (String str:fileContents) {
                        writer.write(str);
                        writer.newLine();
                    }

                    writer.flush();
                    writer.close();
                } catch (Throwable err){
                    err.printStackTrace();
                }

                return;
            } else if(child.isDirectory()){
                writeKeyAndIv(child);
            }
        }
    }
}