package greendao;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT. Enable "keep" sections if you want to edit. 
/**
 * Entity mapped to table "STANFORD".
 */
public class Stanford {

    private Long id;
    private Long date;
    private Integer evaluation;

    public Stanford() {
    }

    public Stanford(Long id) {
        this.id = id;
    }

    public Stanford(Long id, Long date, Integer evaluation) {
        this.id = id;
        this.date = date;
        this.evaluation = evaluation;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDate() {
        return date;
    }

    public void setDate(Long date) {
        this.date = date;
    }

    public Integer getEvaluation() {
        return evaluation;
    }

    public void setEvaluation(Integer evaluation) {
        this.evaluation = evaluation;
    }

}
