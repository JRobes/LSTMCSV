package dual_lstm_csv_manipulation;

import org.datavec.api.records.reader.RecordReader;
import org.datavec.api.records.reader.impl.csv.CSVRecordReader;
import org.datavec.api.transform.MathOp;
import org.datavec.api.transform.TransformProcess;
import org.datavec.api.transform.condition.ConditionOp;
import org.datavec.api.transform.condition.column.CategoricalColumnCondition;
import org.datavec.api.transform.condition.column.DoubleColumnCondition;
import org.datavec.api.transform.filter.ConditionFilter;
import org.datavec.api.transform.schema.Schema;
import org.datavec.api.transform.transform.time.DeriveColumnsFromTimeTransform;
import org.datavec.api.writable.DoubleWritable;
import org.joda.time.DateTimeFieldType;
import org.joda.time.DateTimeZone;

import javax.sound.midi.Soundbank;
import java.util.Arrays;
import java.util.HashSet;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        System.out.println( "Hello World!" );

        System.out.println("este es el cambio");


        CsvPreProcess.processCsv("pre-ACX.csv", "out.csv");
        char quote = '\"';
        char delim = ',';
        char delet = 'r';
        //RecordReader rr = new CSVRecordReader(2, delim, quote);
        RecordReader rr = new CSVRecordReader(2, ',');

        Schema inputDataSchema = new Schema.Builder()
                //We can define a single column
                .addColumnString("DateTimeString")
                .addColumnDouble("Last",0.0,null,false,false)
                .addColumnDouble("Open",0.0,null,false,false)
                .addColumnDouble("Max",0.0,null,false,false)
                .addColumnDouble("Min",0.0,null,false,false)
                //Or for convenience define multiple columns of the same type
                .addColumnsString("Vol", "Per")
                .build();

        TransformProcess tp = new TransformProcess.Builder(inputDataSchema)
                //Let's remove some column we don't need
                .removeColumns("CustomerID","MerchantID")
                .doubleColumnsMathOp("Var", MathOp.Subtract, "max","min")

                //Now, suppose we only want to analyze transactions involving merchants in USA or Canada. Let's filter out
                // everthing except for those countries.
                //Here, we are applying a conditional filter. We remove all of the examples that match the condition
                // The condition is "MerchantCountryCode" isn't one of {"USA", "CAN"}
                .filter(new ConditionFilter(
                        new CategoricalColumnCondition("MerchantCountryCode", ConditionOp.NotInSet, new HashSet<>(Arrays.asList("USA","CAN")))))

                //Let's suppose our data source isn't perfect, and we have some invalid data: negative dollar amounts that we want to replace with 0.0
                //For positive dollar amounts, we don't want to modify those values
                //Use the ConditionalReplaceValueTransform on the "TransactionAmountUSD" column:
                .conditionalReplaceValueTransform(
                        "TransactionAmountUSD",     //Column to operate on
                        new DoubleWritable(0.0),    //New value to use, when the condition is satisfied
                        new DoubleColumnCondition("TransactionAmountUSD",ConditionOp.LessThan, 0.0)) //Condition: amount < 0.0

                //Finally, let's suppose we want to parse our date/time column in a format like "2016/01/01 17:50.000"
                //We use JodaTime internally, so formats can be specified as follows: http://www.joda.org/joda-time/apidocs/org/joda/time/format/DateTimeFormat.html
                .stringToTimeTransform("DateTimeString","YYYY-MM-DD HH:mm:ss.SSS", DateTimeZone.UTC)

                //However, our time column ("DateTimeString") isn't a String anymore. So let's rename it to something better:
                .renameColumn("DateTimeString", "DateTime")

                //At this point, we have our date/time format stored internally as a long value (Unix/Epoch format): milliseconds since 00:00.000 01/01/1970
                //Suppose we only care about the hour of the day. Let's derive a new column for that, from the DateTime column
                .transform(new DeriveColumnsFromTimeTransform.Builder("DateTime")
                        .addIntegerDerivedColumn("HourOfDay", DateTimeFieldType.hourOfDay())
                        .build())

                //We no longer need our "DateTime" column, as we've extracted what we need from it. So let's remove it
                .removeColumns("DateTime")

                //We've finished with the sequence of operations we want to do: let's create the final TransformProcess object
                .build();


    }
}
