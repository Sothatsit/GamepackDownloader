package net.sothatsit.gamepackdownloader.refactor.descriptor;

public class FieldDescriptor extends Descriptor {

    private String type;

    public FieldDescriptor(String descriptorRaw, ClassNameSupplier supplier) {
        super(descriptorRaw);

        this.type = Descriptor.getFullName(descriptorRaw, supplier);
    }

    @Override
    public String getDescriptorReformatted() {
        return Descriptor.getShortName(type);
    }

    @Override
    public String getWorkingDescriptor() {
        return Descriptor.getDescriptorName(type);
    }

    public String getType() {
        return type;
    }

}
