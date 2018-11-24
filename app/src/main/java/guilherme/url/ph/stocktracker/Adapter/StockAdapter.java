package guilherme.url.ph.stocktracker.Adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Map;

import guilherme.url.ph.stocktracker.Entidades.StockClass;
import guilherme.url.ph.stocktracker.R;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;

public class StockAdapter extends ArrayAdapter<StockClass> {

    DecimalFormat df = new DecimalFormat("#.00");

    private ArrayList<StockClass> acao;
    private Context context;

    public StockAdapter(Context c, ArrayList<StockClass> objects) {
        super(c, 0, objects);
        this.context = c;
        this.acao = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ArrayList<String> acoes = new ArrayList<>();
        ArrayList<Double> acoesPreco = new ArrayList<>();
        ArrayList<Double> acoesVariacao = new ArrayList<>();

        View view = null;

        StockClass s3;
        int i = 0;

        for (i = 0; i < acao.size(); i++) {
            s3 = acao.get(i);
            acoes.add(s3.getTicker());
            Log.d("ticker", acoes.get(i) + " " + i);
        }

        String[] arrAcoes = acoes.toArray(new String[acoes.size()]);

        for (i = 0; i < arrAcoes.length; i++) {
            arrAcoes[i] = arrAcoes[i].concat(".SA");
        }

        try {
            Map<String, Stock> stocks = YahooFinance.get(arrAcoes);

            Log.d("stocks size", stocks.size() + "");
            for (i = 0; i < stocks.size(); i++) {
                Stock atual = stocks.get(arrAcoes[i]);
                BigDecimal price = atual.getQuote(false).getPrice();
                double priceDouble = price.doubleValue();
                acoesPreco.add(i, priceDouble);
                BigDecimal change = atual.getQuote(false).getChangeInPercent();
                double changeDouble = change.doubleValue();
                acoesVariacao.add(i, changeDouble);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }


        if (acao != null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);

            view = inflater.inflate(R.layout.lista_acoes, parent, false);

            TextView textViewNome = (TextView) view.findViewById(R.id.textViewStock);
            TextView textViewQtd = (TextView) view.findViewById(R.id.textViewQtd);
            TextView textViewValor = (TextView) view.findViewById(R.id.textViewValor);
            TextView textViewChange = (TextView) view.findViewById(R.id.textViewChange);

            StockClass s2 = acao.get(position);
            textViewNome.setText(s2.getTicker());

            textViewQtd.setText("Quantidade: " + s2.getQtd().toString());

            Double valorDouble = acoesPreco.get(position);
            textViewValor.setText("R$ " + String.valueOf(df.format(valorDouble)));

            Double changeDouble = acoesVariacao.get(position);

            if (changeDouble < 0) {
                textViewChange.setTextColor(context.getResources().getColor(R.color.negativeChange));
                textViewChange.setText(String.valueOf("Variação: " + changeDouble + "%"));
            } else if (changeDouble > 0) {
                textViewChange.setTextColor(context.getResources().getColor(R.color.positiveChange));
                textViewChange.setText(String.valueOf("Variação: " + changeDouble + "%"));
            } else {
                textViewChange.setText(String.valueOf("Variação: " + changeDouble + "%"));
            }


            /*
            Stock stock = null;
            Double valorDouble;
            Double changeDouble;

            try {
                stock = YahooFinance.get(acao.get(position).getTicker().concat(".SA"));
                BigDecimal price = stock.getQuote(true).getPrice();
                BigDecimal change = stock.getQuote().getChangeInPercent();
                changeDouble = change.doubleValue();
                valorDouble = price.doubleValue() * s2.getQtd();
                valorDouble = Double.valueOf(df.format(valorDouble));
                textViewValor.setText("R$: " + String.valueOf(valorDouble));

                if (changeDouble < 0) {
                    textViewChange.setTextColor(context.getResources().getColor(R.color.negativeChange));
                    textViewChange.setText(String.valueOf("Variação: " + changeDouble + "%"));
                } else if (changeDouble > 0) {
                    textViewChange.setTextColor(context.getResources().getColor(R.color.positiveChange));
                    textViewChange.setText(String.valueOf("Variação: " + changeDouble + "%"));
                } else {
                    textViewChange.setText(String.valueOf("Variação: " + changeDouble + "%"));
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
*/
        }

        return view;
    }
}