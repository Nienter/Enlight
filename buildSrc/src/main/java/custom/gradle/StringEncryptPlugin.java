package custom.gradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class StringEncryptPlugin implements Plugin<Project>{
    public static List<String> sModuleSrcList=new ArrayList<>();
    public final static String sKey="Android Activity";
    public final static String sIv="FacebookAudience";//must be 16 bytes

    @Override
    public void apply(Project project){

        File moduleDir = project.getProjectDir();
        File srcDir = new File(moduleDir, "src/main/java");
        String moduleSrc = srcDir.getAbsolutePath();
        System.out.println("module src folder:"+moduleSrc);
        sModuleSrcList.add(moduleSrc);

        project.getTasks().create("updateKey", UpdateKeyTask.class);
        project.getTasks().create("encryptStr", EncryptStrTask.class);
    }



}
