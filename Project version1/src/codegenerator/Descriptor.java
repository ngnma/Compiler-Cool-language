package codegenerator;

public class Descriptor {
    String type;
    String address;
    Object value;

    Descriptor(String type){
        this.type=type;
    }

    public void setAddress(String address) {
        this.address = address;
    }
    public void setValue(Object value){this.value=value;}
    public void setType(String type){this.type=type;}
}
