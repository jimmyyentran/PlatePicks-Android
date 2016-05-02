public class DataObject {
    public String key;
    public String value;

    public DataObject(String key, String value){
        this.key = key;
        this.value = value;
    }

    public String toString(){
        return "key = " +key+ ", value= " +value;
    }
}
