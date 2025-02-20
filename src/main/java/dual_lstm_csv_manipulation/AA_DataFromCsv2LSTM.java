package dual_lstm_csv_manipulation;

public abstract class AA_DataFromCsv2LSTM implements IDataPreparation, IAbsPaths{
    private int percentOfTraining;
    private int numFeatures;
    private int numLabels;

    private String[] filePaths;
    private IAbsPaths paths;


    public AA_DataFromCsv2LSTM(String[] filePaths){
        this.filePaths = filePaths;
    }
    public void setPaths(){

    }
}
