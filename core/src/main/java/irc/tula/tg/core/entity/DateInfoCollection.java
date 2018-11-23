package irc.tula.tg.core.entity;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "dateInfoFile",
        "numItems",
        "lastItemId",
        "items"
})
public class DateInfoCollection {

    @JsonProperty("dateInfoFile")
    private String dateInfoFile;
    @JsonProperty("numItems")
    private Integer numItems;
    @JsonProperty("lastItemId")
    private Integer lastItemId;
    @JsonProperty("items")
    private List<DateItem> items = null;

    @JsonProperty("dateInfoFile")
    public String getDateInfoFile() {
        return dateInfoFile;
    }

    @JsonProperty("dateInfoFile")
    public void setDateInfoFile(String dateInfoFile) {
        this.dateInfoFile = dateInfoFile;
    }

    @JsonProperty("numItems")
    public Integer getNumItems() {
        return numItems;
    }

    @JsonProperty("numItems")
    public void setNumItems(Integer numItems) {
        this.numItems = numItems;
    }

    @JsonProperty("lastItemId")
    public Integer getLastItemId() {
        return lastItemId;
    }

    @JsonProperty("lastItemId")
    public void setLastItemId(Integer lastItemId) {
        this.lastItemId = lastItemId;
    }

    @JsonProperty("items")
    public List<DateItem> getItems() {
        return items;
    }

    @JsonProperty("items")
    public void setItems(List<DateItem> items) {
        this.items = items;
    }

}