package guilherme.url.ph.stocktracker.Entidades;

public class StockClass {

    private String ticker;
    private Integer qtd;

    public StockClass() {

    }

    public String getTicker() {
        return ticker;
    }

    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public Integer getQtd() {
        return qtd;
    }

    public void setQtd(Integer qtd) {
        this.qtd = qtd;
    }
}
