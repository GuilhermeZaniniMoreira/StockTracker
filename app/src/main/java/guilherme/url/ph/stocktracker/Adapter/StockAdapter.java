package guilherme.url.ph.stocktracker.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import guilherme.url.ph.stocktracker.Entidades.StockClass;
import guilherme.url.ph.stocktracker.R;

public class StockAdapter extends ArrayAdapter<StockClass> {

    private ArrayList<StockClass> acao;
    private Context context;

    public StockAdapter(Context c, ArrayList<StockClass> objects) {
        super(c, 0, objects);
        this.context = c;
        this.acao = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = null;

        if (acao != null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);

            view = inflater.inflate(R.layout.lista_acoes, parent, false);

            TextView textViewNome = (TextView) view.findViewById(R.id.textViewStock);
            TextView textViewValor = (TextView) view.findViewById(R.id.textViewQtd);

            StockClass s2 = acao.get(position);
            textViewNome.setText(s2.getTicker());
            textViewValor.setText(s2.getQtd().toString());
        }

        return view;
    }
}