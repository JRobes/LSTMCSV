package dual_lstm_csv_manipulation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class CSVGenerator {
    public static void generateCSVFiles(List<String[]> data, String path, int columOfLabels) {
        if (data == null || data.size() < 2) {
            throw new IllegalArgumentException("La lista debe tener al menos dos elementos.");
        }

        File directory = new File(path);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        int numFiles = data.size() - 1;

        for (int i = 0; i < numFiles; i++) {
            String featFileName = path + File.separator + "feat_" + i + ".csv";
            String labelFileName = path + File.separator + "label_" + i + ".csv";

            writeCSV(featFileName, data.get(i));
            writeCSV(labelFileName, new String[]{data.get(i + 1)[columOfLabels]});
        }
    }

    private static void writeCSV(String fileName, String[] content) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            writer.write(String.join(",", content));
            writer.newLine();
        } catch (IOException e) {
            System.err.println("Error al escribir el archivo: " + fileName);
            e.printStackTrace();
        }
    }
}

