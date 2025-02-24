package dual_lstm_csv_manipulation.paths;

import org.nd4j.common.io.ClassPathResource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class GetSoucePaths implements IAbsPaths {
    private String[] p;
    public GetSoucePaths(String... paths){
        this.p = paths;
    }


    @Override
    public String[] getAbsPaths() {
        List<String> filePaths = new ArrayList<>();
        try{
            String classPath = new ClassPathResource("").getFile().getPath();
            Path path2LevelsUp = Paths.get(classPath).getParent().getParent(); // Retrocede dos niveles
            for (String path : p){
                filePaths.add(path2LevelsUp.toAbsolutePath() + File.separator + path);
            }
            System.out.println("EEEEEEEEEEEEEEEEEEEEEEE: " + filePaths.size());
        }catch (IOException e){
            e.printStackTrace();
        }
        return filePaths.toArray(new String[0]);
    }
}
