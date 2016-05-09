public class DataObject {
    public String key1;
    public String key2;
    public String key3;

    public DataObject(String value1, String value2, String value3){
        this.key1 = value1;
        this.key2 = value2;
//        this.key3 = value3;
    }

    public void printDataObject(){
        System.out.println("key1: " + key1);
        System.out.println("key2: " + key2);

    }

//    public String toString(){
//        return "key = " +key+ ", value= " +value;
//    }
}
