package dual_lstm_csv_manipulation;

public interface IAbsPaths {
    //Esta función devuelve la ruta absoluta de los archivos a procesar. Será buena en producción
    String[] getAbsPaths();
}
