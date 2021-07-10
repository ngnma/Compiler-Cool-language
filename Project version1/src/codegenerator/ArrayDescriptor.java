package codegenerator;

public class ArrayDescriptor extends Descriptor{
    int size;
    Descriptor[] value;
    ArrayDescriptor(String type) {
        super(type);

    }
    public void setSize(int size){
        this.size=size;

    }
}
