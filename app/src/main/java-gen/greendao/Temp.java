package greendao;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT. Enable "keep" sections if you want to edit. 
/**
 * Entity mapped to table "TEMP".
 */
public class Temp {

    private Long id;
    private Double hz;
    private Double minPow;
    private Double maxPow;

    public Temp() {
    }

    public Temp(Long id) {
        this.id = id;
    }

    public Temp(Long id, Double hz, Double minPow, Double maxPow) {
        this.id = id;
        this.hz = hz;
        this.minPow = minPow;
        this.maxPow = maxPow;
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

    public Double getMinPow() {
        return minPow;
    }

    public void setMinPow(Double minPow) {
        this.minPow = minPow;
    }

    public Double getMaxPow() {
        return maxPow;
    }

    public void setMaxPow(Double maxPow) {
        this.maxPow = maxPow;
    }

}