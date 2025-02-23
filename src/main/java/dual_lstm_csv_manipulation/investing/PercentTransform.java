package dual_lstm_csv_manipulation.investing;

import org.datavec.api.transform.transform.string.BaseStringTransform;
import org.datavec.api.writable.Text;
import org.datavec.api.writable.Writable;

public class PercentTransform extends BaseStringTransform {

    public PercentTransform(String columnName) {
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

    private  String changeString(String val){
        double result = Double.parseDouble(val.substring(0, val.length() - 1).replace(',','.')) * 0.01;
        return String.valueOf(result);
    }


}
