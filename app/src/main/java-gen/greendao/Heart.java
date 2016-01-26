package greendao;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT. Enable "keep" sections if you want to edit. 
/**
 * Entity mapped to table "HEART".
 */
public class Heart {

    private Long id;
    private Double hz;
    private Double pow;
    private Double heatRate;
    private Long date;

    public Heart() {
    }

    public Heart(Long id) {
        this.id = id;
    }

    public Heart(Long id, Double hz, Double pow, Double heatRate, Long date) {
        this.id = id;
        this.hz = hz;
        this.pow = pow;
        this.heatRate = heatRate;
        this.date = date;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getHz() {
        return hz;
    }

    public void setHz(Double hz) {
        this.hz = hz;
    }

    public Double getPow() {
        return pow;
    }

    public void setPow(Double pow) {
        this.pow = pow;
    }

    public Double getHeatRate() {
        return heatRate;
    }

    public void setHeatRate(Double heatRate) {
        this.heatRate = heatRate;
    }

    public Long getDate() {
        return date;
    }

    public void setDate(Long date) {
        this.date = date;
    }

}