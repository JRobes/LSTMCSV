package dual_lstm_csv_manipulation;

import org.datavec.api.transform.transform.string.BaseStringTransform;
import org.datavec.api.writable.Text;
import org.datavec.api.writable.Writable;

public class InvestingNumberTransform extends BaseStringTransform {


    public InvestingNumberTransform(String columnName) {
        super(columnName);
    }

    @Override
    public Text map(final Writable writable) {
        String value = writable.toString();
        value = changeString(value);
        return new Text(value);
    }

    @Override
    public Object map(final Object o) {
        String value = o.toString();
        value = changeString(value);
        return value;
    }

    private String changeString(String value){
        Double result;
        if (value.endsWith("K")) {
            result = Double.parseDouble(value.substring(0, value.length() - 1).replace(".","").replace(',','.')) * 1000;
        } else if (value.endsWith("M")) {
            result = Double.parseDouble(value.substring(0, value.length() - 1).replace(".","").replace(',','.')) * 1000000;
        } else {
            result = Double.parseDouble(value.replace(".","").replace(',','.')); // Sin sufijo, se convierte directamente a Double
        }
        return String.valueOf(result);
    }



}