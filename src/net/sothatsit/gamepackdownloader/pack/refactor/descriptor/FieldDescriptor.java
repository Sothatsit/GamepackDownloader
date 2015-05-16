package net.sothatsit.gamepackdownloader.pack.refactor.descriptor;

public class FieldDescriptor extends Descriptor {

    private String type;

    public FieldDescriptor(String descriptorRaw, ClassNameSupplier supplier) {
        super(descriptorRaw);

        this.type = Descriptor.getFullName(descriptorRaw, supplier);
    }

    @Override
    public String getDescriptorReformatted() {
        return type;
    }

    public String getType() {
        return type;
    }

}
