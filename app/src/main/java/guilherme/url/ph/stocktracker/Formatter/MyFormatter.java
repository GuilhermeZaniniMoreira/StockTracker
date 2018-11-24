package guilherme.url.ph.stocktracker.Formatter;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.text.DecimalFormat;

public class MyFormatter implements IValueFormatter {

    DecimalFormat df = new DecimalFormat("#.00");

    @Override
    public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
        return String.valueOf("R$ " + df.format(value));
    }

}
