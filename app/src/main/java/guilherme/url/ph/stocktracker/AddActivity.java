package guilherme.url.ph.stocktracker;

import android.content.Context;
import android.content.Intent;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.math.BigDecimal;

import guilherme.url.ph.stocktracker.Entidades.StockClass;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;

public class AddActivity extends AppCompatActivity {

    Button search, add;
    EditText symbol, quantidade;
    TextView valor, variacao, nameStock;

    private String userID;
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private DatabaseReference myRef;
    private String concatWithSA;
    private StockClass s;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_stock);

        search = (Button) this.findViewById(R.id.search);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // fechar teclado virtual após botão
                InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

                mAuth = FirebaseAuth.getInstance();
                mFirebaseDatabase = FirebaseDatabase.getInstance();
                myRef = mFirebaseDatabase.getReference();
                final FirebaseUser user = mAuth.getCurrentUser();
                userID = user.getUid();

                symbol = (EditText) findViewById(R.id.symbol);
                valor = (TextView) findViewById(R.id.price);
                variacao = (TextView) findViewById(R.id.change);
                add = (Button) findViewById(R.id.add);
                quantidade = (EditText) findViewById(R.id.qtd);
                nameStock = findViewById(R.id.nome_stock);

                Stock stock = null;
                String variacaoPorcentagem = null;

                try {
                    try { // busca por ativos americanos

                        stock = YahooFinance.get(symbol.getText().toString());
                        BigDecimal price = stock.getQuote(true).getPrice();
                        BigDecimal change = stock.getQuote().getChangeInPercent();
                        String name = stock.getName().toString();

                        String valorUSD = String.valueOf(price);
                        valorUSD = "$" + valorUSD;

                        valor.setText(valorUSD);

                        variacaoPorcentagem = String.valueOf(change);
                        variacaoPorcentagem += "%";
                        variacao.setText(variacaoPorcentagem);

                        nameStock.setText(name);

                        quantidade.setVisibility(View.VISIBLE);
                        add.setVisibility(View.VISIBLE);

                    } catch (IOException e) { // busca por ativos brasileiros

                        concatWithSA = symbol.getText().toString();
                        concatWithSA += ".SA"; // se ação for da América do Sul é preciso concatenar com .SA.
                        stock = YahooFinance.get(concatWithSA);
                        BigDecimal change = stock.getQuote().getChangeInPercent();
                        BigDecimal price = stock.getQuote(true).getPrice();
                        String name = stock.getName().toString();

                        add.setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View view) {

                                String ticker = symbol.getText().toString().toUpperCase();
                                Integer qtd = Integer.valueOf(quantidade.getText().toString());

                                if (qtd <= 0) {
                                    Intent i = new Intent(AddActivity.this, MainActivity.class);
                                    startActivity(i);
                                    Toast.makeText(getApplicationContext(), "Quantidade não pode ser igual ou inferior a 0!", Toast.LENGTH_LONG).show();
                                    return;
                                }

                                s = new StockClass();
                                s.setTicker(ticker.toUpperCase());
                                s.setQtd(qtd);

                                myRef.child("users").child(userID).child(ticker).setValue(s);

                                Intent intent = new Intent(AddActivity.this, MainActivity.class);
                                startActivity(intent);

                                Toast.makeText(getApplicationContext(), "Ativo adicionado!", Toast.LENGTH_LONG).show();
                            }
                        });

                        String valorBRL = String.valueOf(price);
                        valorBRL = "R$ " + valorBRL;
                        valor.setText(valorBRL);

                        double changeDouble = change.doubleValue();

                        if (changeDouble < 0) {
                            variacao.setTextColor(getResources().getColor(R.color.negativeChange));
                            variacao.setText(String.valueOf("Variação: " + changeDouble + "%"));
                        } else if (changeDouble > 0) {
                            variacao.setTextColor(getResources().getColor(R.color.positiveChange));
                            variacao.setText(String.valueOf("Variação: " + changeDouble + "%"));
                        } else {
                            variacao.setText(String.valueOf("Variação: " + changeDouble + "%"));
                        }

                        nameStock.setText(name);

                        quantidade.setVisibility(View.VISIBLE);
                        add.setVisibility(View.VISIBLE);

                    }

                } catch (IOException e) {

                    quantidade.setVisibility(View.INVISIBLE);
                    add.setVisibility(View.INVISIBLE);

                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Este ativo não está listado!", Toast.LENGTH_LONG).show();
                    valor.setText("");
                    variacao.setText("");
                }
            }
        });
    }
}